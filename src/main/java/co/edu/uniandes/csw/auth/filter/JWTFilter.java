package co.edu.uniandes.csw.auth.filter;

import co.edu.uniandes.csw.auth.security.JWT;
import co.edu.uniandes.csw.auth.model.UserDTO;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;

/**
 *
 * @author jd.patino10
 */
public class JWTFilter extends AuthenticatingFilter {

    @Override
    protected AuthenticationToken createToken(ServletRequest req, ServletResponse rsp) throws Exception {
        String token = getToken(req);
        if (token == null) {
            throw new Exception("No token provided");
        }
        UserDTO user = JWT.verifyToken(token);
        return createToken(user.getUserName(), user.getPassword(), req, rsp);
    }

    @Override
    protected boolean onAccessDenied(ServletRequest req, ServletResponse rsp) throws Exception {
        return executeLogin(req, rsp);
    }

    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
        ((HttpServletResponse) response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }

    private String getToken(ServletRequest httpRequest) {
        Cookie[] cookies = ((HttpServletRequest) httpRequest).getCookies();

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
