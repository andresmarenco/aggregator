package aggregator.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.LocalDateTime;

public class CommonUtils {

	private static final String HOME_PATH_KEY = "aggregator.homePath";
	private static final String VERTICALS_PATH_KEY = "aggregator.verticalsPath";
	private static final String LOG_PATH_KEY = "aggregator.logPath";
	private static final String INDEX_PATH_KEY = "aggregator.indexPath";
	private static final String SAMPLE_PATH_KEY = "aggregator.samplePath";
	private static final String TIMESTAMP_PATTERN_KEY = "aggregator.timeStampPattern";
	private static Log log = LogFactory.getLog(CommonUtils.class);

	/**
	 * Gets the current timestamp based on the configured pattern
	 * @return Current timestamp
	 */
	public static final String getTimestampString() {
		return LocalDateTime.now().toString(System.getProperty(CommonUtils.TIMESTAMP_PATTERN_KEY, "yyyyMMddhhmmss"));
	}
	
	
	
	
	/**
	 * Gets the configured home path (and creates it if needed)
	 * @return Home path
	 * @throws IOException
	 */
	public static final Path getHomePath() throws IOException {
		return Files.createDirectories(FileSystems.getDefault().getPath(System.getProperty(HOME_PATH_KEY)));
	}
	
	
	
	
	/**
	 * Gets the configured vertical path based on the home path (and creates it if needed)
	 * @return Vertical path
	 * @throws IOException
	 */
	public static final Path getVerticalsPath() throws IOException {
		return Files.createDirectories(getHomePath().resolve(System.getProperty(VERTICALS_PATH_KEY)));
	}
	
	
	
	
	/**
	 * Gets the configured log path based on the home path (and creates it if needed)
	 * @return Log path
	 * @throws IOException
	 */
	public static final Path getLogPath() throws IOException {
		return Files.createDirectories(getHomePath().resolve(System.getProperty(LOG_PATH_KEY)));
	}
	
	
	
	
	/**
	 * Gets the configured index path based on the home path (and creates it if needed)
	 * @return Index path
	 * @throws IOException
	 */
	public static final Path getIndexPath() throws IOException {
		return Files.createDirectories(getHomePath().resolve(System.getProperty(INDEX_PATH_KEY)));
	}
	
	
	
	
	/**
	 * Gets the configured sample path based on the home path (and creates it if needed)
	 * @return Sample path
	 * @throws IOException
	 */
	public static final Path getSamplePath() throws IOException {
		return Files.createDirectories(getHomePath().resolve(System.getProperty(SAMPLE_PATH_KEY)));
	}
	
	
	
	
	/**
	 * Loads the default properties file
	 */
	public static final void loadDefaultProperties() {
		InputStream propertiesInputStream = null;
		
		try
		{
			propertiesInputStream = CommonUtils.class.getResourceAsStream("/aggregator.properties");
			System.getProperties().load(propertiesInputStream);
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
		finally {
			closeInputStream(propertiesInputStream);
		}
	}
	
	
	
	
	/**
	 * Closes the given input stream
	 * @param stream Input stream to close
	 */
	public static final void closeInputStream(InputStream stream) {
		try
		{
			if(stream != null) {
				stream.close();
			}
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}
	
	
	

	/**
	 * Reads an script file
	 * @param scriptFile Script file
	 * @return List with script lines 
	 */
	public static List<String> readScriptFile(Path scriptFile, String... extraPaths) {
		List<String> script = new ArrayList<String>();
		for(String path : extraPaths) {
			scriptFile = scriptFile.resolve(path);
		}
		
		if((Files.exists(scriptFile)) && (Files.isRegularFile(scriptFile))) {
			try
			{
				script = Files.readAllLines(scriptFile);
			}
			catch(Exception ex) {
				log.error(ex.getMessage(), ex);
			}
		}
		
		return script;
	}
	
	
	
	
	/**
	 * Instances a sub class as an instance of the given super class
	 * @param superclass Super class type
	 * @param className Sub class' name
	 * @return Instances of the super class
	 */
	public static final <T> T instanceSubClass(Class<T> superclass, String className) {
		return instanceSubClass(superclass, className, null, null);
	}
	
	
	
	
	/**
	 * Instances a sub class as an instance of the given super class
	 * @param superclass Super class type
	 * @param className Sub class' name
	 * @param constructorParamTypes Parameters types for the instantiation
	 * @param initParams Parameters for the instantiation
	 * @return Instance of the super class
	 */
	public static final <T> T instanceSubClass(Class<T> superclass, String className, Class<?>[] constructorParamTypes, Object[] initParams) {
		T result = null;
		String packageName = superclass.getPackage().getName();
		try
		{
			Class<?> subclass = Class.forName(MessageFormat.format("{0}.{1}", packageName, className));
			if(superclass.isAssignableFrom(subclass)) {
				result = superclass.cast(subclass.getConstructor().newInstance());
			} else {
				log.error(MessageFormat.format("{0} is not an instance of {1}", className, superclass.getName()));
			}
		}
		catch(Exception ex) {
			log.error(MessageFormat.format("{0} not found", className), ex);
		}
		return result;
	}

	
	
	
	
	/**
	 * Gets the sub class type of the given super class
	 * @param superclass Super class type
	 * @param className Sub class' name
	 * @return Sub class type
	 */
	public static final <T> Class<? extends T> getSubClassType(Class<T> superclass, String className) {
		Class<?> result = null;
		if(StringUtils.isNotBlank(className)) {
			String packageName = superclass.getPackage().getName();
			try
			{
				result = Class.forName(MessageFormat.format("{0}.{1}", packageName, className));
				if(!superclass.isAssignableFrom(result)) {
					log.error(MessageFormat.format("{0} is not a sub class of {1}", className, superclass.getName()));
				}
			}
			catch(Exception ex) {
				log.error(MessageFormat.format("{0} not found", className), ex);
			}
		}
		return result.asSubclass(superclass);
	}
}
