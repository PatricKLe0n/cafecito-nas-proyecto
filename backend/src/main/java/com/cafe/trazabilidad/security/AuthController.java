package com.cafe.trazabilidad.security;

import com.cafe.trazabilidad.security.dto.LoginRequest;
import com.cafe.trazabilidad.security.dto.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password()));
        Usuario u = usuarioRepository.findByUsername(req.username()).orElseThrow();
        String token = jwtService.generarToken(u.getUsername(), u.getRol().name());
        return new LoginResponse(token, u.getUsername(), u.getRol().name(), jwtService.getExpirationMillis());
    }
}
