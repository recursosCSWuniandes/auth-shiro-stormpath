package co.edu.uniandes.csw.auth.security;

import co.edu.uniandes.csw.auth.model.UserDTO;
import co.edu.uniandes.csw.auth.stormpath.ApiKeyProperties;
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
    public static final String cookieName = "jwt-token";

    /**
     * Private Key to encrypt the token with
     */
    private static final String key = new ApiKeyProperties().getProperty("apiKey.secret");

    /**
     * Creates a JWT Token using the provided information about the user.
     *
     *
     * @param user User information
     * @param password User password
     * @return Encrypted JWT Token
     */
    public static String createToken(UserDTO user, String password) {
        JwtBuilder jwt = Jwts.builder()
                .claim("email", user.getEmail())
                .claim("username", user.getUserName())
                .claim("roles", user.getRoles())
                .claim("givenName", user.getGivenName())
                .claim("surName", user.getSurName())
                .claim("password", password)
                .setSubject("auth")
                .signWith(SignatureAlgorithm.HS512, key);

        if (user.getMiddleName() != null) {
            jwt.claim("middleName", user.getMiddleName());
        }
        return jwt.compact();
    }

    /**
     * Decrypts a JWT token created with createToken.
     *
     * @param token JWT token
     * @return User information
     */
    public static UserDTO verifyToken(String token) {
        Claims jwtClaims = Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
        UserDTO user = new UserDTO();
        user.setEmail(jwtClaims.get("email").toString());
        user.setUserName(jwtClaims.get("username").toString());
        user.setRoles((List<String>) jwtClaims.get("roles"));
        user.setGivenName(jwtClaims.get("givenName").toString());
        if (jwtClaims.get("middleName") != null) {
            user.setMiddleName(jwtClaims.get("middleName").toString());
        }
        user.setSurName(jwtClaims.get("surName").toString());
        user.setPassword(jwtClaims.get("password").toString());
        return user;
    }
}
