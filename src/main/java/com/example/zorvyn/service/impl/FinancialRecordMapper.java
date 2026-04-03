package com.example.zorvyn.service.impl;

import com.example.zorvyn.dto.response.FinancialRecordResponse;
import com.example.zorvyn.model.entity.FinancialRecord;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FinancialRecordMapper {

    @Mapping(source = "createdBy.id", target = "createdBy")
    @Mapping(target = "type",
            expression = "java(record.getType() != null ? record.getType().name() : null)")
    FinancialRecordResponse toResponse(FinancialRecord record);

    List<FinancialRecordResponse> toResponseList(List<FinancialRecord> records);
}

