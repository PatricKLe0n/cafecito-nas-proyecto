package com.cafe.trazabilidad.loteverde;

import com.cafe.trazabilidad.common.PageResponse;
import com.cafe.trazabilidad.common.RecursoNoEncontradoException;
import com.cafe.trazabilidad.common.ReglaNegocioException;
import com.cafe.trazabilidad.finca.Finca;
import com.cafe.trazabilidad.finca.FincaRepository;
import com.cafe.trazabilidad.lotetostado.LoteTostadoRepository;
import com.cafe.trazabilidad.loteverde.dto.LoteVerdeRequest;
import com.cafe.trazabilidad.loteverde.dto.LoteVerdeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoteVerdeService {

    private final LoteVerdeRepository repo;
    private final FincaRepository fincaRepository;
    private final LoteTostadoRepository tostadoRepository;
    private final LoteVerdeMapper mapper;

    public LoteVerdeService(LoteVerdeRepository repo, FincaRepository fincaRepository,
                            LoteTostadoRepository tostadoRepository, LoteVerdeMapper mapper) {
        this.repo = repo;
        this.fincaRepository = fincaRepository;
        this.tostadoRepository = tostadoRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<LoteVerdeResponse> listar(String q, EstadoLoteVerde estado, Pageable pageable) {
        Page<LoteCafeVerde> page;
        if (estado != null) {
            page = repo.findByEstado(estado, pageable);
        } else if (q != null && !q.isBlank()) {
            page = repo.findByCodigoContainingIgnoreCase(q, pageable);
        } else {
            page = repo.findAll(pageable);
        }
        return PageResponse.from(page.map(mapper::toResponse));
    }

    @Transactional(readOnly = true)
    public LoteVerdeResponse obtener(Long id) {
        return mapper.toResponse(buscar(id));
    }

    @Transactional
    public LoteVerdeResponse crear(LoteVerdeRequest req) {
        if (repo.existsByCodigoIgnoreCase(req.codigo())) {
            throw new ReglaNegocioException("Ya existe un lote verde con el código " + req.codigo());
        }
        Finca finca = fincaRepository.findById(req.fincaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Finca", req.fincaId()));
        LoteCafeVerde lote = new LoteCafeVerde();
        aplicar(lote, req, finca);
        lote.setEstado(lote.getPesoKg().signum() > 0 ? EstadoLoteVerde.DISPONIBLE : EstadoLoteVerde.AGOTADO);
        return mapper.toResponse(repo.save(lote));
    }

    @Transactional
    public LoteVerdeResponse actualizar(Long id, LoteVerdeRequest req) {
        LoteCafeVerde lote = buscar(id);
        Finca finca = fincaRepository.findById(req.fincaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Finca", req.fincaId()));
        aplicar(lote, req, finca);
        return mapper.toResponse(repo.save(lote));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!repo.existsById(id)) {
            throw new RecursoNoEncontradoException("LoteCafeVerde", id);
        }
        if (tostadoRepository.existsByLoteVerdeId(id)) {
            throw new ReglaNegocioException("No se puede eliminar el lote verde: tiene lotes tostados asociados");
        }
        repo.deleteById(id);
    }

    private void aplicar(LoteCafeVerde lote, LoteVerdeRequest req, Finca finca) {
        lote.setCodigo(req.codigo());
        lote.setFinca(finca);
        lote.setPesoKg(req.pesoKg());
        lote.setHumedadPorcentaje(req.humedadPorcentaje());
        lote.setPuntajeSca(req.puntajeSca());
        lote.setFechaRecepcion(req.fechaRecepcion());
    }

    private LoteCafeVerde buscar(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("LoteCafeVerde", id));
    }
}
