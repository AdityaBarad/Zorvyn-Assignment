package com.example.zorvyn.exception;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {

    private boolean success;
    private int status;
    private String error;
    private String message;
    private List<FieldError> errors;
    private String path;
    private String timestamp;

    @Data
    @AllArgsConstructor
    public static class FieldError {
        private String field;
        private String message;
    }
}
