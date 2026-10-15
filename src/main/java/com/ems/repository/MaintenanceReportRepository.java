package com.ems.repository;

import com.ems.model.MaintenanceReport;
import com.ems.model.enums.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MaintenanceReportRepository extends JpaRepository<MaintenanceReport, Long> {

    List<MaintenanceReport> findByTeacherIdOrderByCreatedAtDesc(Long teacherId);

    List<MaintenanceReport> findBySchoolIdOrderByCreatedAtDesc(Long schoolId);

    List<MaintenanceReport> findBySchoolIdAndStatusOrderByCreatedAtDesc(Long schoolId, ReportStatus status);

    Optional<MaintenanceReport> findByIdAndSchoolId(Long id, Long schoolId);

    long countBySchoolIdAndStatus(Long schoolId, ReportStatus status);

    /** Used for the duplicate-report warning in RQ01. */
    boolean existsByEquipmentIdAndCreatedAtAfter(Long equipmentId, LocalDateTime since);
}
