package com.utn.magtea.donacion;

import com.utn.magtea.common.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DonacionServiceTest {

    @Mock private DonacionRepository repository;
    @Mock private DonacionMapper mapper;

    @InjectMocks private DonacionService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "mpAccessToken", "test-token");
        ReflectionTestUtils.setField(service, "mpBackUrlBase", "http://localhost:4200");
        ReflectionTestUtils.setField(service, "mpNotificationUrl", "");
    }

    @Test
    void deberia_retornarDonaciones_cuandoListar() {
        var donacion = new Donacion();
        donacion.setEstado(EstadoDonacion.PENDIENTE);
        var response = new DonacionResponseDTO(1L, 5000L, null, null, EstadoDonacion.PENDIENTE, LocalDateTime.now());

        when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(donacion)));
        when(mapper.toDTO(donacion)).thenReturn(response);

        PageResponse<DonacionResponseDTO> result = service.findAll(0, 20);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).estado()).isEqualTo(EstadoDonacion.PENDIENTE);
        assertThat(result.totalElements()).isEqualTo(1);
    }

    @Test
    void deberia_retornarPaginaVacia_cuandoNoHayDonaciones() {
        when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        PageResponse<DonacionResponseDTO> result = service.findAll(0, 20);

        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isEqualTo(0);
    }

    // Nota: iniciarDonacion y procesarWebhook llaman directamente a la API de Mercado Pago
    // mediante clientes instanciados inline (new PreferenceClient(), new PaymentClient()).
    // Esos métodos requieren tests de integración contra el sandbox de MP — no son testeables
    // con mocks de Mockito sin refactorizar la creación del cliente.
}
