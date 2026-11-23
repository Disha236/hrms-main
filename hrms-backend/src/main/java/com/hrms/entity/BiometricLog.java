package com.hrms.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "biometric_logs")
public class BiometricLog {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "device_id")
    private BiometricDevice device;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id")
    @JsonIgnoreProperties({"department"})
    private Employee employee;

    @Column(nullable = false, length = 50)
    private String biometricId;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false, length = 30)
    private String eventType;

    @Column(columnDefinition = "TEXT")
    private String rawData;

    @Column(nullable = false)
    private Boolean processed = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public BiometricLog() {}

    @PrePersist
    protected void onCreate() {
        if (id == null || id.isBlank()) {
            id = java.util.UUID.randomUUID().toString();
        }
        createdAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public BiometricDevice getDevice() { return device; }
    public void setDevice(BiometricDevice device) { this.device = device; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public String getBiometricId() { return biometricId; }
    public void setBiometricId(String biometricId) { this.biometricId = biometricId; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getRawData() { return rawData; }
    public void setRawData(String rawData) { this.rawData = rawData; }

    public Boolean getProcessed() { return processed; }
    public void setProcessed(Boolean processed) { this.processed = processed; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
