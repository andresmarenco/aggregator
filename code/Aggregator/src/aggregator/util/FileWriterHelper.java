package aggregator.util;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.FileWriter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FileWriterHelper implements Closeable {
	
	protected Log log = LogFactory.getLog(FileWriterHelper.class);
	
	private final Path filePath;
	private FileWriter fileWriter;
	private BufferedWriter bufferedWriter;
	
	
	/**
	 * Default Constructor
	 * @param filePath Path of the output file
	 * @param more Extra paths for the file
	 */
	public FileWriterHelper(String filePath, String... more) {
		this.filePath = FileSystems.getDefault().getPath(filePath, more);
	}
	
	
	/**
	 * Default Constructor
	 * @param filePath Path of the output file
	 */
	public FileWriterHelper(Path filePath) {
		this.filePath = filePath;
	}
	
	
	
	
	/**
	 * Opens the current file
	 * @param append boolean if true, then data will be written to the end of the file rather than the beginning
	 */
	public void open(boolean append) {
		try
		{
			Files.createDirectories(filePath.getParent());
			fileWriter = new FileWriter(filePath.toFile(), append);
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