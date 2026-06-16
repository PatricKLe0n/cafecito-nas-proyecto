package com.cafe.trazabilidad.finca;

import com.cafe.trazabilidad.common.PageResponse;
import com.cafe.trazabilidad.finca.dto.FincaRequest;
import com.cafe.trazabilidad.finca.dto.FincaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST que expone el mantenimiento de las fincas de origen del café.
 * Delega la lógica de negocio en {@link FincaService}.
 */
@Tag(name = "Fincas", description = "Mantenimiento de fincas de origen")
@RestController
@RequestMapping("/api/fincas")
public class FincaController {

    private final FincaService service;

    public FincaController(FincaService service) {
        this.service = service;
    }

    @Operation(summary = "Listar fincas paginadas",
            description = "Devuelve las fincas de forma paginada, con búsqueda opcional por nombre o región.")
    @GetMapping
    public PageResponse<FincaResponse> listar(
            @RequestParam(required = false) String q,
            @PageableDefault(size = 10, sort = "nombre") Pageable pageable) {
        return service.listar(q, pageable);
    }

    @Operation(summary = "Obtener finca por id",
            description = "Recupera el detalle de una finca a partir de su identificador.")
    @GetMapping("/{id}")
    public FincaResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @Operation(summary = "Crear finca",
            description = "Registra una nueva finca de origen. El nombre debe ser único.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FincaResponse crear(@Valid @RequestBody FincaRequest req) {
        return service.crear(req);
    }

    @Operation(summary = "Actualizar finca",
            description = "Actualiza los datos de una finca existente identificada por su id.")
    @PutMapping("/{id}")
    public FincaResponse actualizar(@PathVariable Long id, @Valid @RequestBody FincaRequest req) {
        return service.actualizar(id, req);
    }

    @Operation(summary = "Eliminar finca",
            description = "Elimina una finca. No se permite si tiene lotes de café verde asociados.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}
