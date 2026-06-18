package com.utn.magtea.donacion;

import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import com.utn.magtea.common.PageResponse;
import com.utn.magtea.common.exception.BusinessRuleException;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DonacionServiceTest {

    @Mock private DonacionRepository repository;
    @Mock private DonacionMapper mapper;
    @Mock private MercadoPagoClientProvider mpClientProvider;

    @InjectMocks private DonacionService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "mpAccessToken", "test-token");
        ReflectionTestUtils.setField(service, "mpBackUrlBase", "http://localhost:4200");
        ReflectionTestUtils.setField(service, "mpNotificationUrl", "http://ngrok.io");
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

    @Test
    void deberia_iniciarDonacion_cuandoDatosValidos() throws Exception {
        var dto = new DonacionCreateDTO(5000L, "Juan Pérez", "juan@test.com");
        var donacion = new Donacion();
        donacion.setId(1L);
        donacion.setMonto(5000L);
        donacion.setEstado(EstadoDonacion.PENDIENTE);

        var prefMock = mock(Preference.class);
        when(prefMock.getId()).thenReturn("pref-1234");
        when(prefMock.getInitPoint()).thenReturn("https://www.mercadopago.com/init-point");

        var clientMock = mock(PreferenceClient.class);
        when(clientMock.create(any())).thenReturn(prefMock);

        when(mapper.toEntity(dto)).thenReturn(donacion);
        when(repository.save(any(Donacion.class))).thenReturn(donacion);
        when(mpClientProvider.getPreferenceClient()).thenReturn(clientMock);

        var result = service.iniciarDonacion(dto);

        assertThat(result.initPoint()).isEqualTo("https://www.mercadopago.com/init-point");
        verify(repository, times(2)).save(any(Donacion.class));
        assertThat(donacion.getMpPreferenceId()).isEqualTo("pref-1234");
    }

    @Test
    void deberia_lanzarExcepcion_cuandoMercadoPagoFalla() throws Exception {
        var dto = new DonacionCreateDTO(5000L, "Juan Pérez", "juan@test.com");
        var donacion = new Donacion();
        donacion.setId(1L);
        donacion.setMonto(5000L);
        donacion.setEstado(EstadoDonacion.PENDIENTE);

        var clientMock = mock(PreferenceClient.class);
        when(clientMock.create(any())).thenThrow(new RuntimeException("MP Error"));

        when(mapper.toEntity(dto)).thenReturn(donacion);
        when(repository.save(any(Donacion.class))).thenReturn(donacion);
        when(mpClientProvider.getPreferenceClient()).thenReturn(clientMock);

        assertThatThrownBy(() -> service.iniciarDonacion(dto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Error al crear la preferencia de pago: MP Error");
    }

    @Test
    void deberia_procesarWebhook_cuandoPagoAprobado() throws Exception {
        var paymentMock = mock(Payment.class);
        when(paymentMock.getExternalReference()).thenReturn("1");
        when(paymentMock.getStatus()).thenReturn("approved");

        var clientMock = mock(PaymentClient.class);
        when(clientMock.get(999L)).thenReturn(paymentMock);

        var donacion = new Donacion();
        donacion.setId(1L);
        donacion.setEstado(EstadoDonacion.PENDIENTE);

        when(mpClientProvider.getPaymentClient()).thenReturn(clientMock);
        when(repository.findById(1L)).thenReturn(Optional.of(donacion));

        service.procesarWebhook(999L);

        assertThat(donacion.getEstado()).isEqualTo(EstadoDonacion.APROBADO);
        assertThat(donacion.getMpPaymentId()).isEqualTo("999");
        verify(repository).save(donacion);
    }

    @Test
    void deberia_procesarWebhook_cuandoPagoRechazado() throws Exception {
        var paymentMock = mock(Payment.class);
        when(paymentMock.getExternalReference()).thenReturn("1");
        when(paymentMock.getStatus()).thenReturn("rejected");

        var clientMock = mock(PaymentClient.class);
        when(clientMock.get(999L)).thenReturn(paymentMock);

        var donacion = new Donacion();
        donacion.setId(1L);
        donacion.setEstado(EstadoDonacion.PENDIENTE);

        when(mpClientProvider.getPaymentClient()).thenReturn(clientMock);
        when(repository.findById(1L)).thenReturn(Optional.of(donacion));

        service.procesarWebhook(999L);

        assertThat(donacion.getEstado()).isEqualTo(EstadoDonacion.RECHAZADO);
        verify(repository).save(donacion);
    }
}
