package com.ems.dao;

import com.ems.model.Report;
import com.ems.model.StatusHistory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Year;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public class ReportDao {

    private final JdbcTemplate jdbc;

    public ReportDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final String SELECT =
        "SELECT r.report_id, r.reference_no, r.teacher_id, u.full_name AS teacher_name, "
      + "       r.equipment_id, e.name AS equipment_name, r.issue_type, r.priority, r.status, "
      + "       r.description, r.location_details, r.created_at "
      + "FROM maintenance_reports r "
      + "JOIN users u ON u.user_id = r.teacher_id "
      + "JOIN equipment e ON e.equipment_id = r.equipment_id ";

    private static Report map(ResultSet rs, int rowNum) throws SQLException {
        Report r = new Report();
        r.setId(rs.getLong("report_id"));
        r.setReferenceNo(rs.getString("reference_no"));
        r.setTeacherId(rs.getLong("teacher_id"));
        r.setTeacherName(rs.getString("teacher_name"));
        r.setEquipmentId(rs.getLong("equipment_id"));
        r.setEquipmentName(rs.getString("equipment_name"));
        r.setIssueType(rs.getString("issue_type"));
        r.setPriority(rs.getString("priority"));
        r.setStatus(rs.getString("status"));
        r.setDescription(rs.getString("description"));
        r.setLocationDetails(rs.getString("location_details"));
        r.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return r;
    }

    // ---------- reading ----------

    public List<Report> findByTeacher(long teacherId) {
        return jdbc.query(SELECT + "WHERE r.teacher_id = ? ORDER BY r.created_at DESC",
                ReportDao::map, teacherId);
    }

    /** All reports of one school; status can be null for "all statuses". */
    public List<Report> findBySchool(long schoolId, String status) {
        if (status == null) {
            return jdbc.query(SELECT + "WHERE r.school_id = ? ORDER BY r.created_at DESC",
                    ReportDao::map, schoolId);
        }
        return jdbc.query(SELECT + "WHERE r.school_id = ? AND r.status = ? ORDER BY r.created_at DESC",
                ReportDao::map, schoolId, status);
    }

    /** Returns the report, or null if it does not exist in this school. */
    public Report findById(long id, long schoolId) {
        List<Report> list = jdbc.query(SELECT + "WHERE r.report_id = ? AND r.school_id = ?",
                ReportDao::map, id, schoolId);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<StatusHistory> findHistory(long reportId) {
        return jdbc.query(
            "SELECT h.old_status, h.new_status, h.comment, h.changed_at, u.full_name "
          + "FROM report_status_history h JOIN users u ON u.user_id = h.changed_by "
          + "WHERE h.report_id = ? ORDER BY h.changed_at, h.history_id",
            (rs, i) -> {
                StatusHistory h = new StatusHistory();
                h.setOldStatus(rs.getString("old_status"));
                h.setNewStatus(rs.getString("new_status"));
                h.setComment(rs.getString("comment"));
                h.setChangedBy(rs.getString("full_name"));
                h.setChangedAt(rs.getTimestamp("changed_at").toLocalDateTime());
                return h;
            }, reportId);
    }

    /** Number of reports per status, e.g. {PENDING=3, IN_PROGRESS=1, ...} (RQ11). */
    public Map<String, Integer> countByStatus(long schoolId) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("PENDING", 0);
        counts.put("IN_PROGRESS", 0);
        counts.put("RESOLVED", 0);
        counts.put("REJECTED", 0);

        jdbc.query("SELECT status, COUNT(*) FROM maintenance_reports WHERE school_id = ? GROUP BY status",
                (RowCallbackHandler) rs -> counts.put(rs.getString(1), rs.getInt(2)), schoolId);
        return counts;
    }

    /** Has this equipment been reported in the last 7 days? (duplicate warning, RQ01) */
    public boolean existsRecent(long equipmentId) {
        Integer count = jdbc.queryForObject(
            "SELECT COUNT(*) FROM maintenance_reports "
          + "WHERE equipment_id = ? AND created_at > DATE_SUB(NOW(), INTERVAL 7 DAY)",
            Integer.class, equipmentId);
        return count != null && count > 0;
    }

    // ---------- writing ----------

    /**
     * Saves a new report. @Transactional = all the SQL statements below succeed together,
     * or none of them are saved. Returns the reference number.
     */
    @Transactional
    public String create(long schoolId, long teacherId, long equipmentId, Long locationId,
                         String issueType, String priority, String description, String locationDetails) {

        // Safety check: the equipment must belong to the teacher's own school
        Integer owned = jdbc.queryForObject(
                "SELECT COUNT(*) FROM equipment WHERE equipment_id = ? AND school_id = ?",
                Integer.class, equipmentId, schoolId);
        if (owned == null || owned == 0) {
            throw new IllegalArgumentException("Equipment not found");
        }

        String referenceNo = "MR-" + Year.now().getValue() + "-"
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        jdbc.update("INSERT INTO maintenance_reports (reference_no, school_id, teacher_id, equipment_id, "
                  + "location_id, issue_type, description, priority, status, location_details, created_at) "
                  + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'PENDING', ?, NOW())",
                referenceNo, schoolId, teacherId, equipmentId, locationId,
                issueType, description, priority, locationDetails);

        Long reportId = jdbc.queryForObject(
                "SELECT report_id FROM maintenance_reports WHERE reference_no = ?", Long.class, referenceNo);

        jdbc.update("INSERT INTO report_status_history (report_id, changed_by, old_status, new_status, comment, changed_at) "
                  + "VALUES (?, ?, NULL, 'PENDING', 'Report submitted', NOW())", reportId, teacherId);

        // A fault was reported, so the equipment is now DAMAGED (unless it is already being repaired)
        jdbc.update("UPDATE equipment SET status = 'DAMAGED' WHERE equipment_id = ? AND status IN ('AVAILABLE', 'IN_USE')",
                equipmentId);

        return referenceNo;
    }

    /** Staff changes the status of a report and must give a comment (RQ06). */
    @Transactional
    public void updateStatus(long reportId, long userId, String newStatus, String comment) {
        String oldStatus = jdbc.queryForObject(
                "SELECT status FROM maintenance_reports WHERE report_id = ?", String.class, reportId);

        if (newStatus.equals(oldStatus)) {
            throw new IllegalArgumentException("The report is already " + oldStatus);
        }

        jdbc.update("UPDATE maintenance_reports SET status = ?, "
                  + "resolved_at = IF(? = 'RESOLVED', NOW(), NULL) WHERE report_id = ?",
                newStatus, newStatus, reportId);

        jdbc.update("INSERT INTO report_status_history (report_id, changed_by, old_status, new_status, comment, changed_at) "
                  + "VALUES (?, ?, ?, ?, ?, NOW())", reportId, userId, oldStatus, newStatus, comment);

        // Keep the equipment status in step with the report
        String equipmentStatus = null;
        if (newStatus.equals("IN_PROGRESS")) {
            equipmentStatus = "UNDER_MAINTENANCE";
        } else if (newStatus.equals("RESOLVED") || newStatus.equals("REJECTED")) {
            equipmentStatus = "AVAILABLE";
        }
        if (equipmentStatus != null) {
            jdbc.update("UPDATE equipment SET status = ? "
                      + "WHERE equipment_id = (SELECT equipment_id FROM maintenance_reports WHERE report_id = ?)",
                    equipmentStatus, reportId);
        }
        // TODO RQ06/RQ07: create a notification and send an email to the teacher
    }
}
