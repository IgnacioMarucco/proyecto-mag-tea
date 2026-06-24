package com.utn.magtea.tubo;

import com.utn.magtea.common.exception.BusinessRuleException;
import com.utn.magtea.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TuboService {

    private final TuboRepository repository;

    @Transactional
    public void vaciar(Long tuboId, VaciarTuboRequest req) {
        Tubo tubo = repository.findById(tuboId)
                .orElseThrow(() -> new ResourceNotFoundException("Tubo con id " + tuboId + " no existe"));

        if (tubo.getCantidadRestante().compareTo(BigDecimal.ZERO) > 0) {
            tubo.setCantidadUsada(tubo.getCantidadInicial());
        }
        tubo.setMotivoVaciado(req.motivo());
        tubo.setNotasVaciado(req.notas());
        tubo.setPosicion(null);
        repository.save(tubo);
    }

    /**
     * Verifica que las posiciones nuevas no colisionen con tubos activos de suero o pool
     * en la misma caja. Se usa desde SueroService y PoolService antes de persistir.
     *
     * @param excludeSueroId  ID del suero al que pertenecen los tubos que se están editando
     *                        (null en create, sueroId en update)
     * @param excludePoolId   ID del pool al que pertenecen los tubos que se están editando
     *                        (null en create, poolId en update)
     * @param excluirTuboIds  IDs de tubos de suero que se consideran agotados y cuya posición
     *                        ya fue liberada (usado en pool create; pasar Set.of() si no aplica)
     */
    public void validarPosicionesSinConflicto(Long cajaId, List<TuboInputDTO> nuevas,
                                              Long excludeSueroId, Long excludePoolId,
                                              Set<Long> excluirTuboIds) {
        Set<String> nuevasPos = nuevas.stream()
                .map(TuboInputDTO::posicion)
                .collect(Collectors.toSet());

        Set<String> ocupadasSuero = repository.findByCajaIdAndSueroActivoTrue(cajaId).stream()
                .filter(t -> excludeSueroId == null || !t.getSuero().getId().equals(excludeSueroId))
                .filter(t -> excluirTuboIds.isEmpty() || !excluirTuboIds.contains(t.getId()))
                .filter(t -> t.getCantidadRestante().compareTo(BigDecimal.ZERO) > 0)
                .map(Tubo::getPosicion)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<String> conflictoSuero = new HashSet<>(nuevasPos);
        conflictoSuero.retainAll(ocupadasSuero);
        if (!conflictoSuero.isEmpty()) {
            throw new BusinessRuleException(
                    "Las posiciones " + String.join(", ", conflictoSuero) + " ya están ocupadas por un suero en esta caja");
        }

        Set<String> ocupadasPool = repository.findByCajaIdAndPoolActivoTrue(cajaId).stream()
                .filter(t -> excludePoolId == null || !t.getPool().getId().equals(excludePoolId))
                .filter(t -> t.getCantidadRestante().compareTo(BigDecimal.ZERO) > 0)
                .map(Tubo::getPosicion)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<String> conflictoPool = new HashSet<>(nuevasPos);
        conflictoPool.retainAll(ocupadasPool);
        if (!conflictoPool.isEmpty()) {
            throw new BusinessRuleException(
                    "Las posiciones " + String.join(", ", conflictoPool) + " ya están ocupadas por un pool en esta caja");
        }
    }
}
