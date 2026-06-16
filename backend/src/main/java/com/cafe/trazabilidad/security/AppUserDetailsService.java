package com.cafe.trazabilidad.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Implementación de {@link UserDetailsService} que carga los usuarios desde la base de datos.
 *
 * <p>Sirve de puente entre la entidad {@link Usuario} y el modelo de seguridad de Spring:
 * recupera al usuario por su nombre de acceso y lo traduce a un {@link UserDetails} con su
 * contraseña codificada y su autoridad ({@code ROLE_<rol>}). La utiliza el
 * {@link org.springframework.security.authentication.AuthenticationManager} durante el login.</p>
 */
@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UsuarioRepository repo;

    public AppUserDetailsService(UsuarioRepository repo) {
        this.repo = repo;
    }

    /**
     * Carga los datos de seguridad del usuario a partir de su nombre de acceso.
     *
     * @param username nombre de acceso del usuario a autenticar
     * @return los detalles del usuario (credenciales y autoridades) para Spring Security
     * @throws UsernameNotFoundException si no existe ningún usuario con ese nombre
     */
    @Override
    public UserDetails loadUserByUsername(String username) {
        Usuario u = repo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
        return User.withUsername(u.getUsername())
                .password(u.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + u.getRol().name())))
                .build();
    }
}
