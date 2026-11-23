package com.hrms.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class ShiftCreateRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Start time is required")
    private String startTime;

    @NotBlank(message = "End time is required")
    private String endTime;

    private Integer breakMinutes = 60;
    private List<String> workingDays;
    private String departmentId;
    private Boolean isActive = true;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public Integer getBreakMinutes() { return breakMinutes; }
    public void setBreakMinutes(Integer breakMinutes) { this.breakMinutes = breakMinutes; }

    public List<String> getWorkingDays() { return workingDays; }
    public void setWorkingDays(List<String> workingDays) { this.workingDays = workingDays; }

    public String getDepartmentId() { return departmentId; }
    public void setDepartmentId(String departmentId) { this.departmentId = departmentId; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
