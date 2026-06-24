package com.utn.magtea.common;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.Map;
import java.util.Set;

public final class SpecificationUtils {

    private SpecificationUtils() {}

    public static <T> Specification<T> activoTrue() {
        return (root, query, cb) -> cb.isTrue(root.get("activo"));
    }

    public static Sort buildSort(String sortBy, String sortDir, String defaultField, Set<String> validFields) {
        return buildSort(sortBy, sortDir, defaultField, validFields, Map.of());
    }

    public static Sort buildSort(String sortBy, String sortDir, String defaultField,
                                 Set<String> validFields, Map<String, String> fieldAliases) {
        String field = validFields.contains(sortBy) ? sortBy : defaultField;
        field = fieldAliases.getOrDefault(field, field);
        Sort.Direction dir = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(dir, field);
    }
}
