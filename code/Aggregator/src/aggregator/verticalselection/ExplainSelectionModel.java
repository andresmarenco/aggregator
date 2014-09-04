package aggregator.verticalselection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.similarities.Similarity;

import aggregator.sampler.indexer.AbstractSamplerIndexer;

public class ExplainSelectionModel extends AbstractSelectionModel {

	private AbstractSelectionModel baseModel;
	
	public ExplainSelectionModel(AbstractSelectionModel baseModel) {
		super(baseModel.collection);
		this.baseModel = baseModel;
	}

	@Override
	protected Query prepareQuery(String queryString) {
		return baseModel.prepareQuery(queryString);
	}
	
	@Override
	protected CSIResultData searchCSI(String queryString) {
		try
		{
			IndexSearcher isearcher = new IndexSearcher(ireader);
			isearcher.setSimilarity(getSimilarityModel());
			
			Query query = this.prepareQuery(queryString);
			ScoreDoc[] hits = isearcher.search(query, null, 200).scoreDocs;
			
			for(int i = 0; i < hits.length; i++) {
				Document hitDoc = isearcher.doc(hits[i].doc);
				String docId = hitDoc.get(AbstractSamplerIndexer.INDEX_DOC_NAME_FIELD);
				String verticalName = hitDoc.get(AbstractSamplerIndexer.INDEX_VERTICAL_FIELD);
				float score = hits[i].score;
		
				System.out.println("\n\n\nDOCID: " + docId + ", VERTICAL: " + verticalName + ", SCORE: " + score);
				System.out.println(isearcher.explain(query, hits[i].doc));
			}
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
		
		return new CSIResultData();
	}

	@Override
	protected List<Entry<String, Double>> executeModel(String queryString) {
		this.searchCSI(queryString);
		return new ArrayList<Entry<String,Double>>();
	}

	@Override
	public String getModelCodeName() {
		return "EXPLAIN";
	}
	
	@Override
	public Similarity getSimilarityModel() {
		return baseModel.getSimilarityModel();
	}

}
