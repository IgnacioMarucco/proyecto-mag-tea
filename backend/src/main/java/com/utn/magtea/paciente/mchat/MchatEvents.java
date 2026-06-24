package com.utn.magtea.paciente.mchat;

public final class MchatEvents {
    private MchatEvents() {}

    /** Publicado por MchatService después de guardar respuestas del formulario público. */
    public record MchatFamiliaGuardadaEvent(Long pacienteId) {}

    /** Publicado por MchatService después de actualizar respuestas internamente. */
    public record MchatFamiliaActualizadaEvent(Long pacienteId) {}

    /** Publicado después de guardar el paciente con el nuevo token. El mail se envía post-commit. */
    public record MchatEnviadoEvent(String correo, String nombreTutor,
                                    String apellidoNino, String nombreNino, String token) {}
}
