package com.hrms.dto;

public class UserSessionDto {
    private String id;
    private String email;
    private String name;
    private String role;
    private String departmentId;

    public UserSessionDto() {}

    public UserSessionDto(String id, String email, String name, String role, String departmentId) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.role = role;
        this.departmentId = departmentId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getDepartmentId() { return departmentId; }
    public void setDepartmentId(String departmentId) { this.departmentId = departmentId; }
}
