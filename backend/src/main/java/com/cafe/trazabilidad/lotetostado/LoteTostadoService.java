package com.cafe.trazabilidad.lotetostado;

import com.cafe.trazabilidad.common.PageResponse;
import com.cafe.trazabilidad.common.RecursoNoEncontradoException;
import com.cafe.trazabilidad.common.ReglaNegocioException;
import com.cafe.trazabilidad.loteverde.EstadoLoteVerde;
import com.cafe.trazabilidad.loteverde.LoteCafeVerde;
import com.cafe.trazabilidad.loteverde.LoteVerdeRepository;
import com.cafe.trazabilidad.lotetostado.dto.LoteTostadoRequest;
import com.cafe.trazabilidad.lotetostado.dto.LoteTostadoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class LoteTostadoService {

    private final LoteTostadoRepository repo;
    private final LoteVerdeRepository loteVerdeRepository;
    private final LoteTostadoMapper mapper;

    public LoteTostadoService(LoteTostadoRepository repo, LoteVerdeRepository loteVerdeRepository,
                              LoteTostadoMapper mapper) {
        this.repo = repo;
        this.loteVerdeRepository = loteVerdeRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<LoteTostadoResponse> listar(String q, PerfilTueste perfil,
                                                    EstadoTostado estado, Pageable pageable) {
        Page<LoteTostado> page;
        if (estado != null) {
            page = repo.findByEstado(estado, pageable);
        } else if (perfil != null) {
            page = repo.findByPerfilTueste(perfil, pageable);
        } else if (q != null && !q.isBlank()) {
            page = repo.findByCodigoContainingIgnoreCase(q, pageable);
        } else {
            page = repo.findAll(pageable);
        }
        return PageResponse.from(page.map(mapper::toResponse));
    }

    @Transactional(readOnly = true)
    public LoteTostadoResponse obtener(Long id) {
        return mapper.toResponse(buscar(id));
    }

    @Transactional
    public LoteTostadoResponse crear(LoteTostadoRequest req) {
        LoteCafeVerde verde = loteVerdeRepository.findById(req.loteVerdeId())
                .orElseThrow(() -> new RecursoNoEncontradoException("LoteCafeVerde", req.loteVerdeId()));

        if (req.pesoSalidaKg().compareTo(req.pesoEntradaKg()) >= 0) {
            throw new ReglaNegocioException("El peso de salida debe ser menor que el de entrada");
        }
        if (req.pesoEntradaKg().compareTo(verde.getPesoKg()) > 0) {
            throw new ReglaNegocioException("El peso de entrada supera el stock disponible del lote verde ("
                    + verde.getPesoKg() + " kg)");
        }

        LoteTostado lote = new LoteTostado();
        lote.setCodigo(req.codigo());
        lote.setLoteVerde(verde);
        lote.setPerfilTueste(req.perfilTueste());
        lote.setPesoEntradaKg(req.pesoEntradaKg());
        lote.setPesoSalidaKg(req.pesoSalidaKg());
        lote.setMermaPorcentaje(calcularMerma(req.pesoEntradaKg(), req.pesoSalidaKg()));
        lote.setFechaTueste(req.fechaTueste());
        lote.setEstado(EstadoTostado.REGISTRADO);

        descontarStock(verde, req.pesoEntradaKg());
        return mapper.toResponse(repo.save(lote));
    }

    /** Solo edita campos que no afectan al stock (perfil, código, fecha). */
    @Transactional
    public LoteTostadoResponse actualizar(Long id, LoteTostadoRequest req) {
        LoteTostado lote = buscar(id);
        if (lote.getEstado() == EstadoTostado.ANULADO) {
            throw new ReglaNegocioException("No se puede editar un lote tostado anulado");
        }
        lote.setCodigo(req.codigo());
        lote.setPerfilTueste(req.perfilTueste());
        lote.setFechaTueste(req.fechaTueste());
        return mapper.toResponse(repo.save(lote));
    }

    @Transactional
    public void anular(Long id) {
        LoteTostado lote = buscar(id);
        if (lote.getEstado() == EstadoTostado.ANULADO) {
            throw new ReglaNegocioException("El lote ya está anulado");
        }
        LoteCafeVerde verde = lote.getLoteVerde();
        verde.setPesoKg(verde.getPesoKg().add(lote.getPesoEntradaKg()));
        if (verde.getPesoKg().signum() > 0) {
            verde.setEstado(EstadoLoteVerde.DISPONIBLE);
        }
        lote.setEstado(EstadoTostado.ANULADO);
        repo.save(lote);
    }

    private BigDecimal calcularMerma(BigDecimal entrada, BigDecimal salida) {
        return entrada.subtract(salida)
                .multiply(BigDecimal.valueOf(100))
                .divide(entrada, 2, RoundingMode.HALF_UP);
    }

    private void descontarStock(LoteCafeVerde verde, BigDecimal cantidad) {
        BigDecimal restante = verde.getPesoKg().subtract(cantidad);
        verde.setPesoKg(restante);
        if (restante.signum() <= 0) {
            verde.setEstado(EstadoLoteVerde.AGOTADO);
        }
    }

    private LoteTostado buscar(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("LoteTostado", id));
    }
}
