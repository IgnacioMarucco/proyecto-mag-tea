package com.utn.magtea.donacion;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import com.utn.magtea.common.ApiConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(ApiConstants.V1 + "/public/donaciones")
@Tag(name = "Donaciones")
@RequiredArgsConstructor
public class DonacionPublicController {

    private final DonacionService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Iniciar donación con Mercado Pago Checkout Pro (acceso público)")
    public DonacionInitPointDTO iniciar(@RequestBody @Valid DonacionCreateDTO dto) {
        return service.iniciarDonacion(dto);
    }

    // MP envía type="payment" y data.id=<paymentId>. Se consulta la Payment API para obtener
    // preferenceId y status reales. No testeable en localhost sin ngrok (ngrok http 8080).
    // Siempre devuelve 200 — MP reintenta si recibe cualquier otro status.
    @PostMapping("/webhook")
    @Operation(summary = "Webhook de notificación de Mercado Pago")
    public void webhook(@RequestBody Map<String, Object> body) {
        try {
            String type = String.valueOf(body.getOrDefault("type", body.getOrDefault("topic", "")));
            if ("payment".equals(type)) {
                Object dataObj = body.get("data");
                if (dataObj instanceof Map<?, ?> data) {
                    Object idObj = data.get("id");
                    if (idObj != null) {
                        service.procesarWebhook(Long.parseLong(String.valueOf(idObj)));
                    }
                }
            }
        } catch (Exception ignored) {
            // Absorber — MP no debe recibir errores o reintentará indefinidamente
        }
    }
}
