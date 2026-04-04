package com.example.zorvyn;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.zorvyn.dto.request.CreateRecordRequest;
import com.example.zorvyn.dto.request.RecordFilterRequest;
import com.example.zorvyn.dto.response.FinancialRecordResponse;
import com.example.zorvyn.dto.response.PagedResponse;
import com.example.zorvyn.model.entity.FinancialRecord;
import com.example.zorvyn.model.entity.User;
import com.example.zorvyn.model.enums.RecordType;
import com.example.zorvyn.repository.FinancialRecordRepository;
import com.example.zorvyn.repository.UserRepository;
import com.example.zorvyn.service.impl.FinancialRecordMapper;
import com.example.zorvyn.service.impl.FinancialRecordServiceImpl;
import com.example.zorvyn.service.interfaces.AuditService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class FinancialRecordServiceTest {

    @Mock
    private FinancialRecordRepository recordRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private FinancialRecordMapper recordMapper;

    @InjectMocks
    private FinancialRecordServiceImpl recordService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createRecord_savesEntity() {
        CreateRecordRequest req = new CreateRecordRequest(
                new BigDecimal("100.00"), RecordType.INCOME,
                "Salary", LocalDate.now(), "Test");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("test@test.com", null, List.of()));

        User mockUser = User.builder().id(1L).email("test@test.com").build();
        FinancialRecord savedRecord = FinancialRecord.builder()
                .id(1L).amount(req.getAmount()).type(req.getType())
                .category(req.getCategory()).build();
        FinancialRecordResponse mockResponse = new FinancialRecordResponse(
                1L, req.getAmount(), "INCOME", "Salary",
                LocalDate.now(), "Test", 1L, LocalDateTime.now());

        when(userRepository.findByEmailAndDeletedAtIsNull("test@test.com"))
                .thenReturn(Optional.of(mockUser));
        when(recordRepository.save(any())).thenReturn(savedRecord);
        when(recordMapper.toResponse(any())).thenReturn(mockResponse);

        FinancialRecordResponse result = recordService.createRecord(req);

        verify(recordRepository, times(1)).save(any(FinancialRecord.class));
        assertNotNull(result);
    }

    @Test
    void createRecord_logsAudit() {
        CreateRecordRequest req = new CreateRecordRequest(
                new BigDecimal("200.00"), RecordType.EXPENSE,
                "Food", LocalDate.now(), "Dinner");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("test@test.com", null, List.of()));

        User mockUser = User.builder().id(1L).email("test@test.com").build();
        FinancialRecord savedRecord = FinancialRecord.builder().id(2L).build();
        FinancialRecordResponse mockResp = new FinancialRecordResponse(
                2L, req.getAmount(), "EXPENSE", "Food",
                LocalDate.now(), "Dinner", 1L, LocalDateTime.now());

        when(userRepository.findByEmailAndDeletedAtIsNull(any()))
                .thenReturn(Optional.of(mockUser));
        when(recordRepository.save(any())).thenReturn(savedRecord);
        when(recordMapper.toResponse(any())).thenReturn(mockResp);

        recordService.createRecord(req);

        verify(auditService, times(1)).log(
                eq("RECORD_CREATED"), eq("FINANCIAL_RECORD"), any(), any());
    }

    @Test
    void deleteRecord_setsSoftDeleteTimestamp() {
        FinancialRecord record = FinancialRecord.builder()
                .id(1L).deletedAt(null).build();
        when(recordRepository.findById(1L)).thenReturn(Optional.of(record));
        when(recordRepository.save(any())).thenReturn(record);

        recordService.deleteRecord(1L);

        assertNotNull(record.getDeletedAt());
        verify(recordRepository, never()).delete(any());
    }

    @Test
    void getRecords_passesSpecificationToRepository() {
        RecordFilterRequest filter = new RecordFilterRequest();
        filter.setType(RecordType.INCOME);
        Pageable pageable = PageRequest.of(0, 10);

        Page<FinancialRecord> emptyPage = Page.empty(pageable);
        when(recordRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(emptyPage);
        when(recordMapper.toResponseList(any())).thenReturn(List.of());

        PagedResponse<FinancialRecordResponse> result =
                recordService.getRecords(filter, pageable);

        verify(recordRepository).findAll(any(Specification.class), eq(pageable));
        assertNotNull(result);
    }
}

