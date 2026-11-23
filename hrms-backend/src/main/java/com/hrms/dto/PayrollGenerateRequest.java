package com.hrms.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class PayrollGenerateRequest {

    @NotNull(message = "Month is required")
    @Min(1) @Max(12)
    private Integer month;

    @NotNull(message = "Year is required")
    private Integer year;

    private List<String> employeeIds;

    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public List<String> getEmployeeIds() { return employeeIds; }
    public void setEmployeeIds(List<String> employeeIds) { this.employeeIds = employeeIds; }
}
