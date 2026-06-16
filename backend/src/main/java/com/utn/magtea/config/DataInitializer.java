package com.utn.magtea.config;

import com.utn.magtea.formulariointeres.ComoConocioProyecto;
import com.utn.magtea.formulariointeres.EstadoFormulario;
import com.utn.magtea.formulariointeres.FormularioInteres;
import com.utn.magtea.formulariointeres.FormularioInteresRepository;
import com.utn.magtea.paciente.*;
import com.utn.magtea.paciente.criterios.PacienteCriterios;
import com.utn.magtea.paciente.mchat.MchatFamilia;
import com.utn.magtea.paciente.mchat.MchatFamiliaRepository;
import com.utn.magtea.paciente.mchatseguimiento.MchatResultadoFinal;
import com.utn.magtea.profesional.Profesional;
import com.utn.magtea.profesional.ProfesionalRepository;
import com.utn.magtea.profesional.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final ProfesionalRepository profesionalRepository;
    private final FormularioInteresRepository formularioRepository;
    private final PacienteRepository pacienteRepository;
    private final MchatFamiliaRepository mchatFamiliaRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.init.admin-email:admin@magtea.com}")
    private String adminEmail;

    @Value("${app.init.admin-password:magtea2026}")
    private String adminPassword;

    @Value("${app.init.seed-investigador-email:investigador@magtea.com}")
    private String seedInvestigadorEmail;

    @Value("${app.init.seed-investigador-password:magtea2026}")
    private String seedInvestigadorPassword;

    @Value("${app.init.seed-secretaria-email:secretaria@magtea.com}")
    private String seedSecretariaEmail;

    @Value("${app.init.seed-secretaria-password:magtea2026}")
    private String seedSecretariaPassword;

    @Override
    public void run(ApplicationArguments args) {
        seedProfesionales();
        seedFormularios();
        seedPacientes();
    }

    private void seedProfesionales() {
        seedProfesional("Admin", "Sistema", adminEmail, adminPassword, "351-000-0000", Role.INVESTIGADOR_PRINCIPAL);
        seedProfesional("Ignacio", "Marucco", seedInvestigadorEmail, seedInvestigadorPassword, "351-000-0001", Role.INVESTIGADOR_PRINCIPAL);
        seedProfesional("Secretaria", "Demo", seedSecretariaEmail, seedSecretariaPassword, "351-000-0002", Role.SECRETARIA);
    }

    private void seedProfesional(String nombre, String apellido, String email, String password, String telefono, Role role) {
        if (profesionalRepository.findByEmail(email).isEmpty()) {
            var p = new Profesional();
            p.setNombre(nombre);
            p.setApellido(apellido);
            p.setEmail(email);
            p.setPassword(passwordEncoder.encode(password));
            p.setTelefono(telefono);
            p.setRole(role);
            profesionalRepository.save(p);
        }
    }

    private void seedFormularios() {
        if (formularioRepository.count() >= 25) return;

        var formularios = List.of(
            // --- PENDIENTE (13) ---
            f("García",      "Claudia",   "ignaciomarucco@gmail.com", "11-4523-8871",  "García",      "Matías",    LocalDate.of(2021, 3, 15),  ComoConocioProyecto.INSTAGRAM,                   "Lunes y miércoles",     LocalDate.of(2026, 5, 20), EstadoFormulario.PENDIENTE),
            f("López",       "Roberto",   "ignaciomarucco@gmail.com", "351-608-4412",  "López",       "Valentina", LocalDate.of(2022, 7, 20),  ComoConocioProyecto.SUGERIDO_MEDICO,              "Martes por la tarde",   LocalDate.of(2026, 5, 28), EstadoFormulario.PENDIENTE),
            f("Romero",      "Florencia", "ignaciomarucco@gmail.com", "11-3341-9920",  "Romero",      "Luca",      LocalDate.of(2020, 11, 5),  ComoConocioProyecto.SUGERIDO_EQUIPO_TERAPEUTICO,  "Cualquier día",         LocalDate.of(2026, 6, 3),  EstadoFormulario.PENDIENTE),
            f("Fernández",   "Marcela",   "ignaciomarucco@gmail.com", "351-712-0034",  "Fernández",   "Santiago",  LocalDate.of(2021, 9, 10),  ComoConocioProyecto.INSTAGRAM,                   "Lunes",                 LocalDate.of(2026, 5, 15), EstadoFormulario.PENDIENTE),
            f("Martínez",    "Pablo",     "ignaciomarucco@gmail.com", "11-5500-2233",  "Martínez",    "Sofía",     LocalDate.of(2022, 1, 25),  ComoConocioProyecto.SUGERIDO_PARTICIPANTE,        "Miércoles o jueves",    LocalDate.of(2026, 5, 18), EstadoFormulario.PENDIENTE),
            f("González",    "Andrea",    "ignaciomarucco@gmail.com", "351-489-6677",  "González",    "Tomás",     LocalDate.of(2020, 6, 12),  ComoConocioProyecto.SUGERIDO_MEDICO,              "Viernes",               LocalDate.of(2026, 5, 21), EstadoFormulario.PENDIENTE),
            f("Herrera",     "Jorge",     "ignaciomarucco@gmail.com", "11-4801-3322",  "Herrera",     "Emma",      LocalDate.of(2023, 2, 8),   ComoConocioProyecto.INSTAGRAM,                   "Cualquier día",         LocalDate.of(2026, 5, 22), EstadoFormulario.PENDIENTE),
            f("Morales",     "Silvana",   "ignaciomarucco@gmail.com", "351-601-5544",  "Morales",     "Franco",    LocalDate.of(2021, 12, 3),  ComoConocioProyecto.SUGERIDO_EQUIPO_TERAPEUTICO,  "Lunes y martes",        LocalDate.of(2026, 5, 30), EstadoFormulario.PENDIENTE),
            f("Díaz",        "Carolina",  "ignaciomarucco@gmail.com", "11-6200-8899",  "Díaz",        "Martina",   LocalDate.of(2022, 4, 17),  ComoConocioProyecto.SUGERIDO_MEDICO,              "Jueves",                LocalDate.of(2026, 6, 1),  EstadoFormulario.PENDIENTE),
            f("Pereyra",     "Gustavo",   "ignaciomarucco@gmail.com", "351-555-7766",  "Pereyra",     "Benjamín",  LocalDate.of(2020, 8, 29),  ComoConocioProyecto.INSTAGRAM,                   "Martes",                LocalDate.of(2026, 6, 2),  EstadoFormulario.PENDIENTE),
            f("Castro",      "Valeria",   "ignaciomarucco@gmail.com", "11-4923-1100",  "Castro",      "Agustina",  LocalDate.of(2021, 5, 14),  ComoConocioProyecto.SUGERIDO_PARTICIPANTE,        "Miércoles",             LocalDate.of(2026, 6, 4),  EstadoFormulario.PENDIENTE),
            f("Ruiz",        "Daniel",    "ignaciomarucco@gmail.com", "351-420-3355",  "Ruiz",        "Nicolás",   LocalDate.of(2023, 3, 22),  ComoConocioProyecto.SUGERIDO_MEDICO,              "Lunes a viernes",       LocalDate.of(2026, 6, 5),  EstadoFormulario.PENDIENTE),
            f("Sánchez",     "Natalia",   "ignaciomarucco@gmail.com", "11-5701-4466",  "Sánchez",     "Isabella",  LocalDate.of(2022, 10, 30), ComoConocioProyecto.INSTAGRAM,                   "Cualquier día",         LocalDate.of(2026, 6, 6),  EstadoFormulario.PENDIENTE),
            // --- CONTACTADO (7) ---
            f("Torres",      "Ricardo",   "ignaciomarucco@gmail.com", "351-300-9988",  "Torres",      "Camila",    LocalDate.of(2021, 7, 6),   ComoConocioProyecto.SUGERIDO_EQUIPO_TERAPEUTICO,  "Martes o miércoles",    LocalDate.of(2026, 5, 10), EstadoFormulario.CONTACTADO),
            f("Vargas",      "Mónica",    "ignaciomarucco@gmail.com", "11-4400-7755",  "Vargas",      "Lucas",     LocalDate.of(2020, 9, 18),  ComoConocioProyecto.SUGERIDO_MEDICO,              "Jueves",                LocalDate.of(2026, 5, 12), EstadoFormulario.CONTACTADO),
            f("Acosta",      "Fernando",  "ignaciomarucco@gmail.com", "351-611-2200",  "Acosta",      "Renata",    LocalDate.of(2022, 3, 5),   ComoConocioProyecto.INSTAGRAM,                   "Lunes",                 LocalDate.of(2026, 5, 14), EstadoFormulario.CONTACTADO),
            f("Medina",      "Patricia",  "ignaciomarucco@gmail.com", "11-5800-3344",  "Medina",      "Julián",    LocalDate.of(2021, 11, 20), ComoConocioProyecto.SUGERIDO_PARTICIPANTE,        "Viernes",               LocalDate.of(2026, 5, 16), EstadoFormulario.CONTACTADO),
            f("Ramos",       "Alberto",   "ignaciomarucco@gmail.com", "351-508-6611",  "Ramos",       "Victoria",  LocalDate.of(2020, 4, 9),   ComoConocioProyecto.SUGERIDO_MEDICO,              "Cualquier día",         LocalDate.of(2026, 5, 19), EstadoFormulario.CONTACTADO),
            f("Gutiérrez",   "Laura",     "ignaciomarucco@gmail.com", "11-6100-5577",  "Gutiérrez",   "Máximo",    LocalDate.of(2023, 1, 14),  ComoConocioProyecto.INSTAGRAM,                   "Martes",                LocalDate.of(2026, 5, 23), EstadoFormulario.CONTACTADO),
            f("Domínguez",   "Sergio",    "ignaciomarucco@gmail.com", "351-402-8800",  "Domínguez",   "Catalina",  LocalDate.of(2022, 6, 27),  ComoConocioProyecto.SUGERIDO_EQUIPO_TERAPEUTICO,  "Lunes a jueves",        LocalDate.of(2026, 5, 26), EstadoFormulario.CONTACTADO),
            // --- ADMITIDO (2) ---
            f("Flores",      "Cecilia",   "ignaciomarucco@gmail.com", "11-4700-1122",  "Flores",      "Mateo",     LocalDate.of(2021, 2, 18),  ComoConocioProyecto.SUGERIDO_MEDICO,              "Miércoles",             LocalDate.of(2026, 4, 20), EstadoFormulario.ADMITIDO),
            f("Jiménez",     "Héctor",    "ignaciomarucco@gmail.com", "351-620-4433",  "Jiménez",     "Valentina", LocalDate.of(2020, 7, 31),  ComoConocioProyecto.INSTAGRAM,                   "Jueves o viernes",      LocalDate.of(2026, 4, 25), EstadoFormulario.ADMITIDO),
            // --- DESCARTADO (2) ---
            f("Molina",      "Eduardo",   "ignaciomarucco@gmail.com", "11-5900-6688",  "Molina",      "Enzo",      LocalDate.of(2022, 8, 12),  ComoConocioProyecto.SUGERIDO_PARTICIPANTE,        "Lunes",                 LocalDate.of(2026, 4, 10), EstadoFormulario.DESCARTADO),
            f("Ortega",      "Graciela",  "ignaciomarucco@gmail.com", "351-315-7744",  "Ortega",      "Miranda",   LocalDate.of(2021, 4, 23),  ComoConocioProyecto.SUGERIDO_EQUIPO_TERAPEUTICO,  "Martes",                LocalDate.of(2026, 4, 15), EstadoFormulario.DESCARTADO)
        );

        formularioRepository.saveAll(formularios);
    }

    private void seedPacientes() {
        if (pacienteRepository.count() > 0) return;

        // PROBLEMA - APTO - ADMITIDO (M-CHAT pendiente)
        pacienteRepository.save(buildPaciente(
            "Flores", "Cecilia", "ignaciomarucco@gmail.com", "11-4700-1122",
            "Flores", "Mateo", LocalDate.of(2021, 2, 18), Sexo.MASCULINO,
            TipoPaciente.PROBLEMA, "SEED0001"
        ));

        // PROBLEMA - APTO - MCHAT_RESPONDIDO (score 12 → ALTO_RIESGO → POSITIVA)
        Paciente p2 = buildPaciente(
            "Jiménez", "Héctor", "ignaciomarucco@gmail.com", "351-620-4433",
            "Jiménez", "Valentina", LocalDate.of(2020, 7, 31), Sexo.FEMENINO,
            TipoPaciente.PROBLEMA, "SEED0002"
        );
        p2.setEstadoClinico(PacienteEstado.MCHAT_RESPONDIDO);
        Paciente savedP2 = pacienteRepository.save(p2);
        MchatFamilia familiaP2 = new MchatFamilia();
        familiaP2.setPaciente(savedP2);
        familiaP2.setScoreTotal(12);
        familiaP2.setResultadoFinal(MchatResultadoFinal.POSITIVA);
        mchatFamiliaRepository.save(familiaP2);

        // CONTROL - APTO - ADMITIDO (extracción pendiente de coordinar)
        pacienteRepository.save(buildPaciente(
            "Torres", "Ricardo", "ignaciomarucco@gmail.com", "351-300-9988",
            "Torres", "Camila", LocalDate.of(2021, 7, 6), Sexo.FEMENINO,
            TipoPaciente.CONTROL, "SEED0003"
        ));

        // CONTROL - APTO - EXTRACCION_PENDIENTE
        Paciente p4 = buildPaciente(
            "Medina", "Patricia", "ignaciomarucco@gmail.com", "11-5800-3344",
            "Medina", "Julián", LocalDate.of(2021, 11, 20), Sexo.MASCULINO,
            TipoPaciente.CONTROL, "SEED0004"
        );
        p4.setFechaExtraccion(LocalDate.of(2026, 7, 1));
        p4.setEstadoClinico(PacienteEstado.EXTRACCION_PENDIENTE);
        pacienteRepository.save(p4);
    }

    private Paciente buildPaciente(
            String apellidoTutor, String nombreTutor, String correo, String telefono,
            String apellidoNino, String nombreNino, LocalDate fechaNac, Sexo sexo,
            TipoPaciente tipo, String codigo) {
        Paciente p = new Paciente();
        p.setApellidoTutor(apellidoTutor);
        p.setNombreTutor(nombreTutor);
        p.setCorreoTutor(correo);
        p.setTelefono(telefono);
        p.setApellidoNino(apellidoNino);
        p.setNombreNino(nombreNino);
        p.setFechaNacimientoNino(fechaNac);
        p.setSexo(sexo);
        p.setTipoPaciente(tipo);
        p.setCodigoNumerico(codigo);
        p.setFechaContacto(LocalDate.of(2026, 4, 15));
        p.setFechaPrimeraVisita(LocalDateTime.of(2026, 4, 20, 10, 0));
        p.setConsentimientoFirmado(true);
        p.setEstadoClinico(PacienteEstado.ADMITIDO);

        PacienteCriterios c = new PacienteCriterios();
        c.setPaciente(p);
        c.setCriterioEdad(true);
        if (tipo == TipoPaciente.PROBLEMA) {
            c.setCriterioTEADSMV(true);
            c.setCriterioTGDDSMIV(true);
        }
        p.setCriterios(c);

        return p;
    }

    private FormularioInteres f(
            String apellidoTutor, String nombreTutor, String correo, String telefono,
            String apellidoNino, String nombreNino, LocalDate fechaNacNino,
            ComoConocioProyecto comoConocio, String diasDisponibles,
            LocalDate fechaContacto, EstadoFormulario estado) {
        var form = new FormularioInteres();
        form.setApellidoTutor(apellidoTutor);
        form.setNombreTutor(nombreTutor);
        form.setCorreoTutor(correo);
        form.setTelefono(telefono);
        form.setApellidoNino(apellidoNino);
        form.setNombreNino(nombreNino);
        form.setFechaNacimientoNino(fechaNacNino);
        form.setComoConocioProyecto(comoConocio);
        form.setDiasDisponibles(diasDisponibles);
        form.setFechaContacto(fechaContacto);
        form.setEstado(estado);
        return form;
    }

}
