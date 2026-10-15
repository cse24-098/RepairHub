package com.ems.model;

import java.io.Serializable;

/** The logged-in user. Stored in the HTTP session after login. */
public class User implements Serializable {
    private long id;
    private long schoolId;
    private String fullName;
    private String email;
    private String role;   // TEACHER, TECHNICIAN or SCHOOL_ADMIN

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getSchoolId() { return schoolId; }
    public void setSchoolId(long schoolId) { this.schoolId = schoolId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    /** Technicians and admins manage reports; teachers only submit and follow them. */
    public boolean isStaff() {
        return role.equals("TECHNICIAN") || role.equals("SCHOOL_ADMIN");
    }
}
