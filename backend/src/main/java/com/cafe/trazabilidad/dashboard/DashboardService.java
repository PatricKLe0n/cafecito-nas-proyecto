package com.cafe.trazabilidad.dashboard;

import com.cafe.trazabilidad.dashboard.dto.ResumenResponse;
import com.cafe.trazabilidad.finca.FincaRepository;
import com.cafe.trazabilidad.lotetostado.EstadoTostado;
import com.cafe.trazabilidad.lotetostado.LoteTostado;
import com.cafe.trazabilidad.lotetostado.LoteTostadoRepository;
import com.cafe.trazabilidad.loteverde.EstadoLoteVerde;
import com.cafe.trazabilidad.loteverde.LoteVerdeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

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
}
