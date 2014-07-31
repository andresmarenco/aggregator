package aggregator.sampler.output;

import java.nio.file.Path;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import aggregator.beans.SampledDocument;
import aggregator.util.CommonUtils;
import aggregator.util.FileWriterHelper;

public class SampledDocumentOutput {
	
	private Log log = LogFactory.getLog(SampledDocumentOutput.class);

	/**
	 * Default Constructor
	 */
	public SampledDocumentOutput() {
	}
	
	
	
	/**
	 * Writes a file with the given document
	 * @param document Document to write
	 * @param outputPath Output path
	 */
	public void outputDocument(SampledDocument<?> document, String outputPath) {
		try(FileWriterHelper output = new FileWriterHelper(CommonUtils.getSamplePath().resolve(outputPath))) {
			output.open(false);
			output.writeLine(document.serialize());
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}
	
	
	
	/**
	 * Writes a file with the given document
	 * @param document Document to write
	 * @param outputPath Output path
	 */
	public void outputDocument(SampledDocument<?> document, Path outputPath) {
		try(FileWriterHelper output = new FileWriterHelper(CommonUtils.getSamplePath().resolve(outputPath))) {
			output.open(false);
			output.writeLine(document.serialize());
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}
	
}
