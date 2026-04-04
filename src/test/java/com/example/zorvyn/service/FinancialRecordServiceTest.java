package com.example.zorvyn.service;

import com.example.zorvyn.dto.request.CreateRecordRequest;
import com.example.zorvyn.dto.request.RecordFilterRequest;
import com.example.zorvyn.dto.response.FinancialRecordResponse;
import com.example.zorvyn.dto.response.PagedResponse;
import com.example.zorvyn.exception.ResourceNotFoundException;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private FinancialRecordServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createRecord_savesEntityAndLogsAudit() {
        // given
        CreateRecordRequest request = new CreateRecordRequest(
                new BigDecimal("150.00"),
                RecordType.INCOME,
                "Sales",
                LocalDate.now(),
                "Invoice"
        );
        User creator = User.builder().id(5L).email("creator@example.com").build();
        FinancialRecord saved = FinancialRecord.builder()
                .id(20L)
                .amount(request.getAmount())
                .type(request.getType())
                .category(request.getCategory())
                .recordDate(request.getRecordDate())
                .description(request.getDescription())
                .createdBy(creator)
                .build();
        FinancialRecordResponse response = new FinancialRecordResponse(
                20L,
                request.getAmount(),
                request.getType().name(),
                request.getCategory(),
                request.getRecordDate(),
                request.getDescription(),
                5L,
                LocalDateTime.now()
        );

        SecurityContext context = org.mockito.Mockito.mock(SecurityContext.class);
        org.springframework.security.core.Authentication authentication =
                org.mockito.Mockito.mock(org.springframework.security.core.Authentication.class);
        when(authentication.getName()).thenReturn("creator@example.com");
        when(authentication.getPrincipal()).thenReturn("creator@example.com");
        when(authentication.isAuthenticated()).thenReturn(true);
        when(context.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(context);

        when(userRepository.findByEmailAndDeletedAtIsNull("creator@example.com"))
                .thenReturn(Optional.of(creator));
        when(recordRepository.save(any(FinancialRecord.class))).thenReturn(saved);
        when(recordMapper.toResponse(saved)).thenReturn(response);

        // when
        FinancialRecordResponse result = service.createRecord(request);

        // then
        assertEquals(20L, result.id());
        verify(recordRepository).save(any(FinancialRecord.class));
        verify(auditService).log(eq("RECORD_CREATED"), eq("FINANCIAL_RECORD"), eq(20L), any());
    }

    @Test
    void deleteRecord_setsDeletedAtAndDoesNotCallDelete() {
        // given
        FinancialRecord record = FinancialRecord.builder().id(99L).build();
        when(recordRepository.findById(99L)).thenReturn(Optional.of(record));

        // when
        service.deleteRecord(99L);

        // then
        assertNotNull(record.getDeletedAt());
        verify(recordRepository, never()).delete(any(FinancialRecord.class));
        verify(recordRepository).save(record);
        verify(auditService).log(eq("RECORD_DELETED"), eq("FINANCIAL_RECORD"), eq(99L), any());
    }

    @Test
    void getRecordById_whenNotFound_throwsException() {
        // given
        when(recordRepository.findById(404L)).thenReturn(Optional.empty());

        // when
        // then
        assertThrows(ResourceNotFoundException.class, () -> service.getRecordById(404L));
    }

    @Test
    void getRecords_passesSpecificationToRepository() {
        // given
        RecordFilterRequest filter = new RecordFilterRequest(RecordType.EXPENSE, "Office", null, null);
        Pageable pageable = PageRequest.of(0, 10);
        Page<FinancialRecord> page = new PageImpl<>(List.of());
        when(recordRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(page);
        when(recordMapper.toResponseList(page.getContent())).thenReturn(List.of());

        // when
        PagedResponse<FinancialRecordResponse> result = service.getRecords(filter, pageable);

        // then
        assertEquals(0, result.content().size());
        verify(recordRepository).findAll(any(Specification.class), eq(pageable));
    }
}
