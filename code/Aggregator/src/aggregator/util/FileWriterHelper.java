package aggregator.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FileWriterHelper {
	
	private Log log = LogFactory.getLog(FileWriterHelper.class);
	
	private final String filename;
	private FileWriter fileWriter;
	private BufferedWriter bufferedWriter;
	
	
	/**
	 * Default Constructor
	 * @param filename Name of the output file
	 */
	public FileWriterHelper(String filename) {
		this.filename = filename;
	}
	
	
	
	
	/**
	 * Opens the current file
	 * @param append boolean if true, then data will be written to the end of the file rather than the beginning
	 */
	public void open(boolean append) {
		try
		{
			if(filename.contains(File.separator)) {
				String path = filename.substring(0, filename.lastIndexOf(File.separator));
				new File(path).mkdirs();
			}
			
			fileWriter = new FileWriter(filename, append);
			bufferedWriter = new BufferedWriter(fileWriter);
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}
	
	
	
	
	/**
	 * Closes the current file
	 */
	public void close() {
		try
		{
			this.bufferedWriter.close();
			this.fileWriter.close();
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}
	
	
	
	
	/**
	 * Writes the line to the current file
	 * @param content Content to write
	 */
	public void write(String content) {
		this.write(content, false);
	}
	
	
	
	
	/**
	 * Writes the line to the current file and adds a new line
	 * @param content Content to write
	 */
	public void writeLine(String content) {
		this.write(content, true);
	}
	
	
	
	
	
	/**
	 * Writes the line to the current file
	 * @param content Content to write
	 * @param newline boolean if true, adds also a new line to the file
	 */
	private void write(String content, boolean newline) {
		try
		{
			this.bufferedWriter.write(content);
			if(newline) {
				this.bufferedWriter.newLine();
			}
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}
}