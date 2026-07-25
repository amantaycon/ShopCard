package com.shopcard.auth.exception;

import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.Map;

@Data
@Builder
public class ErrorResponse {
    private ZonedDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private Map<String, String> validationErrors;
}
