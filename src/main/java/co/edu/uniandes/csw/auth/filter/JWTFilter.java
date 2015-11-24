package co.edu.uniandes.csw.auth.filter;

import co.edu.uniandes.csw.auth.security.JWT;
import co.edu.uniandes.csw.auth.model.UserDTO;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;

/**
 *
 * @author jd.patino10
 */
public class JWTFilter extends AuthenticatingFilter {

    @Override
    protected AuthenticationToken createToken(ServletRequest req, ServletResponse rsp) throws Exception {
        UserDTO user = JWT.verifyToken(getToken(req));
        return createToken(user.getUserName(), user.getPassword(), req, rsp);
    }

    @Override
    protected boolean onAccessDenied(ServletRequest sr, ServletResponse response) throws Exception {
        ((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN);
        return false;
    }

    private String getToken(ServletRequest httpRequest) {

        Cookie[] cookies = ((HttpServletRequest) httpRequest).getCookies();
        String token = null;

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("jwt-token") && cookie.isHttpOnly()) {
                    token = cookie.getValue();
                }
            }
        }
        return token;
    }
}
