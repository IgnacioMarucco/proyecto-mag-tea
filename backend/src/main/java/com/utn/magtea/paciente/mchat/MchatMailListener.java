package com.utn.magtea.paciente.mchat;

import com.utn.magtea.common.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class MchatMailListener {

    private final MailService mailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMchatEnviado(MchatEvents.MchatEnviadoEvent event) {
        log.debug("Enviando mail M-CHAT a {}", event.correo());
        try {
            mailService.enviarLinkMchat(event.correo(), event.nombreTutor(),
                    event.apellidoNino(), event.nombreNino(), event.token());
        } catch (Exception e) {
            log.warn("No se pudo enviar mail M-CHAT a {}: {}", event.correo(), e.getMessage());
        }
    }
}
