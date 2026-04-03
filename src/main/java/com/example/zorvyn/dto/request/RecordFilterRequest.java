package com.example.zorvyn.dto.request;

import com.example.zorvyn.model.enums.RecordType;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ValidDateRange
public class RecordFilterRequest {

    private RecordType type;
    private String category;
    private LocalDate startDate;
    private LocalDate endDate;
}

