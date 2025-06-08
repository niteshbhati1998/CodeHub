package com.example.assessment.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtHelper jwtHelper;

    @Autowired
    private UserDetailsService userDetailsService;
    
	private Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    	
    	//Bearer 2352345235sdfrsfgsdfsdf
        String requestHeader = request.getHeader("Authorization");
        LOGGER.info(" Header :  {}", requestHeader);
        
        String username = null;
        String token = null;
        
        if (requestHeader != null && requestHeader.startsWith("Bearer")) {
 
            token = requestHeader.substring(7);
            try {
                username = jwtHelper.getUsernameFromToken(token);
            } catch (IllegalArgumentException ex) {
                LOGGER.error("Illegal Argument while fetching the username !!");
                ex.printStackTrace();
            } catch (ExpiredJwtException ex) {
                LOGGER.error("Given jwt token is expired !!");
                ex.printStackTrace();
            } catch (MalformedJwtException ex) {
                LOGGER.error("Some changed has done in token !! Invalid Token");
                ex.printStackTrace();
            } catch (Exception ex) {
            	ex.printStackTrace();
            }
        } else {
            LOGGER.info("Invalid Header Value !! ");
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            //fetch userdetails from username
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            Boolean validateToken = jwtHelper.validateToken(token, userDetails);
            
            if (validateToken) {

                //set authentication
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                LOGGER.info("Validation fails !!");
            }
        }
        filterChain.doFilter(request, response);
    }
}
