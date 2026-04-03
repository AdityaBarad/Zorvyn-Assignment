package com.example.zorvyn.dto.response;

import java.math.BigDecimal;

public record CategorySummaryResponse(
        String category,
        BigDecimal total
) {
}

