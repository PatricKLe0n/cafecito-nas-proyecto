package com.cafe.trazabilidad.finca;

import com.cafe.trazabilidad.common.PageResponse;
import com.cafe.trazabilidad.common.RecursoNoEncontradoException;
import com.cafe.trazabilidad.common.ReglaNegocioException;
import com.cafe.trazabilidad.finca.dto.FincaRequest;
import com.cafe.trazabilidad.finca.dto.FincaResponse;
import com.cafe.trazabilidad.loteverde.LoteVerdeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FincaService {

    private final FincaRepository fincaRepository;
    private final LoteVerdeRepository loteVerdeRepository;
    private final FincaMapper mapper;

    public FincaService(FincaRepository fincaRepository, LoteVerdeRepository loteVerdeRepository,
                        FincaMapper mapper) {
        this.fincaRepository = fincaRepository;
        this.loteVerdeRepository = loteVerdeRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<FincaResponse> listar(String q, Pageable pageable) {
        Page<Finca> page = (q == null || q.isBlank())
                ? fincaRepository.findAll(pageable)
                : fincaRepository.findByNombreContainingIgnoreCaseOrRegionContainingIgnoreCase(q, q, pageable);
        return PageResponse.from(page.map(mapper::toResponse));
    }

    @Transactional(readOnly = true)
    public FincaResponse obtener(Long id) {
        return mapper.toResponse(buscar(id));
    }

    @Transactional
    public FincaResponse crear(FincaRequest req) {
        if (fincaRepository.existsByNombreIgnoreCase(req.nombre())) {
            throw new ReglaNegocioException("Ya existe una finca con el nombre " + req.nombre());
        }
        Finca guardada = fincaRepository.save(mapper.toEntity(req));
        return mapper.toResponse(guardada);
    }

    @Transactional
    public FincaResponse actualizar(Long id, FincaRequest req) {
        Finca finca = buscar(id);
        mapper.update(finca, req);
        return mapper.toResponse(fincaRepository.save(finca));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!fincaRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Finca", id);
        }
        if (loteVerdeRepository.existsByFincaId(id)) {
            throw new ReglaNegocioException("No se puede eliminar la finca: tiene lotes de café verde asociados");
        }
        fincaRepository.deleteById(id);
    }

    private Finca buscar(Long id) {
        return fincaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Finca", id));
    }
}
