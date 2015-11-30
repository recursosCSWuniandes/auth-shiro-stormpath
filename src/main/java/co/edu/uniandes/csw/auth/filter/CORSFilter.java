package co.edu.uniandes.csw.auth.filter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.apache.shiro.subject.support.DefaultSubjectContext;
import org.apache.shiro.web.filter.PathMatchingFilter;
import org.apache.shiro.web.util.WebUtils;

/**
 * Disables session creation and enables Cross Site Resource Sharing
 * @author kaosterra
 */
public class CORSFilter extends PathMatchingFilter {

    @Override
    protected boolean onPreHandle(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
        request.setAttribute(DefaultSubjectContext.SESSION_CREATION_ENABLED, Boolean.FALSE);
        WebUtils.toHttp(response).setHeader("Access-Control-Allow-Origin", "*");
        WebUtils.toHttp(response).setHeader("Access-Control-Allow-Methods", "*");
        WebUtils.toHttp(response).setHeader("Access-Control-Allow-Headers", "*");
        return true;
    }
}
