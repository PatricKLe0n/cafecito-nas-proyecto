package com.cafe.trazabilidad.lotetostado;

import com.cafe.trazabilidad.common.PageResponse;
import com.cafe.trazabilidad.lotetostado.dto.LoteTostadoRequest;
import com.cafe.trazabilidad.lotetostado.dto.LoteTostadoResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lotes-tostados")
public class LoteTostadoController {

    private final LoteTostadoService service;

    public LoteTostadoController(LoteTostadoService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<LoteTostadoResponse> listar(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) PerfilTueste perfil,
            @RequestParam(required = false) EstadoTostado estado,
            @PageableDefault(size = 10, sort = "fechaTueste") Pageable pageable) {
        return service.listar(q, perfil, estado, pageable);
    }

    @GetMapping("/{id}")
    public LoteTostadoResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LoteTostadoResponse crear(@Valid @RequestBody LoteTostadoRequest req) {
        return service.crear(req);
    }

    @PutMapping("/{id}")
    public LoteTostadoResponse actualizar(@PathVariable Long id, @Valid @RequestBody LoteTostadoRequest req) {
        return service.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void anular(@PathVariable Long id) {
        service.anular(id);
    }
}
