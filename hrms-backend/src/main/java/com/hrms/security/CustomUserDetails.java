package com.hrms.security;

import com.hrms.entity.Employee;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private final String id;
    private final String email;
    private final String password;
    private final String role;
    private final String departmentId;
    private final String name;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(Employee employee) {
        this.id = employee.getId();
        this.email = employee.getEmail();
        this.password = employee.getPassword() != null ? employee.getPassword() : "";
        this.role = employee.getRole().name();
        this.departmentId = employee.getDepartment() != null ? employee.getDepartment().getId() : null;
        this.name = employee.getFullName();
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + employee.getRole().name()));
    }

    public String getId() { return id; }
    public String getRole() { return role; }
    public String getDepartmentId() { return departmentId; }
    public String getName() { return name; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }

    @Override
    public String getPassword() { return password; }

    @Override
    public String getUsername() { return email; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
