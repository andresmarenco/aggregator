package aggregator.util;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;

import javax.net.ssl.SSLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.cookie.BrowserCompatSpec;
import org.apache.http.protocol.HttpContext;

public class HTTPUtils {
	
	private static final Log log = LogFactory.getLog(HTTPUtils.class);

	/**
	 * Creates a default keep alive strategy
	 * @param duration Duration of the keep alive strategy
	 * @return Keep alive strategy object
	 */
	public static final ConnectionKeepAliveStrategy createDefaultConnectionKeepAliveStrategy(final int duration) {
		ConnectionKeepAliveStrategy manejadorConexionViva = new DefaultConnectionKeepAliveStrategy() {
			
			@Override
			public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
				long keepAlive = super.getKeepAliveDuration(response, context);
				if (keepAlive == -1) {
					// Keeps the connections alive during the given time (if it is not
					// explicitly established by the server)
					keepAlive = duration;
				}
				return keepAlive;
			}
		};
		
		return manejadorConexionViva;
	}
	
	
	
	
	/**
	 * Creates a default retry handler
	 * @param maxRetries Maximum allowed retries
	 * @return Retry handler object
	 */
	public static final HttpRequestRetryHandler createDefaultHttpRequestRetryHandler(final int maxRetries) {
		
		HttpRequestRetryHandler retryHandler = new HttpRequestRetryHandler() {
			
			public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
		        if (executionCount >= maxRetries) {
		            // Do not retry if over max retry count
		            return false;
		        }
		        if (exception instanceof InterruptedIOException) {
		            // Timeout
		            return false;
		        }
		        if (exception instanceof UnknownHostException) {
		            // Unknown host
		            return false;
		        }
		        if (exception instanceof ConnectTimeoutException) {
		            // Connection refused
		            return false;
		        }
		        if (exception instanceof SSLException) {
		            // SSL handshake exception
		            return false;
		        }
		        HttpClientContext clientContext = HttpClientContext.adapt(context);
		        HttpRequest request = clientContext.getRequest();
		        boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
		        if (idempotent) {
		            // Retry if the request is considered idempotent
		            return true;
		        }
		        return false;
			}
		};
		
		return retryHandler;
	}
	
	
	
	
	/**
	 * Creates a default cookie specification provider
	 * @return Cookie specification provider object
	 */
	public static final CookieSpecProvider createDefaultCookieFactory() {
		CookieSpecProvider easySpecProvider = new CookieSpecProvider() {

		    public CookieSpec create(HttpContext context) {

		        return new BrowserCompatSpec() {
		        	@Override
		            public void validate(Cookie cookie, CookieOrigin origin) throws MalformedCookieException {
		                // Oh, I am easy
		            }
		        };
		    }

		};
		
		return easySpecProvider;
	}
	
	
	
	
	public static final RedirectStrategy createDefaultRedirectStrategy() {
		RedirectStrategy redirectStrategy = new DefaultRedirectStrategy() {
			 public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context)  {
		            boolean isRedirect=false;
		            try {
		                isRedirect = super.isRedirected(request, response, context);
		            } catch (ProtocolException ex) {
		            	log.error(ex.getMessage(), ex);
		            }
		            if (!isRedirect) {
		                int responseCode = response.getStatusLine().getStatusCode();
		                if (responseCode == 301 || responseCode == 302) {
		                    return true;
		                }
		            }
		            return isRedirect;
		        }
		};
		
		return redirectStrategy;
	}
}
