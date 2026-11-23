package com.hrms.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public class BiometricPunchRequest {

    @NotBlank(message = "biometricId is required")
    private String biometricId;

    @NotBlank(message = "type is required (CHECK_IN or CHECK_OUT)")
    private String type;

    private String deviceId;
    private String location;
    private LocalDateTime timestamp;

    public String getBiometricId() { return biometricId; }
    public void setBiometricId(String biometricId) { this.biometricId = biometricId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
