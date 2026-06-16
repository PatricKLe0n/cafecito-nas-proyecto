package com.cafe.trazabilidad.lotetostado;

import com.cafe.trazabilidad.common.ReglaNegocioException;
import com.cafe.trazabilidad.loteverde.EstadoLoteVerde;
import com.cafe.trazabilidad.loteverde.LoteCafeVerde;
import com.cafe.trazabilidad.loteverde.LoteVerdeRepository;
import com.cafe.trazabilidad.lotetostado.dto.LoteTostadoRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoteTostadoServiceTest {

    @Mock LoteTostadoRepository repo;
    @Mock LoteVerdeRepository loteVerdeRepository;
    @Mock LoteTostadoMapper mapper;
    @InjectMocks LoteTostadoService service;

    private LoteCafeVerde verde;

    @BeforeEach
    void setUp() {
        verde = new LoteCafeVerde();
        verde.setId(1L);
        verde.setPesoKg(new BigDecimal("20.00"));
        verde.setEstado(EstadoLoteVerde.DISPONIBLE);
    }

    private LoteTostadoRequest req(String entrada, String salida) {
        return new LoteTostadoRequest("LT-X", 1L, PerfilTueste.MEDIUM,
                new BigDecimal(entrada), new BigDecimal(salida), LocalDateTime.now());
    }

    @Test
    void crearCalculaMermaYDescuentaStock() {
        when(loteVerdeRepository.findById(1L)).thenReturn(Optional.of(verde));
        when(repo.save(any(LoteTostado.class))).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<LoteTostado> captor = ArgumentCaptor.forClass(LoteTostado.class);
        service.crear(req("10.00", "8.50"));

        verify(repo).save(captor.capture());
        LoteTostado guardado = captor.getValue();
        // merma = (10 - 8.5) / 10 * 100 = 15.00
        assertThat(guardado.getMermaPorcentaje()).isEqualByComparingTo("15.00");
        assertThat(guardado.getEstado()).isEqualTo(EstadoTostado.REGISTRADO);
        // stock del verde: 20 - 10 = 10
        assertThat(verde.getPesoKg()).isEqualByComparingTo("10.00");
        assertThat(verde.getEstado()).isEqualTo(EstadoLoteVerde.DISPONIBLE);
    }

    @Test
    void crearAgotaElLoteVerdeCuandoStockLlegaACero() {
        verde.setPesoKg(new BigDecimal("10.00"));
        when(loteVerdeRepository.findById(1L)).thenReturn(Optional.of(verde));
        when(repo.save(any(LoteTostado.class))).thenAnswer(inv -> inv.getArgument(0));

        service.crear(req("10.00", "8.00"));

        assertThat(verde.getPesoKg()).isEqualByComparingTo("0.00");
        assertThat(verde.getEstado()).isEqualTo(EstadoLoteVerde.AGOTADO);
    }

    @Test
    void crearRechazaSiSalidaMayorOIgualQueEntrada() {
        when(loteVerdeRepository.findById(1L)).thenReturn(Optional.of(verde));
        assertThatThrownBy(() -> service.crear(req("10.00", "10.00")))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("salida");
        verify(repo, never()).save(any());
    }

    @Test
    void crearRechazaSiEntradaSuperaElStock() {
        when(loteVerdeRepository.findById(1L)).thenReturn(Optional.of(verde));
        assertThatThrownBy(() -> service.crear(req("25.00", "20.00")))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("stock");
        verify(repo, never()).save(any());
    }

    @Test
    void anularDevuelveStockYReactivaElLoteVerde() {
        verde.setPesoKg(new BigDecimal("0.00"));
        verde.setEstado(EstadoLoteVerde.AGOTADO);
        LoteTostado tostado = new LoteTostado();
        tostado.setId(5L);
        tostado.setLoteVerde(verde);
        tostado.setPesoEntradaKg(new BigDecimal("10.00"));
        tostado.setEstado(EstadoTostado.REGISTRADO);
        when(repo.findById(5L)).thenReturn(Optional.of(tostado));
        when(repo.save(any(LoteTostado.class))).thenAnswer(inv -> inv.getArgument(0));

        service.anular(5L);

        assertThat(tostado.getEstado()).isEqualTo(EstadoTostado.ANULADO);
        assertThat(verde.getPesoKg()).isEqualByComparingTo("10.00");
        assertThat(verde.getEstado()).isEqualTo(EstadoLoteVerde.DISPONIBLE);
    }

    @Test
    void anularDosVecesEsRechazado() {
        LoteTostado tostado = new LoteTostado();
        tostado.setId(5L);
        tostado.setLoteVerde(verde);
        tostado.setPesoEntradaKg(new BigDecimal("10.00"));
        tostado.setEstado(EstadoTostado.ANULADO);
        when(repo.findById(5L)).thenReturn(Optional.of(tostado));

        assertThatThrownBy(() -> service.anular(5L))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("anulado");
    }
}
