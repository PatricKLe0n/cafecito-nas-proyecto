package com.cafe.trazabilidad.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

/**
 * Servicio responsable de emitir y validar tokens JWT firmados con HMAC-SHA.
 *
 * <p>Constituye la pieza central de la autenticación <em>stateless</em>: en lugar
 * de mantener sesión en el servidor, cada token transporta de forma autocontenida
 * el sujeto (nombre de usuario), el emisor, el rol y la fecha de expiración. El
 * token se firma con una clave secreta compartida, de modo que su autenticidad e
 * integridad pueden verificarse en cada petición sin consultar la base de datos.</p>
 *
 * <p>La clave de firma se deriva de un secreto en Base64 y los parámetros de emisor
 * y caducidad se inyectan desde la configuración de la aplicación.</p>
 */
@Service
public class JwtService {

    private final SecretKey key;
    private final String issuer;
    private final long expirationMillis;

    public JwtService(
            @Value("${app.jwt.secret}") String secretBase64,
            @Value("${app.jwt.issuer}") String issuer,
            @Value("${app.jwt.expiration-minutes}") long expirationMinutes) {
        this.key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretBase64));
        this.issuer = issuer;
        this.expirationMillis = expirationMinutes * 60_000;
    }

    /**
     * Genera un token JWT firmado para el usuario indicado.
     *
     * <p>El token incluye el nombre de usuario como sujeto, el emisor configurado,
     * el rol como <em>claim</em> personalizado y las marcas de emisión y expiración.</p>
     *
     * @param username nombre de usuario que se establece como sujeto del token
     * @param rol      rol del usuario que se incorpora como claim {@code rol}
     * @return el token JWT compactado y firmado, listo para enviarse al cliente
     */
    public String generarToken(String username, String rol) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(username)
                .issuer(issuer)
                .claim("rol", rol)
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + expirationMillis))
                .signWith(key)
                .compact();
    }

    /**
     * Comprueba si un token es válido verificando su firma, su emisor y su vigencia.
     *
     * @param token token JWT a verificar
     * @return {@code true} si el token es íntegro, fue emitido por este servicio y no
     *         ha expirado; {@code false} si la validación falla por cualquier motivo
     */
    public boolean esValido(String token) {
        try {
            parse(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extrae el nombre de usuario (sujeto) contenido en el token.
     *
     * @param token token JWT del que leer el sujeto
     * @return el nombre de usuario almacenado en el claim {@code sub}
     * @throws io.jsonwebtoken.JwtException si el token no puede analizarse o validarse
     */
    public String extraerUsername(String token) {
        return parse(token).getSubject();
    }

    /**
     * Extrae el rol contenido en el token.
     *
     * @param token token JWT del que leer el rol
     * @return el valor del claim {@code rol}
     * @throws io.jsonwebtoken.JwtException si el token no puede analizarse o validarse
     */
    public String extraerRol(String token) {
        return parse(token).get("rol", String.class);
    }

    public long getExpirationMillis() {
        return expirationMillis;
    }

    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
