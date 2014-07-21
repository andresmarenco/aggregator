package aggregator.verticalwrapper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.w3c.dom.Document;

import aggregator.beans.QueryResult;
import aggregator.beans.Vertical;
import aggregator.beans.VerticalConfig;
import aggregator.beans.XMLSampledDocument;
import aggregator.verticalwrapper.query.HTTPQueryScript;
import aggregator.verticalwrapper.query.QueryScript;
import aggregator.verticalwrapper.srr.HTTPSRRScript;
import aggregator.verticalwrapper.srr.SRRScript;

public class HTTPVerticalWrapper extends AbstractVerticalWrapper {
	
	private CleanerProperties cleanerProperties;
	private HtmlCleaner htmlCleaner;
	private DomSerializer domSerializer;
	
	/**
	 * Default Constructor
	 * @param vertical Vertical Object
	 * @param config Vertical Configuration
	 */
	public HTTPVerticalWrapper(Vertical vertical, VerticalConfig config) {
		super(vertical, config);
		
		this.cleanerProperties = new CleanerProperties();
		this.htmlCleaner = new HtmlCleaner(cleanerProperties);
		this.domSerializer = new DomSerializer(cleanerProperties);
	}
	

	@Override
	public HttpClientContext authenticate() {
		CookieStore cookieStore = new BasicCookieStore();
		HttpClientContext httpContext = HttpClientContext.create();
		httpContext.setCookieStore(cookieStore);
		
		return httpContext;
	}

	@Override
	public List<QueryResult> executeQuery(String query) {
		List<QueryResult> result = new ArrayList<QueryResult>();
		HttpClientContext httpContext = this.authenticate();
		
		QueryScript<Document> queryScript = new HTTPQueryScript(httpContext, config.getHttpConfig());
		SRRScript<Document> srrScript = new HTTPSRRScript(vertical);
		
		Document queryResult = queryScript.execute(query, config.getQueryScript());
		result = srrScript.execute(queryResult, config.getSRRScript());
		
		return result;
	}

	@Override
	public XMLSampledDocument downloadDocument(QueryResult document) {
		XMLSampledDocument result = null;
		HttpClientContext httpContext = this.authenticate();
		
		try(CloseableHttpClient httpClient = HttpClients.createDefault())
		{
			String uri = (StringUtils.isNotBlank(config.getIdPattern()) ?
					StringUtils.replace(config.getIdPattern(), "{%ID}", document.getId()) : document.getId());
			
			HttpGet httpGet = new HttpGet(uri);
			try(CloseableHttpResponse httpResponse = httpClient.execute(httpGet, httpContext))
			{
				HttpEntity httpEntity = httpResponse.getEntity();
				if(httpEntity != null) {
					result = new XMLSampledDocument(document.getId());

					try(InputStream in = httpEntity.getContent()) {
						TagNode rootNode = htmlCleaner.clean(in);
						result.setDocument(domSerializer.createDOM(rootNode));
					}
					
				} else {
					log.error("No response for " + document.getId());
				}
			}
			
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
		
		return result;
	}

}
