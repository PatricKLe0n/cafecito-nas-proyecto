package com.cafe.trazabilidad.dashboard;

import com.cafe.trazabilidad.dashboard.dto.PerfilTotal;
import com.cafe.trazabilidad.dashboard.dto.ResumenResponse;
import com.cafe.trazabilidad.dashboard.dto.StockPorFinca;
import com.cafe.trazabilidad.finca.FincaRepository;
import com.cafe.trazabilidad.lotetostado.EstadoTostado;
import com.cafe.trazabilidad.lotetostado.LoteTostado;
import com.cafe.trazabilidad.lotetostado.LoteTostadoRepository;
import com.cafe.trazabilidad.lotetostado.PerfilTueste;
import com.cafe.trazabilidad.loteverde.EstadoLoteVerde;
import com.cafe.trazabilidad.loteverde.LoteCafeVerde;
import com.cafe.trazabilidad.loteverde.LoteVerdeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio de solo lectura que calcula las métricas y agregaciones del panel de
 * control: el resumen general, la distribución de lotes tostados por perfil y el
 * stock de café verde agrupado por finca.
 */
@Service
public class DashboardService {

    private final FincaRepository fincaRepository;
    private final LoteVerdeRepository loteVerdeRepository;
    private final LoteTostadoRepository loteTostadoRepository;

    public DashboardService(FincaRepository fincaRepository, LoteVerdeRepository loteVerdeRepository,
                            LoteTostadoRepository loteTostadoRepository) {
        this.fincaRepository = fincaRepository;
        this.loteVerdeRepository = loteVerdeRepository;
        this.loteTostadoRepository = loteTostadoRepository;
    }

    /**
     * Calcula el resumen general del panel: número de lotes verdes disponibles,
     * lotes tostados registrados, merma media (%) de los lotes tostados registrados
     * y total de fincas.
     *
     * @return el resumen de indicadores generales
     */
    @Transactional(readOnly = true)
    public ResumenResponse resumen() {
        long verdesDisponibles = loteVerdeRepository.countByEstado(EstadoLoteVerde.DISPONIBLE);
        long tostadosRegistrados = loteTostadoRepository.countByEstado(EstadoTostado.REGISTRADO);
        long totalFincas = fincaRepository.count();

        List<LoteTostado> registrados = loteTostadoRepository.findAll().stream()
                .filter(t -> t.getEstado() == EstadoTostado.REGISTRADO)
                .toList();
        BigDecimal mermaMedia = registrados.isEmpty() ? BigDecimal.ZERO
                : registrados.stream().map(LoteTostado::getMermaPorcentaje)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(registrados.size()), 2, RoundingMode.HALF_UP);

        return new ResumenResponse(verdesDisponibles, tostadosRegistrados, mermaMedia, totalFincas);
    }

    /**
     * Calcula la distribución de lotes tostados registrados por perfil de tueste,
     * incluyendo todos los perfiles aunque su total sea cero.
     *
     * @return lista con el total de lotes registrados por cada perfil de tueste
     */
    @Transactional(readOnly = true)
    public List<PerfilTotal> tostadosPorPerfil() {
        return Arrays.stream(PerfilTueste.values())
                .map(p -> new PerfilTotal(p.name(),
                        loteTostadoRepository.countByPerfilTuesteAndEstado(p, EstadoTostado.REGISTRADO)))
                .toList();
    }

    /**
     * Calcula el stock de café verde (kg) agrupado por finca de origen, excluyendo
     * las fincas sin stock y ordenando el resultado de mayor a menor cantidad.
     *
     * @return lista de fincas con su stock de café verde, de mayor a menor
     */
    @Transactional(readOnly = true)
    public List<StockPorFinca> stockPorFinca() {
        Map<String, BigDecimal> porFinca = new LinkedHashMap<>();
        for (LoteCafeVerde l : loteVerdeRepository.findAll()) {
            porFinca.merge(l.getFinca().getNombre(), l.getPesoKg(), BigDecimal::add);
        }
        return porFinca.entrySet().stream()
                .filter(e -> e.getValue().signum() > 0)
                .map(e -> new StockPorFinca(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(StockPorFinca::stockKg).reversed())
                .toList();
    }
}
