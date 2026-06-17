package com.cafe.trazabilidad.security;

import com.cafe.trazabilidad.security.dto.LoginRequest;
import com.cafe.trazabilidad.security.dto.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST que expone el endpoint de autenticación de la aplicación.
 *
 * <p>Recibe las credenciales del cliente, las valida contra el
 * {@link AuthenticationManager} y, en caso de éxito, emite un token JWT mediante
 * {@link JwtService}. Es el único punto de entrada para obtener un token con el que
 * acceder al resto de recursos protegidos.</p>
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public AuthController(AuthenticationManager authManager, JwtService jwtService,
                          UsuarioRepository usuarioRepository) {
        this.authManager = authManager;
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Autentica a un usuario y emite un token JWT.
     *
     * <p>Valida las credenciales recibidas; si son correctas, recupera el usuario y
     * genera un token firmado que incluye su nombre y rol.</p>
     *
     * @param req credenciales de acceso (nombre de usuario y contraseña)
     * @return el token emitido junto con el nombre de usuario, el rol y el tiempo de expiración en milisegundos
     * @throws org.springframework.security.core.AuthenticationException si las credenciales no son válidas
     */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password()));
        Usuario u = usuarioRepository.findByUsername(req.username()).orElseThrow();
        String token = jwtService.generarToken(u.getUsername(), u.getRol().name());
        return new LoginResponse(token, u.getUsername(), u.getRol().name(), jwtService.getExpirationMillis());
    }
}
