package com.cafe.trazabilidad.loteverde;

import com.cafe.trazabilidad.common.PageResponse;
import com.cafe.trazabilidad.loteverde.dto.LoteVerdeRequest;
import com.cafe.trazabilidad.loteverde.dto.LoteVerdeResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lotes-verdes")
public class LoteVerdeController {

    private final LoteVerdeService service;

    public LoteVerdeController(LoteVerdeService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<LoteVerdeResponse> listar(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) EstadoLoteVerde estado,
            @PageableDefault(size = 10, sort = "codigo") Pageable pageable) {
        return service.listar(q, estado, pageable);
    }

    @GetMapping("/{id}")
    public LoteVerdeResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LoteVerdeResponse crear(@Valid @RequestBody LoteVerdeRequest req) {
        return service.crear(req);
    }

    @PutMapping("/{id}")
    public LoteVerdeResponse actualizar(@PathVariable Long id, @Valid @RequestBody LoteVerdeRequest req) {
        return service.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}
