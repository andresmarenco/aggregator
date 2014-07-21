package aggregator.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CommonUtils {

	private static Log log = LogFactory.getLog(CommonUtils.class);
	
	
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
	 * @param filename File name
	 * @return List with script lines 
	 */
	public static List<String> readScriptFile(String filename) {
		List<String> script = new ArrayList<String>();
		if(StringUtils.isNotBlank(filename)) {
			try(BufferedReader br = new BufferedReader(new FileReader(filename))) {
				String line = br.readLine();
				while(line != null) {
					script.add(line);
					line = br.readLine();
				}
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
		return result.asSubclass(superclass);
	}
}
