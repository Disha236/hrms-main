package com.hrms.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public class TimeEntryRequest {

    private String issueId;
    private String employeeId;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotNull(message = "Hours are required")
    @Positive(message = "Hours must be positive")
    private Double hours;

    private String description;

    public String getIssueId() { return issueId; }
    public void setIssueId(String issueId) { this.issueId = issueId; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Double getHours() { return hours; }
    public void setHours(Double hours) { this.hours = hours; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
