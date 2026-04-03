package com.example.zorvyn.repository;

import com.example.zorvyn.model.entity.FinancialRecord;
import com.example.zorvyn.model.enums.RecordType;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FinancialRecordRepository
        extends JpaRepository<FinancialRecord, Long>,
        JpaSpecificationExecutor<FinancialRecord> {

    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM FinancialRecord r " +
            "WHERE r.type = :type AND r.deletedAt IS NULL")
    BigDecimal sumByType(@Param("type") RecordType type);

    @Query("SELECT r.category, SUM(r.amount) FROM FinancialRecord r " +
            "WHERE r.deletedAt IS NULL GROUP BY r.category ORDER BY SUM(r.amount) DESC")
    List<Object[]> groupByCategory();

    @Query("SELECT EXTRACT(MONTH FROM r.recordDate), r.type, SUM(r.amount) " +
            "FROM FinancialRecord r " +
            "WHERE EXTRACT(YEAR FROM r.recordDate) = :year AND r.deletedAt IS NULL " +
            "GROUP BY EXTRACT(MONTH FROM r.recordDate), r.type " +
            "ORDER BY EXTRACT(MONTH FROM r.recordDate)")
    List<Object[]> groupByMonth(@Param("year") int year);
}
