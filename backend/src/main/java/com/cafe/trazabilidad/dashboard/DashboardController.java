package com.cafe.trazabilidad.dashboard;

import com.cafe.trazabilidad.dashboard.dto.PerfilTotal;
import com.cafe.trazabilidad.dashboard.dto.ResumenResponse;
import com.cafe.trazabilidad.dashboard.dto.StockPorFinca;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST que expone las métricas y agregaciones de solo lectura del
 * panel de control: resumen general, distribución por perfil y stock por finca.
 */
@Tag(name = "Dashboard", description = "Métricas y agregaciones de solo lectura")
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @Operation(summary = "Resumen general",
            description = "Devuelve los indicadores generales: lotes verdes disponibles, lotes tostados registrados, merma media y total de fincas.")
    @GetMapping("/resumen")
    public ResumenResponse resumen() {
        return service.resumen();
    }

    @Operation(summary = "Tostados por perfil",
            description = "Devuelve la distribución de lotes tostados registrados agrupada por perfil de tueste.")
    @GetMapping("/tostados-por-perfil")
    public List<PerfilTotal> tostadosPorPerfil() {
        return service.tostadosPorPerfil();
    }

    @Operation(summary = "Stock por finca",
            description = "Devuelve el stock de café verde (kg) agrupado por finca de origen, ordenado de mayor a menor.")
    @GetMapping("/stock-por-finca")
    public List<StockPorFinca> stockPorFinca() {
        return service.stockPorFinca();
    }
}
