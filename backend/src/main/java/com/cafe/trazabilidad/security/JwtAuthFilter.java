package com.cafe.trazabilidad.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro que materializa la autenticación <em>stateless</em> basada en JWT en cada petición.
 *
 * <p>Se ejecuta una sola vez por petición (antes del filtro estándar de usuario y
 * contraseña de Spring Security) e inspecciona la cabecera {@code Authorization}. Si
 * contiene un token {@code Bearer} válido y aún no hay autenticación en el contexto,
 * reconstruye la identidad del usuario (nombre y rol) a partir de los claims del token
 * y la deposita en el {@link SecurityContextHolder}.</p>
 *
 * <p>Al no consultar sesión ni base de datos, el contexto de seguridad se establece de
 * forma efímera para la duración de la petición. Si el token falta o no es válido, el
 * filtro no interrumpe la cadena: delega en las reglas de autorización la decisión de
 * permitir o denegar el acceso.</p>
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtService.esValido(token)
                    && SecurityContextHolder.getContext().getAuthentication() == null) {
                String username = jwtService.extraerUsername(token);
                String rol = jwtService.extraerRol(token);
                var auth = new UsernamePasswordAuthenticationToken(
                        username, null, List.of(new SimpleGrantedAuthority("ROLE_" + rol)));
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        chain.doFilter(request, response);
    }
}
