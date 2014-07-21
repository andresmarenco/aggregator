package aggregator.sampler.output;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;

import aggregator.beans.SampledDocument;
import aggregator.beans.Vertical;
import aggregator.sampler.AbstractSampler;
import aggregator.util.FileWriterHelper;

public class FileSamplerOutput extends AbstractSamplerOutput {
	
	public static final String SEPARATE_FILES_KEY = "aggregator.sampler.fileSampler.separateFiles";
	public static final String FILE_PATTERN_KEY = "aggregator.sampler.fileSampler.filePattern";
	public static final String OUTPUT_PATH_KEY = "aggregator.sampler.fileSampler.outputPath";
	
	private String outputPath;
	private String filePattern;
	private boolean separateFiles;
	private long fileCounter;
	
	private FileWriterHelper fileWriter;
		
	/**
	 * Default Constructor
	 * @param vertical Vertical Object
	 */
	public FileSamplerOutput(Vertical vertical) {
		super(vertical);
		this.fileCounter = 0;
		
		init();
	}
	
	
	/**
	 * Initializes the local variables with the specified properties
	 */
	private void init() {
		this.outputPath = System.getProperty(OUTPUT_PATH_KEY);
		this.filePattern = System.getProperty(FILE_PATTERN_KEY);
		this.separateFiles = Boolean.parseBoolean(System.getProperty(SEPARATE_FILES_KEY, "true"));
	}


	@Override
	public void outputDocument(SampledDocument<?> document) {
		if(separateFiles) {
			openFile(document.getId());
		}
		
		fileWriter.writeLine(document.serialize());
		
		if(separateFiles) {
			closeFile();
		}
	}


	@Override
	public void openResources() {
		if(!separateFiles) {
			openFile(null);
		}
	}


	@Override
	public void closeResources() {
		if(!separateFiles) {
			closeFile();
		}
	}
	
	
	
	
	/**
	 * Opens the next file according to the file pattern and the counter
	 * @param documentId Document ID
	 */
	private void openFile(String documentId) {
		String filename = filePattern.replace("{vertical}", vertical.getId())
				.replace("{docid}", (documentId != null ? documentId : ""))
				.replace("{count}", StringUtils.leftPad(String.valueOf(fileCounter++), 6, '0'))
				.replace("{timestamp}", LocalDateTime.now().toString(System.getProperty(AbstractSampler.TIMESTAMP_PATTERN_KEY, "yyyyMMddhhmmss")));
		
		fileWriter = new FileWriterHelper(outputPath + filename);
		fileWriter.open(false);
	}
	
	
	
	
	/**
	 * Closes the current open file
	 */
	private void closeFile() {
		fileWriter.close();
	}

}
