package com.example.zorvyn;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.zorvyn.config.SecurityConfig;
import com.example.zorvyn.controller.FinancialRecordController;
import com.example.zorvyn.dto.request.CreateRecordRequest;
import com.example.zorvyn.dto.response.FinancialRecordResponse;
import com.example.zorvyn.dto.response.PagedResponse;
import com.example.zorvyn.exception.GlobalExceptionHandler;
import com.example.zorvyn.model.enums.RecordType;
import com.example.zorvyn.security.CustomUserDetailsService;
import com.example.zorvyn.security.JwtAuthenticationFilter;
import com.example.zorvyn.security.JwtTokenProvider;
import com.example.zorvyn.service.interfaces.FinancialRecordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(FinancialRecordController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class FinancialRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FinancialRecordService recordService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ANALYST")
    void createRecord_asAnalyst_returns403() throws Exception {
        CreateRecordRequest req = new CreateRecordRequest(
                new BigDecimal("1500.00"), RecordType.INCOME,
                "Salary", LocalDate.now(), "Monthly salary");

        mockMvc.perform(post("/api/v1/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void createRecord_asViewer_returns403() throws Exception {
        CreateRecordRequest req = new CreateRecordRequest(
                new BigDecimal("500.00"), RecordType.EXPENSE,
                "Food", LocalDate.now(), "Lunch");

        mockMvc.perform(post("/api/v1/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void getRecords_asViewer_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/records"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ANALYST")
    void getRecords_withTypeFilter_returns200() throws Exception {
        PagedResponse<FinancialRecordResponse> paged = new PagedResponse<>(
                List.of(), 0, 20, 0, 0, true, true);
        given(recordService.getRecords(any(), any())).willReturn(paged);

        mockMvc.perform(get("/api/v1/records")
                        .param("type", "INCOME")
                        .param("category", "Salary"))
                .andExpect(status().isOk());
    }
}
