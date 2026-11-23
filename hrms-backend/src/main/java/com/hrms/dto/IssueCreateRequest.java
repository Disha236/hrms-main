package com.hrms.dto;

import com.hrms.entity.enums.IssuePriority;
import com.hrms.entity.enums.IssueStatus;
import com.hrms.entity.enums.IssueType;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public class IssueCreateRequest {

    @NotBlank(message = "projectId is required")
    private String projectId;

    @NotBlank(message = "title is required")
    private String title;

    private String description;
    private IssueType type = IssueType.TASK;
    private IssueStatus status = IssueStatus.TODO;
    private IssuePriority priority = IssuePriority.MEDIUM;
    private String assigneeId;
    private String sprintId;
    private LocalDate dueDate;
    private Double estimatedHours;

    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public IssueType getType() { return type; }
    public void setType(IssueType type) { this.type = type; }

    public IssueStatus getStatus() { return status; }
    public void setStatus(IssueStatus status) { this.status = status; }

    public IssuePriority getPriority() { return priority; }
    public void setPriority(IssuePriority priority) { this.priority = priority; }

    public String getAssigneeId() { return assigneeId; }
    public void setAssigneeId(String assigneeId) { this.assigneeId = assigneeId; }

    public String getSprintId() { return sprintId; }
    public void setSprintId(String sprintId) { this.sprintId = sprintId; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public Double getEstimatedHours() { return estimatedHours; }
    public void setEstimatedHours(Double estimatedHours) { this.estimatedHours = estimatedHours; }
}
