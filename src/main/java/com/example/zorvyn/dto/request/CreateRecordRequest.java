package com.example.zorvyn.dto.request;

import com.example.zorvyn.model.enums.RecordType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreateRecordRequest {

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;

    @NotNull
    private RecordType type;

    @NotBlank
    @Size(max = 100)
    private String category;

    @NotNull
    @PastOrPresent
    private LocalDate recordDate;

    @Size(max = 500)
    private String description;
}

