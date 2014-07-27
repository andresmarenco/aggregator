package aggregator.sampler.output;

import java.nio.file.Path;

import org.apache.commons.lang3.StringUtils;

import aggregator.beans.SampledDocument;
import aggregator.beans.Vertical;
import aggregator.util.CommonUtils;
import aggregator.util.FileWriterHelper;

public class FileSamplerOutput extends AbstractSamplerOutput {
	
	public static final String SEPARATE_FILES_KEY = "aggregator.sampler.fileSampler.separateFiles";
	public static final String FILE_PATTERN_KEY = "aggregator.sampler.fileSampler.filePattern";
	
	private Path outputPath;
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
		this.filePattern = System.getProperty(FILE_PATTERN_KEY);
		this.separateFiles = Boolean.parseBoolean(System.getProperty(SEPARATE_FILES_KEY, "true"));
		
		try
		{
			this.outputPath = CommonUtils.getSamplePath();
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
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
	public void open() {
		if(!separateFiles) {
			openFile(null);
		}
	}


	@Override
	public void close() {
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
				.replace("{timestamp}", CommonUtils.getTimestampString());
		
		Path filePath = this.outputPath.resolve(filename);
		fileWriter = new FileWriterHelper(filePath);
		fileWriter.open(false);
	}
	
	
	
	
	/**
	 * Closes the current open file
	 */
	private void closeFile() {
		fileWriter.close();
	}

}
