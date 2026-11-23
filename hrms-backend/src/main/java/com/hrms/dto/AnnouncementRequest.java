package com.hrms.dto;

import com.hrms.entity.enums.AnnouncementPriority;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public class AnnouncementRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    private String departmentId;
    private AnnouncementPriority priority = AnnouncementPriority.NORMAL;
    private LocalDateTime expiresAt;
    private Boolean isActive = true;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getDepartmentId() { return departmentId; }
    public void setDepartmentId(String departmentId) { this.departmentId = departmentId; }

    public AnnouncementPriority getPriority() { return priority; }
    public void setPriority(AnnouncementPriority priority) { this.priority = priority; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
