package com.cafe.trazabilidad.finca;

import com.cafe.trazabilidad.common.PageResponse;
import com.cafe.trazabilidad.finca.dto.FincaRequest;
import com.cafe.trazabilidad.finca.dto.FincaResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST que expone el mantenimiento de las fincas de origen del café.
 * Delega la lógica de negocio en {@link FincaService}.
 */
@RestController
@RequestMapping("/api/fincas")
public class FincaController {

    private final FincaService service;

    public FincaController(FincaService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<FincaResponse> listar(
            @RequestParam(required = false) String q,
            @PageableDefault(size = 10, sort = "nombre") Pageable pageable) {
        return service.listar(q, pageable);
    }

    @GetMapping("/{id}")
    public FincaResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FincaResponse crear(@Valid @RequestBody FincaRequest req) {
        return service.crear(req);
    }

    @PutMapping("/{id}")
    public FincaResponse actualizar(@PathVariable Long id, @Valid @RequestBody FincaRequest req) {
        return service.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}
