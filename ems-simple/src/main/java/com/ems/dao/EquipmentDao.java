package com.ems.dao;

import com.ems.model.Equipment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class EquipmentDao {

    private final JdbcTemplate jdbc;

    public EquipmentDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final String SELECT =
        "SELECT e.equipment_id, e.asset_tag, e.name, e.condition_state, e.status, "
      + "       c.name AS category_name, CONCAT(l.building, ' - ', l.room) AS location_name "
      + "FROM equipment e "
      + "JOIN categories c ON c.category_id = e.category_id "
      + "LEFT JOIN locations l ON l.location_id = e.location_id ";

    /** Turns one row of the result into one Equipment object. */
    private static Equipment map(ResultSet rs, int rowNum) throws SQLException {
        Equipment e = new Equipment();
        e.setId(rs.getLong("equipment_id"));
        e.setAssetTag(rs.getString("asset_tag"));
        e.setName(rs.getString("name"));
        e.setConditionState(rs.getString("condition_state"));
        e.setStatus(rs.getString("status"));
        e.setCategoryName(rs.getString("category_name"));
        e.setLocationName(rs.getString("location_name"));
        return e;
    }

    /** All equipment of ONE school, optionally filtered by a search word (RQ09). */
    public List<Equipment> findBySchool(long schoolId, String search) {
        if (search == null || search.isBlank()) {
            return jdbc.query(SELECT + "WHERE e.school_id = ? ORDER BY e.name",
                    EquipmentDao::map, schoolId);
        }
        String like = "%" + search.trim() + "%";
        return jdbc.query(SELECT + "WHERE e.school_id = ? AND (e.name LIKE ? OR e.asset_tag LIKE ?) ORDER BY e.name",
                EquipmentDao::map, schoolId, like, like);
    }

    public boolean assetTagExists(String assetTag) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM equipment WHERE asset_tag = ?", Integer.class, assetTag);
        return count != null && count > 0;
    }

    public void add(long schoolId, long categoryId, Long locationId, String assetTag,
                    String name, String serialNumber, String conditionState) {
        jdbc.update("INSERT INTO equipment (school_id, category_id, location_id, asset_tag, name, "
                  + "serial_number, condition_state, status, qr_code_value) "
                  + "VALUES (?, ?, ?, ?, ?, ?, ?, 'AVAILABLE', ?)",
                schoolId, categoryId, locationId, assetTag, name, serialNumber, conditionState, assetTag);
    }
}
