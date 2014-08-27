package aggregator.sampler.analysis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;

import aggregator.beans.TFDF;
import aggregator.sampler.output.DocumentTermsLogFile;
import aggregator.sampler.output.TFDFLogFile;

public class MemoryIntermediateResults implements IntermediateResults {
	
	private List<String> foundTerms;
	private List<String> newTerms;
	private HashMap<String, TFDF> uniqueTerms;
	private List<Map.Entry<String, List<String>>> documentTerms;
	
	/**
	 * Default Constructor
	 */
	public MemoryIntermediateResults() {
		this.documentTerms = new ArrayList<Map.Entry<String,List<String>>>();
		this.foundTerms = new ArrayList<String>();
		this.newTerms = new ArrayList<String>();
		this.uniqueTerms = new HashMap<String, TFDF>();
	}
	
	@Override
	public void clearFoundTerms() {
		this.foundTerms.clear();
	}
	
	@Override
	public void clearNewTerms() {
		this.newTerms.clear();
	}
	
	@Override
	public void addDocumentTerms(String docId, List<String> foundTerms) {
		documentTerms.add(new ImmutablePair<String, List<String>>(docId, new ArrayList<String>(foundTerms)));
	}

	@Override
	public void addFoundTerm(String term) {
		this.foundTerms.add(term);
	}

	@Override
	public void addNewTerm(String term) {
		this.newTerms.add(term);
	}

	@Override
	public void addUniqueTerm(String term, long termFrequency) {
		this.uniqueTerms.put(term, new TFDF(termFrequency, 1));
	}

	@Override
	public void increaseUniqueTermFrequencies(String term, long termFrequency, long documentFrequency) {
		TFDF tfdf = this.uniqueTerms.get(term);
		tfdf.incrementTermFrequency(termFrequency);
		tfdf.incrementDocumentFrequency(documentFrequency);
	}

	@Override
	public boolean hasUniqueTerm(String term) {
		return uniqueTerms.containsKey(term);
	}

	@Override
	public List<String> getNewTerms() {
		return newTerms;
	}

	@Override
	public List<String> getFoundTerms() {
		return foundTerms;
	}

	@Override
	public Set<Entry<String, TFDF>> getUniqueTerms() {
		return uniqueTerms.entrySet();
	}

	@Override
	public int getUniqueTermsCount() {
		return uniqueTerms.size();
	}
	
	@Override
	public void dumpDocumentTerms(String analysisFileName) {
		// Stores the document terms list
		try(DocumentTermsLogFile docTermsLog = DocumentTermsLogFile.newInstance(analysisFileName)) {
			docTermsLog.writeDocumentTerms(documentTerms);
		}
	}

	@Override
	public void dumpTFDF(String analysisFileName) {
		// Stores the term frequency/document frequency file
		try(TFDFLogFile tfdfLog = TFDFLogFile.newInstance(analysisFileName)) {
			tfdfLog.writeTFDFs(this.getUniqueTerms());
		}
	}
	
	@Override
	public void close() throws IOException {
		
	}

}
