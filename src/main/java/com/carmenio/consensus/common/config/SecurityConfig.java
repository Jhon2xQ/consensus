package com.carmenio.consensus.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.core.GrantedAuthority;

import java.util.List;

/**
 * Security configuration for the Consensus API.
 *
 * <p>Configures OAuth2 Resource Server with JWT validation via Logto.
 * Defines the route authorization matrix:
 * <ul>
 *   <li>Public GET endpoints for processes, teams, and results</li>
 *   <li>{@code POST /api/private/records} exempt (Semaphore Relayer)</li>
 *   <li>{@code creator} role for process and team mutations</li>
 *   <li>{@code user} role for enrollment operations</li>
 *   <li>All other requests require authentication</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * JWT decoder that validates tokens using Logto's JWKS endpoint.
     * Keys are fetched lazily on first use, not at application startup.
     * Issuer validation ensures tokens come from the expected Logto tenant.
     */
    @Bean
    public JwtDecoder jwtDecoder(
            @Value("${JWKS_URI}") String jwksUri,
            @Value("${JWT_ISSUER}") String issuer) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwksUri)
                .jwsAlgorithm(SignatureAlgorithm.RS256)
                .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));
        return decoder;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtDecoder jwtDecoder) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // ── Enrollment endpoints (most specific first) ──
                .requestMatchers(HttpMethod.GET, "/api/private/processes/*/enrollments/**").hasRole("user")
                .requestMatchers(HttpMethod.POST, "/api/private/processes/*/enrollments/**").hasRole("user")
                .requestMatchers(HttpMethod.GET, "/api/private/enrollments/{id}").hasRole("user")
                // ── Creator-only: process and team mutations ──
                .requestMatchers(HttpMethod.POST, "/api/private/processes/**").hasRole("creator")
                .requestMatchers(HttpMethod.PUT, "/api/private/processes/**").hasRole("creator")
                .requestMatchers(HttpMethod.DELETE, "/api/private/processes/**").hasRole("creator")
                .requestMatchers(HttpMethod.POST, "/api/private/teams/**").hasRole("creator")
                .requestMatchers(HttpMethod.PUT, "/api/private/teams/**").hasRole("creator")
                .requestMatchers(HttpMethod.DELETE, "/api/private/teams/**").hasRole("creator")
                // ── Public GET endpoints ──
                .requestMatchers(HttpMethod.GET, "/api/private/processes/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/private/teams/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/private/processes/{id}/results").permitAll()
                // ── Semaphore Relayer exemption ──
                .requestMatchers(HttpMethod.POST, "/api/private/records").permitAll()
                // ── Fallback: all other requests require auth ──
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder)
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
