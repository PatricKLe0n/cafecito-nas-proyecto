package com.cafe.trazabilidad.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwt;

    @BeforeEach
    void setUp() {
        jwt = new JwtService(
            "Y2FmZS10cmF6YWJpbGlkYWQtc2VjcmV0by1kZW1vLTI1Ni1iaXRzLW1pbmltbw==",
            "trazabilidad-cafe", 60);
    }

    @Test
    void generaYValidaTokenConUsuarioYRol() {
        String token = jwt.generarToken("admin", "ADMIN");

        assertThat(jwt.esValido(token)).isTrue();
        assertThat(jwt.extraerUsername(token)).isEqualTo("admin");
        assertThat(jwt.extraerRol(token)).isEqualTo("ADMIN");
    }

    @Test
    void tokenManipuladoEsInvalido() {
        String token = jwt.generarToken("admin", "ADMIN");
        assertThat(jwt.esValido(token + "x")).isFalse();
    }
}
