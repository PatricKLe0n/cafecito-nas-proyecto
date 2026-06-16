package com.cafe.trazabilidad.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Configuración central de Spring Security para la API.
 *
 * <p>Define una cadena de filtros <em>stateless</em>: deshabilita la gestión de sesión
 * (política {@link SessionCreationPolicy#STATELESS}) y CSRF —innecesario sin sesión— y
 * delega la identificación del usuario en el {@link JwtAuthFilter}, que se inserta antes
 * del filtro de usuario y contraseña. Como consecuencia, cada petición debe portar su
 * propio token JWT; el servidor no conserva estado de autenticación entre llamadas.</p>
 *
 * <p>Las reglas de autorización abren los endpoints de login y la documentación OpenAPI,
 * conceden lectura ({@code GET}) a los roles {@code USER} y {@code ADMIN} y reservan las
 * operaciones de escritura ({@code POST}, {@code PUT}, {@code DELETE}) al rol {@code ADMIN}.
 * También expone los beans de codificación de contraseñas (BCrypt) y el
 * {@link AuthenticationManager} usados por el resto del módulo de seguridad.</p>
 */
@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, CorsConfigurationSource corsConfigurationSource) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(c -> c.configurationSource(corsConfigurationSource))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")
                // Catch-all: cualquier otro método sobre /api (PATCH, HEAD, etc.) exige autenticación.
                .requestMatchers("/api/**").authenticated()
                // Resto (recursos estáticos del SPA Angular y rutas de cliente): públicos.
                .anyRequest().permitAll())
            .exceptionHandling(e -> e.authenticationEntryPoint(
                (request, response, ex) ->
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No autenticado")))
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }
}
