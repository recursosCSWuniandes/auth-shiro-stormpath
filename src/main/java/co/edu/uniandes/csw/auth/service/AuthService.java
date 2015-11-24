package co.edu.uniandes.csw.auth.service;

import co.edu.uniandes.csw.auth.model.UserDTO;
import co.edu.uniandes.csw.auth.security.JWT;
import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.account.AccountCriteria;
import com.stormpath.sdk.account.AccountList;
import com.stormpath.sdk.account.AccountStatus;
import com.stormpath.sdk.account.Accounts;
import com.stormpath.sdk.application.Application;
import com.stormpath.sdk.client.Client;
import com.stormpath.sdk.group.Group;
import com.stormpath.sdk.group.GroupList;
import com.stormpath.sdk.resource.ResourceException;
import com.stormpath.shiro.realm.ApplicationRealm;
import java.util.Map;
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
import javax.ws.rs.core.Response;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.subject.Subject;
import javax.ws.rs.core.Context;

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
            loggedUser.setPassword(user.getPassword());
            String jwtToken = JWT.generateJWT(loggedUser);

            Cookie jwt = new Cookie("jwt-token", jwtToken);
            jwt.setHttpOnly(true);
            rsp.addCookie(jwt);
            return user;
        } catch (AuthenticationException e) {
            Logger.getLogger(AuthService.class.getName()).log(Level.WARNING, e.getMessage());
            throw new WebApplicationException(e, 400);
        }
    }

    @Path("/logout")
    @GET
    public Response logout() {

        Subject currentUser = SecurityUtils.getSubject();
        if (currentUser != null) {
            currentUser.logout();
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @Path("/currentUser")
    @GET
    public Response getCurrentUser() {
        UserDTO user = new UserDTO();
        Subject currentUser = SecurityUtils.getSubject();
        if (currentUser != null) {
            Map<String, String> userAttributes = (Map<String, String>) currentUser.getPrincipals().oneByType(java.util.Map.class
            );
            user.setEmail(userAttributes.get("email"));
            user.setUserName(userAttributes.get("username"));
            return Response.ok(user)
                    .build();
        } else {
            Logger.getLogger(AuthService.class.getName()).log(Level.WARNING, "user null");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("user null")
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
    }

    @Path("/register")
    @POST
    public Response register(UserDTO user) {
        try {
            createUser(user);
            return Response.ok().build();
        } catch (ResourceException e) {
            return Response.status(e.getStatus())
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
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
    public Response forgotPassword(UserDTO user) {
        try {
            getApplication().sendPasswordResetEmail(user.getEmail());
            return Response.ok().build();
        } catch (ResourceException e) {
            return Response.status(e.getStatus())
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
    }

    protected ApplicationRealm getRealm() {
        return ((ApplicationRealm) ((RealmSecurityManager) SecurityUtils.getSecurityManager()).getRealms().iterator().next());
    }

    protected Client getClient() {
        return getRealm().getClient();
    }

    protected Application getApplication() {
        return getClient().getResource(getRealm().getApplicationRestUrl(), Application.class);
    }
}
