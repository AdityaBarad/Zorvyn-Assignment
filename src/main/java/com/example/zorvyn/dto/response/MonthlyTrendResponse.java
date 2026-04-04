package com.example.zorvyn.dto.response;

import java.math.BigDecimal;

public record MonthlyTrendResponse(
        int month,
        String monthName,
        BigDecimal income,
        BigDecimal expense
) {
}

