package com.hrms.dto;

import jakarta.validation.constraints.NotBlank;

public class GoogleFormPunchRequest {

    @NotBlank(message = "deviceId is required")
    private String deviceId;

    @NotBlank(message = "employeeId is required")
    private String employeeId;

    private String timestamp;

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
