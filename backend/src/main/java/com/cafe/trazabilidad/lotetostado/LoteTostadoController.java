package com.cafe.trazabilidad.lotetostado;

import com.cafe.trazabilidad.common.PageResponse;
import com.cafe.trazabilidad.lotetostado.dto.LoteTostadoRequest;
import com.cafe.trazabilidad.lotetostado.dto.LoteTostadoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para el mantenimiento de lotes tostados (tandas de tostado).
 * Expone las operaciones de consulta, creación, edición y anulación.
 */
@Tag(name = "Lotes tostados", description = "Mantenimiento de tandas de tostado (merma y stock)")
@RestController
@RequestMapping("/api/lotes-tostados")
public class LoteTostadoController {

    private final LoteTostadoService service;

    public LoteTostadoController(LoteTostadoService service) {
        this.service = service;
    }

    @Operation(summary = "Listar lotes tostados",
            description = "Devuelve los lotes tostados de forma paginada. Admite un filtro por código (q), perfil o estado.")
    @GetMapping
    public PageResponse<LoteTostadoResponse> listar(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) PerfilTueste perfil,
            @RequestParam(required = false) EstadoTostado estado,
            @PageableDefault(size = 10, sort = "fechaTueste") Pageable pageable) {
        return service.listar(q, perfil, estado, pageable);
    }

    @Operation(summary = "Obtener un lote tostado",
            description = "Recupera el detalle de un lote tostado por su identificador.")
    @GetMapping("/{id}")
    public LoteTostadoResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @Operation(summary = "Crear un lote tostado",
            description = "Registra una nueva tanda de tostado: valida los pesos, calcula la merma y descuenta el stock del lote verde de origen.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LoteTostadoResponse crear(@Valid @RequestBody LoteTostadoRequest req) {
        return service.crear(req);
    }

    @Operation(summary = "Actualizar un lote tostado",
            description = "Edita un lote tostado vigente. Solo modifica código, perfil y fecha; no altera pesos, merma ni stock.")
    @PutMapping("/{id}")
    public LoteTostadoResponse actualizar(@PathVariable Long id, @Valid @RequestBody LoteTostadoRequest req) {
        return service.actualizar(id, req);
    }

    @Operation(summary = "Anular un lote tostado",
            description = "Anula la tanda de tostado (no la elimina) y devuelve al lote verde de origen el stock descontado al crearla.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void anular(@PathVariable Long id) {
        service.anular(id);
    }
}
