package aggregator.verticalselection;

import java.io.IOException;
import java.math.BigDecimal;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.SmallFloat;

import aggregator.beans.VerticalCollection;
import aggregator.beans.VerticalCollection.VerticalCollectionData;
import aggregator.sampler.indexer.AbstractSamplerIndexer;
import aggregator.util.CommonUtils;

public class UiSDocumentCentricSimilarity extends Similarity {
	
	/** 
	 * True if overlap tokens (tokens with a position of increment of zero) are
	 * discounted from the document's length.
	 */
	protected boolean discountOverlaps = true;
	protected VerticalCollection verticalCollection;
	
	public UiSDocumentCentricSimilarity(VerticalCollection verticalCollection) {
		this.verticalCollection = verticalCollection;
	}
	
	
	
	/**
	 * Fills all member fields defined in {@code BasicStats} in {@code stats}. 
	 * Subclasses can override this method to fill additional stats.
	 */
	protected void fillBasicStats(BasicStats stats, CollectionStatistics collectionStats, TermStatistics termStats) {
		// #positions(field) must be >= #positions(term)
		assert collectionStats.sumTotalTermFreq() == -1 || collectionStats.sumTotalTermFreq() >= termStats.totalTermFreq();
		long numberOfDocuments = collectionStats.maxDoc();
		
		long docFreq = termStats.docFreq();
		long totalTermFreq = termStats.totalTermFreq();
		
		// codec does not supply totalTermFreq: substitute docFreq
		if (totalTermFreq == -1) {
			totalTermFreq = docFreq;
		}
		
		final long numberOfFieldTokens;
		final float avgFieldLength;
		
		long sumTotalTermFreq = collectionStats.sumTotalTermFreq();
		
		if (sumTotalTermFreq <= 0) {
			// field does not exist;
			// We have to provide something if codec doesnt supply these measures,
			// or if someone omitted frequencies for the field... negative values cause
			// NaN/Inf for some scorers.
			numberOfFieldTokens = docFreq;
			avgFieldLength = 1;
		} else {
			numberOfFieldTokens = sumTotalTermFreq;
			avgFieldLength = (float)numberOfFieldTokens / numberOfDocuments;
		}
		
		stats.setNumberOfDocuments(numberOfDocuments);
		stats.setNumberOfFieldTokens(numberOfFieldTokens);
		stats.setAvgFieldLength(avgFieldLength);
		stats.setDocFreq(docFreq);
		stats.setTotalTermFreq(totalTermFreq);
	}
	
	
	@Override
	public SimWeight computeWeight(float queryBoost, CollectionStatistics collectionStats, TermStatistics... termStats) {
		DocumentCentricStats stats[] = new DocumentCentricStats[termStats.length];
		for (int i = 0; i < termStats.length; i++) {
			stats[i] = new DocumentCentricStats(collectionStats.field(), queryBoost);
			fillBasicStats(stats[i], collectionStats, termStats[i]);
		}
		return stats.length == 1 ? stats[0] : new DocumentCentricMultiStats(stats);
	}
	

	@Override
	public SimScorer simScorer(SimWeight weight, AtomicReaderContext context) throws IOException {
		if (weight instanceof DocumentCentricMultiStats) {
			// a multi term query (e.g. phrase). return the summation, 
			// scoring almost as if it were boolean query
			SimWeight subStats[] = ((DocumentCentricMultiStats) weight).subStats;
			SimScorer subScorers[] = new SimScorer[subStats.length];
			for (int i = 0; i < subScorers.length; i++) {
				DocumentCentricStats basicstats = (DocumentCentricStats) subStats[i];
				subScorers[i] = new DocumentCentricSimScorer(context, verticalCollection, basicstats, context.reader().getNormValues(basicstats.getField()));
			}
			
			return new DocumentCentricMultiSimScorer(subScorers);
		} else {
			DocumentCentricStats basicstats = (DocumentCentricStats) weight;
			return new DocumentCentricSimScorer(context, verticalCollection, basicstats, context.reader().getNormValues(basicstats.getField()));
		}
	}
	
	
	// ------------------------------ Norm handling ------------------------------
	  
	/** Norm -> document length map. */
	private static final float[] NORM_TABLE = new float[256];

	static {
	    for (int i = 0; i < 256; i++) {
	      float floatNorm = SmallFloat.byte315ToFloat((byte)i);
	      NORM_TABLE[i] = 1.0f / (floatNorm * floatNorm);
	    }
	}
	  
	/** Decodes a normalization factor (document length) stored in an index.
	 * @see #encodeNormValue(float,float)
	 */
	protected float decodeNormValue(byte norm) {
		return NORM_TABLE[norm & 0xFF];  // & 0xFF maps negative bytes to positive above 127
	}
	  
	/** Encodes the length to a byte via SmallFloat. */
	protected byte encodeNormValue(float boost, float length) {
		return SmallFloat.floatToByte315((boost / (float) Math.sqrt(length)));
	}
	  
	@Override
	public long computeNorm(FieldInvertState state) {
		final long numTerms;
	    if (discountOverlaps)
	      numTerms = state.getLength() - state.getNumOverlap();
	    else
	      numTerms = state.getLength();
	    
	    // If the exact document size is not that relevant, using encodeNormValue()
	    // can help reducing the size of the index using an approximation.
	    return numTerms;
//	    return encodeNormValue(state.getBoost(), numTerms);
	}
	
	
	private static class DocumentCentricStats extends BasicStats {
		private String field;
		
		public DocumentCentricStats(String field, float queryBoost) {
			super(field, queryBoost);
			
			this.field = field;
		}
		
		public String getField() {
			return this.field;
		}
	}
	
	
	
	static class DocumentCentricSimScorer extends SimScorer {
	
		/** Smoothing Parameter */
		private static final float LAMBDA = 0.1f;
		
		
		private DocumentCentricStats stats;
		private NumericDocValues docValues;
		private AtomicReaderContext context;
		private VerticalCollection verticalCollection;
		
		public DocumentCentricSimScorer(AtomicReaderContext context, VerticalCollection verticalCollection, DocumentCentricStats stats, NumericDocValues docValues) {
			this.docValues = docValues;
			this.stats = stats;
			this.context = context;
			this.verticalCollection = verticalCollection;
		}
		

		@Override
		public float computePayloadFactor(int doc, int start, int end, BytesRef payload) {
			return 1f;
		}

		@Override
		public float computeSlopFactor(int distance) {
			return 1.0f / (distance + 1);
		}
		
		protected float computePt() {
			float result = 0;
			long collectionTermFreq = stats.getTotalTermFreq();
			long collectionTotalTerms = stats.getNumberOfFieldTokens();
			
			result = (BigDecimal.valueOf(collectionTermFreq).divide(BigDecimal.valueOf(collectionTotalTerms), CommonUtils.DEFAULT_DIVISION_SCALE, CommonUtils.DEFAULT_ROUNDING_MODE)).multiply(BigDecimal.valueOf(stats.getNumberOfDocuments())).floatValue();
			
//			result = (collectionTermFreq/collectionTotalTerms)*stats.getNumberOfDocuments();
			return result;
		}
		
		protected float computePtd(int doc, float freq) {
			float result = 0;
			long documentTotalTerms = docValues.get(doc);
			
			try
			{
				Document document = context.reader().document(doc);
				VerticalCollectionData verticalData = verticalCollection.getVerticalData(document.get(AbstractSamplerIndexer.INDEX_VERTICAL_FIELD));
				
				result = (BigDecimal.valueOf(freq).divide(BigDecimal.valueOf(documentTotalTerms), CommonUtils.DEFAULT_DIVISION_SCALE, CommonUtils.DEFAULT_ROUNDING_MODE)).multiply(BigDecimal.valueOf(verticalData.getSampleSize())).floatValue();
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}
			
			
			return result;
		}

		@Override
		public float score(int doc, float freq) {
			float Pt = this.computePt();
			float Ptd = this.computePtd(doc, freq);
			
			return (((1-LAMBDA) * Ptd) + (LAMBDA*Pt));
		}
		
		
		
		@Override
		public Explanation explain(int doc, Explanation freq) {
			Explanation result = new Explanation(this.score(doc, freq.getValue()), "score(doc="+doc+"), formula=(((1-LAMBDA)*P(t|d)) + (LAMBDA*P(t)))");

			try
			{
				Document document = context.reader().document(doc);
				VerticalCollectionData verticalData = verticalCollection.getVerticalData(document.get(AbstractSamplerIndexer.INDEX_VERTICAL_FIELD));
				
				
				result.addDetail(new Explanation(LAMBDA, "Lambda"));
				result.addDetail(new Explanation(this.computePt(), "P(t): collectionTermFreq=" + stats.getTotalTermFreq() + ", collectionTotalTerms=" + stats.getNumberOfFieldTokens() + ", docs=" + stats.getNumberOfDocuments()));
				result.addDetail(new Explanation(this.computePtd(doc, freq.getValue()), "P(t|d): documentTermFreq=" + freq.getValue() + ", documentTotalTerms=" + docValues.get(doc) + ", sampleSize=" + verticalData.getSampleSize()));
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}
			
			return result;
		}
	}
	  
	  

	static class DocumentCentricMultiSimScorer extends SimScorer {
	    private final SimScorer subScorers[];
	    
	    public DocumentCentricMultiSimScorer(SimScorer subScorers[]) {
	      this.subScorers = subScorers;
	    }
	    
	    @Override
	    public float score(int doc, float freq) {
	      float sum = 0.0f;
	      for (SimScorer subScorer : subScorers) {
	        sum += subScorer.score(doc, freq);
	      }
	      return sum;
	    }

	    @Override
	    public Explanation explain(int doc, Explanation freq) {
	      Explanation expl = new Explanation(score(doc, freq.getValue()), "sum of:");
	      for (SimScorer subScorer : subScorers) {
	        expl.addDetail(subScorer.explain(doc, freq));
	      }
	      return expl;
	    }

	    @Override
	    public float computeSlopFactor(int distance) {
	      return subScorers[0].computeSlopFactor(distance);
	    }

	    @Override
	    public float computePayloadFactor(int doc, int start, int end, BytesRef payload) {
	    	return subScorers[0].computePayloadFactor(doc, start, end, payload);
	    }
	}

	  
	
	
	
	static class DocumentCentricMultiStats extends SimWeight {
		final SimWeight subStats[];
	    
		DocumentCentricMultiStats(SimWeight subStats[]) {
			this.subStats = subStats;
		}
		
		@Override
		public float getValueForNormalization() {
			float sum = 0.0f;
			for (SimWeight stat : subStats) {
				sum *= stat.getValueForNormalization();
			}
			return sum / subStats.length;
		}
		
		@Override
		public void normalize(float queryNorm, float topLevelBoost) {
			for (SimWeight stat : subStats) {
				stat.normalize(queryNorm, topLevelBoost);
			}
		}
	}
	
}
