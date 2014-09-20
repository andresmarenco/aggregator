package aggregator.verticalselection;

import java.nio.file.Path;
import java.text.MessageFormat;

import aggregator.util.FileWriterHelper;

public class SelectionRunsFile extends FileWriterHelper {

	/**
	 * Default Constructor
	 * @param filePath File Path
	 */
	public SelectionRunsFile(Path filePath) {
		super(filePath);
		
		this.open(false);
	}
	
	
	public void writeRunData(String topicId, String verticalCollectionId, int rank, double score, String runtag) {
		this.writeLine(MessageFormat.format("{0}\tQ0\t{1}\t{2}\t{3}\t{4}", topicId, verticalCollectionId, String.valueOf(rank), String.valueOf(score), runtag));
	}
}
