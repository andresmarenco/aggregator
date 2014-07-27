package aggregator.beans;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PlainTextSampledDocument implements SampledDocument<String> {

	private static final long serialVersionUID = 201407150743L;
	
	private String id;
	private String document;
	private Log log = LogFactory.getLog(PlainTextSampledDocument.class);
	
	/**
	 * Default Constructor
	 * @param id Document ID
	 */
	public PlainTextSampledDocument(String id) {
		this.id = id;
	}

	@Override
	public String deserialize(String document) {
		this.setDocument(document);
		return this.document;
	}

	@Override
	public String serialize() {
		return this.document;
	}

	@Override
	public void setDocument(String document) {
		this.document = document;
	}

	@Override
	public String getDocument() {
		return this.document;
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public void readFromFile(Path path) {
		try
		{
			this.document = new String(Files.readAllBytes(path));
		}
		catch (IOException ex) {
			log.error(ex.getMessage(), ex);
		}
	}

}
