package com.example.zorvyn.repository;

import com.example.zorvyn.model.entity.FinancialRecord;
import com.example.zorvyn.model.enums.RecordType;
import java.time.LocalDate;
import org.springframework.data.jpa.domain.Specification;

public class RecordSpecification {

    public static Specification<FinancialRecord> byType(RecordType type) {
        return (root, query, cb) ->
                type == null ? null : cb.equal(root.get("type"), type);
    }

    public static Specification<FinancialRecord> byCategory(String category) {
        return (root, query, cb) ->
                (category == null || category.isBlank()) ? null :
                        cb.like(cb.lower(root.get("category")), "%" + category.toLowerCase() + "%");
    }

    public static Specification<FinancialRecord> byDateBetween(LocalDate start, LocalDate end) {
        return (root, query, cb) -> {
            if (start == null && end == null) {
                return null;
            }
            if (start == null) {
                return cb.lessThanOrEqualTo(root.get("recordDate"), end);
            }
            if (end == null) {
                return cb.greaterThanOrEqualTo(root.get("recordDate"), start);
            }
            return cb.between(root.get("recordDate"), start, end);
        };
    }

    public static Specification<FinancialRecord> byCreatedBy(Long userId) {
        return (root, query, cb) ->
                userId == null ? null : cb.equal(root.get("createdBy").get("id"), userId);
    }
}

