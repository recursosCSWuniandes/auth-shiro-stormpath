package co.edu.uniandes.csw.auth.filter;


import co.edu.uniandes.csw.auth.model.UserDTO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.impl.crypto.MacProvider;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.KeyGenerator;


/**
 * Created by andre on 25/09/2015.
 */

public class JWT {
    private String key;

    public JWT() {
        if (System.getenv("JWT_KEY")!=null){
            key = System.getenv("JWT_KEY");
        }else{
            key = "uniandes";
        }
            
        
    }

    public String generateJWT(UserDTO user)   {    
    String token = Jwts.builder()
            .claim("email", user.getEmail())
            .claim("username", user.getUserName())
            .claim("role", user.getRole())
            .claim("givenName", user.getGivenName())
            .claim("middleName", user.getMiddleName())
            .claim("surName", user.getSurName())
            .claim("fullName", user.getFullName())
            .claim("password", user.getPassword()).setSubject("auth").signWith(SignatureAlgorithm.HS512,key).compact();
    return token;
        
    }

    public UserDTO verifyToken(String token)  {
        try{
        Claims jwtClaims=Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();            
        UserDTO user =new UserDTO();
        user.setEmail(jwtClaims.get("email").toString());
        user.setUserName(jwtClaims.get("username").toString());
        user.setRole((List<String>) jwtClaims.get("role"));
        user.setGivenName(jwtClaims.get("givenName").toString());
        user.setMiddleName(jwtClaims.get("middleName").toString());
        user.setSurName(jwtClaims.get("surName").toString());
        user.setFullName(jwtClaims.get("fullName").toString());
        user.setPassword(jwtClaims.get("password").toString());
        return user;
        }catch (SignatureException e) {
            throw new SignatureException("El usuario no tiene acceso al recurso." +e.getMessage());
        }
            
        
    }
     
}