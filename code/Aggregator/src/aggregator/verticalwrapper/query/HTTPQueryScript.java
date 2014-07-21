package aggregator.verticalwrapper.query;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.cookie.BestMatchSpecFactory;
import org.apache.http.impl.cookie.BrowserCompatSpecFactory;
import org.apache.http.message.BasicNameValuePair;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.w3c.dom.Document;

import aggregator.beans.HTTPVerticalConfig;
import aggregator.util.HTTPUtils;

public class HTTPQueryScript implements QueryScript<Document> {
	
	private CleanerProperties cleanerProperties;
	private HtmlCleaner htmlCleaner;
	private DomSerializer domSerializer;
	private HttpClientContext httpContext;
	private HttpRequestBase httpMethod;
	private RequestConfig requestConfig;
	private CloseableHttpClient httpClient;
	private URIBuilder uriBuilder;
	private HTTPVerticalConfig verticalConfig;
	private Document responseDocument;
	private List<NameValuePair> params;
	private Log log = LogFactory.getLog(HTTPQueryScript.class);
	
	/**
	 * Default Constructor
	 * @param httpContext HTTP Context
	 */
	public HTTPQueryScript(HttpClientContext httpContext, HTTPVerticalConfig verticalConfig) {
		this.httpContext = httpContext;
		this.verticalConfig = verticalConfig;
	}
	
	
	
	
	/**
	 * Initializes the local variables
	 */
	private void init() {
		Registry<CookieSpecProvider> cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
		        .register(CookieSpecs.BEST_MATCH,
		            new BestMatchSpecFactory())
		        .register(CookieSpecs.BROWSER_COMPATIBILITY,
		            new BrowserCompatSpecFactory())
		        .register("allow_all", HTTPUtils.createDefaultCookieFactory())
		        .build();
		
		RequestConfig.Builder rcBuilder =  RequestConfig.custom();
		rcBuilder.setRedirectsEnabled(true);
		rcBuilder.setCookieSpec(StringUtils.isBlank(verticalConfig.getHttpCookiePolicy()) ?
				CookieSpecs.BEST_MATCH : verticalConfig.getHttpCookiePolicy());
		
		this.requestConfig = rcBuilder.build();
		
		this.httpClient = HttpClients.custom()
				.setDefaultCookieSpecRegistry(cookieSpecRegistry)
				.setDefaultCookieStore(httpContext.getCookieStore())
				.setRedirectStrategy(new LaxRedirectStrategy())
				.build();
		
		this.httpMethod = null;
		this.uriBuilder = new URIBuilder();
		this.uriBuilder.setPort(verticalConfig.getHttpPort());
		this.uriBuilder.setScheme(verticalConfig.isHttpUseSSL() ? "https" : "http");
		
		this.params = new ArrayList<NameValuePair>();
		
		this.cleanerProperties = new CleanerProperties();
		this.htmlCleaner = new HtmlCleaner(cleanerProperties);
		this.domSerializer = new DomSerializer(cleanerProperties);
	}
	
	
	
	
	@Override
	public Document execute(String query, List<String> script) {
		this.init();
		
		try
		{
			for(String command : script) {
				this.parseAndExecute(query, command);
			}
			
			this.cmdExecute();
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
		finally {
			try
			{
				httpClient.close();
			}
			catch (IOException ex) {
				log.error(ex.getMessage(), ex);
			}
		}
		
		return responseDocument;
	}
	
	
	
	
	/**
	 * Parses and executes the given command line
	 * @param query Query string`
	 * @param commandLine Command line
	 */
	private void parseAndExecute(String query, String commandLine) {
		if(StringUtils.isNotBlank(commandLine)) {
			commandLine = commandLine.trim();
			
			if(!StringUtils.startsWith(commandLine, "#")) {
				int spaceIndex = commandLine.indexOf(' ');
				String cmd = commandLine.substring(0, spaceIndex).trim();
				String params = commandLine.substring(spaceIndex).trim();
				
				cmd = StringUtils.removeEnd(cmd, ":");
				
				if(StringUtils.equalsIgnoreCase(cmd, "POST")) {
					spaceIndex = params.indexOf(' ');
					String path = params.substring(0, spaceIndex).trim();
					String protocol = params.substring(spaceIndex).trim();
					
					this.cmdPost(path, protocol);
				} else if(StringUtils.equalsIgnoreCase(cmd, "GET")) {
					spaceIndex = params.indexOf(' ');
					String path = params.substring(0, spaceIndex).trim();
					String protocol = params.substring(spaceIndex).trim();
					
					this.cmdGet(path, protocol);
				} else if(StringUtils.equalsIgnoreCase(cmd, "DATA")) {
					spaceIndex = params.indexOf(' ');
					String name = params.substring(0, spaceIndex).trim();
					String value = params.substring(spaceIndex).trim();
					
					this.cmdData(name, value, query);
				} else if(StringUtils.equalsIgnoreCase(cmd, "HOST")) {
					this.cmdHost(params);
				} else if(StringUtils.equalsIgnoreCase(cmd, "EXECUTE")) {
					this.cmdExecute();
				} else {
					log.warn(MessageFormat.format("Unknown command {0} in {1}", cmd, commandLine));
				}
			}
		}
	}
	
	
	
	
	/**
	 * Executes the current query
	 */
	private void cmdExecute() {
		if(httpMethod != null) {
			try
			{
				if(httpMethod instanceof HttpPost) {
					UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, Consts.UTF_8);
					((HttpPost)httpMethod).setEntity(entity);
				} else if(httpMethod instanceof HttpGet) {
					uriBuilder.setParameters(params);
				}
				
				httpMethod.setURI(uriBuilder.build());
				httpMethod.setConfig(requestConfig);
				
				CloseableHttpResponse response = httpClient.execute(httpMethod, httpContext);
				try
				{
					HttpEntity responseEntity = response.getEntity();
					if(responseEntity != null) {
						try(InputStream in = responseEntity.getContent())
						{
							TagNode rootNode = htmlCleaner.clean(in);
							this.responseDocument = domSerializer.createDOM(rootNode);
						}
						
					} else {
						log.error("No response for " + httpMethod.getURI().toASCIIString());
					}
					
				}
				catch(Exception ex) {
					log.error(ex.getMessage(), ex);
				}
				finally {
					response.close();
				}
				
			} catch (Exception ex) {
				log.error(ex.getMessage(), ex);
			}
			finally {
				params.clear();
				uriBuilder.clearParameters();
				
				httpMethod.releaseConnection();
				httpMethod = null;
			}
		}
	}
	
	
	
	
	/**
	 * Adds the data in the parameters list
	 * @param name Name of the parameter
	 * @param value Value of the parameter
	 * @param query Query string
	 */
	private void cmdData(String name, String value, String query) {
		value = StringUtils.replace(value, "{%QUERY}", query);
		this.params.add(new BasicNameValuePair(name, value));
	}
	
	
	
	
	/**
	 * Initializes the HTTP Method as POST
	 * @param path HTTP Path
	 * @param protocol HTTP Protocol
	 */
	private void cmdPost(String path, String protocol) {
		initMethod(path, protocol, HttpPost.class);
	}
	
	
	
	
	/**
	 * Initializes the HTTP Method as GET
	 * @param path HTTP Path
	 * @param protocol HTTP Protocol
	 */
	private void cmdGet(String path, String protocol) {
		initMethod(path, protocol, HttpGet.class);
	}
	
	
	
	
	/**
	 * Initializes the HTTP Method
	 * @param path HTTP Path
	 * @param protocol HTTP Protocol
	 * @param methodClass Class of the HTTP Method
	 */
	private void initMethod(String path, String protocol, Class<? extends HttpRequestBase> methodClass) {
		if(httpMethod != null) {
			log.warn("Replacing current HTTP method");
		}
		
		try
		{
			httpMethod = methodClass.newInstance();
			httpMethod.setProtocolVersion(this.getProtocolVersion(protocol));
			
			uriBuilder.setPath(path);
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}
	
	
	
	
	/**
	 * Defines the URI Host
	 * @param host URI Host
	 */
	private void cmdHost(String host) {
		uriBuilder.setHost(host);
	}
	
	
	
	
	/**
	 * Finds the correct {@code ProtocolVersion} object
	 * @param protocol HTTP Protocol version
	 * @return {@code ProtocolVersion} object
	 */
	private ProtocolVersion getProtocolVersion(String protocol) {
		ProtocolVersion result = null;
		if(StringUtils.equalsIgnoreCase(protocol, "HTTP/0.9")) {
			result = HttpVersion.HTTP_0_9;
		} else if(StringUtils.equalsIgnoreCase(protocol, "HTTP/1.0")) {
			result = HttpVersion.HTTP_1_0;
		} else {
			result = HttpVersion.HTTP_1_1;
		}
		
		return result;
	}
}
