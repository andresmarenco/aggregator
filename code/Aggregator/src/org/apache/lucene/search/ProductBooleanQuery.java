package org.apache.lucene.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BulkScorer;
import org.apache.lucene.search.ComplexExplanation;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Weight;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.util.Bits;

public class ProductBooleanQuery extends BooleanQuery {
	
	/**
	 * Default Constructor
	 * @param query Base boolean query
	 */
	public ProductBooleanQuery(BooleanQuery query) {
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
	
	
	@Override
	public Weight createWeight(IndexSearcher searcher) throws IOException {
		return new ProductBooleanQueryWeight(searcher, this.isCoordDisabled());
	}
	
	
	/** Returns true iff <code>o</code> is equal to this. */
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof ProductBooleanQuery)) {
			return false;
		}
		ProductBooleanQuery other = (ProductBooleanQuery)o;
		
		return this.getBoost() == other.getBoost()
	        && this.clauses().equals(other.clauses())
	        && this.getMinimumNumberShouldMatch() == other.getMinimumNumberShouldMatch()
	        && this.isCoordDisabled() == other.isCoordDisabled();
	}

	
	/** Returns a hash code value for this object.*/
	@Override
	public int hashCode() {
		return Float.floatToIntBits(getBoost()) ^ clauses().hashCode()
				+ getMinimumNumberShouldMatch() + (isCoordDisabled() ? 17:0);
	}
	
	
	
	/**
	 * Extension of BooleanWeight. Changes BulkScore and Explain to use
	 * multiplication instead of sum
	 * @author andres
	 *
	 */
	protected class ProductBooleanQueryWeight extends BooleanWeight {

		/**
		 * Default Constructor
		 * @param searcher
		 * @param disableCoord
		 * @throws IOException
		 */
		public ProductBooleanQueryWeight(IndexSearcher searcher, boolean disableCoord) throws IOException {
			super(searcher, disableCoord);
		}
		
		
		@Override
		public Scorer scorer(AtomicReaderContext context, Bits acceptDocs) throws IOException {
//			System.out.println("PBQ Scorer!!!");
			return super.scorer(context, acceptDocs);
		}
		
		@Override
		public BulkScorer bulkScorer(AtomicReaderContext context, boolean scoreDocsInOrder, Bits acceptDocs) throws IOException {
//			System.out.println("BULK SCORER!!!! " + minNrShouldMatch);
			
			// Same as BooleanWeight, but changes the return class
			if (scoreDocsInOrder || minNrShouldMatch > 1) {
				return super.bulkScorer(context, scoreDocsInOrder, acceptDocs);
			}
			
			
		    List<BulkScorer> prohibited = new ArrayList<>();
		    List<BulkScorer> optional = new ArrayList<>();
		    Iterator<BooleanClause> cIter = ProductBooleanQuery.this.iterator();
		    for (Weight w  : weights) {
		    	BooleanClause c =  cIter.next();
		    	BulkScorer subScorer = w.bulkScorer(context, false, acceptDocs);
		    	if (subScorer == null) {
		    		if (c.isRequired()) {
		    			return null;
	    			}
	    		} else if (c.isRequired()) {
	    			return super.bulkScorer(context, scoreDocsInOrder, acceptDocs);
    			} else if (c.isProhibited()) {
    				prohibited.add(subScorer);
				} else {
					optional.add(subScorer);
				}
	    	}

		    
//		    System.out.println("CREATING BOOLEAN SCORER");
		    return new ProductBooleanScorer(this, ProductBooleanQuery.this.isCoordDisabled(), minNrShouldMatch, optional, prohibited, maxCoord);    
		}
		
		
		@Override
		public Explanation explain(AtomicReaderContext context, int doc) throws IOException {
			
			// Same as BooleanWeight, but multiplies instead of sum
//			System.out.println("EXPLAIN!!!!!!!!!!!!!");
			
			final int minShouldMatch = ProductBooleanQuery.this.getMinimumNumberShouldMatch();
			ComplexExplanation prodExpl = new ComplexExplanation();
			prodExpl.setDescription("product of:");
			int coord = 0;
			float prod = 0.0f;
			boolean fail = false;
			int shouldMatchCount = 0;
			Iterator<BooleanClause> cIter = ProductBooleanQuery.this.iterator();
			for (Iterator<Weight> wIter = weights.iterator(); wIter.hasNext();) {
				Weight w = wIter.next();
				BooleanClause c = cIter.next();
				if (w.scorer(context, context.reader().getLiveDocs()) == null) {
					if (c.isRequired()) {
						fail = true;
						Explanation r = new Explanation(0.0f, "no match on required clause (" + c.getQuery().toString() + ")");
						prodExpl.addDetail(r);
					}
					continue;
				}
				
				Explanation e = w.explain(context, doc);
				if (e.isMatch()) {
					if (!c.isProhibited()) {
						prodExpl.addDetail(e);
						if(prod == 0) {
							prod = e.getValue();
						} else {
							prod *= e.getValue();
						}
					
						coord++;
					} else {
						Explanation r = new Explanation(0.0f, "match on prohibited clause (" + c.getQuery().toString() + ")");
						r.addDetail(e);
						prodExpl.addDetail(r);
						fail = true;
					}
					
					if (c.getOccur() == Occur.SHOULD) {
						shouldMatchCount++;
					}
				} else if (c.isRequired()) {
					
					Explanation r = new Explanation(0.0f, "no match on required clause (" + c.getQuery().toString() + ")");
					r.addDetail(e);
					prodExpl.addDetail(r);
					fail = true;
				}
			}
			if (fail) {
				prodExpl.setMatch(Boolean.FALSE);
				prodExpl.setValue(0.0f);
				prodExpl.setDescription("Failure to meet condition(s) of required/prohibited clause(s)");
				return prodExpl;
			} else if (shouldMatchCount < minShouldMatch) {
				prodExpl.setMatch(Boolean.FALSE);
				prodExpl.setValue(0.0f);
				prodExpl.setDescription("Failure to match minimum number of optional clauses: " + minShouldMatch);
				return prodExpl;
			}
			
			prodExpl.setMatch(0 < coord ? Boolean.TRUE : Boolean.FALSE);
			prodExpl.setValue(prod);
			
			final float coordFactor = ProductBooleanQuery.this.isCoordDisabled() ? 1.0f : coord(coord, maxCoord);
			if (coordFactor == 1.0f) {
				return prodExpl;                             // eliminate wrapper
			} else {
				ComplexExplanation result = new ComplexExplanation(prodExpl.isMatch(), prod*coordFactor, "product of:");
				result.addDetail(prodExpl);
				result.addDetail(new Explanation(coordFactor, "coord("+coord+"/"+maxCoord+")"));
				return result;
			}
		}
		
	}
	
	
	
	
	
	
	
	
	
	/**
	 * Re-write of Boolean Scorer. Changed sum for multiplication
	 * Could be better just to extend it, but Lucene people though that it could be cool
	 * to make that class final, because: who needs inheritance?
	 * @author andres
	 *
	 */
	private static class ProductBooleanScorer extends BulkScorer {
		
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
				
				if (bucket.doc != doc) {					// invalid bucket
					bucket.doc = doc;						// set doc
					bucket.score = scorer.score();			// initialize score
					bucket.bits = mask;						// initialize mask
					bucket.coord = 1;						// initialize coord
					
					bucket.next = table.first;				// push onto valid list
					table.first = bucket;
				} else {									// valid bucket
					bucket.score *= scorer.score();			// increment score
					bucket.bits |= mask;					// add bits in mask
					bucket.coord++;							// increment coord
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
			int doc = -1;			// tells if bucket is valid
			double score;			// incremental score
			int bits;				// used for bool constraints
			int coord;				// count of terms in score
			Bucket next;			// next valid bucket
		}
		
		
		/** A simple hash table of document scores within a range. */
		static final class BucketTable {
			public static final int SIZE = 1 << 11;
			public static final int MASK = SIZE - 1;
			
			final Bucket[] buckets = new Bucket[SIZE];
			Bucket first = null;					// head of valid list
		  
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
		    
//		    public int size() { return SIZE; }
	    }

		
		
		static final class SubScorer {
			public BulkScorer scorer;
			public Collector collector;
			public SubScorer next;
			public boolean more;
			
			
			public SubScorer(BulkScorer scorer, boolean required, boolean prohibited, Collector collector, SubScorer next) {
				if (required) {
					throw new IllegalArgumentException("this scorer cannot handle required=true");
				}
				
				this.scorer = scorer;
				this.more = true;
				this.collector = collector;
				this.next = next;
			}
		}
		
		
		private SubScorer scorers = null;
		private BucketTable bucketTable = new BucketTable();
		private final float[] coordFactors;
		private final int minNrShouldMatch;
		private int end;
		private Bucket current;
		
		// Any time a prohibited clause matches we set bit 0:
		private static final int PROHIBITED_MASK = 1;

		ProductBooleanScorer(BooleanWeight weight, boolean disableCoord, int minNrShouldMatch, List<BulkScorer> optionalScorers, List<BulkScorer> prohibitedScorers, int maxCoord) throws IOException {
			this.minNrShouldMatch = minNrShouldMatch;
			
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
				
				
				if (bucketTable.first != null) {
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
}
