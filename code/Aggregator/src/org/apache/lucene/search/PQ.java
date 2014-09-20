package org.apache.lucene.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.ToStringUtils;

public class PQ extends Query  implements Iterable<BooleanClause> {
	
	private static int maxClauseCount = 1024;
	
	public PQ(BooleanQuery query) {
		this();
		Map<Term, MutableInt> termsMap = new HashMap<Term, MutableInt>();
		List<Term> termsList = new ArrayList<Term>();
		
		for(BooleanClause clause : query.getClauses()) {
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
			this.add(new BooleanClause(new PowTermQuery(t, termsMap.get(t).intValue()), Occur.SHOULD));
		}
	}
	
	

	  /** Thrown when an attempt is made to add more than {@link
	   * #getMaxClauseCount()} clauses. This typically happens if
	   * a PrefixQuery, FuzzyQuery, WildcardQuery, or TermRangeQuery 
	   * is expanded to many terms during search. 
	   */
	  public static class TooManyClauses extends RuntimeException {
	    public TooManyClauses() {
	      super("maxClauseCount is set to " + maxClauseCount);
	    }
	  }

	  /** Return the maximum number of clauses permitted, 1024 by default.
	   * Attempts to add more than the permitted number of clauses cause {@link
	   * TooManyClauses} to be thrown.
	   * @see #setMaxClauseCount(int)
	   */
	  public static int getMaxClauseCount() { return maxClauseCount; }

	  /** 
	   * Set the maximum number of clauses permitted per BooleanQuery.
	   * Default value is 1024.
	   */
	  public static void setMaxClauseCount(int maxClauseCount) {
	    if (maxClauseCount < 1) {
	      throw new IllegalArgumentException("maxClauseCount must be >= 1");
	    }
	    PQ.maxClauseCount = maxClauseCount;
	  }

	  private ArrayList<BooleanClause> clauses = new ArrayList<>();
	  private final boolean disableCoord;

	  /** Constructs an empty boolean query. */
	  public PQ() {
	    disableCoord = false;
	  }

	  /** Constructs an empty boolean query.
	   *
	   * {@link Similarity#coord(int,int)} may be disabled in scoring, as
	   * appropriate. For example, this score factor does not make sense for most
	   * automatically generated queries, like {@link WildcardQuery} and {@link
	   * FuzzyQuery}.
	   *
	   * @param disableCoord disables {@link Similarity#coord(int,int)} in scoring.
	   */
	  public PQ(boolean disableCoord) {
	    this.disableCoord = disableCoord;
	  }

	  /** Returns true iff {@link Similarity#coord(int,int)} is disabled in
	   * scoring for this query instance.
	   * @see #BooleanQuery(boolean)
	   */
	  public boolean isCoordDisabled() { return disableCoord; }

	  /**
	   * Specifies a minimum number of the optional BooleanClauses
	   * which must be satisfied.
	   *
	   * <p>
	   * By default no optional clauses are necessary for a match
	   * (unless there are no required clauses).  If this method is used,
	   * then the specified number of clauses is required.
	   * </p>
	   * <p>
	   * Use of this method is totally independent of specifying that
	   * any specific clauses are required (or prohibited).  This number will
	   * only be compared against the number of matching optional clauses.
	   * </p>
	   *
	   * @param min the number of optional clauses that must match
	   */
	  public void setMinimumNumberShouldMatch(int min) {
	    this.minNrShouldMatch = min;
	  }
	  protected int minNrShouldMatch = 0;

	  /**
	   * Gets the minimum number of the optional BooleanClauses
	   * which must be satisfied.
	   */
	  public int getMinimumNumberShouldMatch() {
	    return minNrShouldMatch;
	  }

	  /** Adds a clause to a boolean query.
	   *
	   * @throws TooManyClauses if the new number of clauses exceeds the maximum clause number
	   * @see #getMaxClauseCount()
	   */
	  public void add(Query query, BooleanClause.Occur occur) {
	    add(new BooleanClause(query, occur));
	  }

	  /** Adds a clause to a boolean query.
	   * @throws TooManyClauses if the new number of clauses exceeds the maximum clause number
	   * @see #getMaxClauseCount()
	   */
	  public void add(BooleanClause clause) {
	    if (clauses.size() >= maxClauseCount) {
	      throw new TooManyClauses();
	    }

	    clauses.add(clause);
	  }

	  /** Returns the set of clauses in this query. */
	  public BooleanClause[] getClauses() {
	    return clauses.toArray(new BooleanClause[clauses.size()]);
	  }

	  /** Returns the list of clauses in this query. */
	  public List<BooleanClause> clauses() { return clauses; }

	  /** Returns an iterator on the clauses in this query. It implements the {@link Iterable} interface to
	   * make it possible to do:
	   * <pre class="prettyprint">for (BooleanClause clause : booleanQuery) {}</pre>
	   */
	  @Override
	  public final Iterator<BooleanClause> iterator() { return clauses.iterator(); }

	  /**
	   * Expert: the Weight for BooleanQuery, used to
	   * normalize, score and explain these queries.
	   *
	   * @lucene.experimental
	   */
	  protected class BooleanWeight extends Weight {
	    /** The Similarity implementation. */
	    protected Similarity similarity;
	    protected ArrayList<Weight> weights;
	    protected int maxCoord;  // num optional + num required
	    private final boolean disableCoord;

	    public BooleanWeight(IndexSearcher searcher, boolean disableCoord)
	      throws IOException {
	      this.similarity = searcher.getSimilarity();
	      this.disableCoord = disableCoord;
	      weights = new ArrayList<>(clauses.size());
	      for (int i = 0 ; i < clauses.size(); i++) {
	        BooleanClause c = clauses.get(i);
	        Weight w = c.getQuery().createWeight(searcher);
	        weights.add(w);
	        if (!c.isProhibited()) {
	          maxCoord++;
	        }
	      }
	    }

	    @Override
	    public Query getQuery() { return PQ.this; }

	    @Override
	    public float getValueForNormalization() throws IOException {
	      float sum = 0.0f;
	      for (int i = 0 ; i < weights.size(); i++) {
	        // call sumOfSquaredWeights for all clauses in case of side effects
	        float s = weights.get(i).getValueForNormalization();         // sum sub weights
	        if (!clauses.get(i).isProhibited()) {
	          // only add to sum for non-prohibited clauses
	          sum += s;
	        }
	      }

	      sum *= getBoost() * getBoost();             // boost each sub-weight

	      return sum ;
	    }

	    public float coord(int overlap, int maxOverlap) {
	      // LUCENE-4300: in most cases of maxOverlap=1, BQ rewrites itself away,
	      // so coord() is not applied. But when BQ cannot optimize itself away
	      // for a single clause (minNrShouldMatch, prohibited clauses, etc), its
	      // important not to apply coord(1,1) for consistency, it might not be 1.0F
	      return maxOverlap == 1 ? 1F : similarity.coord(overlap, maxOverlap);
	    }

	    @Override
	    public void normalize(float norm, float topLevelBoost) {
	      topLevelBoost *= getBoost();                         // incorporate boost
	      for (Weight w : weights) {
	        // normalize all clauses, (even if prohibited in case of side affects)
	        w.normalize(norm, topLevelBoost);
	      }
	    }

	    @Override
	    public Explanation explain(AtomicReaderContext context, int doc)
	      throws IOException {
	      final int minShouldMatch =
	        PQ.this.getMinimumNumberShouldMatch();
	      ComplexExplanation sumExpl = new ComplexExplanation();
	      sumExpl.setDescription("product of:");
	      int coord = 0;
	      float sum = 0.0f;
	      boolean fail = false;
	      int shouldMatchCount = 0;
	      Iterator<BooleanClause> cIter = clauses.iterator();
	      for (Iterator<Weight> wIter = weights.iterator(); wIter.hasNext();) {
	        Weight w = wIter.next();
	        BooleanClause c = cIter.next();
	        if (w.scorer(context, context.reader().getLiveDocs()) == null) {
	          if (c.isRequired()) {
	            fail = true;
	            Explanation r = new Explanation(0.0f, "no match on required clause (" + c.getQuery().toString() + ")");
	            sumExpl.addDetail(r);
	          }
	          continue;
	        }
	        Explanation e = w.explain(context, doc);
	        if (e.isMatch()) {
	          if (!c.isProhibited()) {
	            sumExpl.addDetail(e);
	            if(sum == 0) {
	            	sum = e.getValue();
	            } else {
	            	sum *= e.getValue();
	            }
	            coord++;
	          } else {
	            Explanation r =
	              new Explanation(0.0f, "match on prohibited clause (" + c.getQuery().toString() + ")");
	            r.addDetail(e);
	            sumExpl.addDetail(r);
	            fail = true;
	          }
	          if (c.getOccur() == Occur.SHOULD) {
	            shouldMatchCount++;
	          }
	        } else if (c.isRequired()) {
	          Explanation r = new Explanation(0.0f, "no match on required clause (" + c.getQuery().toString() + ")");
	          r.addDetail(e);
	          sumExpl.addDetail(r);
	          fail = true;
	        }
	      }
	      if (fail) {
	        sumExpl.setMatch(Boolean.FALSE);
	        sumExpl.setValue(0.0f);
	        sumExpl.setDescription
	          ("Failure to meet condition(s) of required/prohibited clause(s)");
	        return sumExpl;
	      } else if (shouldMatchCount < minShouldMatch) {
	        sumExpl.setMatch(Boolean.FALSE);
	        sumExpl.setValue(0.0f);
	        sumExpl.setDescription("Failure to match minimum number "+
	                               "of optional clauses: " + minShouldMatch);
	        return sumExpl;
	      }
	      
	      sumExpl.setMatch(0 < coord ? Boolean.TRUE : Boolean.FALSE);
	      sumExpl.setValue(sum);
	      
	      final float coordFactor = disableCoord ? 1.0f : coord(coord, maxCoord);
	      if (coordFactor == 1.0f) {
	        return sumExpl;                             // eliminate wrapper
	      } else {
	        ComplexExplanation result = new ComplexExplanation(sumExpl.isMatch(),
	                                                           sum*coordFactor,
	                                                           "product of:");
	        result.addDetail(sumExpl);
	        result.addDetail(new Explanation(coordFactor,
	                                         "coord("+coord+"/"+maxCoord+")"));
	        return result;
	      }
	    }

	    @Override
	    public BulkScorer bulkScorer(AtomicReaderContext context, boolean scoreDocsInOrder,
	                                 Bits acceptDocs) throws IOException {

	      if (scoreDocsInOrder || minNrShouldMatch > 1) {
	        // TODO: (LUCENE-4872) in some cases BooleanScorer may be faster for minNrShouldMatch
	        // but the same is even true of pure conjunctions...
	        return super.bulkScorer(context, scoreDocsInOrder, acceptDocs);
	      }

	      List<BulkScorer> prohibited = new ArrayList<>();
	      List<BulkScorer> optional = new ArrayList<>();
	      Iterator<BooleanClause> cIter = clauses.iterator();
	      for (Weight w  : weights) {
	        BooleanClause c =  cIter.next();
	        BulkScorer subScorer = w.bulkScorer(context, false, acceptDocs);
	        if (subScorer == null) {
	          if (c.isRequired()) {
	            return null;
	          }
	        } else if (c.isRequired()) {
	          // TODO: there are some cases where BooleanScorer
	          // would handle conjunctions faster than
	          // BooleanScorer2...
	          return super.bulkScorer(context, scoreDocsInOrder, acceptDocs);
	        } else if (c.isProhibited()) {
	          prohibited.add(subScorer);
	        } else {
	          optional.add(subScorer);
	        }
	      }

	      return new PQScorer(this, disableCoord, minNrShouldMatch, optional, prohibited, maxCoord);
	    }

	    @Override
	    public Scorer scorer(AtomicReaderContext context, Bits acceptDocs)
	        throws IOException {
	      // initially the user provided value,
	      // but if minNrShouldMatch == optional.size(),
	      // we will optimize and move these to required, making this 0
	      int minShouldMatch = minNrShouldMatch;

	      List<Scorer> required = new ArrayList<>();
	      List<Scorer> prohibited = new ArrayList<>();
	      List<Scorer> optional = new ArrayList<>();
	      Iterator<BooleanClause> cIter = clauses.iterator();
	      for (Weight w  : weights) {
	        BooleanClause c =  cIter.next();
	        Scorer subScorer = w.scorer(context, acceptDocs);
	        if (subScorer == null) {
	          if (c.isRequired()) {
	            return null;
	          }
	        } else if (c.isRequired()) {
	          required.add(subScorer);
	        } else if (c.isProhibited()) {
	          prohibited.add(subScorer);
	        } else {
	          optional.add(subScorer);
	        }
	      }
	      
	      // scorer simplifications:
	      
	      if (optional.size() == minShouldMatch) {
	        // any optional clauses are in fact required
	        required.addAll(optional);
	        optional.clear();
	        minShouldMatch = 0;
	      }
	      
	      if (required.isEmpty() && optional.isEmpty()) {
	        // no required and optional clauses.
	        return null;
	      } else if (optional.size() < minShouldMatch) {
	        // either >1 req scorer, or there are 0 req scorers and at least 1
	        // optional scorer. Therefore if there are not enough optional scorers
	        // no documents will be matched by the query
	        return null;
	      }
	      
	      // three cases: conjunction, disjunction, or mix
	      
	      // pure conjunction
	      if (optional.isEmpty()) {
	        return excl(req(required, disableCoord), prohibited);
	      }
	      
	      // pure disjunction
	      if (required.isEmpty()) {
	        return excl(opt(optional, minShouldMatch, disableCoord), prohibited);
	      }
	      
	      // conjunction-disjunction mix:
	      // we create the required and optional pieces with coord disabled, and then
	      // combine the two: if minNrShouldMatch > 0, then its a conjunction: because the
	      // optional side must match. otherwise its required + optional, factoring the
	      // number of optional terms into the coord calculation
	      
	      Scorer req = excl(req(required, true), prohibited);
	      Scorer opt = opt(optional, minShouldMatch, true);

	      // TODO: clean this up: its horrible
	      if (disableCoord) {
	        if (minShouldMatch > 0) {
	          return new ConjunctionScorer(this, new Scorer[] { req, opt }, 1F);
	        } else {
	          return new ReqOptSumScorer(req, opt);          
	        }
	      } else if (optional.size() == 1) {
	        if (minShouldMatch > 0) {
	          return new ConjunctionScorer(this, new Scorer[] { req, opt }, coord(required.size()+1, maxCoord));
	        } else {
	          float coordReq = coord(required.size(), maxCoord);
	          float coordBoth = coord(required.size() + 1, maxCoord);
	          return new BooleanTopLevelScorers.ReqSingleOptScorer(req, opt, coordReq, coordBoth);
	        }
	      } else {
	        if (minShouldMatch > 0) {
	          return new BooleanTopLevelScorers.CoordinatingConjunctionScorer(this, coords(), req, required.size(), opt);
	        } else {
	          return new BooleanTopLevelScorers.ReqMultiOptScorer(req, opt, required.size(), coords()); 
	        }
	      }
	    }
	    
	    @Override
	    public boolean scoresDocsOutOfOrder() {
	      if (minNrShouldMatch > 1) {
	        // BS2 (in-order) will be used by scorer()
	        return false;
	      }
	      int optionalCount = 0;
	      for (BooleanClause c : clauses) {
	        if (c.isRequired()) {
	          // BS2 (in-order) will be used by scorer()
	          return false;
	        } else if (!c.isProhibited()) {
	          optionalCount++;
	        }
	      }
	      
	      if (optionalCount == minNrShouldMatch) {
	        return false; // BS2 (in-order) will be used, as this means conjunction
	      }
	      
	      // scorer() will return an out-of-order scorer if requested.
	      return true;
	    }
	    
	    private Scorer req(List<Scorer> required, boolean disableCoord) {
	      if (required.size() == 1) {
	        Scorer req = required.get(0);
	        if (!disableCoord && maxCoord > 1) {
	          return new BooleanTopLevelScorers.BoostedScorer(req, coord(1, maxCoord));
	        } else {
	          return req;
	        }
	      } else {
	        return new ConjunctionScorer(this, 
	                                     required.toArray(new Scorer[required.size()]),
	                                     disableCoord ? 1.0F : coord(required.size(), maxCoord));
	      }
	    }
	    
	    private Scorer excl(Scorer main, List<Scorer> prohibited) throws IOException {
	      if (prohibited.isEmpty()) {
	        return main;
	      } else if (prohibited.size() == 1) {
	        return new ReqExclScorer(main, prohibited.get(0));
	      } else {
	        float coords[] = new float[prohibited.size()+1];
	        Arrays.fill(coords, 1F);
	        return new ReqExclScorer(main, 
	                                 new DisjunctionSumScorer(this, 
	                                                          prohibited.toArray(new Scorer[prohibited.size()]), 
	                                                          coords));
	      }
	    }
	    
	    private Scorer opt(List<Scorer> optional, int minShouldMatch, boolean disableCoord) throws IOException {
	      if (optional.size() == 1) {
	        Scorer opt = optional.get(0);
	        if (!disableCoord && maxCoord > 1) {
	          return new BooleanTopLevelScorers.BoostedScorer(opt, coord(1, maxCoord));
	        } else {
	          return opt;
	        }
	      } else {
	        float coords[];
	        if (disableCoord) {
	          coords = new float[optional.size()+1];
	          Arrays.fill(coords, 1F);
	        } else {
	          coords = coords();
	        }
	        if (minShouldMatch > 1) {
	          return new MinShouldMatchSumScorer(this, optional, minShouldMatch, coords);
	        } else {
	          return new DisjunctionSumScorer(this, 
	                                          optional.toArray(new Scorer[optional.size()]), 
	                                          coords);
	        }
	      }
	    }
	    
	    private float[] coords() {
	      float[] coords = new float[maxCoord+1];
	      coords[0] = 0F;
	      for (int i = 1; i < coords.length; i++) {
	        coords[i] = coord(i, maxCoord);
	      }
	      return coords;
	    }
	  }

	  @Override
	  public Weight createWeight(IndexSearcher searcher) throws IOException {
	    return new BooleanWeight(searcher, disableCoord);
	  }

	  @Override
	  public Query rewrite(IndexReader reader) throws IOException {
	    if (minNrShouldMatch == 0 && clauses.size() == 1) {                    // optimize 1-clause queries
	      BooleanClause c = clauses.get(0);
	      if (!c.isProhibited()) {  // just return clause

	        Query query = c.getQuery().rewrite(reader);    // rewrite first

	        if (getBoost() != 1.0f) {                 // incorporate boost
	          if (query == c.getQuery()) {                   // if rewrite was no-op
	            query = query.clone();         // then clone before boost
	          }
	          // Since the BooleanQuery only has 1 clause, the BooleanQuery will be
	          // written out. Therefore the rewritten Query's boost must incorporate both
	          // the clause's boost, and the boost of the BooleanQuery itself
	          query.setBoost(getBoost() * query.getBoost());
	        }

	        return query;
	      }
	    }

	    PQ clone = null;                    // recursively rewrite
	    for (int i = 0 ; i < clauses.size(); i++) {
	      BooleanClause c = clauses.get(i);
	      Query query = c.getQuery().rewrite(reader);
	      if (query != c.getQuery()) {                     // clause rewrote: must clone
	        if (clone == null) {
	          // The BooleanQuery clone is lazily initialized so only initialize
	          // it if a rewritten clause differs from the original clause (and hasn't been
	          // initialized already).  If nothing differs, the clone isn't needlessly created
	          clone = this.clone();
	        }
	        clone.clauses().set(i, new BooleanClause(query, c.getOccur()));
	      }
	    }
	    if (clone != null) {
	      return clone;                               // some clauses rewrote
	    } else {
	      return this;                                // no clauses rewrote
	    }
	  }

	  // inherit javadoc
	  @Override
	  public void extractTerms(Set<Term> terms) {
	    for (BooleanClause clause : clauses) {
	      if (clause.getOccur() != Occur.MUST_NOT) {
	        clause.getQuery().extractTerms(terms);
	      }
	    }
	  }

	  @Override @SuppressWarnings("unchecked")
	  public PQ clone() {
	    PQ clone = (PQ)super.clone();
	    clone.clauses = (ArrayList<BooleanClause>) this.clauses.clone();
	    return clone;
	  }

	  /** Prints a user-readable version of this query. */
	  @Override
	  public String toString(String field) {
	    StringBuilder buffer = new StringBuilder();
	    boolean needParens= getBoost() != 1.0 || getMinimumNumberShouldMatch() > 0;
	    if (needParens) {
	      buffer.append("(");
	    }

	    for (int i = 0 ; i < clauses.size(); i++) {
	      BooleanClause c = clauses.get(i);
	      if (c.isProhibited()) {
	        buffer.append("-");
	      } else if (c.isRequired()) {
	        buffer.append("+");
	      }

	      Query subQuery = c.getQuery();
	      if (subQuery != null) {
	        if (subQuery instanceof PQ) {  // wrap sub-bools in parens
	          buffer.append("(");
	          buffer.append(subQuery.toString(field));
	          buffer.append(")");
	        } else {
	          buffer.append(subQuery.toString(field));
	        }
	      } else {
	        buffer.append("null");
	      }

	      if (i != clauses.size()-1) {
	        buffer.append(" ");
	      }
	    }

	    if (needParens) {
	      buffer.append(")");
	    }

	    if (getMinimumNumberShouldMatch()>0) {
	      buffer.append('~');
	      buffer.append(getMinimumNumberShouldMatch());
	    }

	    if (getBoost() != 1.0f) {
	      buffer.append(ToStringUtils.boost(getBoost()));
	    }

	    return buffer.toString();
	  }

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((clauses == null) ? 0 : clauses.hashCode());
		result = prime * result + (disableCoord ? 1231 : 1237);
		result = prime * result + minNrShouldMatch;
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
		PQ other = (PQ) obj;
		if (clauses == null) {
			if (other.clauses != null)
				return false;
		} else if (!clauses.equals(other.clauses))
			return false;
		if (disableCoord != other.disableCoord)
			return false;
		if (minNrShouldMatch != other.minNrShouldMatch)
			return false;
		return true;
	}
	
	
	
	
	
	
	
	
	
	
	protected static class PQScorer extends BulkScorer {
		  
		  private static final class BooleanScorerCollector extends Collector {
		    private BucketTable bucketTable;
		    private int mask;
		    private Scorer scorer;
		    
		    public BooleanScorerCollector(int mask, BucketTable bucketTable) {
		      this.mask = mask;
		      this.bucketTable = bucketTable;
		    }
		    
		    @Override
		    public void collect(final int doc) throws IOException {
		      final BucketTable table = bucketTable;
		      final int i = doc & BucketTable.MASK;
		      final Bucket bucket = table.buckets[i];
		      
		      if (bucket.doc != doc) {                    // invalid bucket
		        bucket.doc = doc;                         // set doc
		        bucket.score = scorer.score();            // initialize score
		        bucket.bits = mask;                       // initialize mask
		        bucket.coord = 1;                         // initialize coord

		        bucket.next = table.first;                // push onto valid list
		        table.first = bucket;
		      } else {                                    // valid bucket
		        bucket.score *= scorer.score();           // increment score
		        bucket.bits |= mask;                      // add bits in mask
		        bucket.coord++;                           // increment coord
		      }
		    }
		    
		    @Override
		    public void setNextReader(AtomicReaderContext context) {
		      // not needed by this implementation
		    }
		    
		    @Override
		    public void setScorer(Scorer scorer) {
		      this.scorer = scorer;
		    }
		    
		    @Override
		    public boolean acceptsDocsOutOfOrder() {
		      return true;
		    }

		  }
		  
		  static final class Bucket {
		    int doc = -1;            // tells if bucket is valid
		    double score;             // incremental score
		    // TODO: break out bool anyProhibited, int
		    // numRequiredMatched; then we can remove 32 limit on
		    // required clauses
		    int bits;                // used for bool constraints
		    int coord;               // count of terms in score
		    Bucket next;             // next valid bucket
		  }
		  
		  /** A simple hash table of document scores within a range. */
		  static final class BucketTable {
		    public static final int SIZE = 1 << 11;
		    public static final int MASK = SIZE - 1;

		    final Bucket[] buckets = new Bucket[SIZE];
		    Bucket first = null;                          // head of valid list
		  
		    public BucketTable() {
		      // Pre-fill to save the lazy init when collecting
		      // each sub:
		      for(int idx=0;idx<SIZE;idx++) {
		        buckets[idx] = new Bucket();
		      }
		    }

		    public Collector newCollector(int mask) {
		      return new BooleanScorerCollector(mask, this);
		    }

		    public int size() { return SIZE; }
		  }

		  static final class SubScorer {
		    public BulkScorer scorer;
		    // TODO: re-enable this if BQ ever sends us required clauses
		    //public boolean required = false;
		    public boolean prohibited;
		    public Collector collector;
		    public SubScorer next;
		    public boolean more;

		    public SubScorer(BulkScorer scorer, boolean required, boolean prohibited,
		        Collector collector, SubScorer next) {
		      if (required) {
		        throw new IllegalArgumentException("this scorer cannot handle required=true");
		      }
		      this.scorer = scorer;
		      this.more = true;
		      // TODO: re-enable this if BQ ever sends us required clauses
		      //this.required = required;
		      this.prohibited = prohibited;
		      this.collector = collector;
		      this.next = next;
		    }
		  }
		  
		  private SubScorer scorers = null;
		  private BucketTable bucketTable = new BucketTable();
		  private final float[] coordFactors;
		  // TODO: re-enable this if BQ ever sends us required clauses
		  //private int requiredMask = 0;
		  private final int minNrShouldMatch;
		  private int end;
		  private Bucket current;
		  // Any time a prohibited clause matches we set bit 0:
		  private static final int PROHIBITED_MASK = 1;

		  private final Weight weight;

		  PQScorer(BooleanWeight weight, boolean disableCoord, int minNrShouldMatch,
		      List<BulkScorer> optionalScorers, List<BulkScorer> prohibitedScorers, int maxCoord) throws IOException {
		    this.minNrShouldMatch = minNrShouldMatch;
		    this.weight = weight;

		    for (BulkScorer scorer : optionalScorers) {
		      scorers = new SubScorer(scorer, false, false, bucketTable.newCollector(0), scorers);
		    }
		    
		    for (BulkScorer scorer : prohibitedScorers) {
		      scorers = new SubScorer(scorer, false, true, bucketTable.newCollector(PROHIBITED_MASK), scorers);
		    }

		    coordFactors = new float[optionalScorers.size() + 1];
		    for (int i = 0; i < coordFactors.length; i++) {
		      coordFactors[i] = disableCoord ? 1.0f : weight.coord(i, maxCoord); 
		    }
		  }

		  @Override
		  public boolean score(Collector collector, int max) throws IOException {

		    boolean more;
		    Bucket tmp;
		    FakeScorer fs = new FakeScorer();

		    // The internal loop will set the score and doc before calling collect.
		    collector.setScorer(fs);
		    do {
		      bucketTable.first = null;
		      
		      while (current != null) {         // more queued 

		        // check prohibited & required
		        if ((current.bits & PROHIBITED_MASK) == 0) {

		          // TODO: re-enable this if BQ ever sends us required
		          // clauses
		          //&& (current.bits & requiredMask) == requiredMask) {
		          
		          // NOTE: Lucene always passes max =
		          // Integer.MAX_VALUE today, because we never embed
		          // a BooleanScorer inside another (even though
		          // that should work)... but in theory an outside
		          // app could pass a different max so we must check
		          // it:
		          if (current.doc >= max) {
		            tmp = current;
		            current = current.next;
		            tmp.next = bucketTable.first;
		            bucketTable.first = tmp;
		            continue;
		          }
		          
		          if (current.coord >= minNrShouldMatch) {
		            fs.score = (float) (current.score * coordFactors[current.coord]);
		            fs.doc = current.doc;
		            fs.freq = current.coord;
		            collector.collect(current.doc);
		          }
		        }
		        
		        current = current.next;         // pop the queue
		      }
		      
		      if (bucketTable.first != null){
		        current = bucketTable.first;
		        bucketTable.first = current.next;
		        return true;
		      }

		      // refill the queue
		      more = false;
		      end += BucketTable.SIZE;
		      for (SubScorer sub = scorers; sub != null; sub = sub.next) {
		        if (sub.more) {
		          sub.more = sub.scorer.score(sub.collector, end);
		          more |= sub.more;
		        }
		      }
		      current = bucketTable.first;
		      
		    } while (current != null || more);

		    return false;
		  }

		  @Override
		  public String toString() {
		    StringBuilder buffer = new StringBuilder();
		    buffer.append("boolean(");
		    for (SubScorer sub = scorers; sub != null; sub = sub.next) {
		      buffer.append(sub.scorer.toString());
		      buffer.append(" ");
		    }
		    buffer.append(")");
		    return buffer.toString();
		  }
		}
	
	
	
	
	
	
//	protected List<Query> queries;
//	
//	public PQ() {
//		this.queries = new ArrayList<Query>();
//	}
//	
//	public PQ(Query query) {
//		this();
//		
//		if(query instanceof TermQuery) {
//			queries.add(query);
//		} else {
//			System.out.println("NOT IMPLEMENTED!!!!!!!!!!!!!!");
//		}
//	}
//	
//	
//	
//	@Override
//	public Weight createWeight(IndexSearcher searcher) throws IOException {
//		return super.createWeight(searcher);
//	}
//	
//	
//	
//	protected class PQWeight extends Weight {
//		protected IndexSearcher searcher;
//		protected List<Weight> weights;
//		
//		public PQWeight(IndexSearcher searcher) {
//			this.searcher = searcher;
//			this.weights = new ArrayList<Weight>();
//			
//			try
//			{
//				for(Query q : PQ.this.queries) {
//					weights.add(q.createWeight(searcher));
//				}
//			}
//			catch(Exception ex) {
//				ex.printStackTrace();
//			}
//		}
//		
//		
//		@Override
//		public Explanation explain(AtomicReaderContext context, int doc) throws IOException {
//			return new Explanation(1, "NOT DONE");
//		}
//
//		@Override
//		public Query getQuery() {
//			return PQ.this;
//		}
//
//		@Override
//		public float getValueForNormalization() throws IOException {
//			float result = 0;
//			
//			for(Weight w : this.weights) {
//				result += w.getValueForNormalization();
//			}
//			
//			return result;
//		}
//
//		@Override
//		public void normalize(float norm, float topLevelBoost) {
//			for(Weight w : this.weights) {
//				w.normalize(norm, topLevelBoost);
//			}
//		}
//
//		@Override
//		public Scorer scorer(AtomicReaderContext context, Bits acceptDocs) throws IOException {
//			List<Scorer> scorers = new ArrayList<Scorer>();
//			
//			for(Weight w : this.weights) {
//				scorers.add(w.scorer(context, acceptDocs));
//			}
//			
//			
//			return new PQScorer(this, scorers);
//		}
//		
//	}
//	
//	
//	
//	protected static class PQScorer extends BulkScorer {
//		  
//		  private static final class BooleanScorerCollector extends Collector {
//		    private BucketTable bucketTable;
//		    private int mask;
//		    private Scorer scorer;
//		    
//		    public BooleanScorerCollector(int mask, BucketTable bucketTable) {
//		      this.mask = mask;
//		      this.bucketTable = bucketTable;
//		    }
//		    
//		    @Override
//		    public void collect(final int doc) throws IOException {
//		      final BucketTable table = bucketTable;
//		      final int i = doc & BucketTable.MASK;
//		      final Bucket bucket = table.buckets[i];
//		      
//		      if (bucket.doc != doc) {                    // invalid bucket
//		        bucket.doc = doc;                         // set doc
//		        bucket.score = scorer.score();            // initialize score
//		        bucket.bits = mask;                       // initialize mask
//		        bucket.coord = 1;                         // initialize coord
//
//		        bucket.next = table.first;                // push onto valid list
//		        table.first = bucket;
//		      } else {                                    // valid bucket
//		        bucket.score += scorer.score();           // increment score
//		        bucket.bits |= mask;                      // add bits in mask
//		        bucket.coord++;                           // increment coord
//		      }
//		    }
//		    
//		    @Override
//		    public void setNextReader(AtomicReaderContext context) {
//		      // not needed by this implementation
//		    }
//		    
//		    @Override
//		    public void setScorer(Scorer scorer) {
//		      this.scorer = scorer;
//		    }
//		    
//		    @Override
//		    public boolean acceptsDocsOutOfOrder() {
//		      return true;
//		    }
//
//		  }
//		  
//		  static final class Bucket {
//		    int doc = -1;            // tells if bucket is valid
//		    double score;             // incremental score
//		    // TODO: break out bool anyProhibited, int
//		    // numRequiredMatched; then we can remove 32 limit on
//		    // required clauses
//		    int bits;                // used for bool constraints
//		    int coord;               // count of terms in score
//		    Bucket next;             // next valid bucket
//		  }
//		  
//		  /** A simple hash table of document scores within a range. */
//		  static final class BucketTable {
//		    public static final int SIZE = 1 << 11;
//		    public static final int MASK = SIZE - 1;
//
//		    final Bucket[] buckets = new Bucket[SIZE];
//		    Bucket first = null;                          // head of valid list
//		  
//		    public BucketTable() {
//		      // Pre-fill to save the lazy init when collecting
//		      // each sub:
//		      for(int idx=0;idx<SIZE;idx++) {
//		        buckets[idx] = new Bucket();
//		      }
//		    }
//
//		    public Collector newCollector(int mask) {
//		      return new BooleanScorerCollector(mask, this);
//		    }
//
//		    public int size() { return SIZE; }
//		  }
//
//		  static final class SubScorer {
//		    public BulkScorer scorer;
//		    // TODO: re-enable this if BQ ever sends us required clauses
//		    //public boolean required = false;
//		    public boolean prohibited;
//		    public Collector collector;
//		    public SubScorer next;
//		    public boolean more;
//
//		    public SubScorer(BulkScorer scorer, boolean required, boolean prohibited,
//		        Collector collector, SubScorer next) {
//		      if (required) {
//		        throw new IllegalArgumentException("this scorer cannot handle required=true");
//		      }
//		      this.scorer = scorer;
//		      this.more = true;
//		      // TODO: re-enable this if BQ ever sends us required clauses
//		      //this.required = required;
//		      this.prohibited = prohibited;
//		      this.collector = collector;
//		      this.next = next;
//		    }
//		  }
//		  
//		  private SubScorer scorers = null;
//		  private BucketTable bucketTable = new BucketTable();
//		  private final float[] coordFactors;
//		  // TODO: re-enable this if BQ ever sends us required clauses
//		  //private int requiredMask = 0;
//		  private final int minNrShouldMatch;
//		  private int end;
//		  private Bucket current;
//		  // Any time a prohibited clause matches we set bit 0:
//		  private static final int PROHIBITED_MASK = 1;
//
//		  private final Weight weight;
//
//		  PQScorer(PQWeight weight, boolean disableCoord, int minNrShouldMatch,
//		      List<BulkScorer> optionalScorers, List<BulkScorer> prohibitedScorers, int maxCoord) throws IOException {
//		    this.minNrShouldMatch = minNrShouldMatch;
//		    this.weight = weight;
//
//		    for (BulkScorer scorer : optionalScorers) {
//		      scorers = new SubScorer(scorer, false, false, bucketTable.newCollector(0), scorers);
//		    }
//		    
//		    for (BulkScorer scorer : prohibitedScorers) {
//		      scorers = new SubScorer(scorer, false, true, bucketTable.newCollector(PROHIBITED_MASK), scorers);
//		    }
//
//		    coordFactors = new float[optionalScorers.size() + 1];
//		    for (int i = 0; i < coordFactors.length; i++) {
//		      coordFactors[i] = disableCoord ? 1.0f : weight.coord(i, maxCoord); 
//		    }
//		  }
//
//		  @Override
//		  public boolean score(Collector collector, int max) throws IOException {
//
//		    boolean more;
//		    Bucket tmp;
//		    FakeScorer fs = new FakeScorer();
//
//		    // The internal loop will set the score and doc before calling collect.
//		    collector.setScorer(fs);
//		    do {
//		      bucketTable.first = null;
//		      
//		      while (current != null) {         // more queued 
//
//		        // check prohibited & required
//		        if ((current.bits & PROHIBITED_MASK) == 0) {
//
//		          // TODO: re-enable this if BQ ever sends us required
//		          // clauses
//		          //&& (current.bits & requiredMask) == requiredMask) {
//		          
//		          // NOTE: Lucene always passes max =
//		          // Integer.MAX_VALUE today, because we never embed
//		          // a BooleanScorer inside another (even though
//		          // that should work)... but in theory an outside
//		          // app could pass a different max so we must check
//		          // it:
//		          if (current.doc >= max) {
//		            tmp = current;
//		            current = current.next;
//		            tmp.next = bucketTable.first;
//		            bucketTable.first = tmp;
//		            continue;
//		          }
//		          
//		          if (current.coord >= minNrShouldMatch) {
//		            fs.score = (float) (current.score * coordFactors[current.coord]);
//		            fs.doc = current.doc;
//		            fs.freq = current.coord;
//		            collector.collect(current.doc);
//		          }
//		        }
//		        
//		        current = current.next;         // pop the queue
//		      }
//		      
//		      if (bucketTable.first != null){
//		        current = bucketTable.first;
//		        bucketTable.first = current.next;
//		        return true;
//		      }
//
//		      // refill the queue
//		      more = false;
//		      end += BucketTable.SIZE;
//		      for (SubScorer sub = scorers; sub != null; sub = sub.next) {
//		        if (sub.more) {
//		          sub.more = sub.scorer.score(sub.collector, end);
//		          more |= sub.more;
//		        }
//		      }
//		      current = bucketTable.first;
//		      
//		    } while (current != null || more);
//
//		    return false;
//		  }
//
//		  @Override
//		  public String toString() {
//		    StringBuilder buffer = new StringBuilder();
//		    buffer.append("boolean(");
//		    for (SubScorer sub = scorers; sub != null; sub = sub.next) {
//		      buffer.append(sub.scorer.toString());
//		      buffer.append(" ");
//		    }
//		    buffer.append(")");
//		    return buffer.toString();
//		  }
//		}
//	
//	
////	protected class PQScorer extends Scorer {
////		private List<Scorer> scorers;
////		
////		protected PQScorer(Weight weight, List<Scorer> scorers) {
////			super(weight);
////			
////			this.scorers = scorers;
////		}
////
////		@Override
////		public float score() throws IOException {
////			float score = 0;
////			for(Scorer s : scorers) {
////				if(score == 0) {
////					score = s.score();
////				} else {
////					score *= s.score();
////				}
////			}
////			
////			return score;
////		}
////
////		@Override
////		public int freq() throws IOException {
////			// TODO Auto-generated method stub
////			return 0;
////		}
////
////		@Override
////		public int advance(int target) throws IOException {
////			for(Scorer s : scorers) {
////				s.advance(target);
////			}
////			
////			return docID();
////		}
////
////		@Override
////		public long cost() {
////			// TODO Auto-generated method stub
////			return 0;
////		}
////
////		@Override
////		public int docID() {
////			// TODO Auto-generated method stub
////			return 0;
////		}
////
////		@Override
////		public int nextDoc() throws IOException {
////			int currDocId = docID();
////		    
////			for(Scorer s : scorers) {
////				if(currDocId == s.docID()) {
////					s.nextDoc();
////				}
////			}
////		    return docID();
////		}
////		
////	}
//	
//	
//	
//
//	/* (non-Javadoc)
//	 * @see java.lang.Object#hashCode()
//	 */
//	@Override
//	public int hashCode() {
//		final int prime = 31;
//		int result = super.hashCode();
//		result = prime * result + ((queries == null) ? 0 : queries.hashCode());
//		return result;
//	}
//
//	/* (non-Javadoc)
//	 * @see java.lang.Object#equals(java.lang.Object)
//	 */
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj)
//			return true;
//		if (!super.equals(obj))
//			return false;
//		if (getClass() != obj.getClass())
//			return false;
//		PQ other = (PQ) obj;
//		if (queries == null) {
//			if (other.queries != null)
//				return false;
//		} else if (!queries.equals(other.queries))
//			return false;
//		return true;
//	}
//
//	@Override
//	public String toString(String field) {
//		return "PQ";
//	}

	  
	  
	  
}
