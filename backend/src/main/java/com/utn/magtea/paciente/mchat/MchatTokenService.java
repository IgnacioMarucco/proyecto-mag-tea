package com.utn.magtea.paciente.mchat;

import com.utn.magtea.common.exception.ResourceNotFoundException;
import com.utn.magtea.paciente.Paciente;
import com.utn.magtea.paciente.PacienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MchatTokenService {

    private final PacienteRepository repository;
    private final Clock clock;

    @Value("${app.mchat.token-expiry-days:30}")
    private int tokenExpiryDays;

    @Transactional(readOnly = true)
    public MchatInfoDTO validarToken(String token) {
        Paciente p = repository.findByMchatToken(token)
                .filter(pac -> pac.getMchatTokenExpiry() != null
                        && pac.getMchatTokenExpiry().isAfter(LocalDateTime.now(clock)))
                .orElseThrow(() -> new ResourceNotFoundException("El enlace no es válido o ha expirado"));
        return new MchatInfoDTO(p.getId(), p.getNombreNino(), p.getApellidoNino());
    }

    /** Asigna UUID + expiry al paciente (sin guardar — el llamador guarda). */
    public void generarToken(Paciente paciente) {
        paciente.setMchatToken(UUID.randomUUID().toString());
        paciente.setMchatTokenExpiry(LocalDateTime.now(clock).plusDays(tokenExpiryDays));
    }

    /** Verifica que el paciente exista y esté activo. Lanza ResourceNotFoundException si no. */
    public void verificarActivo(Long pacienteId) {
        repository.findById(pacienteId)
                .filter(Paciente::isActivo)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Paciente con id " + pacienteId + " no existe"));
    }
}
