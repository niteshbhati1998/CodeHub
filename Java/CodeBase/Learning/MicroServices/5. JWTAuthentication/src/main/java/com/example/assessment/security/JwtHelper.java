package com.example.assessment.security;
 
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@Component
public class JwtHelper {
 
	//token validity (converting 5 hours to ms)
    public static final long JWT_TOKEN_VALIDITY = 5 * 60 * 60 * 1000;
 
    //secret key 
    private String secretKey = "afafasfafafasfasfasfafacasdasfasxASFACASDFACASDFASFASFDAFASFASDAADSCSDFADCVSGCFVADXCcadwavfsfarvf";
    
    //hashing algorithm
    String algorithm = "HmacSHA512";
    
    private SecretKey getSignInKey() {
        byte[] bytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(bytes, algorithm);
    }

    //retrieve username from token
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }
 
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }
 
    //for retrieving any information from token we will need the SecretKey
    private Claims getAllClaimsFromToken(String token){
    	return Jwts.parser()
    			.verifyWith(getSignInKey())
    			.build()
    			.parseSignedClaims(token)
    			.getPayload();
    }
 
    //check if the token has expired
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }
 
    //generate token for user
    public String generateToken(UserDetails userDetails) {
        return generateToken(userDetails.getUsername());
    }
 
    //1. Define  claims of the token, like Issuer, Expiration, Subject, and the ID
    //2. Sign the JWT using SecretKey
    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY))
                .signWith(getSignInKey())
                .compact();
    }
    
    //validate token
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}