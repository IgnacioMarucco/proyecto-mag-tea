package com.utn.magtea.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private MailService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "from", "noreply@magtea.com");
        ReflectionTestUtils.setField(service, "baseUrl", "http://localhost:4200");
    }

    @Test
    void deberia_enviarCorreoMchatConFormatoCorrecto() {
        String destinatario = "tutor@test.com";
        String nombreTutor = "Juan";
        String apellidoNino = "Pérez";
        String nombreNino = "Mateo";
        String token = "uuid-token-123";

        service.enviarLinkMchat(destinatario, nombreTutor, apellidoNino, nombreNino, token);

        var captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage mensaje = captor.getValue();
        assertThat(mensaje.getFrom()).isEqualTo("noreply@magtea.com");
        assertThat(mensaje.getTo()).containsExactly("tutor@test.com");
        assertThat(mensaje.getSubject()).isEqualTo("Formulario M-CHAT-R — Mateo Pérez");
        assertThat(mensaje.getText()).contains("Estimado/a Juan:")
                .contains("Mateo Pérez")
                .contains("http://localhost:4200/mchat/uuid-token-123")
                .contains("Este enlace expirará en 30 días.");
    }
}
