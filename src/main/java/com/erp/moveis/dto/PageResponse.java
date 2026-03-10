package com.erp.moveis.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PageResponse<T> {
    private java.util.List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public static <T> PageResponse<T> of(org.springframework.data.domain.Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }
}
