package com.example.zorvyn.service.impl;

import com.example.zorvyn.dto.request.CreateRecordRequest;
import com.example.zorvyn.dto.request.RecordFilterRequest;
import com.example.zorvyn.dto.request.UpdateRecordRequest;
import com.example.zorvyn.dto.response.FinancialRecordResponse;
import com.example.zorvyn.dto.response.PagedResponse;
import com.example.zorvyn.exception.InvalidOperationException;
import com.example.zorvyn.exception.ResourceNotFoundException;
import com.example.zorvyn.model.entity.FinancialRecord;
import com.example.zorvyn.model.entity.User;
import com.example.zorvyn.repository.FinancialRecordRepository;
import com.example.zorvyn.repository.RecordSpecification;
import com.example.zorvyn.repository.UserRepository;
import com.example.zorvyn.service.interfaces.AuditService;
import com.example.zorvyn.service.interfaces.FinancialRecordService;
import com.example.zorvyn.util.AppConstants;
import com.example.zorvyn.util.SecurityUtils;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinancialRecordServiceImpl implements FinancialRecordService {

    private final FinancialRecordRepository recordRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final FinancialRecordMapper recordMapper;

    @Override
    @Transactional
    public FinancialRecordResponse createRecord(CreateRecordRequest request) {
        String email = SecurityUtils.getCurrentUserEmail();
        User creator = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

        FinancialRecord record = FinancialRecord.builder()
                .amount(request.getAmount())
                .type(request.getType())
                .category(request.getCategory())
                .recordDate(request.getRecordDate())
                .description(request.getDescription())
                .createdBy(creator)
                .build();

        FinancialRecord saved = recordRepository.save(record);
        auditService.log("RECORD_CREATED", "FINANCIAL_RECORD", saved.getId(),
                "Record created: " + saved.getType() + " " + saved.getAmount());
        return recordMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public FinancialRecordResponse getRecordById(Long id) {
        FinancialRecord record = recordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FinancialRecord", id));
        return recordMapper.toResponse(record);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<FinancialRecordResponse> getRecords(
            RecordFilterRequest filter, Pageable pageable) {
        if (pageable.getPageSize() > AppConstants.MAX_PAGE_SIZE) {
            throw new InvalidOperationException(
                    "Page size must not exceed " + AppConstants.MAX_PAGE_SIZE);
        }

        Specification<FinancialRecord> spec = Specification
                .where(RecordSpecification.byType(filter.getType()))
                .and(RecordSpecification.byCategory(filter.getCategory()))
                .and(RecordSpecification.byDateBetween(filter.getStartDate(), filter.getEndDate()));

        Page<FinancialRecord> page = recordRepository.findAll(spec, pageable);
        List<FinancialRecordResponse> content = recordMapper.toResponseList(page.getContent());

        return new PagedResponse<>(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(),
                page.isFirst(), page.isLast());
    }

    @Override
    @Transactional
    public FinancialRecordResponse updateRecord(Long id, UpdateRecordRequest request) {
        FinancialRecord record = recordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FinancialRecord", id));

        if (request.getAmount() != null) {
            record.setAmount(request.getAmount());
        }
        if (request.getType() != null) {
            record.setType(request.getType());
        }
        if (request.getCategory() != null) {
            record.setCategory(request.getCategory());
        }
        if (request.getRecordDate() != null) {
            record.setRecordDate(request.getRecordDate());
        }
        if (request.getDescription() != null) {
            record.setDescription(request.getDescription());
        }

        FinancialRecord saved = recordRepository.save(record);
        auditService.log("RECORD_UPDATED", "FINANCIAL_RECORD", id, "Record updated");
        return recordMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteRecord(Long id) {
        FinancialRecord record = recordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FinancialRecord", id));
        record.setDeletedAt(LocalDateTime.now());
        recordRepository.save(record);
        auditService.log("RECORD_DELETED", "FINANCIAL_RECORD", id, "Record soft-deleted");
        log.info("Soft-deleted financial record id: {}", id);
    }
}
