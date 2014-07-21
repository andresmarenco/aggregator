package aggregator.beans;

import java.io.Serializable;
import java.util.Properties;

public class HTTPVerticalConfig implements Serializable {

	private static final long serialVersionUID = 201407172115L;
	private static final String HTTP_COOKIE_POLICY_KEY = "http.cookiePolicy";
	private static final String HTTP_USE_SSL_KEY = "http.useSSL";
	private static final String HTTP_PORT_KEY = "http.port";

	private String httpCookiePolicy;
	private boolean httpUseSSL;
	private int httpPort;
	
	/**
	 * Default Constructor
	 */
	public HTTPVerticalConfig() {
	}
	
	
	
	/**
	 * Creates a new instance of {@code HTTPVerticalConfig} based on the properties file
	 * @param properties Properties file
	 * @return {@code HTTPVerticalConfig} instance
	 */
	public static final HTTPVerticalConfig newInstance(Properties properties) {
		HTTPVerticalConfig config = new HTTPVerticalConfig();
		config.setHttpUseSSL(Boolean.parseBoolean(properties.getProperty(HTTP_USE_SSL_KEY, "false")));
		config.setHttpCookiePolicy(properties.getProperty(HTTP_COOKIE_POLICY_KEY));
		config.setHttpPort(Integer.parseInt(properties.getProperty(HTTP_PORT_KEY, "80")));
		
		return config;
	}
	
	

	/**
	 * @return the httpCookiePolicy
	 */
	public String getHttpCookiePolicy() {
		return httpCookiePolicy;
	}

	/**
	 * @param httpCookiePolicy the httpCookiePolicy to set
	 */
	public void setHttpCookiePolicy(String httpCookiePolicy) {
		this.httpCookiePolicy = httpCookiePolicy;
	}

	/**
	 * @return the httpUseSSL
	 */
	public boolean isHttpUseSSL() {
		return httpUseSSL;
	}

	/**
	 * @param httpUseSSL the httpUseSSL to set
	 */
	public void setHttpUseSSL(boolean httpUseSSL) {
		this.httpUseSSL = httpUseSSL;
	}

	/**
	 * @return the httpPort
	 */
	public int getHttpPort() {
		return httpPort;
	}

	/**
	 * @param httpPort the httpPort to set
	 */
	public void setHttpPort(int httpPort) {
		this.httpPort = httpPort;
	}
}
