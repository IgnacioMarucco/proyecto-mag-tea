package com.utn.magtea.common;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Value("${app.base-url}")
    private String baseUrl;

    public void enviarLinkMchat(String destinatario, String nombreTutor,
                                String apellidoNino, String nombreNino,
                                String token) {
        String enlace = baseUrl + "/mchat/" + token;
        String cuerpo = """
                Estimado/a %s:

                Le enviamos el formulario M-CHAT-R para %s %s.
                Por favor complételo antes de la visita haciendo click en el siguiente enlace:

                %s

                Este enlace expirará en 30 días. Una vez que lo complete, el equipo MAG-TEA
                tendrá acceso a sus respuestas antes de la consulta.

                Muchas gracias por participar en el proyecto MAG-TEA.
                Centro Wernicke · CIQUIBIC · Córdoba
                """.formatted(nombreTutor, nombreNino, apellidoNino, enlace);

        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(from);
        mensaje.setTo(destinatario);
        mensaje.setSubject("Formulario M-CHAT-R — %s %s".formatted(nombreNino, apellidoNino));
        mensaje.setText(cuerpo);
        mailSender.send(mensaje);
    }
}
