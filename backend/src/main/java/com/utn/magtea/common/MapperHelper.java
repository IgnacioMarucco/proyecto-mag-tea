package com.utn.magtea.common;

import com.utn.magtea.tubo.Tubo;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Component
public class MapperHelper {

    private final Clock clock;

    public MapperHelper(Clock clock) {
        this.clock = clock;
    }

    @Named("calculateAgeYears")
    public Integer calculateAgeYears(LocalDate fechaNacimiento) {
        if (fechaNacimiento == null) return null;
        return Period.between(fechaNacimiento, LocalDate.now(clock)).getYears();
    }

    @Named("calculateAgeMonths")
    public Integer calculateAgeMonths(LocalDate fechaNacimiento) {
        if (fechaNacimiento == null) return null;
        return Period.between(fechaNacimiento, LocalDate.now(clock)).getMonths();
    }

    @Named("sumCantidadInicial")
    public BigDecimal sumCantidadInicial(List<Tubo> tubos) {
        if (tubos == null) return BigDecimal.ZERO;
        return tubos.stream().map(Tubo::getCantidadInicial).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Named("sumCantidadRestante")
    public BigDecimal sumCantidadRestante(List<Tubo> tubos) {
        if (tubos == null) return BigDecimal.ZERO;
        return tubos.stream().map(Tubo::getCantidadRestante).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
