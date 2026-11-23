package com.hrms.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public class DailyWorkLogRequest {

    private String employeeId;
    private LocalDate date;

    @NotBlank(message = "Summary is required")
    private String summary;

    private String tasksDone;
    private String blockers;
    private String nextSteps;
    private Double hoursWorked;

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getTasksDone() { return tasksDone; }
    public void setTasksDone(String tasksDone) { this.tasksDone = tasksDone; }

    public String getBlockers() { return blockers; }
    public void setBlockers(String blockers) { this.blockers = blockers; }

    public String getNextSteps() { return nextSteps; }
    public void setNextSteps(String nextSteps) { this.nextSteps = nextSteps; }

    public Double getHoursWorked() { return hoursWorked; }
    public void setHoursWorked(Double hoursWorked) { this.hoursWorked = hoursWorked; }
}
