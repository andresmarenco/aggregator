package aggregator.verticalselection;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.CustomScoreProvider;
import org.apache.lucene.queries.CustomScoreQuery;
import org.apache.lucene.search.BulkScorer;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.ComplexExplanation;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Weight;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.util.Bits;

import aggregator.beans.VerticalCollection;
import aggregator.beans.VerticalCollection.VerticalCollectionData;
import aggregator.sampler.indexer.AbstractSamplerIndexer;
import aggregator.util.CommonUtils;

public class UiSDocumentCentricScoreQuery extends Query {
//	protected VerticalCollection collection;
//	
//	public UiSDocumentCentricScoreQuery(Query subQuery, VerticalCollection collection) {
//		super(subQuery);
//	}
//	
////	@Override
////	public Query rewrite(IndexReader reader) throws IOException {
////		System.out.println("REWRITE!!!!!!!!!!!!!!!!!");
////		return this;
////	}
//	
//
//	@Override
//	public Weight createWeight(IndexSearcher searcher) throws IOException {
//		return new UiSDCWeight(super.createWeight(searcher));
//	}
//	
//	
//	protected class UiSDCWeight extends Weight {
//		protected Weight bWeight;
//		
//		public UiSDCWeight(Weight w) {
//			this.bWeight = w;
//		}
//		
//
//		@Override
//		public Explanation explain(AtomicReaderContext context, int doc) throws IOException {
//			return this.bWeight.explain(context, doc);
//		}
//
//		@Override
//		public Query getQuery() {
//			return UiSDocumentCentricScoreQuery.this;
//		}
//
//		@Override
//		public float getValueForNormalization() throws IOException {
//			return this.bWeight.getValueForNormalization();
//		}
//
//		@Override
//		public void normalize(float norm, float topLevelBoost) {
//			this.bWeight.normalize(norm, topLevelBoost);
//		}
//
//		@Override
//		public Scorer scorer(AtomicReaderContext context, Bits acceptDocs) throws IOException {
//			return this.bWeight.scorer(context, acceptDocs);
//		}
//
//
//		/* (non-Javadoc)
//		 * @see java.lang.Object#hashCode()
//		 */
//		@Override
//		public int hashCode() {
//			final int prime = 31;
//			int result = 1;
//			result = prime * result + getOuterType().hashCode();
//			result = prime * result
//					+ ((bWeight == null) ? 0 : bWeight.hashCode());
//			return result;
//		}
//
//
//		/* (non-Javadoc)
//		 * @see java.lang.Object#equals(java.lang.Object)
//		 */
//		@Override
//		public boolean equals(Object obj) {
//			if (this == obj)
//				return true;
//			if (obj == null)
//				return false;
//			if (getClass() != obj.getClass())
//				return false;
//			UiSDCWeight other = (UiSDCWeight) obj;
//			if (!getOuterType().equals(other.getOuterType()))
//				return false;
//			if (bWeight == null) {
//				if (other.bWeight != null)
//					return false;
//			} else if (!bWeight.equals(other.bWeight))
//				return false;
//			return true;
//		}
//
//
//		private UiSDocumentCentricScoreQuery getOuterType() {
//			return UiSDocumentCentricScoreQuery.this;
//		}
//		
//		
//	}
//	
//
//	
////	@Override
////	protected CustomScoreProvider getCustomScoreProvider(AtomicReaderContext context) throws IOException {
////		return new CustomScoreProvider(context) {
////			
////			@Override
////			public float customScore(int doc, float subQueryScore, float valSrcScore) throws IOException {
////				
////				// TODO Auto-generated method stub
////				return 1;
////			}
////			
////		};
////	}
//	
//
//	/* (non-Javadoc)
//	 * @see java.lang.Object#hashCode()
//	 */
//	@Override
//	public int hashCode() {
//		final int prime = 31;
//		int result = super.hashCode();
//		result = prime * result
//				+ ((collection == null) ? 0 : collection.hashCode());
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
//		UiSDocumentCentricScoreQuery other = (UiSDocumentCentricScoreQuery) obj;
//		if (collection == null) {
//			if (other.collection != null)
//				return false;
//		} else if (!collection.equals(other.collection))
//			return false;
//		return true;
//	}
	

	protected Query subQuery;
	protected VerticalCollection collection;

	public UiSDocumentCentricScoreQuery(Query subQuery, VerticalCollection collection) {
		System.out.println(subQuery.getClass() + " SUB");
		this.collection = collection;
		this.subQuery = subQuery;
	}

	@Override
	public String toString(String field) {
		return "UiSDCS";
	}
	
	@Override
	public Weight createWeight(IndexSearcher searcher) throws IOException {
		System.out.println("WEIGHT!!!!!!!!!!!!!!!!!!");
		return new UiSDocumentCentricScoreQueryWeight(subQuery.createWeight(searcher));
//		return subQuery.createWeight(searcher);
	}
	
	@Override
	public void extractTerms(Set<Term> terms) {
		subQuery.extractTerms(terms);
	}
	
	@Override
	public Query rewrite(IndexReader reader) throws IOException {
		System.out.println("rewrite!!!!!!!!!!!!!!!!!!");
		return this;
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
				+ ((subQuery == null) ? 0 : subQuery.hashCode());
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
		UiSDocumentCentricScoreQuery other = (UiSDocumentCentricScoreQuery) obj;
		if (collection == null) {
			if (other.collection != null)
				return false;
		} else if (!collection.equals(other.collection))
			return false;
		if (subQuery == null) {
			if (other.subQuery != null)
				return false;
		} else if (!subQuery.equals(other.subQuery))
			return false;
		return true;
	}
	
	
	
	protected class UiSDocumentCentricScoreQueryWeight extends Weight {
		protected Weight baseWeight;
		
		public UiSDocumentCentricScoreQueryWeight(Weight baseWeight) {
			this.baseWeight = baseWeight;
		}

		@Override
		public Explanation explain(AtomicReaderContext context, int doc) throws IOException {
			ComplexExplanation result = new ComplexExplanation();
			Explanation sum = this.baseWeight.explain(context, doc);
			
			result.addDetail(sum);
			result.setMatch(true);
			result.setDescription("product of:");
			
			
			Document document = context.reader().document(doc);
			VerticalCollectionData verticalData = collection.getVerticalData(document.get(AbstractSamplerIndexer.INDEX_VERTICAL_FIELD));
			float pdc = getPdc(verticalData);
			
			result.addDetail(new Explanation(pdc, "P(d|c)"));
			result.setValue(pdc * sum.getValue());
			return result;
			
//			return this.baseWeight.explain(context, doc);
		}

		@Override
		public Query getQuery() {
			return UiSDocumentCentricScoreQuery.this.subQuery;
		}

		@Override
		public float getValueForNormalization() throws IOException {
			return this.baseWeight.getValueForNormalization();
		}

		@Override
		public void normalize(float norm, float topLevelBoost) {
			this.baseWeight.normalize(norm, topLevelBoost);
		}

		@Override
		public Scorer scorer(AtomicReaderContext context, Bits acceptDocs) throws IOException {
			System.out.println("SCORER!!!!!!!!!!!!!!!!!!");
			return this.baseWeight.scorer(context, acceptDocs);
		}
		
		@Override
		public BulkScorer bulkScorer(AtomicReaderContext context, boolean scoreDocsInOrder, Bits acceptDocs) throws IOException {
			System.out.println("BULK SCORER!!!!!!!!!!!!!!!!!!");
			return new UiSDocumentCentricScoreQueryBulkScorer(this.baseWeight.bulkScorer(context, scoreDocsInOrder, acceptDocs));
		}
		
		@Override
		public boolean scoresDocsOutOfOrder() {
			return this.baseWeight.scoresDocsOutOfOrder();
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result
					+ ((baseWeight == null) ? 0 : baseWeight.hashCode());
			return result;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			UiSDocumentCentricScoreQueryWeight other = (UiSDocumentCentricScoreQueryWeight) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (baseWeight == null) {
				if (other.baseWeight != null)
					return false;
			} else if (!baseWeight.equals(other.baseWeight))
				return false;
			return true;
		}

		private UiSDocumentCentricScoreQuery getOuterType() {
			return UiSDocumentCentricScoreQuery.this;
		}
		
		
	}
	
	
	protected class UiSDocumentCentricScoreQueryBulkScorer extends BulkScorer {
		protected BulkScorer base;
		
		public UiSDocumentCentricScoreQueryBulkScorer(BulkScorer base) {
			this.base = base;
		}
		
		@Override
		public boolean score(Collector collector, int max) throws IOException {
			System.out.println("BULK S 1!!");
			return this.base.score(collector, max);
		}
		
		@Override
		public void score(Collector collector) throws IOException {
			System.out.println("BULK S 2!!");
			this.base.score(collector);
			
		}
	}
	
	
	
	
	protected class UiSDocumentCentricScoreQueryScorer extends Scorer {

		protected UiSDocumentCentricScoreQueryScorer(Weight weight) {
			super(weight);
		}

		@Override
		public float score() throws IOException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int freq() throws IOException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int advance(int target) throws IOException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public long cost() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int docID() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int nextDoc() throws IOException {
			// TODO Auto-generated method stub
			return 0;
		}
		
	}

	//	@Override
//	protected CustomScoreProvider getCustomScoreProvider(AtomicReaderContext context) throws IOException {
//		return new CustomScoreProvider(context) {
//			
////			@Override
////			public Explanation customExplain(int doc, Explanation subQueryExpl, Explanation[] valSrcExpl) throws IOException {
////				Document document = context.reader().document(doc);
////				VerticalCollectionData verticalData = collection.getVerticalData(document.get(AbstractSamplerIndexer.INDEX_VERTICAL_FIELD));
////				float pdc = UiSDocumentCentricScoreQuery.this.getPdc(verticalData);
////				
////				Explanation result = super.customExplain(doc, subQueryExpl, valSrcExpl);
////				result.addDetail(new Explanation(pdc, "P(d|c)"));
////				
////				result.setValue(result.getValue() * pdc);
////				
////				return result;
////			}
////			
////			
////			@Override
////			public float customScore(int doc, float subQueryScore, float[] valSrcScore) throws IOException {
////				Document document = context.reader().document(doc);
////				VerticalCollectionData verticalData = collection.getVerticalData(document.get(AbstractSamplerIndexer.INDEX_VERTICAL_FIELD));
////				
//////				return super.customScore(doc, subQueryScore, valSrcScore) * UiSDocumentCentricScoreQuery.this.getPdc(verticalData);
////				return subQueryScore;
////			}
//		};
//	}
//	
//	
//	
//	
	/**
	 * Gets the P(d|c)
	 * @param vertical Vertical to compute the value
	 * @return P(d|c) value
	 */
	protected float getPdc(VerticalCollectionData vertical) {
		BigDecimal sampleSize = BigDecimal.valueOf(vertical.getSampleSize());
		return BigDecimal.ONE.divide(sampleSize, CommonUtils.DEFAULT_DIVISION_SCALE, CommonUtils.DEFAULT_ROUNDING_MODE).floatValue();
	}
}
