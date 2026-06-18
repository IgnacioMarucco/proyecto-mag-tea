package com.utn.magtea.suero;

import com.utn.magtea.caja.Caja;
import com.utn.magtea.caja.CajaRepository;
import com.utn.magtea.common.exception.BusinessRuleException;
import com.utn.magtea.common.exception.ResourceNotFoundException;
import com.utn.magtea.paciente.Paciente;
import com.utn.magtea.paciente.PacienteEstado;
import com.utn.magtea.paciente.PacienteRepository;
import com.utn.magtea.paciente.TipoPaciente;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SueroServiceTest {

    @Mock private SueroRepository repository;
    @Mock private SueroMapper mapper;
    @Mock private PacienteRepository pacienteRepository;
    @Mock private CajaRepository cajaRepository;

    @InjectMocks private SueroService service;

    @Test
    void deberia_crearSuero_cuandoDatosValidos() {
        var dto = new SueroCreateDTO(1L, 1L, "A1,A2", LocalDate.now(), 2.0, 1500.0);
        var paciente = new Paciente();
        paciente.setId(1L);
        paciente.setActivo(true);
        paciente.setTipoPaciente(TipoPaciente.PROBLEMA);

        var caja = new Caja();
        caja.setId(1L);
        caja.setActivo(true);

        var suero = new Suero();
        suero.setPaciente(paciente);
        suero.setCaja(caja);
        suero.setTubos("A1,A2");
        suero.setFechaExtraccion(dto.fechaExtraccion());
        suero.setCantidadTotal(2.0);
        suero.setValorAnticuerpos(1500.0);
        suero.setRango(1);

        var response = new SueroResponseDTO(
                1L, 1L, "Paciente Test", 1L, "A1,A2", dto.fechaExtraccion(),
                2.0, 0.0, 2.0, 1500.0, 1, SueroUso.PROBLEMA,
                true, null
        );

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(repository.existsByPacienteIdAndActivoTrue(1L)).thenReturn(false);
        when(cajaRepository.findById(1L)).thenReturn(Optional.of(caja));
        when(mapper.toEntity(dto)).thenReturn(suero);
        when(repository.save(suero)).thenReturn(suero);
        when(mapper.toDTO(suero)).thenReturn(response);

        var result = service.create(dto);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.rango()).isEqualTo(1);
        assertThat(paciente.getEstadoClinico()).isEqualTo(PacienteEstado.EXTRACCION_REALIZADA);
        verify(pacienteRepository).save(paciente);
        verify(repository).save(suero);
    }

    @Test
    void deberia_actualizarEstadoClinicoAExtraccionRealizada_cuandoCreaSuero() {
        var dto = new SueroCreateDTO(1L, 1L, "A1", LocalDate.now(), 1.0, 0.0);
        var paciente = new Paciente();
        paciente.setId(1L);
        paciente.setActivo(true);
        paciente.setTipoPaciente(TipoPaciente.CONTROL);

        var caja = new Caja();
        caja.setId(1L);
        caja.setActivo(true);

        var suero = new Suero();
        suero.setPaciente(paciente);
        suero.setCaja(caja);

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(repository.existsByPacienteIdAndActivoTrue(1L)).thenReturn(false);
        when(cajaRepository.findById(1L)).thenReturn(Optional.of(caja));
        when(mapper.toEntity(dto)).thenReturn(suero);
        when(repository.save(suero)).thenReturn(suero);

        service.create(dto);

        assertThat(paciente.getEstadoClinico()).isEqualTo(PacienteEstado.EXTRACCION_REALIZADA);
        verify(pacienteRepository).save(paciente);
    }

    @Test
    void deberia_calcularRango0_cuandoBTUMenorOIgual1313() {
        assertThat(SueroRangoUtil.calcularRango(0.0)).isEqualTo(0);
        assertThat(SueroRangoUtil.calcularRango(1313.0)).isEqualTo(0);
    }

    @Test
    void deberia_calcularRango1_cuandoBTUEntre1314y2500() {
        assertThat(SueroRangoUtil.calcularRango(1314.0)).isEqualTo(1);
        assertThat(SueroRangoUtil.calcularRango(2500.0)).isEqualTo(1);
    }

    @Test
    void deberia_calcularRango2_cuandoBTUEntre2501y8000() {
        assertThat(SueroRangoUtil.calcularRango(2501.0)).isEqualTo(2);
        assertThat(SueroRangoUtil.calcularRango(8000.0)).isEqualTo(2);
    }

    @Test
    void deberia_calcularRango3_cuandoBTUMayorA8000() {
        assertThat(SueroRangoUtil.calcularRango(8001.0)).isEqualTo(3);
    }

    @Test
    void deberia_lanzarBusinessRuleException_cuandoPacienteYaTieneSuero() {
        var dto = new SueroCreateDTO(1L, 1L, "A1", LocalDate.now(), 1.0, 100.0);
        var paciente = new Paciente();
        paciente.setId(1L);
        paciente.setActivo(true);

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(repository.existsByPacienteIdAndActivoTrue(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("El paciente ya tiene un suero registrado");
    }

    @Test
    void deberia_lanzarBusinessRuleException_cuandoPacienteControlConValorPositivo() {
        var dto = new SueroCreateDTO(1L, 1L, "A1", LocalDate.now(), 1.0, 500.0);
        var paciente = new Paciente();
        paciente.setId(1L);
        paciente.setActivo(true);
        paciente.setTipoPaciente(TipoPaciente.CONTROL);

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(repository.existsByPacienteIdAndActivoTrue(1L)).thenReturn(false);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Los pacientes caso control deben tener valor de anticuerpos igual a 0");
    }

    @Test
    void deberia_permitirSueroControl_cuandoPacienteControlConValorCero() {
        var dto = new SueroCreateDTO(1L, 1L, "A1", LocalDate.now(), 1.0, 0.0);
        var paciente = new Paciente();
        paciente.setId(1L);
        paciente.setActivo(true);
        paciente.setTipoPaciente(TipoPaciente.CONTROL);

        var caja = new Caja();
        caja.setId(1L);
        caja.setActivo(true);

        var suero = new Suero();
        suero.setPaciente(paciente);
        suero.setCaja(caja);

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(repository.existsByPacienteIdAndActivoTrue(1L)).thenReturn(false);
        when(cajaRepository.findById(1L)).thenReturn(Optional.of(caja));
        when(mapper.toEntity(dto)).thenReturn(suero);
        when(repository.save(suero)).thenReturn(suero);

        assertThatNoException().isThrownBy(() -> service.create(dto));
    }

    @Test
    @SuppressWarnings("unchecked")
    void deberia_calcularDisponibilidadPool_excluyendoSuerosAgotados() {
        var sueroAgotado = new Suero();
        sueroAgotado.setId(1L);
        sueroAgotado.setRango(1);
        sueroAgotado.setCantidadTotal(1.0);
        sueroAgotado.setCantidadUsada(1.0);

        var sueroDisponible = new Suero();
        sueroDisponible.setId(2L);
        sueroDisponible.setRango(1);
        sueroDisponible.setCantidadTotal(1.0);
        sueroDisponible.setCantidadUsada(0.5);

        var sueroRango2 = new Suero();
        sueroRango2.setId(3L);
        sueroRango2.setRango(2);
        sueroRango2.setCantidadTotal(0.5);
        sueroRango2.setCantidadUsada(0.1);

        when(repository.findAll(any(org.springframework.data.jpa.domain.Specification.class)))
                .thenReturn(List.of(sueroAgotado, sueroDisponible, sueroRango2));

        var result = service.getDisponibilidadPool();

        assertThat(result).hasSize(3);

        var dispRango1 = result.stream().filter(d -> d.rango() == 1).findFirst().orElseThrow();
        assertThat(dispRango1.cantidadSueros()).isEqualTo(1L);
        assertThat(dispRango1.mlDisponibles()).isEqualTo(0.5);
        assertThat(dispRango1.ratonesPosibles()).isEqualTo(2);

        var dispRango2 = result.stream().filter(d -> d.rango() == 2).findFirst().orElseThrow();
        assertThat(dispRango2.cantidadSueros()).isEqualTo(1L);
        assertThat(dispRango2.mlDisponibles()).isEqualTo(0.4);
        assertThat(dispRango2.ratonesPosibles()).isEqualTo(2);

        var dispRango3 = result.stream().filter(d -> d.rango() == 3).findFirst().orElseThrow();
        assertThat(dispRango3.cantidadSueros()).isEqualTo(0L);
        assertThat(dispRango3.mlDisponibles()).isEqualTo(0.0);
        assertThat(dispRango3.ratonesPosibles()).isEqualTo(0);
    }
}
