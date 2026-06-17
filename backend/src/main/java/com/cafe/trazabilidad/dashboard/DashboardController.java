package com.cafe.trazabilidad.dashboard;

import com.cafe.trazabilidad.dashboard.dto.PerfilTotal;
import com.cafe.trazabilidad.dashboard.dto.ResumenResponse;
import com.cafe.trazabilidad.dashboard.dto.StockPorFinca;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST que expone las métricas y agregaciones de solo lectura del
 * panel de control: resumen general, distribución por perfil y stock por finca.
 */
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

    @GetMapping("/stock-por-finca")
    public List<StockPorFinca> stockPorFinca() {
        return service.stockPorFinca();
    }
}
