package com.hrms.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hrms.entity.enums.ProjectRole;
import jakarta.persistence.*;

@Entity
@Table(name = "project_members", uniqueConstraints = {
    @UniqueConstraint(name = "uk_project_emp", columnNames = {"project_id", "employee_id"})
})
public class ProjectMember {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id", nullable = false)
    @JsonIgnoreProperties({"lead"})
    private Project project;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonIgnoreProperties({"department"})
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProjectRole role = ProjectRole.MEMBER;

    public ProjectMember() {}

    public ProjectMember(Project project, Employee employee, ProjectRole role) {
        this.project = project;
        this.employee = employee;
        this.role = role != null ? role : ProjectRole.MEMBER;
    }

    @PrePersist
    protected void onCreate() {
        if (id == null || id.isBlank()) {
            id = java.util.UUID.randomUUID().toString();
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public ProjectRole getRole() { return role; }
    public void setRole(ProjectRole role) { this.role = role; }
}
