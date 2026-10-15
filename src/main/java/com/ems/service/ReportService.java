package com.ems.service;

import com.ems.dto.ReportForm;
import com.ems.dto.StatusUpdateForm;
import com.ems.model.*;
import com.ems.model.enums.EquipmentStatus;
import com.ems.model.enums.ReportStatus;
import com.ems.repository.*;
import com.ems.security.AppUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ReportService {

    private final MaintenanceReportRepository reportRepository;
    private final ReportStatusHistoryRepository historyRepository;
    private final EquipmentRepository equipmentRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;

    public ReportService(MaintenanceReportRepository reportRepository,
                         ReportStatusHistoryRepository historyRepository,
                         EquipmentRepository equipmentRepository,
                         LocationRepository locationRepository,
                         UserRepository userRepository,
                         SchoolRepository schoolRepository) {
        this.reportRepository = reportRepository;
        this.historyRepository = historyRepository;
        this.equipmentRepository = equipmentRepository;
        this.locationRepository = locationRepository;
        this.userRepository = userRepository;
        this.schoolRepository = schoolRepository;
    }

    // ---------- RQ01: submit ----------

    /** Is there already a report for this equipment in the last 7 days? */
    @Transactional(readOnly = true)
    public boolean isDuplicate(ReportForm form, AppUserDetails user) {
        return reportRepository.existsByEquipmentIdAndCreatedAtAfter(
                form.getEquipmentId(), LocalDateTime.now().minusDays(7));
    }

    @Transactional
    public MaintenanceReport submit(ReportForm form, AppUserDetails user) {
        Equipment equipment = equipmentRepository
                .findByIdAndSchoolId(form.getEquipmentId(), user.getSchoolId())
                .orElseThrow(() -> new IllegalArgumentException("Equipment not found"));

        MaintenanceReport report = new MaintenanceReport();
        report.setReferenceNo(generateReferenceNo());
        report.setSchool(schoolRepository.getReferenceById(user.getSchoolId()));
        report.setTeacher(userRepository.getReferenceById(user.getId()));
        report.setEquipment(equipment);
        if (form.getLocationId() != null) {
            report.setLocation(locationRepository.findByIdAndSchoolId(form.getLocationId(), user.getSchoolId())
                    .orElseThrow(() -> new IllegalArgumentException("Location not found")));
        }
        report.setIssueType(form.getIssueType());
        report.setDescription(form.getDescription().trim());
        report.setPriority(form.getPriority());
        report.setStatus(ReportStatus.PENDING);
        report.setLocationDetails(form.getLocationDetails());
        report.setLatitude(form.getLatitude());
        report.setLongitude(form.getLongitude());
        report.setCreatedAt(LocalDateTime.now());
        report = reportRepository.save(report);

        saveHistory(report, user.getId(), null, ReportStatus.PENDING, "Report submitted");

        // Equipment state diagram: Available/In use --fault reported--> Damaged
        if (equipment.getStatus() != EquipmentStatus.UNDER_MAINTENANCE
                && equipment.getStatus() != EquipmentStatus.RETIRED) {
            equipment.setStatus(EquipmentStatus.DAMAGED);
        }
        // TODO RQ02: save uploaded photos/videos (ReportAttachment)
        return report;
    }

    // ---------- RQ04 / RQ05: read ----------

    @Transactional(readOnly = true)
    public List<MaintenanceReport> myReports(Long teacherId) {
        return reportRepository.findByTeacherIdOrderByCreatedAtDesc(teacherId);
    }

    @Transactional(readOnly = true)
    public List<MaintenanceReport> schoolReports(Long schoolId, ReportStatus status) {
        if (status == null) {
            return reportRepository.findBySchoolIdOrderByCreatedAtDesc(schoolId);
        }
        return reportRepository.findBySchoolIdAndStatusOrderByCreatedAtDesc(schoolId, status);
    }

    @Transactional(readOnly = true)
    public MaintenanceReport getReport(Long id, AppUserDetails user) {
        MaintenanceReport report = reportRepository.findByIdAndSchoolId(id, user.getSchoolId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found"));
        // Teachers may only open their own reports; staff may open any report in their school.
        if (!user.isStaff() && !report.getTeacher().getId().equals(user.getId())) {
            throw new AccessDeniedException("Not your report");
        }
        return report;
    }

    @Transactional(readOnly = true)
    public List<ReportStatusHistory> getHistory(Long reportId) {
        return historyRepository.findByReportIdOrderByChangedAtAsc(reportId);
    }

    /** RQ11: unresolved = PENDING + IN_PROGRESS. */
    @Transactional(readOnly = true)
    public long countUnresolved(Long schoolId) {
        return reportRepository.countBySchoolIdAndStatus(schoolId, ReportStatus.PENDING)
             + reportRepository.countBySchoolIdAndStatus(schoolId, ReportStatus.IN_PROGRESS);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> countByStatus(Long schoolId) {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (ReportStatus s : ReportStatus.values()) {
            counts.put(s.name(), reportRepository.countBySchoolIdAndStatus(schoolId, s));
        }
        return counts;
    }

    // ---------- RQ06: update status ----------

    @Transactional
    public void updateStatus(Long reportId, StatusUpdateForm form, AppUserDetails user) {
        MaintenanceReport report = reportRepository.findByIdAndSchoolId(reportId, user.getSchoolId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found"));

        ReportStatus oldStatus = report.getStatus();
        ReportStatus newStatus = form.getNewStatus();
        if (oldStatus == newStatus) {
            throw new IllegalArgumentException("Report is already " + oldStatus);
        }

        report.setStatus(newStatus);
        report.setResolvedAt(newStatus == ReportStatus.RESOLVED ? LocalDateTime.now() : null);
        saveHistory(report, user.getId(), oldStatus, newStatus, form.getComment().trim());

        // Keep the equipment status in step with the report (equipment state diagram)
        Equipment equipment = report.getEquipment();
        if (newStatus == ReportStatus.IN_PROGRESS) {
            equipment.setStatus(EquipmentStatus.UNDER_MAINTENANCE);
        } else if (newStatus == ReportStatus.RESOLVED || newStatus == ReportStatus.REJECTED) {
            equipment.setStatus(EquipmentStatus.AVAILABLE);
        }

        // TODO RQ06/RQ07: create a Notification row and email the teacher (log result in EmailLog)
    }

    // ---------- helpers ----------

    private void saveHistory(MaintenanceReport report, Long userId, ReportStatus oldStatus,
                             ReportStatus newStatus, String comment) {
        ReportStatusHistory h = new ReportStatusHistory();
        h.setReport(report);
        h.setChangedBy(userRepository.getReferenceById(userId));
        h.setOldStatus(oldStatus);
        h.setNewStatus(newStatus);
        h.setComment(comment);
        h.setChangedAt(LocalDateTime.now());
        historyRepository.save(h);
    }

    /** e.g. MR-2026-A1B2C3 (random part keeps it unique without extra database queries). */
    private String generateReferenceNo() {
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "MR-" + LocalDateTime.now().getYear() + "-" + random;
    }
}
