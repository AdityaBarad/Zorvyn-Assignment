package com.example.zorvyn.service.impl;

import com.example.zorvyn.dto.response.CategorySummaryResponse;
import com.example.zorvyn.dto.response.DashboardSummaryResponse;
import com.example.zorvyn.dto.response.FinancialRecordResponse;
import com.example.zorvyn.dto.response.MonthlyTrendResponse;
import com.example.zorvyn.model.enums.RecordType;
import com.example.zorvyn.repository.FinancialRecordRepository;
import com.example.zorvyn.service.interfaces.DashboardService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final FinancialRecordRepository recordRepository;
    private final FinancialRecordMapper recordMapper;

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary() {
        BigDecimal totalIncome = recordRepository.sumByType(RecordType.INCOME);
        BigDecimal totalExpenses = recordRepository.sumByType(RecordType.EXPENSE);
        if (totalIncome == null) {
            totalIncome = BigDecimal.ZERO;
        }
        if (totalExpenses == null) {
            totalExpenses = BigDecimal.ZERO;
        }
        BigDecimal netBalance = totalIncome.subtract(totalExpenses);
        long count = recordRepository.count();
        return new DashboardSummaryResponse(totalIncome, totalExpenses,
                netBalance, count, LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategorySummaryResponse> getCategoryBreakdown() {
        return recordRepository.groupByCategory().stream()
                .map(row -> new CategorySummaryResponse(
                        (String) row[0],
                        ((Number) row[1]).doubleValue() != 0 ?
                                new BigDecimal(row[1].toString()) : BigDecimal.ZERO))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MonthlyTrendResponse> getMonthlyTrends(int year) {
        List<Object[]> rows = recordRepository.groupByMonth(year);

        Map<Integer, BigDecimal[]> monthMap = new HashMap<>();
        for (int i = 1; i <= 12; i++) {
            monthMap.put(i, new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
        }

        for (Object[] row : rows) {
            int month = ((Number) row[0]).intValue();
            RecordType type = RecordType.valueOf(row[1].toString());
            BigDecimal amount = new BigDecimal(row[2].toString());
            BigDecimal[] entry = monthMap.get(month);
            if (type == RecordType.INCOME) {
                entry[0] = amount;
            } else {
                entry[1] = amount;
            }
        }

        return monthMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new MonthlyTrendResponse(
                        e.getKey(),
                        Month.of(e.getKey()).getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                        e.getValue()[0], e.getValue()[1]))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FinancialRecordResponse> getRecentActivity(int limit) {
        int safeLimit = Math.min(limit, 50);
        Pageable pageable = PageRequest.of(0, safeLimit,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return recordMapper.toResponseList(
                recordRepository.findAll(pageable).getContent());
    }
}

