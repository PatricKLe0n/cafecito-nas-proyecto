package com.cafe.trazabilidad.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Reenvía las rutas de cliente de Angular a index.html para que el refresco o el
 * acceso directo a una URL del SPA (p. ej. /dashboard) funcionen. Las llamadas a
 * /api/** y los recursos estáticos (con extensión) no pasan por aquí.
 */
@Controller
public class SpaForwardController {

    @GetMapping({"/login", "/dashboard", "/fincas", "/lotes-verdes", "/lotes-tostados"})
    public String forward() {
        return "forward:/index.html";
    }
}
