package com.hrms.dto;

import java.time.LocalDateTime;
import java.util.List;

public class BiometricSyncRequest {

    private String deviceId;
    private List<LogItem> logs;

    public static class LogItem {
        private String biometricId;
        private LocalDateTime timestamp;
        private String eventType;

        public String getBiometricId() { return biometricId; }
        public void setBiometricId(String biometricId) { this.biometricId = biometricId; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
    }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public List<LogItem> getLogs() { return logs; }
    public void setLogs(List<LogItem> logs) { this.logs = logs; }
}
