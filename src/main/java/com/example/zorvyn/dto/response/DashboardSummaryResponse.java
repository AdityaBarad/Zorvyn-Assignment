package com.example.zorvyn.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DashboardSummaryResponse(
        BigDecimal totalIncome,
        BigDecimal totalExpenses,
        BigDecimal netBalance,
        long recordCount,
        LocalDateTime lastUpdated
) {
}

