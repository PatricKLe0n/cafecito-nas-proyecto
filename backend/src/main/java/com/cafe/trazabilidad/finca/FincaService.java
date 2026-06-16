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

/**
 * Servicio que concentra la lógica de negocio transaccional de las fincas de origen.
 *
 * <p>Garantiza las reglas del dominio: unicidad del nombre de finca y la prohibición de
 * eliminar una finca que aún tenga lotes de café verde asociados.</p>
 */
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

    /**
     * Lista las fincas de forma paginada. Si se proporciona un término de búsqueda, filtra
     * por coincidencia parcial (sin distinguir mayúsculas) en el nombre o la región.
     *
     * @param q        término de búsqueda opcional; si es {@code null} o vacío se devuelven todas
     * @param pageable parámetros de paginación y ordenación
     * @return página de fincas convertidas a su representación de salida
     */
    @Transactional(readOnly = true)
    public PageResponse<FincaResponse> listar(String q, Pageable pageable) {
        Page<Finca> page = (q == null || q.isBlank())
                ? fincaRepository.findAll(pageable)
                : fincaRepository.findByNombreContainingIgnoreCaseOrRegionContainingIgnoreCase(q, q, pageable);
        return PageResponse.from(page.map(mapper::toResponse));
    }

    /**
     * Obtiene una finca por su identificador.
     *
     * @param id identificador de la finca
     * @return la finca encontrada en su representación de salida
     * @throws RecursoNoEncontradoException si no existe ninguna finca con ese identificador
     */
    @Transactional(readOnly = true)
    public FincaResponse obtener(Long id) {
        return mapper.toResponse(buscar(id));
    }

    /**
     * Crea una nueva finca.
     *
     * <p>Regla de negocio: el nombre de la finca debe ser único; si ya existe otra finca
     * con el mismo nombre (sin distinguir mayúsculas) la operación se rechaza.</p>
     *
     * @param req datos de la finca a crear
     * @return la finca creada en su representación de salida
     * @throws ReglaNegocioException si ya existe una finca con el mismo nombre
     */
    @Transactional
    public FincaResponse crear(FincaRequest req) {
        if (fincaRepository.existsByNombreIgnoreCase(req.nombre())) {
            throw new ReglaNegocioException("Ya existe una finca con el nombre " + req.nombre());
        }
        Finca guardada = fincaRepository.save(mapper.toEntity(req));
        return mapper.toResponse(guardada);
    }

    /**
     * Actualiza los datos de una finca existente.
     *
     * @param id  identificador de la finca a actualizar
     * @param req nuevos datos de la finca
     * @return la finca actualizada en su representación de salida
     * @throws RecursoNoEncontradoException si no existe ninguna finca con ese identificador
     */
    @Transactional
    public FincaResponse actualizar(Long id, FincaRequest req) {
        Finca finca = buscar(id);
        mapper.update(finca, req);
        return mapper.toResponse(fincaRepository.save(finca));
    }

    /**
     * Elimina una finca.
     *
     * <p>Regla de negocio: no se permite eliminar una finca que tenga lotes de café verde
     * asociados, para preservar la integridad de la trazabilidad (conflicto 409).</p>
     *
     * @param id identificador de la finca a eliminar
     * @throws RecursoNoEncontradoException si no existe ninguna finca con ese identificador
     * @throws ReglaNegocioException        si la finca tiene lotes de café verde asociados
     */
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
