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

/**
 * Servicio que concentra la lógica de negocio transaccional de los lotes de café verde (stock).
 *
 * <p>Garantiza las reglas del dominio: unicidad del código de lote, existencia de la finca de
 * procedencia, derivación del estado de disponibilidad a partir del peso y la prohibición de
 * eliminar un lote que ya tenga lotes tostados asociados.</p>
 */
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

    /**
     * Lista los lotes de café verde de forma paginada. El filtro por {@code estado} tiene
     * prioridad sobre la búsqueda por código; si no se indica estado y se proporciona un término
     * de búsqueda, filtra por coincidencia parcial (sin distinguir mayúsculas) en el código.
     *
     * @param estado   estado de disponibilidad por el que filtrar; opcional
     * @param q        término de búsqueda por código; opcional, ignorado si se indica estado
     * @param pageable parámetros de paginación y ordenación
     * @return página de lotes de café verde convertidos a su representación de salida
     */
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

    /**
     * Obtiene un lote de café verde por su identificador.
     *
     * @param id identificador del lote
     * @return el lote encontrado en su representación de salida
     * @throws RecursoNoEncontradoException si no existe ningún lote con ese identificador
     */
    @Transactional(readOnly = true)
    public LoteVerdeResponse obtener(Long id) {
        return mapper.toResponse(buscar(id));
    }

    /**
     * Crea un nuevo lote de café verde.
     *
     * <p>Reglas de negocio: el código del lote debe ser único (sin distinguir mayúsculas) y la
     * finca de procedencia debe existir. El estado inicial se deriva del peso: {@code DISPONIBLE}
     * si el peso es positivo, {@code AGOTADO} en caso contrario.</p>
     *
     * @param req datos del lote a crear
     * @return el lote creado en su representación de salida
     * @throws ReglaNegocioException        si ya existe un lote con el mismo código
     * @throws RecursoNoEncontradoException si la finca indicada no existe
     */
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

    /**
     * Actualiza los datos de un lote de café verde existente.
     *
     * <p>Regla de negocio: la finca de procedencia indicada debe existir.</p>
     *
     * @param id  identificador del lote a actualizar
     * @param req nuevos datos del lote
     * @return el lote actualizado en su representación de salida
     * @throws RecursoNoEncontradoException si el lote o la finca indicada no existen
     */
    @Transactional
    public LoteVerdeResponse actualizar(Long id, LoteVerdeRequest req) {
        LoteCafeVerde lote = buscar(id);
        Finca finca = fincaRepository.findById(req.fincaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Finca", req.fincaId()));
        aplicar(lote, req, finca);
        return mapper.toResponse(repo.save(lote));
    }

    /**
     * Elimina un lote de café verde.
     *
     * <p>Regla de negocio: no se permite eliminar un lote que tenga lotes tostados asociados,
     * para preservar la integridad de la trazabilidad (conflicto 409).</p>
     *
     * @param id identificador del lote a eliminar
     * @throws RecursoNoEncontradoException si no existe ningún lote con ese identificador
     * @throws ReglaNegocioException        si el lote tiene lotes tostados asociados
     */
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
