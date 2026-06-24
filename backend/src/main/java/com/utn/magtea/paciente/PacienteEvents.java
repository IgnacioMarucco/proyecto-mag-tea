package com.utn.magtea.paciente;

public final class PacienteEvents {

    private PacienteEvents() {}

    /** Publicado por SueroService al registrar un suero exitosamente. */
    public record SueroRegistradoEvent(Long pacienteId) {}

    /** Publicado por SueroService al eliminar (soft-delete) un suero. */
    public record SueroEliminadoEvent(Long pacienteId) {}
}
