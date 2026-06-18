package com.utn.magtea.donacion;

import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.PreferenceClient;
import org.springframework.stereotype.Component;

@Component
public class MercadoPagoClientProvider {

    public PreferenceClient getPreferenceClient() {
        return new PreferenceClient();
    }

    public PaymentClient getPaymentClient() {
        return new PaymentClient();
    }
}
