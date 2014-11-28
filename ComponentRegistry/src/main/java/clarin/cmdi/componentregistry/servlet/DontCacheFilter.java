/**
 * 
 */
package clarin.cmdi.componentregistry.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

/**
 * Adds no-cache HTTP headers to each response
 * @author george.georgovassilis@mpi.nl
 *
 */
public class DontCacheFilter implements Filter{

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {
		HttpServletResponse httpResponse = (HttpServletResponse)response;
		httpResponse.addHeader("Cache-Control", "no-cache, no-store, must-revalidate");
		httpResponse.addHeader("Pragma", "no-cache");
		httpResponse.addHeader("Expires", "0");
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
	}

}
