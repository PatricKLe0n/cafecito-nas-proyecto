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

/**
 * Servicio de negocio de los lotes tostados. Centraliza las reglas de la tanda
 * de tostado: validación de pesos, cálculo de merma y mantenimiento del stock
 * del lote de café verde de origen.
 * <p>
 * Reglas principales:
 * <ul>
 *   <li>El peso de salida debe ser estrictamente menor que el de entrada.</li>
 *   <li>El peso de entrada no puede superar el stock disponible del lote verde.</li>
 *   <li>La merma (%) = (entrada − salida) / entrada × 100, calculada en el servidor.</li>
 *   <li>Al crear se descuenta el stock del lote verde (si llega a 0 pasa a AGOTADO).</li>
 *   <li>Al anular se devuelve el stock descontado al lote verde.</li>
 *   <li>La edición no modifica pesos ni stock.</li>
 * </ul>
 */
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

    /**
     * Lista lotes tostados de forma paginada aplicando, como máximo, un filtro.
     * Tiene prioridad el filtro por estado, después por perfil y por último la
     * búsqueda por código; si no se indica ninguno se devuelven todos.
     *
     * @param q        texto a buscar (coincidencia parcial, sin distinguir mayúsculas) en el código
     * @param perfil   perfil de tueste por el que filtrar
     * @param estado   estado por el que filtrar
     * @param pageable parámetros de paginación y ordenación
     * @return página de lotes tostados que cumplen el filtro
     */
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

    /**
     * Obtiene un lote tostado por su identificador.
     *
     * @param id identificador del lote tostado
     * @return el lote tostado solicitado
     * @throws RecursoNoEncontradoException si no existe un lote con ese identificador
     */
    @Transactional(readOnly = true)
    public LoteTostadoResponse obtener(Long id) {
        return mapper.toResponse(buscar(id));
    }

    /**
     * Crea un lote tostado a partir de un lote de café verde existente, aplicando
     * las reglas de negocio de la tanda de tostado.
     * <p>
     * Valida que el peso de salida sea menor que el de entrada y que el peso de
     * entrada no supere el stock disponible del lote verde. Calcula la merma en
     * el servidor, marca el lote como REGISTRADO y descuenta del stock del lote
     * verde el peso de entrada (dejándolo AGOTADO si llega a 0).
     *
     * @param req datos de entrada del lote tostado
     * @return el lote tostado creado, con la merma calculada
     * @throws RecursoNoEncontradoException si el lote de café verde indicado no existe
     * @throws ReglaNegocioException        si el peso de salida no es menor que el de
     *                                      entrada o el peso de entrada supera el stock disponible
     */
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

    /**
     * Actualiza un lote tostado vigente. Solo modifica campos que no afectan al
     * stock (código, perfil de tueste y fecha de tueste); los pesos, la merma y
     * el stock del lote verde no se alteran. No se permite editar un lote anulado.
     *
     * @param id  identificador del lote tostado a actualizar
     * @param req datos de entrada (solo se aplican código, perfil y fecha)
     * @return el lote tostado actualizado
     * @throws RecursoNoEncontradoException si no existe un lote con ese identificador
     * @throws ReglaNegocioException        si el lote está anulado
     */
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

    /**
     * Anula un lote tostado vigente y devuelve al lote de café verde de origen el
     * peso de entrada que se descontó al crearlo. Si tras la devolución el stock
     * del lote verde es positivo, este vuelve al estado DISPONIBLE. El lote tostado
     * queda marcado como ANULADO. No se permite anular un lote ya anulado.
     *
     * @param id identificador del lote tostado a anular
     * @throws RecursoNoEncontradoException si no existe un lote con ese identificador
     * @throws ReglaNegocioException        si el lote ya está anulado
     */
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
