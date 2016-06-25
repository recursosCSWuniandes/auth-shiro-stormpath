package co.edu.uniandes.csw.auth.security;

import co.edu.uniandes.csw.auth.model.NewUserDTO;
import co.edu.uniandes.csw.auth.stormpath.ApiKeyProperties;
import com.stormpath.sdk.account.Account;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.List;

/**
 * JWT Utilities Class
 *
 * @author jd.patino10
 */
public abstract class JWT {

    /**
     * Name for the cookie to contain the token
     */
    public static final String cookieName = "accessToken";

    /**
     * Private Key to encrypt the token with
     */
    private static final String key = new ApiKeyProperties().getApiKeySecret();

    /**
     * Creates a JWT Token using the provided information about the user.
     *
     *
     * @param acc User information
     * @param password User password
     * @return Encrypted JWT Token
     */
    public static String createToken(Account acc, String password) {
        JwtBuilder jwt = Jwts.builder()
                .claim("email", acc.getEmail())
                .claim("username", acc.getUsername())
                .claim("roles", acc.getGroups())
                .claim("givenName", acc.getGivenName())
                .claim("surName", acc.getSurname())
                .claim("password", password)
                .setSubject("auth")
                .signWith(SignatureAlgorithm.HS512, key);

        if (acc.getMiddleName() != null) {
            jwt.claim("middleName", acc.getMiddleName());
        }
        return jwt.compact();
    }

    /**
     * Decrypts a JWT token created with createToken.
     *
     * @param token JWT token
     * @return User information
     */
    public static NewUserDTO verifyToken(String token) {
        Claims jwtClaims = Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
        NewUserDTO user = new NewUserDTO();
        user.setEmail(jwtClaims.get("email").toString());
        user.setUserName(jwtClaims.get("username").toString());
        user.setGroups((List<String>) jwtClaims.get("roles"));
        user.setGivenName(jwtClaims.get("givenName").toString());
        if (jwtClaims.get("middleName") != null) {
            user.setMiddleName(jwtClaims.get("middleName").toString());
        }
        user.setSurName(jwtClaims.get("surName").toString());
        user.setPassword(jwtClaims.get("password").toString());
        return user;
    }
}
