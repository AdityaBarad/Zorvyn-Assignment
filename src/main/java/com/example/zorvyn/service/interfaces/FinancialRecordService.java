package com.example.zorvyn.service.interfaces;

import com.example.zorvyn.dto.request.CreateRecordRequest;
import com.example.zorvyn.dto.request.RecordFilterRequest;
import com.example.zorvyn.dto.request.UpdateRecordRequest;
import com.example.zorvyn.dto.response.FinancialRecordResponse;
import com.example.zorvyn.dto.response.PagedResponse;
import org.springframework.data.domain.Pageable;

public interface FinancialRecordService {
    FinancialRecordResponse createRecord(CreateRecordRequest request);

    FinancialRecordResponse getRecordById(Long id);

    PagedResponse<FinancialRecordResponse> getRecords(RecordFilterRequest filter, Pageable pageable);

    FinancialRecordResponse updateRecord(Long id, UpdateRecordRequest request);

    void deleteRecord(Long id);
}

