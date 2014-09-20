package org.apache.lucene.search;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Set;

import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.IndexReaderContext;
import org.apache.lucene.index.ReaderUtil;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermContext;
import org.apache.lucene.index.TermState;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.Similarity.SimScorer;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.ToStringUtils;

public class PowTermQuery extends Query {
	  private final Term term;
	  private final int docFreq;
	  private final TermContext perReaderTermState;
	  private final int queryFreq;
	
	  /** Constructs a query for the term <code>t</code>. */
	  public PowTermQuery(Term t, int queryFreq) {
	    this(t, -1, queryFreq);
	  }

	  /** Expert: constructs a TermQuery that will use the
	   *  provided docFreq instead of looking up the docFreq
	   *  against the searcher. */
	  public PowTermQuery(Term t, int docFreq, int queryFreq) {
	    term = t;
	    this.docFreq = docFreq;
	    this.queryFreq = queryFreq;
	    perReaderTermState = null;
	  }
	  
	  /** Expert: constructs a TermQuery that will use the
	   *  provided docFreq instead of looking up the docFreq
	   *  against the searcher. */
	  public PowTermQuery(Term t, TermContext states, int queryFreq) {
	    assert states != null;
	    term = t;
	    docFreq = states.docFreq();
	    this.queryFreq = queryFreq;
	    perReaderTermState = states;
	  }

	  /** Returns the term of this query. */
	  public Term getTerm() { return term; }
	  
	  

	  /**
	 * @return the queryFreq
	 */
	public int getQueryFreq() {
		return queryFreq;
	}

	@Override
	  public Weight createWeight(IndexSearcher searcher) throws IOException {
//	  System.out.println("POW CREATE WEIGHT!!!!!!!!!!!!!!!!");
		  
		  final IndexReaderContext context = searcher.getTopReaderContext();
	    final TermContext termState;
	    if (perReaderTermState == null || perReaderTermState.topReaderContext != context) {
	      // make TermQuery single-pass if we don't have a PRTS or if the context differs!
	      termState = TermContext.build(context, term);
	    } else {
	     // PRTS was pre-build for this IS
	     termState = this.perReaderTermState;
	    }

	    // we must not ignore the given docFreq - if set use the given value (lie)
	    if (docFreq != -1)
	      termState.setDocFreq(docFreq);
	    
	    return new PowTermWeight(searcher, termState);
	  }

	  @Override
	  public void extractTerms(Set<Term> terms) {
	    terms.add(getTerm());
	  }

	  /** Prints a user-readable version of this query. */
	  @Override
	  public String toString(String field) {
	    StringBuilder buffer = new StringBuilder();
	    if (!term.field().equals(field)) {
	      buffer.append(term.field());
	      buffer.append(":");
	    }
	    buffer.append(term.text());
	    buffer.append(ToStringUtils.boost(getBoost()));
	    return buffer.toString();
	  }

	  /** Returns true iff <code>o</code> is equal to this. */
	  @Override
	  public boolean equals(Object o) {
	    if (!(o instanceof PowTermQuery))
	      return false;
	    PowTermQuery other = (PowTermQuery)o;
	    return (this.getBoost() == other.getBoost())
	      && this.term.equals(other.term);
	  }

	  /** Returns a hash code value for this object.*/
	  @Override
	  public int hashCode() {
	    return Float.floatToIntBits(getBoost()) ^ term.hashCode();
	  }
	  
	  
	
	
	
	private class PowTermWeight extends Weight {
		 private final Similarity similarity;
		    private final Similarity.SimWeight stats;
		    private final TermContext termStates;

		    public PowTermWeight(IndexSearcher searcher, TermContext termStates)
		      throws IOException {
		      assert termStates != null : "TermContext must not be null";
		      this.termStates = termStates;
		      this.similarity = searcher.getSimilarity();
		      this.stats = similarity.computeWeight(
		          getBoost(), 
		          searcher.collectionStatistics(getTerm().field()), 
		          searcher.termStatistics(getTerm(), termStates));
		    }

		    @Override
		    public String toString() { return "weight(" + PowTermQuery.this + ")"; }

		    @Override
		    public Query getQuery() { return PowTermQuery.this; }

		    @Override
		    public float getValueForNormalization() {
		      return stats.getValueForNormalization();
		    }

		    @Override
		    public void normalize(float queryNorm, float topLevelBoost) {
		      stats.normalize(queryNorm, topLevelBoost);
		    }

		    @Override
		    public Scorer scorer(AtomicReaderContext context, Bits acceptDocs) throws IOException {
		      assert termStates.topReaderContext == ReaderUtil.getTopLevelContext(context) : "The top-reader used to create Weight (" + termStates.topReaderContext + ") is not the same as the current reader's top-reader (" + ReaderUtil.getTopLevelContext(context);
		      
//		      System.out.println("POW SCORER!!!!!!!!!!");
		      
		      final TermsEnum termsEnum = getTermsEnum(context);
		      if (termsEnum == null) {
		        return null;
		      }
		      DocsEnum docs = termsEnum.docs(acceptDocs, null);
		      assert docs != null;
		      return new PowTermScorer(this, docs, similarity.simScorer(stats, context));
		    }
		    
		    /**
		     * Returns a {@link TermsEnum} positioned at this weights Term or null if
		     * the term does not exist in the given context
		     */
		    private TermsEnum getTermsEnum(AtomicReaderContext context) throws IOException {
		      final TermState state = termStates.get(context.ord);
		      if (state == null) { // term is not present in that reader
		        assert termNotInReader(context.reader(), getTerm()) : "no termstate found but term exists in reader term=" + getTerm();
		        termNotInReader(context.reader(), getTerm());
		        return null;
		      }
		      //System.out.println("LD=" + reader.getLiveDocs() + " set?=" + (reader.getLiveDocs() != null ? reader.getLiveDocs().get(0) : "null"));
		      final TermsEnum termsEnum = context.reader().terms(getTerm().field()).iterator(null);
		      termsEnum.seekExact(getTerm().bytes(), state);
		      return termsEnum;
		    }
		    
		    private boolean termNotInReader(AtomicReader reader, Term term) throws IOException {
		      // only called from assert
//		      System.out.println("TQ.termNotInReader reader=" + reader + " term=" + getTerm().field() + ":" + getTerm().bytes().utf8ToString());
		      return reader.docFreq(term) == 0;
		    }
		    
		    @Override
		    public Explanation explain(AtomicReaderContext context, int doc) throws IOException {
		    	Scorer scorer = scorer(context, context.reader().getLiveDocs());
		    	float freq = 0;
		    	
		    	ComplexExplanation result = new ComplexExplanation();
		    	result.setDescription("weight("+getQuery()+" in "+doc+") [" + similarity.getClass().getSimpleName() + "], result of:");
		    	
		    	if (scorer != null) {
		    		int newDoc = scorer.advance(doc);
		    		if(newDoc == doc) {
		    			result.setMatch(true);
		    			freq = scorer.freq();
		    		} else {
		    			result.setMatch(false);
		    		}
		    	}
		    	
		    	SimScorer docScorer = similarity.simScorer(stats, context);
		    	Explanation scoreExplanation = docScorer.explain(doc, new Explanation(freq, "termFreq=" + freq));
		    	
		        ComplexExplanation powResult = new ComplexExplanation();
		    	powResult.setDescription("score^[n(t,q)]");  
		    	powResult.addDetail(scoreExplanation);
		    	powResult.setValue(BigDecimal.valueOf(scoreExplanation.getValue()).pow(queryFreq).floatValue());
		    	powResult.addDetail(new Explanation(queryFreq, "n(t,q)"));
		    	
		    	result.addDetail(powResult);
		    	result.setValue(powResult.getValue());
		    	
		        
		    	return result;
		    	
		    	
		    	
//		      Scorer scorer = scorer(context, context.reader().getLiveDocs());
//		      if (scorer != null) {
//		        int newDoc = scorer.advance(doc);
//		        if (newDoc == doc) {
//		          float freq = scorer.freq();
//		          SimScorer docScorer = similarity.simScorer(stats, context);
//		          ComplexExplanation result = new ComplexExplanation();
//		          result.setDescription("weight("+getQuery()+" in "+doc+") [" + similarity.getClass().getSimpleName() + "], result of:");
//		          
//		          ComplexExplanation powResult = new ComplexExplanation();
//		          Explanation scoreExplanation = docScorer.explain(doc, new Explanation(freq, "termFreq=" + freq));
//		          
//		          
//		          powResult.setDescription("score^[n(t,q)]");
//		          powResult.addDetail(scoreExplanation);
//		          powResult.addDetail(new Explanation(queryFreq, "n(t,q)"));
//		          powResult.setValue(BigDecimal.valueOf(scoreExplanation.getValue()).pow(queryFreq).floatValue());
//		          
//
//		          result.addDetail(powResult);
//		          result.setMatch(true);
//		          result.setValue(powResult.getValue());
//		          return result;
//		        }
//		      }
//		      return new ComplexExplanation(false, 0.0f, "no matching term");      
		    }
	}
	
	
	
	
	
	
	protected class PowTermScorer extends Scorer {
		
		private final DocsEnum docsEnum;
		  private final Similarity.SimScorer docScorer;
		  
		  /**
		   * Construct a <code>TermScorer</code>.
		   * 
		   * @param weight
		   *          The weight of the <code>Term</code> in the query.
		   * @param td
		   *          An iterator over the documents matching the <code>Term</code>.
		   * @param docScorer
		   *          The </code>Similarity.SimScorer</code> implementation 
		   *          to be used for score computations.
		   */
		  PowTermScorer(Weight weight, DocsEnum td, Similarity.SimScorer docScorer) {
		    super(weight);
		    this.docScorer = docScorer;
		    this.docsEnum = td;
		  }

		  @Override
		  public int docID() {
		    return docsEnum.docID();
		  }

		  @Override
		  public int freq() throws IOException {
		    return docsEnum.freq();
		  }

		  /**
		   * Advances to the next document matching the query. <br>
		   * 
		   * @return the document matching the query or NO_MORE_DOCS if there are no more documents.
		   */
		  @Override
		  public int nextDoc() throws IOException {
		    return docsEnum.nextDoc();
		  }
		  
		  @Override
		  public float score() throws IOException {
		    assert docID() != NO_MORE_DOCS;
		    return BigDecimal.valueOf(docScorer.score(docsEnum.docID(), docsEnum.freq())).pow(queryFreq).floatValue();  
		  }

		  /**
		   * Advances to the first match beyond the current whose document number is
		   * greater than or equal to a given target. <br>
		   * The implementation uses {@link DocsEnum#advance(int)}.
		   * 
		   * @param target
		   *          The target document number.
		   * @return the matching document or NO_MORE_DOCS if none exist.
		   */
		  @Override
		  public int advance(int target) throws IOException {
		    return docsEnum.advance(target);
		  }
		  
		  @Override
		  public long cost() {
		    return docsEnum.cost();
		  }

		  /** Returns a string representation of this <code>TermScorer</code>. */
		  @Override
		  public String toString() { return "scorer(" + weight + ")"; }
	}
	
}
