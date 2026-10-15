package com.ems.dao;

import com.ems.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserDao {

    private final JdbcTemplate jdbc;

    public UserDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Returns the user if the email and password hash match, otherwise null. */
    public User login(String email, String passwordHash) {
        String sql = "SELECT user_id, school_id, full_name, email, role FROM users "
                   + "WHERE email = ? AND password_hash = ? AND is_active = TRUE";

        List<User> users = jdbc.query(sql, (rs, rowNum) -> {
            User u = new User();
            u.setId(rs.getLong("user_id"));
            u.setSchoolId(rs.getLong("school_id"));
            u.setFullName(rs.getString("full_name"));
            u.setEmail(rs.getString("email"));
            u.setRole(rs.getString("role"));
            return u;
        }, email, passwordHash);

        return users.isEmpty() ? null : users.get(0);
    }
}
