package com.example.zorvyn.controller;

import com.example.zorvyn.dto.request.CreateRecordRequest;
import com.example.zorvyn.dto.response.FinancialRecordResponse;
import com.example.zorvyn.dto.response.PagedResponse;
import com.example.zorvyn.exception.GlobalExceptionHandler;
import com.example.zorvyn.model.enums.RecordType;
import com.example.zorvyn.service.interfaces.FinancialRecordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FinancialRecordController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class FinancialRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @MockitoBean
    private FinancialRecordService recordService;

    @MockitoBean
    private com.example.zorvyn.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private com.example.zorvyn.security.JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private com.example.zorvyn.security.CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createRecord_withAdminRole_returnsCreated() throws Exception {
        // given
        CreateRecordRequest request = new CreateRecordRequest(
                new BigDecimal("120.50"),
                RecordType.INCOME,
                "Sales",
                LocalDate.now(),
                "Quarterly sales"
        );
        FinancialRecordResponse response = new FinancialRecordResponse(
                1L,
                request.getAmount(),
                request.getType().name(),
                request.getCategory(),
                request.getRecordDate(),
                request.getDescription(),
                10L,
                LocalDateTime.now()
        );
        when(recordService.createRecord(any(CreateRecordRequest.class)))
                .thenReturn(response);

        // when
        // then
        mockMvc.perform(post("/api/v1/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(recordService).createRecord(any(CreateRecordRequest.class));
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void createRecord_withViewerRole_returnsForbidden() throws Exception {
        // given
        CreateRecordRequest request = new CreateRecordRequest(
                new BigDecimal("55.00"),
                RecordType.EXPENSE,
                "Travel",
                LocalDate.now(),
                "Taxi"
        );

        // when
        // then
        mockMvc.perform(post("/api/v1/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden());

        verifyNoInteractions(recordService);
    }

    @Test
    @WithMockUser(roles = "ANALYST")
    void getRecords_withAuthenticatedUser_returnsOk() throws Exception {
        // given
        FinancialRecordResponse record = new FinancialRecordResponse(
                5L,
                new BigDecimal("25.00"),
                "EXPENSE",
                "Meals",
                LocalDate.now(),
                "Lunch",
                10L,
                LocalDateTime.now()
        );
        PagedResponse<FinancialRecordResponse> paged = new PagedResponse<>(
                List.of(record), 0, 20, 1, 1, true, true
        );
        when(recordService.getRecords(any(), any(Pageable.class)))
                .thenReturn(paged);

        // when
        // then
        mockMvc.perform(get("/api/v1/records"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].id").value(5));

        verify(recordService).getRecords(any(), any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "ANALYST")
    void getRecords_withFilterParams_returnsOk() throws Exception {
        // given
        FinancialRecordResponse record = new FinancialRecordResponse(
                6L,
                new BigDecimal("75.00"),
                "INCOME",
                "Consulting",
                LocalDate.now(),
                "Retainer",
                10L,
                LocalDateTime.now()
        );
        PagedResponse<FinancialRecordResponse> paged = new PagedResponse<>(
                List.of(record), 0, 20, 1, 1, true, true
        );
        when(recordService.getRecords(any(), any(Pageable.class)))
                .thenReturn(paged);

        // when
        // then
        mockMvc.perform(get("/api/v1/records")
                        .param("type", "INCOME")
                        .param("category", "Consulting")
                        .param("startDate", LocalDate.now().minusDays(7).toString())
                        .param("endDate", LocalDate.now().toString())
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].category").value("Consulting"));

        verify(recordService).getRecords(any(), any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteRecord_withAdminRole_returnsOk() throws Exception {
        // given
        // when
        // then
        mockMvc.perform(delete("/api/v1/records/{id}", 9L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Record deleted successfully"));

        verify(recordService).deleteRecord(9L);
    }

    @Test
    @WithMockUser(roles = "ANALYST")
    void deleteRecord_withAnalystRole_returnsForbidden() throws Exception {
        // given
        // when
        // then
        mockMvc.perform(delete("/api/v1/records/{id}", 9L))
                .andDo(print())
                .andExpect(status().isForbidden());

        verifyNoInteractions(recordService);
    }
}
