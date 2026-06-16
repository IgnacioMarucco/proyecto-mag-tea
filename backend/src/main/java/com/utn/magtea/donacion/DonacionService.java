package com.utn.magtea.donacion;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.resources.preference.Preference;
import com.utn.magtea.common.PageResponse;
import com.utn.magtea.common.exception.BusinessRuleException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DonacionService {

    private final DonacionRepository repository;
    private final DonacionMapper mapper;

    @Value("${mp.access-token}")
    private String mpAccessToken;

    @Value("${mp.back-url-base}")
    private String mpBackUrlBase;

    // Vacío en dev local — el webhook no se puede testear sin ngrok (ngrok http 8080)
    @Value("${mp.notification-url:}")
    private String mpNotificationUrl;

    @Transactional
    public DonacionInitPointDTO iniciarDonacion(DonacionCreateDTO dto) {
        // Guardar primero para obtener el ID — se usa como externalReference en MP
        Donacion donacion = mapper.toEntity(dto);
        donacion.setEstado(EstadoDonacion.PENDIENTE);
        donacion = repository.save(donacion);

        try {
            MercadoPagoConfig.setAccessToken(mpAccessToken);

            String resultadoUrl = mpBackUrlBase + "/donacion/resultado";

            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success(resultadoUrl)
                    .failure(resultadoUrl)
                    .pending(resultadoUrl)
                    .build();

            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .title("Donación MAG-TEA")
                    .quantity(1)
                    .unitPrice(new BigDecimal(dto.monto()))
                    .currencyId("ARS")
                    .build();

            PreferenceRequest.PreferenceRequestBuilder builder = PreferenceRequest.builder()
                    .items(List.of(item))
                    .backUrls(backUrls)
                    .autoReturn("approved")
                    .externalReference(donacion.getId().toString());

            if (mpNotificationUrl != null && !mpNotificationUrl.isBlank()) {
                builder.notificationUrl(mpNotificationUrl + "/api/public/donaciones/webhook?source_news=webhooks");
            }

            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(builder.build());

            donacion.setMpPreferenceId(preference.getId());
            repository.save(donacion);

            return new DonacionInitPointDTO(preference.getInitPoint());

        } catch (com.mercadopago.exceptions.MPApiException e) {
            String detalle = e.getApiResponse() != null ? e.getApiResponse().getContent() : e.getMessage();
            throw new BusinessRuleException("Error MP [" + e.getStatusCode() + "]: " + detalle);
        } catch (Exception e) {
            throw new BusinessRuleException("Error al crear la preferencia de pago: " + e.getMessage());
        }
    }

    @Transactional
    public void procesarWebhook(Long paymentId) {
        try {
            MercadoPagoConfig.setAccessToken(mpAccessToken);
            com.mercadopago.client.payment.PaymentClient client =
                    new com.mercadopago.client.payment.PaymentClient();
            com.mercadopago.resources.payment.Payment payment = client.get(paymentId);

            // externalReference es el ID de nuestra Donacion, asignado al crear la preferencia
            String externalReference = payment.getExternalReference();
            String status = payment.getStatus();

            if (externalReference == null) return;

            repository.findById(Long.parseLong(externalReference)).ifPresent(donacion -> {
                donacion.setMpPaymentId(String.valueOf(paymentId));
                donacion.setEstado(switch (status) {
                    case "approved" -> EstadoDonacion.APROBADO;
                    case "rejected" -> EstadoDonacion.RECHAZADO;
                    case "cancelled" -> EstadoDonacion.CANCELADO;
                    default -> donacion.getEstado();
                });
                repository.save(donacion);
            });
        } catch (Exception e) {
            throw new BusinessRuleException("Error al procesar webhook de MP: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<DonacionResponseDTO> findAll(int page, int size) {
        Page<Donacion> result = repository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return new PageResponse<>(
                result.map(mapper::toDTO).getContent(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.getNumber(),
                result.getSize()
        );
    }
}
