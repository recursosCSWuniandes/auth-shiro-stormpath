package co.edu.uniandes.csw.auth.service;

import co.edu.uniandes.csw.auth.model.UserDTO;
import co.edu.uniandes.csw.auth.provider.StatusCreated;
import co.edu.uniandes.csw.auth.security.JWT;
import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.account.AccountCriteria;
import com.stormpath.sdk.account.AccountList;
import com.stormpath.sdk.account.AccountStatus;
import com.stormpath.sdk.account.Accounts;
import com.stormpath.sdk.application.Application;
import com.stormpath.sdk.group.Group;
import com.stormpath.sdk.group.GroupList;
import com.stormpath.sdk.resource.ResourceException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import javax.ws.rs.core.Context;
import static co.edu.uniandes.csw.auth.stormpath.Utils.*;

@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthService {

    @Context
    private HttpServletRequest req;

    @Context
    private HttpServletResponse rsp;

    @Path("/login")
    @POST
    public UserDTO login(UserDTO user) {
        UsernamePasswordToken token = new UsernamePasswordToken(user.getUserName(), user.getPassword());
        Subject currentUser = SecurityUtils.getSubject();
        try {
            currentUser.login(token);
            Account account = getClient().getResource(req.getRemoteUser(), Account.class);
            UserDTO loggedUser = new UserDTO(account);
            rsp.addCookie(createJWTCookie(loggedUser, user.getPassword()));
            return loggedUser;
        } catch (AuthenticationException e) {
            Logger.getLogger(AuthService.class.getName()).log(Level.WARNING, e.getMessage());
            throw new WebApplicationException(e, HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    @Path("/logout")
    @GET
    public void logout() {
        Subject currentUser = SecurityUtils.getSubject();
        if (currentUser != null) {
            currentUser.logout();
            Cookie cookie = new Cookie(JWT.cookieName, "");
            cookie.setMaxAge(0);
            cookie.setPath(req.getContextPath());
            cookie.setHttpOnly(true);
            rsp.addCookie(cookie);
        }
    }

    @Path("/me")
    @GET
    public UserDTO getCurrentUser() {
        String accountHref = req.getRemoteUser();
        if (accountHref != null) {
            Account account = getClient().getResource(accountHref, Account.class);
            return new UserDTO(account);
        } else {
            return null;
        }
    }

    @Path("/register")
    @POST
    @StatusCreated
    public void register(UserDTO user) {
        try {
            createUser(user);
        } catch (ResourceException e) {
            throw new WebApplicationException(e, e.getStatus());
        }
    }

    protected Account createUser(UserDTO user) {
        Account acct = getClient().instantiate(Account.class);

        acct.setUsername(user.getUserName());
        acct.setPassword(user.getPassword());
        acct.setEmail(user.getEmail());
        acct.setGivenName(user.getGivenName());
        acct.setMiddleName(user.getMiddleName());
        acct.setSurname(user.getSurName());
        acct.setStatus(AccountStatus.ENABLED);

        Application application = getApplication();
        acct = application.createAccount(acct);

        GroupList groups = application.getGroups();
        for (String role : user.getRoles()) {
            for (Group grp : groups) {
                if (grp.getName().equals(role)) {
                    acct.addGroup(grp);
                    break;
                }
            }
        }
        return acct;
    }

    @Path("delete/{username}")
    @DELETE
    public void deleteAccount(@PathParam("username") String username) {
        AccountCriteria criteria = Accounts.where(Accounts.username().eqIgnoreCase(username));
        AccountList accounts = getApplication().getAccounts(criteria);
        for (Account account : accounts) {
            account.delete();
        }
    }

    @Path("/forgot")
    @POST
    public void forgotPassword(UserDTO user) {
        try {
            getApplication().sendPasswordResetEmail(user.getEmail());
        } catch (ResourceException e) {
            throw new WebApplicationException(e, e.getStatus());
        }
    }

    private Cookie createJWTCookie(UserDTO user, String password) {
        String token = JWT.createToken(user, password);
        Cookie cookie = new Cookie(JWT.cookieName, token);
        cookie.setHttpOnly(true);
        cookie.setDomain(req.getServerName() + ":" + req.getServerPort());
        cookie.setPath(req.getContextPath());
        return cookie;
    }
}
