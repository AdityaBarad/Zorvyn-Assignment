package com.example.zorvyn.controller;

import com.example.zorvyn.dto.response.ApiResponse;
import com.example.zorvyn.dto.response.CategorySummaryResponse;
import com.example.zorvyn.dto.response.DashboardSummaryResponse;
import com.example.zorvyn.dto.response.FinancialRecordResponse;
import com.example.zorvyn.dto.response.MonthlyTrendResponse;
import com.example.zorvyn.service.interfaces.DashboardService;
import com.example.zorvyn.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Dashboard", description = "Aggregated analytics and summary data")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','VIEWER')")
    @Operation(summary = "Get dashboard summary",
            description = "Returns total income, expenses, net balance, and record count")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Summary retrieved")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getSummary() {
        log.info("REQUEST: GET /api/v1/dashboard/summary by: {}", SecurityUtils.getCurrentUserEmail());
        return ResponseEntity.ok(
                ApiResponse.success(dashboardService.getSummary(), "Summary retrieved"));
    }

    @GetMapping("/categories")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    @Operation(summary = "Get category-wise breakdown")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Breakdown retrieved")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<List<CategorySummaryResponse>>> getCategoryBreakdown() {
        return ResponseEntity.ok(
                ApiResponse.success(dashboardService.getCategoryBreakdown(), "Category breakdown retrieved"));
    }

    @GetMapping("/trends/{year}")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    @Operation(summary = "Get monthly income vs expense trends for a year")
    @Parameter(name = "year", description = "4-digit year e.g. 2024")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Trends retrieved")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<List<MonthlyTrendResponse>>> getMonthlyTrends(
            @PathVariable int year) {
        return ResponseEntity.ok(
                ApiResponse.success(dashboardService.getMonthlyTrends(year), "Monthly trends retrieved"));
    }

    @GetMapping("/recent")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','VIEWER')")
    @Operation(summary = "Get recent financial activity")
    @Parameter(name = "limit", description = "Number of records to return (max 50)")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Recent activity retrieved")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<List<FinancialRecordResponse>>> getRecentActivity(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(
                ApiResponse.success(dashboardService.getRecentActivity(limit), "Recent activity retrieved"));
    }
}
