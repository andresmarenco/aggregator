package aggregator.sampler.analysis;

import java.io.Closeable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import aggregator.beans.TFDF;

public interface IntermediateResults extends Closeable {
	public void addDocumentTerms(String docId, List<String> foundTerms);
	public void addFoundTerm(String term);
	public void addNewTerm(String term);
	public void addUniqueTerm(String term, long termFrequency);
	public void increaseUniqueTermFrequencies(String term, long termFrequency, long documentFrequency);
	public void clearFoundTerms();	
	public void clearNewTerms();
	public void dumpDocumentTerms(String analysisFileName);
	public void dumpTFDF(String analysisFileName);
	public boolean hasUniqueTerm(String term);
	public List<String> getNewTerms();
	public List<String> getFoundTerms();
	public Set<Map.Entry<String, TFDF>> getUniqueTerms();
	public int getUniqueTermsCount();
}
