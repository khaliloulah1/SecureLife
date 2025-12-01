package gestion.securelife.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth

                        // -----------------------------
                        //         ROUTES PUBLIQUES
                        // -----------------------------
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // -----------------------------
                        //        ADMIN ONLY
                        // -----------------------------
                        .requestMatchers("/api/v1/insurances/stats").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/insurances/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/documents/**").hasRole("ADMIN")

                        // -----------------------------
                        //   ADMIN + AGENT : création
                        // -----------------------------
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/insurances/**",
                                "/api/v1/insurances/auto",
                                "/api/v1/insurances/home",
                                "/api/v1/insurances/life",
                                "/api/v1/insurances/{id}/documents"
                        ).hasAnyRole("ADMIN", "AGENT")

                        // -----------------------------
                        //   ADMIN + AGENT : modification
                        // -----------------------------
                        .requestMatchers(HttpMethod.PUT,
                                "/api/v1/insurances/**",
                                "/api/v1/insurances/auto/**",
                                "/api/v1/insurances/home/**",
                                "/api/v1/insurances/life/**"
                        ).hasAnyRole("ADMIN", "AGENT")

                        .requestMatchers(HttpMethod.PATCH,
                                "/api/v1/insurances/{id}/status"

                        ).hasAnyRole("ADMIN", "AGENT")

                        // -----------------------------
                        //    ADMIN + AGENT + CLIENT
                        //          LECTURE
                        // -----------------------------
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/insurances/**",
                                "/api/v1/insurances/auto/**",
                                "/api/v1/insurances/home/**",
                                "/api/v1/insurances/life/**",
                                "/api/v1/documents/**"
                        ).hasAnyRole("ADMIN", "AGENT", "CLIENT")

                        // -----------------------------
                        //   TOUT LE RESTE = JWT REQUIS
                        // -----------------------------
                        .anyRequest().authenticated()
                )

                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
