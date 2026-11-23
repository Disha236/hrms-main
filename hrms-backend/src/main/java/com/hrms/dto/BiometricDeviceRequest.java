package com.hrms.dto;

import com.hrms.entity.enums.DeviceType;
import jakarta.validation.constraints.NotBlank;

public class BiometricDeviceRequest {

    @NotBlank(message = "deviceId is required")
    private String deviceId;

    @NotBlank(message = "name is required")
    private String name;

    private DeviceType deviceType = DeviceType.ZK_DEVICE;
    private String location;
    private String ipAddress;
    private Integer port = 4370;
    private String googleFormUrl;
    private Boolean isActive = true;

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public DeviceType getDeviceType() { return deviceType; }
    public void setDeviceType(DeviceType deviceType) { this.deviceType = deviceType; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public Integer getPort() { return port; }
    public void setPort(Integer port) { this.port = port; }

    public String getGoogleFormUrl() { return googleFormUrl; }
    public void setGoogleFormUrl(String googleFormUrl) { this.googleFormUrl = googleFormUrl; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
