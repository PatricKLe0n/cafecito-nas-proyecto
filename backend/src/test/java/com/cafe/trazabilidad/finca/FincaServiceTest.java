package com.cafe.trazabilidad.finca;

import com.cafe.trazabilidad.common.RecursoNoEncontradoException;
import com.cafe.trazabilidad.common.ReglaNegocioException;
import com.cafe.trazabilidad.loteverde.LoteVerdeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FincaServiceTest {

    @Mock FincaRepository fincaRepository;
    @Mock LoteVerdeRepository loteVerdeRepository;
    @Mock FincaMapper mapper;
    @InjectMocks FincaService service;

    @Test
    void eliminarLanzaConflictoSiTieneLotesVerdes() {
        when(fincaRepository.existsById(1L)).thenReturn(true);
        when(loteVerdeRepository.existsByFincaId(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.eliminar(1L))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("lotes");
        verify(fincaRepository, never()).deleteById(anyLong());
    }

    @Test
    void eliminarBorraSiNoTieneDependencias() {
        when(fincaRepository.existsById(1L)).thenReturn(true);
        when(loteVerdeRepository.existsByFincaId(1L)).thenReturn(false);

        service.eliminar(1L);

        verify(fincaRepository).deleteById(1L);
    }

    @Test
    void obtenerInexistenteLanzaNoEncontrado() {
        when(fincaRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.obtener(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }
}
