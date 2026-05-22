package com.carmenio.consensus.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
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
 * The {@link JwtDecoder} is created explicitly to accept {@code at+jwt}
 * token types (RFC 9068) and discover signing algorithms from the JWKS endpoint.
 * Validates token signature, issuer, audience, client_id, expiration, and not-before claims.
 *
 * <p>Defines the route authorization matrix with {@code /public/**} for
 * unauthenticated access and {@code /private/**} for role-protected operations.
 * The {@code /api} prefix is applied via {@code server.servlet.context-path}.
 *
 * <h3>Route matrix</h3>
 * <ul>
 *   <li>{@code /public/**} — open access (GET processes, teams)</li>
 *   <li>{@code /private/records} POST — exempt (Semaphore Relayer)</li>
 *   <li>{@code /private/records/**} GET — authenticated</li>
 *   <li>{@code /private/processes/**} POST, PUT, DELETE — {@code consensus-creator} role</li>
 *   <li>{@code /private/teams/**} POST, PUT, DELETE — {@code consensus-creator} role</li>
 *   <li>{@code /private/**} enrollment GET endpoints — authenticated</li>
 *   <li>{@code /private/**} enrollment POST, DELETE endpoints — {@code consensus-creator} role</li>
 *   <li>{@code /private/**} enrollment PUT endpoint — {@code consensus-user} role</li>
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

    /**
     * Custom {@link JwtDecoder} that accepts both {@code JWT} and {@code at+jwt}
     * token types (RFC 9068) via {@link JwtValidators#createAtJwtValidator()},
     * and discovers supported signing algorithms from the JWKS endpoint.
     *
     * <p>Created explicitly instead of relying on Spring Boot auto-configuration
     * because Logto issues {@code typ: at+jwt} access tokens, which are not
     * handled by the default auto-configuration.
     *
     * @param issuerUri  the expected issuer (validated against the {@code iss} claim)
     * @param jwkSetUri  the Logto JWKS endpoint for signature verification keys
     * @param audience   the required audience (validated against the {@code aud} claim)
     * @param clientId   the expected client_id (validated against the {@code client_id} claim, mandatory for AT+jwt)
     * @return fully configured {@link NimbusJwtDecoder}
     */
    @Bean
    public JwtDecoder jwtDecoder(
            @Value("${JWT_ISSUER}") String issuerUri,
            @Value("${JWKS_URI}") String jwkSetUri,
            @Value("${AUDIENCE}") String audience,
            @Value("${CLIENT_ID}") String clientId) {

        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri)
                .validateType(false)
                .discoverJwsAlgorithms()
                .build();

        decoder.setJwtValidator(JwtValidators.createAtJwtValidator()
                .issuer(issuerUri)
                .audience(audience)
                .clientId(clientId)
                .build());

        return decoder;
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
                .requestMatchers(HttpMethod.GET, "/private/processes/*/enrollments/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/private/enrollments/{id}/commitment").hasRole("consensus-user")
                .requestMatchers(HttpMethod.GET, "/private/enrollments/{id}").authenticated()
                .requestMatchers(HttpMethod.POST, "/private/processes/*/enrollments/**").hasRole("consensus-creator")
                .requestMatchers(HttpMethod.DELETE, "/private/enrollments/{id}").hasRole("consensus-creator")
                // ── Creator-only: process and team mutations ──
                .requestMatchers(HttpMethod.GET, "/private/processes/**").hasRole("consensus-creator")
                .requestMatchers(HttpMethod.POST, "/private/processes/**").hasRole("consensus-creator")
                .requestMatchers(HttpMethod.PUT, "/private/processes/**").hasRole("consensus-creator")
                .requestMatchers(HttpMethod.DELETE, "/private/processes/**").hasRole("consensus-creator")
                .requestMatchers(HttpMethod.POST, "/private/teams/**").hasRole("consensus-creator")
                .requestMatchers(HttpMethod.PUT, "/private/teams/**").hasRole("consensus-creator")
                .requestMatchers(HttpMethod.DELETE, "/private/teams/**").hasRole("consensus-creator")
                // ── Records (POST exempt for Relayer; GET requires any authenticated user) ──
                .requestMatchers(HttpMethod.POST, "/private/records").permitAll()
                .requestMatchers(HttpMethod.GET, "/private/records/**").authenticated()
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
     * <p>Logto sends roles as a {@code roles} claim (e.g. {@code ["consensus-creator", "consensus-user"]}).
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
