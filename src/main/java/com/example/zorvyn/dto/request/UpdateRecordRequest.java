package com.example.zorvyn.dto.request;

import com.example.zorvyn.model.enums.RecordType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRecordRequest {

    @DecimalMin("0.01")
    private BigDecimal amount;

    private RecordType type;

    @Size(max = 100)
    private String category;

    @PastOrPresent
    private LocalDate recordDate;

    @Size(max = 500)
    private String description;
}

