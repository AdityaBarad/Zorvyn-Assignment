package com.example.zorvyn.controller;

import com.example.zorvyn.dto.request.CreateRecordRequest;
import com.example.zorvyn.dto.request.RecordFilterRequest;
import com.example.zorvyn.dto.request.UpdateRecordRequest;
import com.example.zorvyn.dto.response.ApiResponse;
import com.example.zorvyn.dto.response.FinancialRecordResponse;
import com.example.zorvyn.dto.response.PagedResponse;
import com.example.zorvyn.service.interfaces.FinancialRecordService;
import com.example.zorvyn.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/records")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Financial Records", description = "Manage financial transactions")
public class FinancialRecordController {

    private final FinancialRecordService recordService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    @Operation(summary = "Create financial record")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Record created")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error", content = @Content)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Viewers cannot create records", content = @Content)
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<FinancialRecordResponse>> createRecord(
            @Valid @RequestBody CreateRecordRequest request) {
        log.info("REQUEST: POST /api/v1/records by: {}", SecurityUtils.getCurrentUserEmail());
        FinancialRecordResponse response = recordService.createRecord(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Record created successfully"));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','VIEWER')")
    @Operation(summary = "Get records with optional filters and pagination",
            description = "Filter by type, category, startDate, endDate. Dates format: yyyy-MM-dd")
    @Parameter(name = "type", description = "INCOME or EXPENSE")
    @Parameter(name = "category", description = "Category keyword search")
    @Parameter(name = "startDate", description = "Start date (yyyy-MM-dd)")
    @Parameter(name = "endDate", description = "End date (yyyy-MM-dd)")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Records retrieved")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<PagedResponse<FinancialRecordResponse>>> getRecords(
            @Valid @ModelAttribute RecordFilterRequest filter,
            @PageableDefault(size = 20, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("REQUEST: GET /api/v1/records by: {}", SecurityUtils.getCurrentUserEmail());
        return ResponseEntity.ok(
                ApiResponse.success(recordService.getRecords(filter, pageable), "Records retrieved"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','VIEWER')")
    @Operation(summary = "Get record by ID")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Record found")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Record not found", content = @Content)
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<FinancialRecordResponse>> getRecordById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(recordService.getRecordById(id), "Record retrieved"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    @Operation(summary = "Update financial record")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Record updated")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Record not found", content = @Content)
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<FinancialRecordResponse>> updateRecord(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRecordRequest request) {
        log.info("REQUEST: PUT /api/v1/records/{} by: {}", id, SecurityUtils.getCurrentUserEmail());
        return ResponseEntity.ok(
                ApiResponse.success(recordService.updateRecord(id, request), "Record updated"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete record (soft delete)")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Record deleted")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Only admins can delete", content = @Content)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Record not found", content = @Content)
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<Void>> deleteRecord(@PathVariable Long id) {
        log.info("REQUEST: DELETE /api/v1/records/{} by: {}", id, SecurityUtils.getCurrentUserEmail());
        recordService.deleteRecord(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Record deleted successfully"));
    }
}
