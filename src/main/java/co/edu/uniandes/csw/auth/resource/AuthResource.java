/*
 * Copyright 2015 Los Andes University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package co.edu.uniandes.csw.auth.resource;

import co.edu.uniandes.csw.auth.model.CredentialsDTO;
import co.edu.uniandes.csw.auth.model.NewUserDTO;
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

/**
 * Generic REST Resource for user authentication
 *
 * @author af.esguerra10
 */
@Path("/accounts")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    private static final Logger logger = Logger.getLogger(AuthResource.class.getName());

    @Context
    private HttpServletRequest req;

    @Context
    private HttpServletResponse rsp;

    /**
     * Performs a login based on the user's username and password
     *
     * @param credentials User information
     * @return Authenticated user information
     */
    @Path("/login")
    @POST
    public UserDTO login(CredentialsDTO credentials) {
        UsernamePasswordToken token = new UsernamePasswordToken(credentials.getUsername(), credentials.getPassword());
        Subject currentUser = SecurityUtils.getSubject();
        try {
            currentUser.login(token);
            Account account = getClient().getResource(req.getRemoteUser(), Account.class);
            rsp.addCookie(createJWTCookie(account, credentials.getPassword()));
            return new UserDTO(account);
        } catch (AuthenticationException e) {
            logger.log(Level.WARNING, e.getMessage());
            throw new WebApplicationException(e, HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    /**
     * Performs a logout and destroys the JWT Cookie
     */
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

    /**
     * Retrieves the information of the currently-logged user if any
     *
     * @return User information
     */
    @Path("/me")
    @GET
    public UserDTO getCurrentUser() {
        String accountHref = req.getRemoteUser();
        if (accountHref != null) {
            Account account = getClient().getResource(accountHref, Account.class);
            return new UserDTO(account);
        } else {
            throw new WebApplicationException(401);
        }
    }

    /**
     * Creates a user account
     *
     * @param user User information
     *
     * @return Usuario creado
     */
    @Path("/register")
    @POST
    @StatusCreated
    public UserDTO register(NewUserDTO user) {
        try {
            Account acc = createUser(user);
            return new UserDTO(acc);
        } catch (ResourceException e) {
            throw new WebApplicationException(e, e.getStatus());
        }
    }

    /**
     * Creates a user account in Stormpath.
     *
     * @param user User information
     * @return Created account
     */
    public Account createUser(NewUserDTO user) {
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

        for (String userGroup : user.getGroups()) {
            for (Group group : groups) {
                if (group.getName().equals(userGroup)) {
                    acct.addGroup(group);
                    break;
                }
            }
        }
        return acct;
    }

    /**
     * Deletes an account from stormpath.
     *
     * @param username Username
     */
    @Path("delete/{username}")
    @DELETE
    public void deleteAccount(@PathParam("username") String username) {
        AccountCriteria criteria = Accounts.where(Accounts.username().eqIgnoreCase(username));
        AccountList accounts = getApplication().getAccounts(criteria);
        for (Account account : accounts) {
            account.delete();
        }
    }

    /**
     * Sends an email to recover a lost password to the registered email.
     *
     * @param user User information
     */
    @Path("/forgot")
    @POST
    public void forgotPassword(UserDTO user) {
        if (user != null) {
            try {
                getApplication().sendPasswordResetEmail(user.getEmail());
            } catch (ResourceException e) {
                throw new WebApplicationException(e, e.getStatus());
            }
        }
    }

    /**
     * Creates a Cookie with a JWT Token
     *
     * @param acc User information
     * @param password User password
     * @return Cookie with token
     */
    private Cookie createJWTCookie(Account acc, String password) {
        String token = JWT.createToken(acc, password);
        Cookie cookie = new Cookie(JWT.cookieName, token);
        cookie.setHttpOnly(true);
        cookie.setPath(req.getContextPath());
        return cookie;
    }
}
