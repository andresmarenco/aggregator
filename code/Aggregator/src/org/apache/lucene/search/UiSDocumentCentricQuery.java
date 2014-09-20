package org.apache.lucene.search;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.util.Bits;

import aggregator.beans.VerticalCollection;
import aggregator.sampler.indexer.AbstractSamplerIndexer;
import aggregator.util.CommonUtils;
import aggregator.verticalselection.UiSDocumentCentricSimilarity;

public class UiSDocumentCentricQuery extends Query {
	
	protected List<PowTermQuery> termQueries;
	protected VerticalCollection collection;
	protected Map<String, Float> pdcData;
	protected Log log = LogFactory.getLog(UiSDocumentCentricQuery.class);
	
	/**
	 * Constructs a query with the field name and the term string
	 * @param field Field name
	 * @param term Term string
	 * @param collection Vertical collection
	 */
	public UiSDocumentCentricQuery(String field, String term, VerticalCollection collection) {
		this(new Term(field, term), collection);
	}
	
	/**
	 * Constructs a query with the term
	 * @param term Term to query
	 * @param collection Vertical collection
	 */
	public UiSDocumentCentricQuery(Term term, VerticalCollection collection) {
		termQueries = new ArrayList<PowTermQuery>();
		termQueries.add(new PowTermQuery(term, 1));
		
		this.collection = collection;
		this.pdcData = new HashMap<String, Float>();
	}
	
	/**
	 * Constructs a query based on a query
	 * @param query Base query
	 * @param collection Vertical collection
	 */
	public UiSDocumentCentricQuery(Query query, VerticalCollection collection) {
		this.collection = collection;
		this.pdcData = new HashMap<String, Float>();
		this.termQueries = new ArrayList<PowTermQuery>();
		
		if(query instanceof TermQuery) {
			termQueries.add(new PowTermQuery(((TermQuery)query).getTerm(), 1));
		} else if(query instanceof BooleanQuery) {
			Map<Term, MutableInt> termsMap = new HashMap<Term, MutableInt>();
			List<Term> termsList = new ArrayList<Term>();
			
			for(BooleanClause clause : ((BooleanQuery)query).getClauses()) {
				Set<Term> terms = new HashSet<Term>();
				clause.getQuery().extractTerms(terms);
				Term currentTerm = terms.iterator().next();
				
				MutableInt count = termsMap.get(currentTerm);
				if(count == null) {
					count = new MutableInt(1);
					termsMap.put(currentTerm, count);
				} else {
					count.increment();
				}
				
				termsList.add(currentTerm);
			}
			
			
			for(Term t : termsList) {
				termQueries.add(new PowTermQuery(t, termsMap.get(t).intValue()));
			}
		}
	}
	

	@Override
	public String toString(String field) {
		return "field:" + field;
	}
	
	
//	/**
//	 * Gets the P(d|c)
//	 * @param vertical Vertical to compute the value
//	 * @return P(d|c) value
//	 */
//	protected double getPdc(VerticalCollectionData vertical) {
//		BigDecimal sampleSize = BigDecimal.valueOf(vertical.getSampleSize());
//		return BigDecimal.ONE.divide(sampleSize, CommonUtils.DEFAULT_DIVISION_SCALE, CommonUtils.DEFAULT_ROUNDING_MODE).doubleValue();
//	}
	
	protected float getPdc(String vertical) {
		Float pdc = this.pdcData.get(vertical);
		if(pdc == null) {
			BigDecimal sampleSize = BigDecimal.valueOf(collection.getVerticalData(vertical).getSampleSize());
			pdc = BigDecimal.ONE.divide(sampleSize, CommonUtils.DEFAULT_DIVISION_SCALE, CommonUtils.DEFAULT_ROUNDING_MODE).floatValue();
			
//			pdc = 1F / collection.getVerticalData(vertical).getSampleSize();
			this.pdcData.put(vertical, pdc);
		}
		
		return pdc;
	}

	
		
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((collection == null) ? 0 : collection.hashCode());
		result = prime * result
				+ ((termQueries == null) ? 0 : termQueries.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		UiSDocumentCentricQuery other = (UiSDocumentCentricQuery) obj;
		if (collection == null) {
			if (other.collection != null)
				return false;
		} else if (!collection.equals(other.collection))
			return false;
		if (termQueries == null) {
			if (other.termQueries != null)
				return false;
		} else if (!termQueries.equals(other.termQueries))
			return false;
		return true;
	}

	@Override
	public Weight createWeight(IndexSearcher searcher) throws IOException {
		return new UiSDocumentCentricWeight(searcher);
	}

	
	
	protected class UiSDocumentCentricWeight extends Weight {
		protected long totalTerms;
		protected List<Weight> termWeights;
		protected List<Long> totalTermFreq;
		protected IndexSearcher searcher;
		
		public UiSDocumentCentricWeight(IndexSearcher searcher) {
			super();
			this.searcher = searcher;
			try
			{
				this.termWeights = new ArrayList<Weight>();
				this.totalTermFreq = new ArrayList<Long>();
				for(Query query : UiSDocumentCentricQuery.this.termQueries) {
					this.termWeights.add(query.createWeight(searcher));
					this.totalTermFreq.add(searcher.getIndexReader().totalTermFreq(((PowTermQuery)query).getTerm()));
				}
				
				this.termWeights.add(new MatchAllDocsWeight(searcher));
				this.totalTermFreq.add(0L);
				this.totalTerms = searcher.getIndexReader().getSumTotalTermFreq(AbstractSamplerIndexer.INDEX_CONTENTS_FIELD);
			}
			catch (IOException ex) {
				log.error(ex.getMessage(), ex);
			}
			
			
		}

		@Override
		public Explanation explain(AtomicReaderContext context, int doc) throws IOException {
			float result = 1;
			
			ComplexExplanation explanation = new ComplexExplanation();
			explanation.setDescription("P(d|c) * product score");
			
			ComplexExplanation prodExplanation = new ComplexExplanation();
			prodExplanation.setDescription("product of:");
			
			for(Weight weight : this.termWeights) {
				if(!(weight instanceof MatchAllDocsWeight)) {
					Explanation weightExplanation = weight.explain(context, doc);
					prodExplanation.addDetail(weightExplanation);
					
					result *= weightExplanation.getValue();
				}
			}
			
			prodExplanation.setValue(result);
			
			explanation.addDetail(prodExplanation);
			
			Explanation pdc = new Explanation();
			String vertical = context.reader().document(doc).getField(AbstractSamplerIndexer.INDEX_VERTICAL_FIELD).stringValue();
			int size = collection.getVerticalData(vertical).getSampleSize();
			
			pdc.setDescription("P(d|c): vertical=" + vertical + ", size=" + size);
			pdc.setValue(UiSDocumentCentricQuery.this.getPdc(vertical));
			
			explanation.addDetail(pdc);
			
			explanation.setValue(prodExplanation.getValue() * pdc.getValue());
			
			return explanation;
		}

		@Override
		public Query getQuery() {
			return UiSDocumentCentricQuery.this;
		}

		@Override
		public float getValueForNormalization() throws IOException {
			return 0;
		}

		@Override
		public void normalize(float norm, float topLevelBoost) {
		}

		@Override
		public Scorer scorer(AtomicReaderContext context, Bits acceptDocs) throws IOException {
			List<Scorer> termScorers = new ArrayList<Scorer>();
			boolean allNull = true;
//			System.out.println("SCORER!!!!!!!!!!!!! " );
			
			for(Weight weight : this.termWeights) {
				Scorer scorer = weight.scorer(context, acceptDocs);
				termScorers.add(scorer);
				
//				System.out.println("ADDING A SCORER " + scorer);
				
				if(scorer != null) {
					allNull = false;
				}
			}
			
			if(!allNull) {
				return new UiSDocumentCentricScorer(this, totalTerms, totalTermFreq, context, termScorers, termWeights);
			} else {
				return null;
			}
		}
	}
	
	
	
	protected class UiSDocumentCentricScorer extends Scorer {
		
		protected List<Scorer> termScorers;
		protected List<Weight> termWeights;
		protected AtomicReaderContext context;
		protected final long totalTerms;
		protected final List<Long> totalTermFreq;

		protected UiSDocumentCentricScorer(Weight weight, long totalTerms, List<Long> totalTermFreq, AtomicReaderContext context, List<Scorer> termScorers, List<Weight> termWeights) {
			super(weight);
			
//			System.out.println("START ----------------" + weight);
			
			this.totalTerms = totalTerms;
			this.termScorers = termScorers;
			this.termWeights = termWeights;
			this.context = context;
			this.totalTermFreq = totalTermFreq;
		}

		@Override
		public float score() throws IOException {
			float score = 1;
			int currentDocID = docID();
			
//			System.out.println("DOC " + currentDocID + "   " + context.reader().document(currentDocID).getField(AbstractSamplerIndexer.INDEX_VERTICAL_FIELD).stringValue());
			String vertical = context.reader().document(currentDocID).getField(AbstractSamplerIndexer.INDEX_VERTICAL_FIELD).stringValue();
//			if(vertical.equals("cern")) System.out.println("SCORE DOC " + docID() + "  " + termScorers.size());
			
			for(int i = 0; i < termScorers.size(); i++) {
				float termScore;
				Scorer scorer = termScorers.get(i);
//				if(vertical.equals("cern")) System.out.println(scorer instanceof MatchAllScorer);
//				if((scorer != null) && (scorer.docID() != NO_MORE_DOCS)) {
				if(!(scorer instanceof MatchAllScorer)) {
					if((scorer != null) && (scorer.docID() == currentDocID) && (scorer.docID() != NO_MORE_DOCS)) {
						termScore = scorer.score();
//						if(vertical.equals("cern")) System.out.println("SCORING " + scorer.getWeight().getQuery() + "  " + docID() + "  " + termScore);
					} else {
						UiSDocumentCentricSimilarity.DocumentCentricStats stats = new UiSDocumentCentricSimilarity.DocumentCentricStats(null, 0);
//						if(vertical.equals("cern")) System.out.println(" ->  " + totalTermFreq.get(i) + "  " + totalTerms);
						
						stats.setTotalTermFreq(totalTermFreq.get(i));
						stats.setNumberOfFieldTokens(totalTerms);
						
						UiSDocumentCentricSimilarity.DocumentCentricSimScorer simScorer = new UiSDocumentCentricSimilarity.DocumentCentricSimScorer(context, null, stats, null);
						
						
						Query scoreQuery = termWeights.get(i).getQuery();
						int queryFrequency;
						if(scoreQuery instanceof PowTermQuery) {
							queryFrequency = ((PowTermQuery)scoreQuery).getQueryFreq();
						} else {
							queryFrequency = 1;
						}
						
						termScore = (float)Math.pow(simScorer.score(NO_MORE_DOCS, 0), queryFrequency);
						
//						if(vertical.equals("cern")) System.out.println("SCORING A NULL " + termWeights.get(i) + "  " + termScore);
					}
					
					score *= termScore;
				}
			}
			
//			String vertical = context.reader().document(currentDocID).getField(AbstractSamplerIndexer.INDEX_VERTICAL_FIELD).stringValue();
			score *= UiSDocumentCentricQuery.this.getPdc(vertical);
			
//			System.out.println("DOC " + currentDocID + "   " + context.reader().document(currentDocID).getField(AbstractSamplerIndexer.INDEX_VERTICAL_FIELD).stringValue());
			
			return score;
		}

		@Override
		public int freq() throws IOException {
			return 0;
		}

		@Override
		public int advance(int target) throws IOException {
//			System.out.println("ADVANCE ----------------");
			for(Scorer scorer : termScorers) {
				if(scorer != null) {
					scorer.advance(target);
				}
			}
			return docID();
		}

		@Override
		public long cost() {
			return 0;
		}

		@Override
		public int docID() {
			int docID = -1;
			int internalDocID = NO_MORE_DOCS;
//			System.out.println("----------------");
			
			for(Scorer scorer : termScorers) {
				if(scorer != null) {
//					System.out.println(internalDocID + "  " + scorer.docID());
					
					internalDocID = Math.min(internalDocID, scorer.docID());
					
					
					
					
				}
			}
			
			docID = Math.max(docID, internalDocID);
			
//			System.out.println("================= " + docID);
			
			return docID;
		}

		@Override
		public int nextDoc() throws IOException {
			int currentID = docID();
//			System.out.println("NEXT DOC ---------------- " + currentID);
			for(Scorer scorer : termScorers) {
				if(scorer != null) {
//					if(scorer.docID() != NO_MORE_DOCS) {
					
					if(currentID == scorer.docID()) {
						scorer.nextDoc();
//					System.out.println("NEXT DOC ITER ---------------- " + scorer.weight + "   " +scorer.docID() + "  " + scorer.nextDoc());
					}
//						scorer.nextDoc();
//					}
				}
			}
			return docID();
		}
		
	}
	
	
	
	
	
	
	
	
	
	private class MatchAllScorer extends Scorer {
	    final float score;
	    private int doc = -1;
	    private final int maxDoc;
	    private final Bits liveDocs;

	    MatchAllScorer(IndexReader reader, Bits liveDocs, Weight w, float score) {
	      super(w);
	      this.liveDocs = liveDocs;
	      this.score = score;
	      maxDoc = reader.maxDoc();
	    }

	    @Override
	    public int docID() {
	      return doc;
	    }

	    @Override
	    public int nextDoc() throws IOException {
	      doc++;
	      while(liveDocs != null && doc < maxDoc && !liveDocs.get(doc)) {
	        doc++;
	      }
	      if (doc == maxDoc) {
	        doc = NO_MORE_DOCS;
	      }
	      return doc;
	    }
	    
	    @Override
	    public float score() {
	      return score;
	    }

	    @Override
	    public int freq() {
	      return 1;
	    }

	    @Override
	    public int advance(int target) throws IOException {
	      doc = target-1;
	      return nextDoc();
	    }

	    @Override
	    public long cost() {
	      return maxDoc;
	    }
	  }

	  private class MatchAllDocsWeight extends Weight {

	    public MatchAllDocsWeight(IndexSearcher searcher) {
	    }

	    @Override
	    public String toString() {
	      return "weight(" + UiSDocumentCentricQuery.this + ")";
	    }

	    @Override
	    public Query getQuery() {
	      return UiSDocumentCentricQuery.this;
	    }

	    @Override
	    public float getValueForNormalization() {
	    	return 0;
	    }

	    @Override
	    public void normalize(float queryNorm, float topLevelBoost) {
	    }

	    @Override
	    public Scorer scorer(AtomicReaderContext context, Bits acceptDocs) throws IOException {
	      return new MatchAllScorer(context.reader(), acceptDocs, this, 0);
	    }

	    @Override
	    public Explanation explain(AtomicReaderContext context, int doc) {
	      // explain query weight
//	      Explanation queryExpl = new ComplexExplanation
//	        (true, queryWeight, "MatchAllDocsQuery, product of:");
//	      if (getBoost() != 1.0f) {
//	        queryExpl.addDetail(new Explanation(getBoost(),"boost"));
//	      }
//	      queryExpl.addDetail(new Explanation(queryNorm,"queryNorm"));

	    	Explanation queryExpl = new Explanation();
	      return queryExpl;
	    }
	  }
}
