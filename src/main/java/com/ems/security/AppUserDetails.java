package com.ems.security;

import com.ems.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * The logged-in user as Spring Security sees them.
 * Controllers receive it with @AuthenticationPrincipal AppUserDetails user.
 * It copies plain values (no lazy entities) so it is safe to keep in the session.
 */
public class AppUserDetails implements UserDetails {

    private static final Set<String> STAFF_ROLES = Set.of("TECHNICIAN", "SCHOOL_ADMIN", "SYSTEM_ADMIN");

    private final Long id;
    private final Long schoolId;   // null for SYSTEM_ADMIN
    private final String fullName;
    private final String email;
    private final String passwordHash;
    private final String roleName;
    private final boolean active;

    public AppUserDetails(User user) {
        this.id = user.getId();
        this.schoolId = user.getSchool() == null ? null : user.getSchool().getId();
        this.fullName = user.getFullName();
        this.email = user.getEmail();
        this.passwordHash = user.getPasswordHash();
        this.roleName = user.getRole().getName();
        this.active = user.isActive();
    }

    public Long getId() { return id; }
    public Long getSchoolId() { return schoolId; }
    public String getFullName() { return fullName; }
    public String getRoleName() { return roleName; }

    /** True for technicians and admins (anyone who manages reports, not just submits them). */
    public boolean isStaff() { return STAFF_ROLES.contains(roleName); }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + roleName));
    }
    @Override public String getPassword() { return passwordHash; }
    @Override public String getUsername() { return email; }
    @Override public boolean isEnabled() { return active; }
}
