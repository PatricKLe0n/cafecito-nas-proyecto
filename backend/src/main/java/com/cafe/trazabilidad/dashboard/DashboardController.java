package com.cafe.trazabilidad.dashboard;

import com.cafe.trazabilidad.dashboard.dto.PerfilTotal;
import com.cafe.trazabilidad.dashboard.dto.ResumenResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping("/resumen")
    public ResumenResponse resumen() {
        return service.resumen();
    }

    @GetMapping("/tostados-por-perfil")
    public List<PerfilTotal> tostadosPorPerfil() {
        return service.tostadosPorPerfil();
    }
}
