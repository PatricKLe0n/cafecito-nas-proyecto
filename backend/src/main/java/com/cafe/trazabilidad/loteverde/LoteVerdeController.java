package com.cafe.trazabilidad.loteverde;

import com.cafe.trazabilidad.common.PageResponse;
import com.cafe.trazabilidad.loteverde.dto.LoteVerdeRequest;
import com.cafe.trazabilidad.loteverde.dto.LoteVerdeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST que expone el mantenimiento de los lotes de café verde (stock).
 * Delega la lógica de negocio en {@link LoteVerdeService}.
 */
@Tag(name = "Lotes de café verde", description = "Mantenimiento de lotes de café verde (stock)")
@RestController
@RequestMapping("/api/lotes-verdes")
public class LoteVerdeController {

    private final LoteVerdeService service;

    public LoteVerdeController(LoteVerdeService service) {
        this.service = service;
    }

    @Operation(summary = "Listar lotes de café verde paginados",
            description = "Devuelve los lotes de forma paginada, con filtro opcional por estado o búsqueda por código.")
    @GetMapping
    public PageResponse<LoteVerdeResponse> listar(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) EstadoLoteVerde estado,
            @PageableDefault(size = 10, sort = "codigo") Pageable pageable) {
        return service.listar(q, estado, pageable);
    }

    @Operation(summary = "Obtener lote de café verde por id",
            description = "Recupera el detalle de un lote de café verde a partir de su identificador.")
    @GetMapping("/{id}")
    public LoteVerdeResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @Operation(summary = "Crear lote de café verde",
            description = "Registra un nuevo lote de café verde. El código debe ser único y la finca debe existir.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LoteVerdeResponse crear(@Valid @RequestBody LoteVerdeRequest req) {
        return service.crear(req);
    }

    @Operation(summary = "Actualizar lote de café verde",
            description = "Actualiza los datos de un lote de café verde existente identificado por su id.")
    @PutMapping("/{id}")
    public LoteVerdeResponse actualizar(@PathVariable Long id, @Valid @RequestBody LoteVerdeRequest req) {
        return service.actualizar(id, req);
    }

    @Operation(summary = "Eliminar lote de café verde",
            description = "Elimina un lote de café verde. No se permite si tiene lotes tostados asociados.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}
