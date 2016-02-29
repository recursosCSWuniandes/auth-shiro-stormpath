package co.edu.uniandes.csw.auth.security;

import co.edu.uniandes.csw.auth.model.UserDTO;
import co.edu.uniandes.csw.auth.stormpath.ApiKeyProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.List;

/**
 * Created by andre on 25/09/2015.
 */
public abstract class JWT {

    public static final String cookieName = "jwt-token";

    private static final String key = new ApiKeyProperties().getProperty("apiKey.secret");

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
