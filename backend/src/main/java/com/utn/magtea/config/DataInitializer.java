package com.utn.magtea.config;

import com.utn.magtea.formulariointeres.ComoConocioProyecto;
import com.utn.magtea.formulariointeres.EstadoFormulario;
import com.utn.magtea.formulariointeres.FormularioInteres;
import com.utn.magtea.formulariointeres.FormularioInteresRepository;
import com.utn.magtea.paciente.*;
import com.utn.magtea.paciente.cars.EvaluacionCars;
import com.utn.magtea.paciente.cars.EvaluacionCarsRepository;
import com.utn.magtea.paciente.criterios.Criterios;
import com.utn.magtea.paciente.mchat.MchatFamilia;
import com.utn.magtea.paciente.mchat.MchatFamiliaRepository;
import com.utn.magtea.paciente.mchat.MchatResultadoFinal;
import com.utn.magtea.paciente.mchat.MchatSeguimiento;
import com.utn.magtea.paciente.mchat.MchatSeguimientoRepository;
import com.utn.magtea.paciente.vineland.EvaluacionVineland;
import com.utn.magtea.paciente.vineland.EvaluacionVinelandRepository;
import com.utn.magtea.profesional.Profesional;
import com.utn.magtea.profesional.ProfesionalRepository;
import com.utn.magtea.profesional.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.utn.magtea.caja.Caja;
import com.utn.magtea.caja.CajaRepository;
import com.utn.magtea.suero.Suero;
import com.utn.magtea.suero.SueroRepository;
import com.utn.magtea.suero.SueroRangoUtil;
import com.utn.magtea.suero.SueroUso;
import com.utn.magtea.pool.Pool;
import com.utn.magtea.pool.PoolRepository;
import com.utn.magtea.pool.PoolSueroAporte;
import com.utn.magtea.pool.PoolSueroAporteRepository;
import com.utn.magtea.tubo.Tubo;
import com.utn.magtea.tubo.TipoTubo;
import com.utn.magtea.tubo.TuboRepository;
import com.utn.magtea.camada.Camada;
import com.utn.magtea.camada.CamadaRepository;
import com.utn.magtea.modeloanimal.ModeloAnimal;
import com.utn.magtea.modeloanimal.ModeloAnimalRepository;
import com.utn.magtea.modeloanimal.ModeloAnimalPoolAporte;
import com.utn.magtea.modeloanimal.ModeloAnimalPoolAporteRepository;
import com.utn.magtea.modeloanimal.SexoRaton;
import com.utn.magtea.modeloanimal.estudios.VocalizacionesUltrasonicas;
import com.utn.magtea.modeloanimal.estudios.TresCamaras;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final ProfesionalRepository profesionalRepository;
    private final FormularioInteresRepository formularioRepository;
    private final PacienteRepository pacienteRepository;
    private final MchatFamiliaRepository mchatFamiliaRepository;
    private final MchatSeguimientoRepository mchatSeguimientoRepository;
    private final EvaluacionCarsRepository evaluacionCarsRepository;
    private final EvaluacionVinelandRepository evaluacionVinelandRepository;
    private final PasswordEncoder passwordEncoder;
    private final CajaRepository cajaRepository;
    private final SueroRepository sueroRepository;
    private final TuboRepository tuboRepository;
    private final PoolRepository poolRepository;
    private final PoolSueroAporteRepository poolSueroAporteRepository;
    private final CamadaRepository camadaRepository;
    private final ModeloAnimalRepository modeloAnimalRepository;
    private final ModeloAnimalPoolAporteRepository modeloAnimalPoolAporteRepository;
    private final Clock clock;

    private static final Set<Integer> MCHAT_INVERTIDAS = Set.of(2, 5, 12);

    @Value("${app.init.admin-email:admin@magtea.com}")
    private String adminEmail;

    @Value("${app.init.admin-password:magtea2026}")
    private String adminPassword;

    @Value("${app.init.seed-investigador-email:investigador@magtea.com}")
    private String seedInvestigadorEmail;

    @Value("${app.init.seed-investigador-password:magtea2026}")
    private String seedInvestigadorPassword;

    @Value("${app.init.seed-medico-email:medico@magtea.com}")
    private String seedMedicoEmail;

    @Value("${app.init.seed-medico-password:magtea2026}")
    private String seedMedicoPassword;

    @Value("${app.init.seed-tecnico-email:tecnico@magtea.com}")
    private String seedTecnicoEmail;

    @Value("${app.init.seed-tecnico-password:magtea2026}")
    private String seedTecnicoPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedProfesionales();
        seedFormularios();
        // seedPacientes();
        // seedEvaluaciones();
        seedCajas();
        // seedSueros();
        // seedPools();
        // seedCamadas();
        // seedModelosAnimales();
    }

    // ===========================
    // PROFESIONALES
    // ===========================

    private void seedProfesionales() {
        seedProfesional("Admin",      "Sistema",  adminEmail,            adminPassword,            "351-000-0000", Role.INVESTIGADOR_PRINCIPAL);
        seedProfesional("Ignacio",    "Marucco",  seedInvestigadorEmail, seedInvestigadorPassword, "351-000-0001", Role.INVESTIGADOR_PRINCIPAL);
        seedProfesional("Médico",     "Demo",     seedMedicoEmail,       seedMedicoPassword,       "351-000-0002", Role.CUERPO_MEDICO);
        seedProfesional("Técnico",    "Demo",     seedTecnicoEmail,      seedTecnicoPassword,      "351-000-0003", Role.CUERPO_TECNICO);
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

    // ===========================
    // FORMULARIOS DE INTERÉS (40)
    // ===========================

    private void seedFormularios() {
        if (formularioRepository.count() >= 40) return;

        String em = "ignaciomarucco@gmail.com";

        var formularios = List.of(
            f("Flores",    "Cecilia",   em, "11-4700-1122",  "Flores",    "Mateo",     LocalDate.of(2021,  2, 18), ComoConocioProyecto.SUGERIDO_MEDICO,             "Miércoles",            LocalDate.of(2026, 1, 10), EstadoFormulario.ADMITIDO),
            f("Jiménez",   "Héctor",    em, "351-620-4433",  "Jiménez",   "Valentina", LocalDate.of(2020,  7, 31), ComoConocioProyecto.INSTAGRAM,                   "Jueves o viernes",     LocalDate.of(2026, 1, 15), EstadoFormulario.ADMITIDO),
            f("Torres",    "Ricardo",   em, "351-300-9988",  "Torres",    "Camila",    LocalDate.of(2021,  7,  6), ComoConocioProyecto.SUGERIDO_EQUIPO_TERAPEUTICO,  "Martes o miércoles",   LocalDate.of(2026, 1, 20), EstadoFormulario.ADMITIDO),
            f("Medina",    "Patricia",  em, "11-5800-3344",  "Medina",    "Julián",    LocalDate.of(2021, 11, 20), ComoConocioProyecto.SUGERIDO_PARTICIPANTE,        "Viernes",              LocalDate.of(2026, 1, 25), EstadoFormulario.ADMITIDO),
            f("Vargas",    "Mónica",    em, "11-4400-7755",  "Vargas",    "Lucas",     LocalDate.of(2020,  9, 18), ComoConocioProyecto.SUGERIDO_MEDICO,             "Jueves",               LocalDate.of(2026, 2,  1), EstadoFormulario.ADMITIDO),
            f("Acosta",    "Fernando",  em, "351-611-2200",  "Acosta",    "Renata",    LocalDate.of(2022,  3,  5), ComoConocioProyecto.INSTAGRAM,                   "Lunes",                LocalDate.of(2026, 2,  5), EstadoFormulario.ADMITIDO),
            f("Ramos",     "Alberto",   em, "351-508-6611",  "Ramos",     "Victoria",  LocalDate.of(2020,  4,  9), ComoConocioProyecto.SUGERIDO_MEDICO,             "Cualquier día",        LocalDate.of(2026, 2, 10), EstadoFormulario.ADMITIDO),
            f("Gutiérrez", "Laura",     em, "11-6100-5577",  "Gutiérrez", "Máximo",    LocalDate.of(2023,  1, 14), ComoConocioProyecto.INSTAGRAM,                   "Martes",               LocalDate.of(2026, 2, 14), EstadoFormulario.ADMITIDO),
            f("Domínguez", "Sergio",    em, "351-402-8800",  "Domínguez", "Catalina",  LocalDate.of(2022,  6, 27), ComoConocioProyecto.SUGERIDO_EQUIPO_TERAPEUTICO,  "Lunes a jueves",       LocalDate.of(2026, 2, 18), EstadoFormulario.ADMITIDO),
            f("Castro",    "Valeria",   em, "11-4923-1100",  "Castro",    "Agustina",  LocalDate.of(2021,  5, 14), ComoConocioProyecto.SUGERIDO_PARTICIPANTE,        "Miércoles",            LocalDate.of(2026, 2, 22), EstadoFormulario.ADMITIDO),
            f("Pereyra",   "Gustavo",   em, "351-555-7766",  "Pereyra",   "Benjamín",  LocalDate.of(2020,  8, 29), ComoConocioProyecto.INSTAGRAM,                   "Martes",               LocalDate.of(2026, 3,  1), EstadoFormulario.ADMITIDO),
            f("Ruiz",      "Daniel",    em, "351-420-3355",  "Ruiz",      "Nicolás",   LocalDate.of(2023,  3, 22), ComoConocioProyecto.SUGERIDO_MEDICO,             "Lunes a viernes",      LocalDate.of(2026, 3,  5), EstadoFormulario.ADMITIDO),
            f("Sánchez",   "Natalia",   em, "11-5701-4466",  "Sánchez",   "Isabella",  LocalDate.of(2022, 10, 30), ComoConocioProyecto.INSTAGRAM,                   "Cualquier día",        LocalDate.of(2026, 3,  8), EstadoFormulario.ADMITIDO),
            f("Herrera",   "Jorge",     em, "11-4801-3322",  "Herrera",   "Emma",      LocalDate.of(2023,  2,  8), ComoConocioProyecto.INSTAGRAM,                   "Cualquier día",        LocalDate.of(2026, 3, 12), EstadoFormulario.ADMITIDO),
            f("Morales",   "Silvana",   em, "351-601-5544",  "Morales",   "Franco",    LocalDate.of(2021, 12,  3), ComoConocioProyecto.SUGERIDO_EQUIPO_TERAPEUTICO,  "Lunes y martes",       LocalDate.of(2026, 3, 15), EstadoFormulario.ADMITIDO),
            f("González",  "Andrea",    em, "351-489-6677",  "González",  "Tomás",     LocalDate.of(2020,  6, 12), ComoConocioProyecto.SUGERIDO_MEDICO,             "Viernes",              LocalDate.of(2026, 3, 18), EstadoFormulario.ADMITIDO),
            f("Martínez",  "Pablo",     em, "11-5500-2233",  "Martínez",  "Sofía",     LocalDate.of(2022,  1, 25), ComoConocioProyecto.SUGERIDO_PARTICIPANTE,        "Miércoles o jueves",   LocalDate.of(2026, 3, 22), EstadoFormulario.ADMITIDO),
            f("Fernández", "Marcela",   em, "351-712-0034",  "Fernández", "Santiago",  LocalDate.of(2021,  9, 10), ComoConocioProyecto.INSTAGRAM,                   "Lunes",                LocalDate.of(2026, 3, 25), EstadoFormulario.ADMITIDO),
            f("Romero",    "Florencia", em, "11-3341-9920",  "Romero",    "Luca",      LocalDate.of(2020, 11,  5), ComoConocioProyecto.SUGERIDO_EQUIPO_TERAPEUTICO,  "Cualquier día",        LocalDate.of(2026, 3, 28), EstadoFormulario.ADMITIDO),
            f("García",    "Claudia",   em, "11-4523-8871",  "García",    "Matías",    LocalDate.of(2021,  3, 15), ComoConocioProyecto.INSTAGRAM,                   "Lunes y miércoles",    LocalDate.of(2026, 4,  1), EstadoFormulario.ADMITIDO),
            f("López",     "Roberto",   em, "351-608-4412",  "López",     "Valentina", LocalDate.of(2022,  7, 20), ComoConocioProyecto.SUGERIDO_MEDICO,             "Martes por la tarde",  LocalDate.of(2026, 4,  8), EstadoFormulario.CONTACTADO),
            f("Díaz",      "Carolina",  em, "11-6200-8899",  "Díaz",      "Martina",   LocalDate.of(2022,  4, 17), ComoConocioProyecto.SUGERIDO_MEDICO,             "Jueves",               LocalDate.of(2026, 4, 12), EstadoFormulario.CONTACTADO),
            f("Velázquez", "Marcelo",   em, "351-723-1155",  "Velázquez", "Thiago",    LocalDate.of(2021,  8, 14), ComoConocioProyecto.INSTAGRAM,                   "Lunes y martes",       LocalDate.of(2026, 4, 15), EstadoFormulario.CONTACTADO),
            f("Bravo",     "Susana",    em, "11-4312-6677",  "Bravo",     "Emilia",    LocalDate.of(2023,  5, 20), ComoConocioProyecto.SUGERIDO_EQUIPO_TERAPEUTICO,  "Cualquier día",        LocalDate.of(2026, 4, 18), EstadoFormulario.CONTACTADO),
            f("Ibarra",    "Germán",    em, "351-890-3344",  "Ibarra",    "Joel",      LocalDate.of(2022,  9,  3), ComoConocioProyecto.SUGERIDO_MEDICO,             "Viernes",              LocalDate.of(2026, 4, 22), EstadoFormulario.CONTACTADO),
            f("Salinas",   "Mirta",     em, "11-5120-9900",  "Salinas",   "Valentín",  LocalDate.of(2021,  6, 11), ComoConocioProyecto.SUGERIDO_PARTICIPANTE,        "Miércoles",            LocalDate.of(2026, 4, 25), EstadoFormulario.CONTACTADO),
            f("Ponce",     "Adrián",    em, "351-445-2288",  "Ponce",     "Marisol",   LocalDate.of(2022, 12,  7), ComoConocioProyecto.INSTAGRAM,                   "Lunes a viernes",      LocalDate.of(2026, 4, 28), EstadoFormulario.CONTACTADO),
            f("Aguirre",   "Estela",    em, "11-6510-4433",  "Aguirre",   "Rodrigo",   LocalDate.of(2021,  3, 25), ComoConocioProyecto.SUGERIDO_MEDICO,             "Cualquier día",        LocalDate.of(2026, 5,  2), EstadoFormulario.CONTACTADO),
            f("Núñez",     "Horacio",   em, "351-334-7766",  "Núñez",     "Alma",      LocalDate.of(2023,  7, 18), ComoConocioProyecto.INSTAGRAM,                   "Martes o jueves",      LocalDate.of(2026, 5,  6), EstadoFormulario.CONTACTADO),
            f("Espínola",  "Valeria",   em, "11-4800-2211",  "Espínola",  "Iara",      LocalDate.of(2022,  2, 28), ComoConocioProyecto.SUGERIDO_EQUIPO_TERAPEUTICO,  "Lunes",                LocalDate.of(2026, 5, 10), EstadoFormulario.CONTACTADO),
            f("Molina",    "Eduardo",   em, "11-5900-6688",  "Molina",    "Enzo",      LocalDate.of(2022,  8, 12), ComoConocioProyecto.SUGERIDO_PARTICIPANTE,        "Lunes",                LocalDate.of(2026, 5, 14), EstadoFormulario.PENDIENTE),
            f("Ortega",    "Graciela",  em, "351-315-7744",  "Ortega",    "Miranda",   LocalDate.of(2021,  4, 23), ComoConocioProyecto.SUGERIDO_EQUIPO_TERAPEUTICO,  "Martes",               LocalDate.of(2026, 5, 17), EstadoFormulario.PENDIENTE),
            f("Rojas",     "Felipe",    em, "351-678-1133",  "Rojas",     "Delfina",   LocalDate.of(2023,  1,  5), ComoConocioProyecto.INSTAGRAM,                   "Miércoles o viernes",  LocalDate.of(2026, 5, 20), EstadoFormulario.PENDIENTE),
            f("Mansilla",  "Claudia",   em, "11-4411-5588",  "Mansilla",  "Agustín",   LocalDate.of(2021, 10, 17), ComoConocioProyecto.SUGERIDO_MEDICO,             "Lunes a miércoles",    LocalDate.of(2026, 5, 22), EstadoFormulario.PENDIENTE),
            f("Leiva",     "Jorge",     em, "351-567-4422",  "Leiva",     "Pilar",     LocalDate.of(2022,  5, 30), ComoConocioProyecto.SUGERIDO_EQUIPO_TERAPEUTICO,  "Cualquier día",        LocalDate.of(2026, 5, 25), EstadoFormulario.PENDIENTE),
            f("Vera",      "Leticia",   em, "11-5344-7700",  "Vera",      "Bautista",  LocalDate.of(2023,  3, 11), ComoConocioProyecto.INSTAGRAM,                   "Martes y jueves",      LocalDate.of(2026, 5, 28), EstadoFormulario.PENDIENTE),
            f("Córdoba",   "Ramón",     em, "351-212-9955",  "Córdoba",   "Abril",     LocalDate.of(2021, 11,  8), ComoConocioProyecto.SUGERIDO_MEDICO,             "Viernes",              LocalDate.of(2026, 6,  1), EstadoFormulario.PENDIENTE),
            f("Benítez",   "Sandra",    em, "11-6300-8811",  "Benítez",   "Ignacio",   LocalDate.of(2022,  7,  4), ComoConocioProyecto.SUGERIDO_PARTICIPANTE,        "Cualquier día",        LocalDate.of(2026, 6,  4), EstadoFormulario.PENDIENTE),
            f("Cáceres",   "Nora",      em, "351-789-6622",  "Cáceres",   "Facundo",   LocalDate.of(2021,  9, 22), ComoConocioProyecto.SUGERIDO_MEDICO,             "Lunes",                LocalDate.of(2026, 6,  7), EstadoFormulario.PENDIENTE),
            f("Zárate",    "Graciela",  em, "11-4900-3399",  "Zárate",    "Valentina", LocalDate.of(2023,  6, 15), ComoConocioProyecto.INSTAGRAM,                   "Miércoles",            LocalDate.of(2026, 6, 10), EstadoFormulario.PENDIENTE)
        );

        formularioRepository.saveAll(formularios);
    }

    // ===========================
    // PACIENTES (20)
    // ===========================

    private void seedPacientes() {
        if (pacienteRepository.count() > 0) return;

        String em = "ignaciomarucco@gmail.com";

        saveControl("Flores",    "Cecilia",  em, "11-4700-1122",  "Flores",    "Mateo",     LocalDate.of(2021,  2, 18), Sexo.MASCULINO, "C0001", LocalDate.of(2026, 1, 10), LocalDateTime.of(2026, 1, 20, 10, 0), LocalDate.of(2026, 7, 10));
        saveControl("Jiménez",   "Héctor",   em, "351-620-4433",  "Jiménez",   "Valentina", LocalDate.of(2020,  7, 31), Sexo.FEMENINO,  "C0002", LocalDate.of(2026, 1, 15), LocalDateTime.of(2026, 1, 25, 10, 0), LocalDate.of(2026, 7, 12));
        saveControl("Torres",    "Ricardo",  em, "351-300-9988",  "Torres",    "Camila",    LocalDate.of(2021,  7,  6), Sexo.FEMENINO,  "C0003", LocalDate.of(2026, 1, 20), LocalDateTime.of(2026, 1, 30, 10, 0), LocalDate.of(2026, 7, 15));
        saveControl("Medina",    "Patricia", em, "11-5800-3344",  "Medina",    "Julián",    LocalDate.of(2021, 11, 20), Sexo.MASCULINO, "C0004", LocalDate.of(2026, 1, 25), LocalDateTime.of(2026, 2,  4, 10, 0), LocalDate.of(2026, 7, 20));
        saveControl("Vargas",    "Mónica",   em, "11-4400-7755",  "Vargas",    "Lucas",     LocalDate.of(2020,  9, 18), Sexo.MASCULINO, "C0005", LocalDate.of(2026, 2,  1), LocalDateTime.of(2026, 2, 12, 10, 0), null);

        saveProblema("García",    "Claudia",   em, "11-4523-8871",  "García",    "Matías",    LocalDate.of(2021,  3, 15), Sexo.MASCULINO, "P0001", LocalDate.of(2026, 4,  1), LocalDateTime.of(2026, 4, 10, 10, 0));
        saveProblema("Romero",    "Florencia", em, "11-3341-9920",  "Romero",    "Luca",      LocalDate.of(2020, 11,  5), Sexo.MASCULINO, "P0002", LocalDate.of(2026, 3, 28), LocalDateTime.of(2026, 4,  7, 10, 0));
        saveProblema("Fernández", "Marcela",   em, "351-712-0034",  "Fernández", "Santiago",  LocalDate.of(2021,  9, 10), Sexo.MASCULINO, "P0003", LocalDate.of(2026, 3, 25), LocalDateTime.of(2026, 4,  3, 10, 0));
        saveProblema("Martínez",  "Pablo",     em, "11-5500-2233",  "Martínez",  "Sofía",     LocalDate.of(2022,  1, 25), Sexo.FEMENINO,  "P0004", LocalDate.of(2026, 3, 22), LocalDateTime.of(2026, 4,  1, 10, 0));
        saveProblema("González",  "Andrea",    em, "351-489-6677",  "González",  "Tomás",     LocalDate.of(2020,  6, 12), Sexo.MASCULINO, "P0005", LocalDate.of(2026, 3, 18), LocalDateTime.of(2026, 3, 28, 10, 0));

        saveProblemaConMchat("Herrera", "Jorge",   em, "11-4801-3322",  "Herrera", "Emma",   LocalDate.of(2023,  2,  8), Sexo.FEMENINO,  "P0006", LocalDate.of(2026, 3, 12), LocalDateTime.of(2026, 3, 22, 10, 0), LocalDate.of(2026, 7, 18), 1, MchatResultadoFinal.NEGATIVA);
        saveProblemaConMchat("Morales", "Silvana", em, "351-601-5544",  "Morales", "Franco", LocalDate.of(2021, 12,  3), Sexo.MASCULINO, "P0007", LocalDate.of(2026, 3, 15), LocalDateTime.of(2026, 3, 25, 10, 0), LocalDate.of(2026, 7, 22), 2, MchatResultadoFinal.NEGATIVA);

        saveProblemaConMchatYSeguimiento("Sánchez",   "Natalia",  em, "11-5701-4466",  "Sánchez",   "Isabella", LocalDate.of(2022, 10, 30), Sexo.FEMENINO,  "P0008", LocalDate.of(2026, 3,  8), LocalDateTime.of(2026, 3, 18, 10, 0), LocalDate.of(2026, 8,  5), 5, 3, MchatResultadoFinal.POSITIVA);
        saveProblemaConMchatYSeguimiento("Ruiz",       "Daniel",   em, "351-420-3355",  "Ruiz",       "Nicolás",  LocalDate.of(2023,  3, 22), Sexo.MASCULINO, "P0009", LocalDate.of(2026, 3,  5), LocalDateTime.of(2026, 3, 15, 10, 0), LocalDate.of(2026, 8, 10), 4, 2, MchatResultadoFinal.POSITIVA);
        saveProblemaConMchatYSeguimiento("Pereyra",    "Gustavo",  em, "351-555-7766",  "Pereyra",    "Benjamín", LocalDate.of(2020,  8, 29), Sexo.MASCULINO, "P0010", LocalDate.of(2026, 3,  1), LocalDateTime.of(2026, 3, 11, 10, 0), LocalDate.of(2026, 8, 12), 6, 4, MchatResultadoFinal.POSITIVA);
        saveProblemaConMchatYSeguimiento("Castro",     "Valeria",  em, "11-4923-1100",  "Castro",     "Agustina", LocalDate.of(2021,  5, 14), Sexo.FEMENINO,  "P0011", LocalDate.of(2026, 2, 22), LocalDateTime.of(2026, 3,  5, 10, 0), LocalDate.of(2026, 8, 15), 7, 3, MchatResultadoFinal.POSITIVA);
        saveProblemaConMchatYSeguimiento("Ramos",      "Alberto",  em, "351-508-6611",  "Ramos",      "Victoria", LocalDate.of(2020,  4,  9), Sexo.FEMENINO,  "P0012", LocalDate.of(2026, 2, 10), LocalDateTime.of(2026, 2, 20, 10, 0), LocalDate.of(2026, 8, 18), 3, 2, MchatResultadoFinal.POSITIVA);
        saveProblemaConMchatYSeguimiento("Acosta",     "Fernando", em, "351-611-2200",  "Acosta",     "Renata",   LocalDate.of(2022,  3,  5), Sexo.FEMENINO,  "P0013", LocalDate.of(2026, 2,  5), LocalDateTime.of(2026, 2, 15, 10, 0), LocalDate.of(2026, 8, 20), 5, 3, MchatResultadoFinal.POSITIVA);
        saveProblemaConMchatYSeguimiento("Domínguez",  "Sergio",   em, "351-402-8800",  "Domínguez",  "Catalina", LocalDate.of(2022,  6, 27), Sexo.FEMENINO,  "P0014", LocalDate.of(2026, 2, 18), LocalDateTime.of(2026, 2, 28, 10, 0), LocalDate.of(2026, 8, 25), 4, 1, MchatResultadoFinal.NEGATIVA);
        saveProblemaConMchatYSeguimiento("Gutiérrez",  "Laura",    em, "11-6100-5577",  "Gutiérrez",  "Máximo",   LocalDate.of(2023,  1, 14), Sexo.MASCULINO, "P0015", LocalDate.of(2026, 2, 14), LocalDateTime.of(2026, 2, 24, 10, 0), LocalDate.of(2026, 8, 28), 6, 1, MchatResultadoFinal.NEGATIVA);
    }

    private void saveControl(
            String apellidoTutor, String nombreTutor, String correo, String telefono,
            String apellidoNino, String nombreNino, LocalDate fechaNac, Sexo sexo,
            String codigo, LocalDate fechaContacto, LocalDateTime fechaPrimeraVisita,
            LocalDate fechaExtraccion) {
        Paciente p = buildPaciente(apellidoTutor, nombreTutor, correo, telefono,
                apellidoNino, nombreNino, fechaNac, sexo,
                TipoPaciente.CONTROL, codigo, fechaContacto, fechaPrimeraVisita);
        if (fechaExtraccion != null) {
            p.setFechaExtraccion(fechaExtraccion);
            p.setEstadoClinico(PacienteEstado.EXTRACCION_PENDIENTE);
        }
        pacienteRepository.save(p);
    }

    private void saveProblema(
            String apellidoTutor, String nombreTutor, String correo, String telefono,
            String apellidoNino, String nombreNino, LocalDate fechaNac, Sexo sexo,
            String codigo, LocalDate fechaContacto, LocalDateTime fechaPrimeraVisita) {
        pacienteRepository.save(buildPaciente(apellidoTutor, nombreTutor, correo, telefono,
                apellidoNino, nombreNino, fechaNac, sexo,
                TipoPaciente.PROBLEMA, codigo, fechaContacto, fechaPrimeraVisita));
    }

    private void saveProblemaConMchat(
            String apellidoTutor, String nombreTutor, String correo, String telefono,
            String apellidoNino, String nombreNino, LocalDate fechaNac, Sexo sexo,
            String codigo, LocalDate fechaContacto, LocalDateTime fechaPrimeraVisita,
            LocalDate fechaExtraccion, int scoreMchat, MchatResultadoFinal resultado) {
        Paciente p = buildPaciente(apellidoTutor, nombreTutor, correo, telefono,
                apellidoNino, nombreNino, fechaNac, sexo,
                TipoPaciente.PROBLEMA, codigo, fechaContacto, fechaPrimeraVisita);
        p.setFechaExtraccion(fechaExtraccion);
        p.setEstadoClinico(PacienteEstado.EXTRACCION_PENDIENTE);
        Paciente saved = pacienteRepository.save(p);
        mchatFamiliaRepository.save(buildMchatFamilia(saved, scoreMchat, resultado));
    }

    private void saveProblemaConMchatYSeguimiento(
            String apellidoTutor, String nombreTutor, String correo, String telefono,
            String apellidoNino, String nombreNino, LocalDate fechaNac, Sexo sexo,
            String codigo, LocalDate fechaContacto, LocalDateTime fechaPrimeraVisita,
            LocalDate fechaExtraccion, int scoreMchat, int fallas, MchatResultadoFinal resultado) {
        Paciente p = buildPaciente(apellidoTutor, nombreTutor, correo, telefono,
                apellidoNino, nombreNino, fechaNac, sexo,
                TipoPaciente.PROBLEMA, codigo, fechaContacto, fechaPrimeraVisita);
        p.setFechaExtraccion(fechaExtraccion);
        p.setEstadoClinico(PacienteEstado.EXTRACCION_PENDIENTE);
        Paciente saved = pacienteRepository.save(p);
        mchatFamiliaRepository.save(buildMchatFamilia(saved, scoreMchat, resultado));
        mchatSeguimientoRepository.save(buildMchatSeguimiento(saved, fallas));
    }

    private Paciente buildPaciente(
            String apellidoTutor, String nombreTutor, String correo, String telefono,
            String apellidoNino, String nombreNino, LocalDate fechaNac, Sexo sexo,
            TipoPaciente tipo, String codigo, LocalDate fechaContacto, LocalDateTime fechaPrimeraVisita) {
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
        p.setFechaContacto(fechaContacto);
        p.setFechaPrimeraVisita(fechaPrimeraVisita);
        p.setConsentimientoFirmado(true);
        p.setEstadoClinico(PacienteEstado.ADMITIDO);

        Criterios c = new Criterios();
        c.setPaciente(p);
        c.setCriterioEdad(true);
        if (tipo == TipoPaciente.PROBLEMA) {
            c.setCriterioTEADSMV(true);
            c.setCriterioTGDDSMIV(true);
        }
        p.setCriterios(c);

        return p;
    }

    private MchatFamilia buildMchatFamilia(Paciente paciente, int score, MchatResultadoFinal resultado) {
        boolean[] r = respuestasConScore(score);
        MchatFamilia m = new MchatFamilia();
        m.setPaciente(paciente);
        m.setP1(r[0]);   m.setP2(r[1]);   m.setP3(r[2]);   m.setP4(r[3]);   m.setP5(r[4]);
        m.setP6(r[5]);   m.setP7(r[6]);   m.setP8(r[7]);   m.setP9(r[8]);   m.setP10(r[9]);
        m.setP11(r[10]); m.setP12(r[11]); m.setP13(r[12]); m.setP14(r[13]); m.setP15(r[14]);
        m.setP16(r[15]); m.setP17(r[16]); m.setP18(r[17]); m.setP19(r[18]); m.setP20(r[19]);
        m.setScoreTotal(score);
        m.setResultadoFinal(resultado);
        return m;
    }

    private MchatSeguimiento buildMchatSeguimiento(Paciente paciente, int fallas) {
        boolean[] r = respuestasConScore(fallas);
        MchatSeguimiento s = new MchatSeguimiento();
        s.setPaciente(paciente);
        s.setItem1(r[0]);   s.setItem2(r[1]);   s.setItem3(r[2]);   s.setItem4(r[3]);   s.setItem5(r[4]);
        s.setItem6(r[5]);   s.setItem7(r[6]);   s.setItem8(r[7]);   s.setItem9(r[8]);   s.setItem10(r[9]);
        s.setItem11(r[10]); s.setItem12(r[11]); s.setItem13(r[12]); s.setItem14(r[13]); s.setItem15(r[14]);
        s.setItem16(r[15]); s.setItem17(r[16]); s.setItem18(r[17]); s.setItem19(r[18]); s.setItem20(r[19]);
        s.setFallas(fallas);
        return s;
    }

    private boolean[] respuestasConScore(int score) {
        boolean[] r = new boolean[20];
        for (int i = 0; i < 20; i++) {
            r[i] = !MCHAT_INVERTIDAS.contains(i + 1);
        }
        int pendiente = score;
        for (int i = 0; i < 20 && pendiente > 0; i++) {
            if (!MCHAT_INVERTIDAS.contains(i + 1)) { r[i] = false; pendiente--; }
        }
        for (int i = 0; i < 20 && pendiente > 0; i++) {
            if (MCHAT_INVERTIDAS.contains(i + 1)) { r[i] = true; pendiente--; }
        }
        return r;
    }

    // ===========================
    // EVALUACIONES CARS-2 Y VINELAND-II
    // ===========================

    private void seedEvaluaciones() {
        if (evaluacionCarsRepository.count() > 0) return;

        Map<String, Paciente> porCodigo = pacienteRepository.findAll().stream()
                .collect(Collectors.toMap(Paciente::getCodigoNumerico, p -> p));

        seedCars(porCodigo, "P0006", 40.0, 10,
                1.0, 1.5, 1.0, 1.5, 1.5, 1.5, 1.0, 1.5, 1.5, 1.5, 1.0, 1.5, 1.5, 1.5, 1.5);
        seedVineland(porCodigo, "P0006", 95, 98, 92, 100, 95, 18, 17, 19);

        seedCars(porCodigo, "P0007", 44.0, 20,
                1.5, 1.5, 1.5, 1.5, 2.0, 1.5, 1.5, 1.5, 1.5, 1.5, 2.0, 1.5, 1.5, 1.5, 1.5);
        seedVineland(porCodigo, "P0007", 90, 92, 88, 95, 90, 20, 19, 21);

        seedCars(porCodigo, "P0014", 47.0, 35,
                1.5, 2.0, 1.5, 2.0, 2.0, 1.5, 2.0, 1.5, 2.0, 2.0, 1.5, 2.0, 1.5, 2.0, 2.0);
        seedVineland(porCodigo, "P0014", 88, 90, 85, 92, 88, 18, 17, 19);

        seedCars(porCodigo, "P0015", 49.0, 40,
                2.0, 2.0, 2.0, 2.0, 2.0, 1.5, 2.0, 2.0, 2.0, 2.0, 1.5, 2.0, 2.0, 2.0, 1.5);
        seedVineland(porCodigo, "P0015", 82, 88, 80, 90, 85, 19, 18, 20);

        seedCars(porCodigo, "P0009", 55.0, 70,
                2.0, 2.5, 2.0, 2.5, 2.5, 2.0, 2.5, 2.0, 2.5, 2.5, 2.0, 2.5, 2.0, 2.5, 2.0);
        seedVineland(porCodigo, "P0009", 75, 80, 74, 85, 78, 21, 20, 22);

        seedCars(porCodigo, "P0012", 52.0, 60,
                2.0, 2.0, 2.5, 2.0, 2.0, 2.5, 2.0, 2.0, 2.5, 2.0, 2.5, 2.0, 2.0, 2.5, 2.0);
        seedVineland(porCodigo, "P0012", 80, 85, 78, 90, 82, 19, 18, 20);

        seedCars(porCodigo, "P0013", 58.0, 78,
                2.5, 2.5, 2.5, 2.5, 2.0, 2.5, 2.5, 2.0, 2.5, 2.5, 2.0, 2.5, 2.5, 2.0, 2.0);
        seedVineland(porCodigo, "P0013", 70, 75, 68, 82, 72, 22, 20, 23);

        seedCars(porCodigo, "P0008", 64.0, 91,
                2.5, 3.0, 2.5, 3.0, 2.5, 2.5, 2.5, 2.5, 2.5, 2.5, 3.0, 2.5, 2.5, 2.5, 3.0);
        seedVineland(porCodigo, "P0008", 65, 72, 60, 80, 68, 24, 22, 25);

        seedCars(porCodigo, "P0010", 68.0, 96,
                3.0, 3.0, 3.0, 3.0, 3.0, 2.5, 3.0, 2.5, 3.0, 2.5, 3.0, 2.5, 3.0, 2.5, 3.0);
        seedVineland(porCodigo, "P0010", 55, 62, 50, 72, 58, 26, 25, 28);

        seedCars(porCodigo, "P0011", 63.0, 89,
                2.5, 3.0, 2.5, 2.5, 3.0, 2.5, 2.5, 2.5, 2.5, 3.0, 2.5, 2.5, 2.5, 2.5, 2.5);
        seedVineland(porCodigo, "P0011", 60, 70, 58, 78, 65, 23, 21, 24);
    }

    private void seedCars(Map<String, Paciente> porCodigo, String codigo,
                          double tScore, int percentil, double... items) {
        Paciente p = porCodigo.get(codigo);
        if (p == null) return;
        EvaluacionCars e = new EvaluacionCars();
        e.setPaciente(p);
        e.setItem1(bd(items[0]));   e.setItem2(bd(items[1]));   e.setItem3(bd(items[2]));
        e.setItem4(bd(items[3]));   e.setItem5(bd(items[4]));   e.setItem6(bd(items[5]));
        e.setItem7(bd(items[6]));   e.setItem8(bd(items[7]));   e.setItem9(bd(items[8]));
        e.setItem10(bd(items[9]));  e.setItem11(bd(items[10])); e.setItem12(bd(items[11]));
        e.setItem13(bd(items[12])); e.setItem14(bd(items[13])); e.setItem15(bd(items[14]));
        BigDecimal rawScore = BigDecimal.ZERO;
        for (double v : items) rawScore = rawScore.add(bd(v));
        e.setRawScore(rawScore);
        e.setTScore(bd(tScore));
        e.setPercentil(percentil);
        evaluacionCarsRepository.save(e);
    }

    private void seedVineland(Map<String, Paciente> porCodigo, String codigo,
                               int comunicacion, int autovalimiento, int social, int motor,
                               int cocienteFinal, int conductaDesadaptativa, int internalizante, int externalizante) {
        Paciente p = porCodigo.get(codigo);
        if (p == null) return;
        EvaluacionVineland v = new EvaluacionVineland();
        v.setPaciente(p);
        v.setComunicacion(comunicacion);
        v.setAutovalimiento(autovalimiento);
        v.setSocial(social);
        v.setMotor(motor);
        v.setCocienteFinal(cocienteFinal);
        v.setConductaDesadaptativa(conductaDesadaptativa);
        v.setInternalizante(internalizante);
        v.setExternalizante(externalizante);
        evaluacionVinelandRepository.save(v);
    }

    private BigDecimal bd(double val) {
        return BigDecimal.valueOf(val);
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

    // ===========================
    // BASIC STAGE SEEDING
    // ===========================

    private void seedCajas() {
        if (cajaRepository.count() > 0) return;

        var cajas = List.of(
            createCaja("A", 1, 1),
            createCaja("A", 1, 2),
            createCaja("B", 2, 1),
            createCaja("B", 3, 1)
        );
        cajaRepository.saveAll(cajas);
    }

    private Caja createCaja(String freezer, Integer cajon, Integer numero) {
        Caja c = new Caja();
        c.setFreezer(freezer);
        c.setCajon(cajon);
        c.setNumero(numero);
        c.setActivo(true);
        return c;
    }

    private void seedSueros() {
        if (sueroRepository.count() > 0) return;

        Map<String, Paciente> porCodigo = pacienteRepository.findAll().stream()
                .collect(Collectors.toMap(Paciente::getCodigoNumerico, p -> p));

        List<Caja> cajas = cajaRepository.findAll();
        Caja cajaControl  = cajas.get(0);  // Caja A/1/1
        Caja cajaProblema = cajas.get(1);  // Caja A/1/2

        // Control: rango 0
        saveSueroConTubos(porCodigo.get("C0001"), cajaControl,  0.0,  "A1", 1.0, "A2", 1.0);
        saveSueroConTubos(porCodigo.get("C0002"), cajaControl,  0.0,  "A3", 1.0, "A4", 1.0);
        saveSueroConTubos(porCodigo.get("C0003"), cajaControl,  0.0,  "A5", 1.0, "A6", 1.0);
        saveSueroConTubos(porCodigo.get("C0004"), cajaControl,  0.0,  "A7", 1.0, "A8", 1.0);

        // Rango 1 (1314–2500 BTU)
        saveSueroConTubos(porCodigo.get("P0006"), cajaProblema, 1800.0, "B1", 0.75, "B2", 0.75);
        saveSueroConTubos(porCodigo.get("P0007"), cajaProblema, 2000.0, "B3", 0.75, "B4", 0.75);
        saveSueroConTubos(porCodigo.get("P0014"), cajaProblema, 1500.0, "B5", 0.75, "B6", 0.75);
        saveSueroConTubos(porCodigo.get("P0015"), cajaProblema, 2200.0, "B7", 0.75, "B8", 0.75);

        // Rango 2 (2501–8000 BTU)
        saveSueroConTubos(porCodigo.get("P0009"), cajaProblema, 5000.0, "C1", 0.75, "C2", 0.75);
        saveSueroConTubos(porCodigo.get("P0012"), cajaProblema, 4000.0, "C3", 0.75, "C4", 0.75);
        saveSueroConTubos(porCodigo.get("P0013"), cajaProblema, 4500.0, "C5", 0.75, "C6", 0.75);

        // Rango 3 (> 8000 BTU)
        saveSueroConTubos(porCodigo.get("P0008"), cajaProblema, 9000.0, "D1", 0.75, "D2", 0.75);
        saveSueroConTubos(porCodigo.get("P0010"), cajaProblema, 9500.0, "D3", 0.75, "D4", 0.75);
        saveSueroConTubos(porCodigo.get("P0011"), cajaProblema, 8500.0, "D5", 0.75, "D6", 0.75);
    }

    private void saveSueroConTubos(Paciente paciente, Caja caja, double valorAnticuerpos,
                                    String pos1, double cant1, String pos2, double cant2) {
        if (paciente == null) return;

        Suero s = new Suero();
        s.setPaciente(paciente);
        s.setCaja(caja);
        s.setFechaExtraccion(paciente.getFechaExtraccion());
        s.setValorAnticuerpos(valorAnticuerpos);
        s.setRango(SueroRangoUtil.calcularRango(valorAnticuerpos));
        s.setUso(paciente.getTipoPaciente() == TipoPaciente.CONTROL ? SueroUso.CONTROL : SueroUso.PROBLEMA);
        Suero saved = sueroRepository.save(s);

        Tubo t1 = new Tubo();
        t1.setTipo(TipoTubo.SUERO);
        t1.setCaja(caja);
        t1.setSuero(saved);
        t1.setPosicion(pos1);
        t1.setCantidadInicial(cant1);
        tuboRepository.save(t1);

        Tubo t2 = new Tubo();
        t2.setTipo(TipoTubo.SUERO);
        t2.setCaja(caja);
        t2.setSuero(saved);
        t2.setPosicion(pos2);
        t2.setCantidadInicial(cant2);
        tuboRepository.save(t2);

        paciente.setEstadoClinico(PacienteEstado.EXTRACCION_REALIZADA);
        pacienteRepository.save(paciente);
    }

    private void seedPools() {
        if (poolRepository.count() > 0) return;

        List<Caja> cajas = cajaRepository.findAll();
        Caja cajaPools = cajas.get(2);  // Caja B/2/1

        // Buscar tubos de suero por posición
        Map<String, Tubo> tubosPorPosicion = tuboRepository.findBySueroActivoTrue().stream()
                .collect(Collectors.toMap(Tubo::getPosicion, t -> t));

        Tubo st_C1 = tubosPorPosicion.get("C1");
        Tubo st_C2 = tubosPorPosicion.get("C2");
        Tubo st_C3 = tubosPorPosicion.get("C3");
        Tubo st_C4 = tubosPorPosicion.get("C4");
        Tubo st_D1 = tubosPorPosicion.get("D1");
        Tubo st_D2 = tubosPorPosicion.get("D2");
        Tubo st_D3 = tubosPorPosicion.get("D3");
        Tubo st_D4 = tubosPorPosicion.get("D4");

        // Pool 1: Rango 2 (P0009 + P0012) — usa tubos C1, C2, C3, C4
        // Aportes: 0.5 mL de cada tubo → total 2.0 mL
        // Pool tubo: P2-A con 2.0 mL initial
        if (st_C1 != null && st_C2 != null && st_C3 != null && st_C4 != null) {
            Pool pool1 = new Pool();
            pool1.setCodigo(generarCodigoPool());
            pool1.setCaja(cajaPools);
            pool1.setFechaCreacion(LocalDate.now(clock).minusDays(20));
            pool1.setRango(2);
            pool1.setUso(SueroUso.PROBLEMA);
            pool1.setActivo(true);
            Pool savedPool1 = poolRepository.save(pool1);

            Tubo pt1 = new Tubo();
            pt1.setTipo(TipoTubo.POOL);
            pt1.setCaja(cajaPools);
            pt1.setPool(savedPool1);
            pt1.setPosicion("P2-A");
            pt1.setCantidadInicial(2.0);
            Tubo savedPt1 = tuboRepository.save(pt1);

            crearAporte(savedPool1, st_C1, 0.5);
            crearAporte(savedPool1, st_C2, 0.5);
            crearAporte(savedPool1, st_C3, 0.5);
            crearAporte(savedPool1, st_C4, 0.5);

            // Seed algunos aportes de modelos animales en pool1 (se usan en seedModelosAnimales)
            // Pool tubo P2-A tendrá cantidadUsada = 0 por ahora; se actualiza en seedModelosAnimales
            poolRepository.save(savedPool1);
        }

        // Pool 2: Rango 3 (P0008 + P0010) — usa tubos D1, D2, D3, D4
        if (st_D1 != null && st_D2 != null && st_D3 != null && st_D4 != null) {
            Pool pool2 = new Pool();
            pool2.setCodigo(generarCodigoPool());
            pool2.setCaja(cajaPools);
            pool2.setFechaCreacion(LocalDate.now(clock).minusDays(10));
            pool2.setRango(3);
            pool2.setUso(SueroUso.PROBLEMA);
            pool2.setActivo(true);
            Pool savedPool2 = poolRepository.save(pool2);

            Tubo pt2 = new Tubo();
            pt2.setTipo(TipoTubo.POOL);
            pt2.setCaja(cajaPools);
            pt2.setPool(savedPool2);
            pt2.setPosicion("P3-A");
            pt2.setCantidadInicial(2.0);
            tuboRepository.save(pt2);

            crearAporte(savedPool2, st_D1, 0.5);
            crearAporte(savedPool2, st_D2, 0.5);
            crearAporte(savedPool2, st_D3, 0.5);
            crearAporte(savedPool2, st_D4, 0.5);
        }
    }

    private void crearAporte(Pool pool, Tubo tubo, double cantidad) {
        PoolSueroAporte aporte = new PoolSueroAporte();
        aporte.setPool(pool);
        aporte.setTubo(tubo);
        aporte.setCantidadAportada(cantidad);
        poolSueroAporteRepository.save(aporte);

        tubo.setCantidadUsada(tubo.getCantidadUsada() + cantidad);
        tuboRepository.save(tubo);
    }

    private void seedCamadas() {
        if (camadaRepository.count() > 0) return;

        var camadas = List.of(
            createCamada("C2026-A"),
            createCamada("C2026-B")
        );
        camadaRepository.saveAll(camadas);
    }

    private Camada createCamada(String nombre) {
        Camada c = new Camada();
        c.setNombre(nombre);
        c.setActivo(true);
        return c;
    }

    private void seedModelosAnimales() {
        if (modeloAnimalRepository.count() > 0) return;

        List<Pool> pools = poolRepository.findAll();
        if (pools.size() < 2) return;

        Pool pool1 = pools.get(0);
        Pool pool2 = pools.get(1);

        List<Camada> camadas = camadaRepository.findAll();
        if (camadas.size() < 2) return;

        Camada camada1 = camadas.get(0);
        Camada camada2 = camadas.get(1);

        LocalDate hoy = LocalDate.now(clock);

        // Buscar tubo de pool P2-A del pool1
        List<Tubo> tubosPool1 = tuboRepository.findByPoolId(pool1.getId());
        Tubo tuboP2A = tubosPool1.isEmpty() ? null : tubosPool1.get(0);

        // Ratón 1: M-R2-A — rango 2, estudios completos
        ModeloAnimal m1 = new ModeloAnimal();
        m1.setIdentificador("M-R2-A");
        m1.setPool(pool1);
        m1.setCamada(camada1);
        m1.setFechaNacimiento(hoy.minusDays(25));
        m1.setSexo(SexoRaton.MACHO);
        m1.setFechaDia1Inoculacion(hoy.minusDays(19));
        m1.setNumCelulasGanglionares(150);
        m1.setNumCelulasPurkinje(85);

        VocalizacionesUltrasonicas vus1 = new VocalizacionesUltrasonicas();
        vus1.setModeloAnimal(m1);
        vus1.setMuestra1Khz(45.0);
        vus1.setMuestra2Khz(65.0);
        m1.setVocalizaciones(vus1);

        TresCamaras tc1 = new TresCamaras();
        tc1.setModeloAnimal(m1);
        tc1.setM1TiempoRatonNovedad(4.5);
        tc1.setM1TiempoObjetoNovedoso(3.0);
        tc1.setM2TiempoRatonDesconocido(5.0);
        tc1.setM2TiempoRatonFamiliar(2.5);
        m1.setTresCamaras(tc1);
        m1.setActivo(true);
        ModeloAnimal savedM1 = modeloAnimalRepository.save(m1);

        if (tuboP2A != null) {
            crearAporteModelo(savedM1, tuboP2A, 0.2, 1);
            crearAporteModelo(savedM1, tuboP2A, 0.2, 2);
            crearAporteModelo(savedM1, tuboP2A, 0.2, 3);
            crearAporteModelo(savedM1, tuboP2A, 0.2, 4);
        }

        // Ratón 2: M-R2-B — rango 2, solo vocalizaciones
        ModeloAnimal m2 = new ModeloAnimal();
        m2.setIdentificador("M-R2-B");
        m2.setPool(pool1);
        m2.setCamada(camada1);
        m2.setFechaNacimiento(hoy.minusDays(25));
        m2.setSexo(SexoRaton.HEMBRA);
        m2.setFechaDia1Inoculacion(hoy.minusDays(19));
        m2.setNumCelulasGanglionares(160);
        m2.setNumCelulasPurkinje(90);

        VocalizacionesUltrasonicas vus2 = new VocalizacionesUltrasonicas();
        vus2.setModeloAnimal(m2);
        vus2.setMuestra1Khz(30.0);
        vus2.setMuestra2Khz(40.0);
        m2.setVocalizaciones(vus2);
        m2.setActivo(true);
        modeloAnimalRepository.save(m2);

        // Ratón 3: M-R3-A — rango 3, sin estudios, nacido hace 5 días → alerta vocalizaciones
        ModeloAnimal m3 = new ModeloAnimal();
        m3.setIdentificador("M-R3-A");
        m3.setPool(pool2);
        m3.setCamada(camada2);
        m3.setFechaNacimiento(hoy.minusDays(5));
        m3.setSexo(SexoRaton.MACHO);
        m3.setFechaDia1Inoculacion(hoy.minusDays(1));
        m3.setActivo(true);
        modeloAnimalRepository.save(m3);

        // Ratón 4: M-R3-B — rango 3, sin estudios, nacido hace 19 días → alerta tres cámaras
        ModeloAnimal m4 = new ModeloAnimal();
        m4.setIdentificador("M-R3-B");
        m4.setPool(pool2);
        m4.setCamada(camada2);
        m4.setFechaNacimiento(hoy.minusDays(19));
        m4.setSexo(SexoRaton.HEMBRA);
        m4.setFechaDia1Inoculacion(hoy.minusDays(15));
        m4.setActivo(true);
        modeloAnimalRepository.save(m4);
    }

    private void crearAporteModelo(ModeloAnimal modelo, Tubo tubo, double cantidadConsumida, int dia) {
        ModeloAnimalPoolAporte aporte = new ModeloAnimalPoolAporte();
        aporte.setModeloAnimal(modelo);
        aporte.setTubo(tubo);
        aporte.setCantidadConsumida(cantidadConsumida);
        aporte.setDia(dia);
        modeloAnimalPoolAporteRepository.save(aporte);

        tubo.setCantidadUsada(tubo.getCantidadUsada() + cantidadConsumida);
        tuboRepository.save(tubo);
    }

    private String generarCodigoPool() {
        String codigo;
        do {
            codigo = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        } while (poolRepository.existsByCodigo(codigo));
        return codigo;
    }
}
