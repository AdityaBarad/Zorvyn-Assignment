package com.example.zorvyn.service.interfaces;

import com.example.zorvyn.dto.response.CategorySummaryResponse;
import com.example.zorvyn.dto.response.DashboardSummaryResponse;
import com.example.zorvyn.dto.response.FinancialRecordResponse;
import com.example.zorvyn.dto.response.MonthlyTrendResponse;
import java.util.List;

public interface DashboardService {
    DashboardSummaryResponse getSummary();

    List<CategorySummaryResponse> getCategoryBreakdown();

    List<MonthlyTrendResponse> getMonthlyTrends(int year);

    List<FinancialRecordResponse> getRecentActivity(int limit);
}

