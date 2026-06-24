package com.utn.magtea.common;

import com.utn.magtea.common.exception.BusinessRuleException;

import java.util.UUID;
import java.util.function.Predicate;

public final class CodigoUtil {

    private CodigoUtil() {}

    public static String generarCodigo(Predicate<String> exists) {
        String codigo;
        int intentos = 0;
        do {
            if (++intentos > 10) {
                throw new BusinessRuleException("No se pudo generar un código único. Intente nuevamente.");
            }
            codigo = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        } while (exists.test(codigo));
        return codigo;
    }
}
