package aggregator.verticalselection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;

import aggregator.sampler.indexer.AbstractSamplerIndexer;
import aggregator.util.CommonUtils;
import aggregator.util.analysis.AggregatorAnalyzer;

public class ExplainSelectionModel extends AbstractSelectionModel {

	private AbstractSelectionModel baseModel;
	
	public ExplainSelectionModel(AbstractSelectionModel baseModel) {
		super(baseModel.collection);
		this.baseModel = baseModel;
		
		try
		{
			this.index = new NIOFSDirectory(CommonUtils.getIndexPath().resolve(collection.getId()).resolve(AbstractSamplerIndexer.getIndexName()).toFile());
			this.ireader = DirectoryReader.open(index);
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}
	
	
	public ExplainSelectionModel(AbstractSelectionModel baseModel, Directory index) {
		super(baseModel.collection);
		this.baseModel = baseModel;
		
		try
		{
			if(this.index != null) { this.index.close(); }
			if(this.ireader != null) { this.ireader.close(); }
			
			this.index = index;
			this.ireader = DirectoryReader.open(index);
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
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
			ScoreDoc[] hits = isearcher.search(query, null, Integer.MAX_VALUE).scoreDocs;
			Map<String, MutableInt> resultsCount = new HashMap<String, MutableInt>();
			
			for(int i = 0; i < hits.length; i++) {
				Document hitDoc = isearcher.doc(hits[i].doc);
				String docId = hitDoc.get(AbstractSamplerIndexer.INDEX_DOC_NAME_FIELD);
				String verticalName = hitDoc.get(AbstractSamplerIndexer.INDEX_VERTICAL_FIELD);
				
				MutableInt count = resultsCount.get(verticalName);
				if(count == null) {
					count = new MutableInt(0);
					resultsCount.put(verticalName, count);
				}
				
				if(count.intValue() < rankCutOff) {
					float score = hits[i].score;
					
					if(verticalName.equals("cern")) {
						System.out.println("\n\n\nDOCID: " + docId + ", VERTICAL: " + verticalName + ", SCORE: " + score);
						System.out.println(isearcher.explain(query, hits[i].doc));
					}
					
					count.increment();
				}
				
				
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
