package co.edu.uniandes.csw.auth.filter;

import co.edu.uniandes.csw.auth.security.JWT;
import co.edu.uniandes.csw.auth.model.UserDTO;
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
 *
 * @author jd.patino10
 */
public class JWTFilter extends AuthenticatingFilter {

    @Override
    protected AuthenticationToken createToken(ServletRequest req, ServletResponse rsp) throws Exception {
        String token = getToken(req);
        try {
            UserDTO user = JWT.verifyToken(token);
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
