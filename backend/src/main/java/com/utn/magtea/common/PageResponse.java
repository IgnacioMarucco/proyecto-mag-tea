package com.utn.magtea.common;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        long    totalElements,
        int     totalPages,
        int     page,
        int     size
) {}
