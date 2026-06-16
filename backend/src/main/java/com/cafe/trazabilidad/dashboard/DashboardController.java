package com.cafe.trazabilidad.dashboard;

import com.cafe.trazabilidad.dashboard.dto.ResumenResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
