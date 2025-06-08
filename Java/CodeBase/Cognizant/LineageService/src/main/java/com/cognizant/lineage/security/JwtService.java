package com.cognizant.lineage.security;

import java.security.Key;
import java.util.Date;

import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class JwtService {
	
	@Autowired
	Environment env;
	
    //private String SECRET_KEY = "mysecretkey";
    //private long EXPIRE_DURATION = 60*60*1000;
    private final String TOKEN_HEADER = "Authorization";
    private final String TOKEN_PREFIX = "Bearer ";
    
    public long getJwtTokenExpireDurationInMilliSec() {
    	long jwtTokenExpireDurationInMilliSec = 0L;
    	if(StringUtils.isNoneBlank(env.getProperty("jwtTokenExpireDurationInMilliSec"))) {
    		jwtTokenExpireDurationInMilliSec = Long.parseLong(env.getProperty("jwtTokenExpireDurationInMilliSec"));
    	}
    	return jwtTokenExpireDurationInMilliSec;
    }
    /**
     * hash-based message authentication code (HMAC) and is cryptographic hash function
     * JWT HMAC signature algorithms
     * @return
     */
    public Key createHmacKey() {
    	
    	String secret = env.getProperty("jwtSecretKey");
    	Key hmacKey = new SecretKeySpec(secret.getBytes(), 
                SignatureAlgorithm.HS256.getJcaName());

    	return hmacKey;
    }
    
    public String createToken(String userId, String authoririties) {
        return Jwts.builder()
        		.setSubject(userId)
        		.setIssuer("DataScan")
        		.claim("authoririties", authoririties)
        		.setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + getJwtTokenExpireDurationInMilliSec()))
                //.signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .signWith(createHmacKey())
                .compact();
    }

    public Claims resolveClaims(HttpServletRequest req) {
        try {
            String token = resolveToken(req);
            if (token != null) {
                return parseJwtClaims(token);
            }
            return null;
        } catch (ExpiredJwtException ex) {
            req.setAttribute("expired", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            req.setAttribute("invalid", ex.getMessage());
            throw ex;
        }
    }

    public String resolveToken(HttpServletRequest request) {

        String bearerToken = request.getHeader(TOKEN_HEADER);
        if (bearerToken != null && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        return null;
    }
    
    private Claims parseJwtClaims(String token) {
    	JwtParser jwtParser = Jwts.parser()
    			.setSigningKey(createHmacKey())
    			.build();
        return jwtParser.parseClaimsJws(token).getBody();
    }

    public boolean validateClaims(Claims claims) throws AuthenticationException {
        try {
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            throw e;
        }
    }

    public String getUserId(Claims claims) {
        return claims.getSubject();
    }
}
