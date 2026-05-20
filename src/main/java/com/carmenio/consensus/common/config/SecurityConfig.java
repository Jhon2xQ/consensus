package com.carmenio.consensus.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import org.springframework.security.core.GrantedAuthority;

import java.util.List;

/**
 * Security configuration for the Consensus API.
 *
 * <p>Configures OAuth2 Resource Server with JWT validation via Logto.
 * JWT decoder is auto-configured by Spring Boot from
 * {@code spring.security.oauth2.resourceserver.jwt.*} properties.
 * Validates token signature (RS256), issuer, audience, and expiration.
 *
 * <p>Defines the route authorization matrix with {@code /public/**} for
 * unauthenticated access and {@code /private/**} for role-protected operations.
 * The {@code /api} prefix is applied via {@code server.servlet.context-path}.
 *
 * <h3>Route matrix</h3>
 * <ul>
 *   <li>{@code /public/**} — open access (GET processes, teams, results; POST records)</li>
 *   <li>{@code /private/processes/**} POST, PUT, DELETE — {@code creator} role</li>
 *   <li>{@code /private/teams/**} POST, PUT, DELETE — {@code creator} role</li>
 *   <li>{@code /private/**} enrollment endpoints — {@code user} role</li>
 *   <li>Fallback — authenticated</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * CORS configuration allowing the SvelteKit frontend to call the API.
     * Origins are configured via the {@code CORS_ORIGINS} environment variable.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${CORS_ORIGINS:}") String corsOrigins) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(corsOrigins.isBlank()
                ? List.of("*")
                : List.of(corsOrigins.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CorsConfigurationSource corsConfigurationSource) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // ── Enrollment endpoints (most specific first) ──
                .requestMatchers(HttpMethod.GET, "/private/processes/*/enrollments/**").hasRole("user")
                .requestMatchers(HttpMethod.POST, "/private/processes/*/enrollments/**").hasRole("user")
                .requestMatchers(HttpMethod.GET, "/private/enrollments/{id}").hasRole("user")
                // ── Creator-only: process and team mutations ──
                .requestMatchers(HttpMethod.POST, "/private/processes/**").hasRole("creator")
                .requestMatchers(HttpMethod.PUT, "/private/processes/**").hasRole("creator")
                .requestMatchers(HttpMethod.DELETE, "/private/processes/**").hasRole("creator")
                .requestMatchers(HttpMethod.POST, "/private/teams/**").hasRole("creator")
                .requestMatchers(HttpMethod.PUT, "/private/teams/**").hasRole("creator")
                .requestMatchers(HttpMethod.DELETE, "/private/teams/**").hasRole("creator")
                // ── Public endpoints ──
                .requestMatchers("/public/**").permitAll()
                // ── Fallback: all other requests require auth ──
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            );

        return http.build();
    }

    /**
     * Converts the JWT {@code roles} claim into Spring Security authorities.
     *
     * <p>Logto sends roles as a {@code roles} claim (e.g. {@code ["creator", "user"]}).
     * Spring Security expects authorities in the format {@code ROLE_<rolename>}.
     * This converter maps each role to a {@code ROLE_}-prefixed {@link SimpleGrantedAuthority}.
     */
    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            List<String> roles = jwt.getClaimAsStringList("roles");
            if (roles == null) {
                return List.of();
            }
            return roles.stream()
                    .<GrantedAuthority>map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .toList();
        });
        return converter;
    }
}
