package com.ems.dao;

import com.ems.model.Option;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Small queries that fill dropdown lists. */
@Repository
public class LookupDao {

    private final JdbcTemplate jdbc;

    public LookupDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Option> categories() {
        return jdbc.query("SELECT category_id, name FROM categories ORDER BY name",
                (rs, i) -> new Option(String.valueOf(rs.getLong(1)), rs.getString(2)));
    }

    public List<Option> locations(long schoolId) {
        return jdbc.query("SELECT location_id, CONCAT(building, ' - ', room) FROM locations "
                        + "WHERE school_id = ? ORDER BY building, room",
                (rs, i) -> new Option(String.valueOf(rs.getLong(1)), rs.getString(2)), schoolId);
    }

    public List<Option> equipment(long schoolId) {
        return jdbc.query("SELECT equipment_id, CONCAT(asset_tag, ' - ', name) FROM equipment "
                        + "WHERE school_id = ? ORDER BY name",
                (rs, i) -> new Option(String.valueOf(rs.getLong(1)), rs.getString(2)), schoolId);
    }
}
