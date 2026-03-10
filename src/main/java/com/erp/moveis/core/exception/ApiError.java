package com.erp.moveis.core.exception;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ApiError {

    private int status;

    private String error;

    private String message;

    private String path;

    private LocalDateTime timestamp;
}
