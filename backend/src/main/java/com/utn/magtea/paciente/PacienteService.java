package com.utn.magtea.paciente;

import com.utn.magtea.common.MailService;
import com.utn.magtea.common.PageResponse;
import com.utn.magtea.common.exception.BusinessRuleException;
import com.utn.magtea.common.exception.ResourceNotFoundException;
import com.utn.magtea.formulariointeres.FormularioInteres;
import com.utn.magtea.formulariointeres.FormularioInteresService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PacienteService {

    private static final Set<Double>  CARS_VALORES_VALIDOS = Set.of(1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0);
    private static final Set<String>  SORT_FIELDS_VALIDOS  = Set.of("createdAt", "apellidoNino", "apellidoTutor", "nombreNino");

    private final PacienteRepository repository;
    private final PacienteMapper mapper;
    private final FormularioInteresService formularioService;
    private final MailService mailService;
    private final Clock clock;

    @Value("${app.mchat.token-expiry-days:30}")
    private int tokenExpiryDays;

    @Transactional(readOnly = true)
    public PageResponse<PacienteResponseDTO> findAll(int page, int size, String q,
                                                     List<PacienteEstado> estados,
                                                     String sortBy, String sortDir) {
        Sort sort = buildSort(sortBy, sortDir, "createdAt");
        Page<Paciente> result = repository.findAll(buildSpec(q, estados), PageRequest.of(page, size, sort));
        return new PageResponse<>(
                result.map(mapper::toDTO).getContent(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.getNumber(),
                result.getSize()
        );
    }

    @Transactional(readOnly = true)
    public PacienteResponseDTO findById(Long id) {
        return mapper.toDTO(findActiveById(id));
    }

    @Transactional
    public PacienteResponseDTO create(PacienteCreateDTO dto) {
        Paciente paciente = mapper.toEntity(dto);
        paciente.setCodigoNumerico(generarCodigoNumerico());
        paciente.setFechaContacto(LocalDate.now());
        paciente.setFechaPrimeraVisita(dto.fechaPrimeraVisita());

        if (dto.formularioInteresId() != null) {
            FormularioInteres formulario = formularioService.admitir(dto.formularioInteresId());
            paciente.setFormularioInteresId(formulario.getId());
            paciente.setFechaContacto(formulario.getFechaContacto());
            paciente.setComoConocioProyecto(formulario.getComoConocioProyecto());
        }

        paciente.setMchatToken(UUID.randomUUID().toString());
        paciente.setMchatTokenExpiry(LocalDateTime.now(clock).plusDays(tokenExpiryDays));
        paciente.setEstadoClinico(PacienteEstado.ADMITIDO);

        Paciente saved = repository.save(paciente);

        try {
            mailService.enviarLinkMchat(
                    saved.getCorreoTutor(),
                    saved.getNombreTutor(),
                    saved.getApellidoNino(),
                    saved.getNombreNino(),
                    saved.getMchatToken()
            );
        } catch (Exception e) {
            log.warn("No se pudo enviar el mail M-CHAT para paciente {}: {}", saved.getId(), e.getMessage());
        }

        return mapper.toDTO(saved);
    }

    @Transactional
    public PacienteResponseDTO update(Long id, PacienteUpdateDTO dto) {
        Paciente paciente = findActiveById(id);
        paciente.setApellidoTutor(dto.apellidoTutor());
        paciente.setNombreTutor(dto.nombreTutor());
        paciente.setCorreoTutor(dto.correoTutor());
        paciente.setTelefono(dto.telefono());
        paciente.setApellidoNino(dto.apellidoNino());
        paciente.setNombreNino(dto.nombreNino());
        paciente.setFechaNacimientoNino(dto.fechaNacimientoNino());
        paciente.setSexo(dto.sexo());
        paciente.setNotas(dto.notas());
        return mapper.toDTO(repository.save(paciente));
    }

    @Transactional
    public PacienteResponseDTO updatePrimeraVisita(Long id, PacientePrimeraVisitaDTO dto) {
        Paciente paciente = findActiveById(id);
        paciente.setFechaPrimeraVisita(dto.fechaPrimeraVisita());
        return mapper.toDTO(repository.save(paciente));
    }

    @Transactional
    public PacienteResponseDTO updateConsentimiento(Long id, PacienteConsentimientoDTO dto) {
        Paciente paciente = findActiveById(id);
        paciente.setConsentimientoFirmado(dto.consentimientoFirmado());
        return mapper.toDTO(repository.save(paciente));
    }

    @Transactional
    public PacienteResponseDTO updateCriterios(Long id, PacienteCriteriosDTO dto) {
        Paciente paciente = findActiveById(id);
        paciente.setCriterioTEADSMV(dto.criterioTEADSMV());
        paciente.setCriterioTGDDSMIV(dto.criterioTGDDSMIV());
        paciente.setCriterioEdad(dto.criterioEdad());
        paciente.setEpilepsia(dto.epilepsia());
        paciente.setParalisisCerebral(dto.paralisisCerebral());
        paciente.setInfeccionesCongenitas(dto.infeccionesCongenitas());
        paciente.setLesionesEstructuralesSNC(dto.lesionesEstructuralesSNC());
        paciente.setFacomatosis(dto.facomatosis());
        paciente.setPatologiasNeurometabolicas(dto.patologiasNeurometabolicas());
        paciente.setLesionesOcupantesEspacioSNC(dto.lesionesOcupantesEspacioSNC());
        paciente.setPatologiaPsiquiatrica(dto.patologiaPsiquiatrica());
        paciente.setOtrosSindromesGeneticos(dto.otrosSindromesGeneticos());
        paciente.setPubertadPrecoz(dto.pubertadPrecoz());
        paciente.setConsentimientoFirmado(dto.consentimientoFirmado());
        paciente.setCriteriosRegistrados(true);
        return mapper.toDTO(repository.save(paciente));
    }

    @Transactional
    public PacienteResponseDTO updateMchatSeguimiento(Long id, PacienteMchatSeguimientoDTO dto) {
        Paciente paciente = findActiveById(id);

        if (paciente.getMchatScoreTotal() == null
                || paciente.getMchatScoreTotal() < 3
                || paciente.getMchatScoreTotal() > 7) {
            throw new BusinessRuleException(
                    "El seguimiento M-CHAT solo aplica cuando el score está entre 3 y 7");
        }

        paciente.setSeguimientoItem1(dto.item1());   paciente.setSeguimientoItem2(dto.item2());
        paciente.setSeguimientoItem3(dto.item3());   paciente.setSeguimientoItem4(dto.item4());
        paciente.setSeguimientoItem5(dto.item5());   paciente.setSeguimientoItem6(dto.item6());
        paciente.setSeguimientoItem7(dto.item7());   paciente.setSeguimientoItem8(dto.item8());
        paciente.setSeguimientoItem9(dto.item9());   paciente.setSeguimientoItem10(dto.item10());
        paciente.setSeguimientoItem11(dto.item11()); paciente.setSeguimientoItem12(dto.item12());
        paciente.setSeguimientoItem13(dto.item13()); paciente.setSeguimientoItem14(dto.item14());
        paciente.setSeguimientoItem15(dto.item15()); paciente.setSeguimientoItem16(dto.item16());
        paciente.setSeguimientoItem17(dto.item17()); paciente.setSeguimientoItem18(dto.item18());
        paciente.setSeguimientoItem19(dto.item19()); paciente.setSeguimientoItem20(dto.item20());

        int fallas = contarFallas(dto);
        paciente.setMchatSeguimientoFallas(fallas);
        paciente.setMchatResultadoFinal(fallas <= 1
                ? MchatResultadoFinal.NEGATIVA
                : MchatResultadoFinal.POSITIVA);

        return mapper.toDTO(repository.save(paciente));
    }

    @Transactional
    public PacienteResponseDTO updateCars(Long id, PacienteCarsDTO dto) {
        validarItemsCars(dto);
        Paciente paciente = findActiveById(id);

        double rawScore = dto.item1() + dto.item2() + dto.item3() + dto.item4() + dto.item5()
                + dto.item6() + dto.item7() + dto.item8() + dto.item9() + dto.item10()
                + dto.item11() + dto.item12() + dto.item13() + dto.item14() + dto.item15();

        paciente.setCarsRawScore(rawScore);
        return mapper.toDTO(repository.save(paciente));
    }

    @Transactional
    public PacienteResponseDTO updateVineland(Long id, PacienteVinelandDTO dto) {
        Paciente paciente = findActiveById(id);
        paciente.setVinelandComunicacion(dto.vinelandComunicacion());
        paciente.setVinelandAutovalimiento(dto.vinelandAutovalimiento());
        paciente.setVinelandSocial(dto.vinelandSocial());
        paciente.setVinelandMotor(dto.vinelandMotor());
        paciente.setVinelandCocienteFinal(dto.vinelandCocienteFinal());
        paciente.setVinelandConductaDesadaptativa(dto.vinelandConductaDesadaptativa());
        paciente.setVinelandInternalizante(dto.vinelandInternalizante());
        paciente.setVinelandExternalizante(dto.vinelandExternalizante());
        return mapper.toDTO(repository.save(paciente));
    }

    @Transactional
    public PacienteResponseDTO updateSegundaVisita(Long id, PacienteSegundaVisitaDTO dto) {
        Paciente paciente = findActiveById(id);
        paciente.setFechaExtraccion(dto.fechaExtraccion());
        paciente.refreshEstadoClinico();
        return mapper.toDTO(repository.save(paciente));
    }

    @Transactional
    public PacienteResponseDTO reenviarMchat(Long id) {
        Paciente paciente = findActiveById(id);
        paciente.setMchatToken(UUID.randomUUID().toString());
        paciente.setMchatTokenExpiry(LocalDateTime.now(clock).plusDays(tokenExpiryDays));
        Paciente saved = repository.save(paciente);

        try {
            mailService.enviarLinkMchat(
                    saved.getCorreoTutor(),
                    saved.getNombreTutor(),
                    saved.getApellidoNino(),
                    saved.getNombreNino(),
                    saved.getMchatToken()
            );
        } catch (Exception e) {
            log.warn("No se pudo reenviar el mail M-CHAT para paciente {}: {}", saved.getId(), e.getMessage());
        }

        return mapper.toDTO(saved);
    }

    @Transactional
    public void delete(Long id) {
        Paciente paciente = findActiveById(id);
        paciente.setActivo(false);
        repository.save(paciente);
    }

    private Paciente findActiveById(Long id) {
        return repository.findById(id)
                .filter(Paciente::isActivo)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Paciente con id " + id + " no existe"));
    }

    private Specification<Paciente> buildSpec(String q, List<PacienteEstado> estados) {
        Specification<Paciente> spec = activoTrue();
        if (q != null && !q.isBlank()) spec = spec.and(searchText(q));
        if (estados != null && !estados.isEmpty()) spec = spec.and(estadoIn(estados));
        return spec;
    }

    private Specification<Paciente> activoTrue() {
        return (root, query, cb) -> cb.isTrue(root.get("activo"));
    }

    private Specification<Paciente> searchText(String q) {
        String like = "%" + q.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("nombreNino")), like),
                cb.like(cb.lower(root.get("apellidoNino")), like),
                cb.like(cb.lower(root.get("codigoNumerico")), like),
                cb.like(cb.lower(root.get("apellidoTutor")), like)
        );
    }

    private Specification<Paciente> estadoIn(List<PacienteEstado> estados) {
        return (root, query, cb) -> root.get("estadoClinico").in(estados);
    }

    private Sort buildSort(String sortBy, String sortDir, String defaultField) {
        String field = SORT_FIELDS_VALIDOS.contains(sortBy) ? sortBy : defaultField;
        Sort.Direction dir = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(dir, field);
    }

    private String generarCodigoNumerico() {
        String codigo;
        do {
            codigo = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        } while (repository.existsByCodigoNumerico(codigo));
        return codigo;
    }

    private int contarFallas(PacienteMchatSeguimientoDTO dto) {
        int fallas = 0;
        if (dto.item1())  fallas++; if (dto.item2())  fallas++; if (dto.item3())  fallas++;
        if (dto.item4())  fallas++; if (dto.item5())  fallas++; if (dto.item6())  fallas++;
        if (dto.item7())  fallas++; if (dto.item8())  fallas++; if (dto.item9())  fallas++;
        if (dto.item10()) fallas++; if (dto.item11()) fallas++; if (dto.item12()) fallas++;
        if (dto.item13()) fallas++; if (dto.item14()) fallas++; if (dto.item15()) fallas++;
        if (dto.item16()) fallas++; if (dto.item17()) fallas++; if (dto.item18()) fallas++;
        if (dto.item19()) fallas++; if (dto.item20()) fallas++;
        return fallas;
    }

    private void validarItemsCars(PacienteCarsDTO dto) {
        Double[] items = {
            dto.item1(), dto.item2(), dto.item3(), dto.item4(), dto.item5(),
            dto.item6(), dto.item7(), dto.item8(), dto.item9(), dto.item10(),
            dto.item11(), dto.item12(), dto.item13(), dto.item14(), dto.item15()
        };
        for (int i = 0; i < items.length; i++) {
            if (!CARS_VALORES_VALIDOS.contains(items[i])) {
                throw new BusinessRuleException(
                        "Valor inválido en ítem " + (i + 1) + " de CARS-2: " + items[i]
                        + ". Valores permitidos: 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0");
            }
        }
    }
}
