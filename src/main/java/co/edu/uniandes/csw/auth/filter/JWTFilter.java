package co.edu.uniandes.csw.auth.filter;

import co.edu.uniandes.csw.auth.model.NewUserDTO;
import co.edu.uniandes.csw.auth.security.JWT;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;

/**
 * Authenticating filter that looks for a cookie with a JWT containing authenticating information.
 * This filter allows users to authenticate using a JWT Token stored in a Cookie.
 * It also allows the use of the permissive flag, in order to allow requests to pass through
 * even when user is not authenticated.
 * 
 * @author jd.patino10
 * @author af.esguerra10
 */
public class JWTFilter extends AuthenticatingFilter {

    @Override
    protected AuthenticationToken createToken(ServletRequest req, ServletResponse rsp) throws Exception {
        String token = getToken(req);
        try {
            NewUserDTO user = JWT.verifyToken(token);
            return createToken(user.getUserName(), user.getPassword(), req, rsp);
        } catch (Exception e) {
            throw new AuthenticationException(e);
        }
    }

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        if ("OPTIONS".equals(WebUtils.toHttp(request).getMethod())) {
            return true;
        }
        Subject subject = getSubject(request, response);
        return subject.isAuthenticated();
    }

    @Override
    protected boolean onAccessDenied(ServletRequest req, ServletResponse rsp, Object mappedValue) throws Exception {
        boolean allowThru = onAccessDenied(req, rsp) || isPermissive(mappedValue);
        if (!allowThru) {
            WebUtils.toHttp(rsp).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
        return allowThru;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest req, ServletResponse rsp) throws Exception {
        try {
            return executeLogin(req, rsp);
        } catch (Exception e) {
            Logger.getLogger(JWTFilter.class.getName()).log(Level.ALL, null, e);
            return false;
        }
    }

    /**
     * Searches through all the cookies in the httpRequest for a cookie containing a JWT Token.
     * The name of the cookie to look for is configured in the JWT Class.
     *
     * @param httpRequest Servlet request
     * @return Value of JWT cookie
     */
    private String getToken(ServletRequest httpRequest) {
        Cookie[] cookies = WebUtils.toHttp(httpRequest).getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(JWT.cookieName)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
