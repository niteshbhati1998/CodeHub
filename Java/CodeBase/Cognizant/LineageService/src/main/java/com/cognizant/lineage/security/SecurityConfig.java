package com.cognizant.lineage.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;


@EnableWebSecurity
@Configuration
public class SecurityConfig {

    @Autowired
    private UserDetailsService uds;

    @Autowired
    JwtAuthorizationFilter jwtAuthorizationFilter;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.cors(httpSecurityCorsConfigurer ->
                httpSecurityCorsConfigurer.configurationSource(request ->
                    new CorsConfiguration().applyPermitDefaultValues()))
            .csrf().disable().authorizeRequests()
            .requestMatchers("/actuator/**").permitAll()
            .requestMatchers("/datascan/test").permitAll()
            .requestMatchers("/datascan/authenticate").permitAll()
            .requestMatchers("/getprojectlist").permitAll()
            .requestMatchers("/columnLineage/parse/sql").permitAll()
            .anyRequest().authenticated()

            .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

            /*.and()
            .authenticationProvider(authenticationProvider());*/

            .and()
            .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }

    /**@Bean public BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
    }**/

    /**
     * Variants = argon2id
     * Salt = 16 bytes, 128-bit
     * Hash length = 32 bytes, 256-bit
     * Iteration = 10
     * Memory = 1 << 16, or 2 ^ 16, 65536k, 64M
     * Parallelism = 1
     * The Argon2PasswordEncoder doesn’t allow to change the Argon2’s variants.
     * The Argon2 algorithm has three variants:
     * Argon2d, maximizes resistance to GPU cracking attacks, suitable for Cryptocurrency.
     * Argon2i, optimized to resist side-channel attacks, suitable for password hashing.
     * Argon2id, hybrid version, if not sure, picks this.
     * <p>
     * Besides 16 bytes salt and 32 bytes key length, the rest of the parameters depends on
     * the server capacity. Run the Argon2 password hashing at the production server and
     * fine-tune the iterations, threads, memory, and time that each call can afford.
     * Generally, Argon2 authentication takes 0.5ms to 1 second is recommended.
     *
     * @return
     */
    @Bean
    public Argon2PasswordEncoder passwordEncoder() {
        //return new Argon2PasswordEncoder();
        // int saltLength, int hashLength, int parallelism, int memory, int iterations
        Argon2PasswordEncoder encoder = new Argon2PasswordEncoder(16, 32, 1, 65536, 10);
        return encoder;

    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(uds);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
            http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.authenticationProvider(authenticationProvider());
        return authenticationManagerBuilder.build();
    }

/**    @Bean CorsConfigurationSource corsConfigurationSource()  {

CorsConfiguration config = new CorsConfiguration();
config.setAllowCredentials(true);
config.setAllowedOriginPatterns(List.of("*"));
config.setAllowedOrigins(List.of("*"));
config.setAllowedMethods(Arrays.asList("GET","POST","PUT","DELETE","PATCH"));
config.setAllowedHeaders(List.of("*"));
config.setExposedHeaders(List.of("*"));

UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
source.registerCorsConfiguration("/**", config);
return source;
}
 **/
/**    @Bean public CorsWebFilter corsWebFilter() {
return new CorsWebFilter(corsConfigurationSource());
}
 **/
}
