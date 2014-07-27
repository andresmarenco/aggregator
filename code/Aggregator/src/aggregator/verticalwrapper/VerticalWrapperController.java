package aggregator.verticalwrapper;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import aggregator.beans.Vertical;
import aggregator.beans.VerticalConfig;
import aggregator.util.CommonUtils;
import aggregator.util.LRUMap;

public class VerticalWrapperController {
	
	//public static final String CONFIG_PATH_KEY = "aggregator.verticalWrapper.configPath";
	public static final String CACHE_SIZE_KEY = "aggregator.verticalWrapper.cacheSize";
	
	/** Current instance of the class for the Singleton Pattern */
	private static VerticalWrapperController _ClassInstance;
	
	private Path configPath;
	private int cacheSize;
	private LRUMap<Vertical, VerticalConfig> verticalData;
	private Log log = LogFactory.getLog(VerticalWrapperController.class);
	
	/**
	 * Default Constructor
	 */
	private VerticalWrapperController() {
		init();
	}
	
	
	
	
	/**
     * Returns an instance of the class
     * @return Singleton instance of the class
     */
    public synchronized static VerticalWrapperController getInstance()
    {
        try
		{
		    if(_ClassInstance == null)
		    {
			    _ClassInstance = new VerticalWrapperController();    
		    }
		    return _ClassInstance;
		}
		catch(Exception ex)
		{
            ex.printStackTrace();
		    return null;
		}
    }
    
    
    

	/**
     * The Clone method is not supported due the Singleton Pattern
     * @return Always throws an exception
     * @throws java.lang.CloneNotSupportedException
     */
    @Override
	public Object clone() throws CloneNotSupportedException {
    	throw new CloneNotSupportedException();
    }
	
	
	
	
	/**
	 * Initializes the local variables with the specified properties
	 */
	private void init() {
		this.cacheSize = Math.max(1, Integer.parseInt(System.getProperty(CACHE_SIZE_KEY, "1")));
		this.verticalData = new LRUMap<Vertical, VerticalConfig>(cacheSize);
		
		try
		{
			this.configPath = CommonUtils.getVerticalsPath();
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}
	
	
	
	
	/**
	 * Creates a wrapper for the given vertical
	 * @param vertical Vertical
	 * @return Vertical wrapper instance
	 */
	public AbstractVerticalWrapper createVerticalWrapper(Vertical vertical) {
		AbstractVerticalWrapper result = null;
		VerticalConfig config = this.getVerticalConfig(vertical);
		if(config != null) {
			if(config.getWrapperType().equals(XMLVerticalWrapper.class)) {
				result = new XMLVerticalWrapper(vertical, config);
			} else if(config.getWrapperType().equals(HTTPVerticalWrapper.class)) {
				result = new HTTPVerticalWrapper(vertical, config);
			} else {
				throw new NoClassDefFoundError(MessageFormat.format("Undefined wrapper for {0} vertical", vertical.getName()));
			}
		}
		
		return result;
	}
	
	
	
	
	
	/**
	 * Gets the configuration for the vertical reading the corresponding file
	 * @param vertical Vertical
	 * @return Vertical configuration object
	 */
	public VerticalConfig getVerticalConfig(Vertical vertical) {
		VerticalConfig result = verticalData.get(vertical);
		if(result == null) {
			InputStream input = null;
			try
			{
				Path configFile = this.configPath.resolve(MessageFormat.format("{0}.conf", vertical.getId()));
				input = new FileInputStream(configFile.toFile());
				
				Properties properties = new Properties();
				properties.load(input);
				
				result = VerticalConfig.newInstance(properties);
				verticalData.put(vertical, result);
			}
			catch(Exception ex) {
				log.error(ex.getMessage(), ex);
			}
			finally {
				CommonUtils.closeInputStream(input);
			}
		}
		
		return result;
	}

}
