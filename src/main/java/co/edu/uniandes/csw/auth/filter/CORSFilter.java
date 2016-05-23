package co.edu.uniandes.csw.auth.filter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.apache.shiro.web.filter.PathMatchingFilter;
import org.apache.shiro.web.util.WebUtils;

/**
 * Enables Cross Site Resource Sharing
 *
 * @author af.esguerra10
 */
public class CORSFilter extends PathMatchingFilter {

    private static final String ORIGIN_VAR = "ALLOW_ORIGIN";

    @Override
    protected boolean onPreHandle(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
        String allowOrigin = System.getenv(ORIGIN_VAR);
        if (allowOrigin == null) {
            allowOrigin = "http://localhost:9000";
        }
        WebUtils.toHttp(response).setHeader("Access-Control-Allow-Origin", allowOrigin);
        WebUtils.toHttp(response).setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
        WebUtils.toHttp(response).setHeader("Access-Control-Allow-Headers", "accept, content-type, X-xsrf-token");
        WebUtils.toHttp(response).setHeader("Access-Control-Allow-Credentials", "true");
        return true;
    }
}
