package com.example.zorvyn.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record FinancialRecordResponse(
        Long id,
        BigDecimal amount,
        String type,
        String category,
        LocalDate recordDate,
        String description,
        Long createdBy,
        LocalDateTime createdAt
) {
}

