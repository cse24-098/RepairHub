package com.ems.repository;

import com.ems.model.ReportStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportStatusHistoryRepository extends JpaRepository<ReportStatusHistory, Long> {
    List<ReportStatusHistory> findByReportIdOrderByChangedAtAsc(Long reportId);
}
