package com.hrms.dto;

import com.hrms.entity.enums.AttendanceSource;
import com.hrms.entity.enums.AttendanceStatus;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AttendanceCreateRequest {

    @NotBlank(message = "employeeId is required")
    private String employeeId;

    private LocalDate date;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private AttendanceStatus status = AttendanceStatus.PRESENT;
    private AttendanceSource source = AttendanceSource.MANUAL;
    private String deviceId;
    private String location;
    private String notes;

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalDateTime getCheckIn() { return checkIn; }
    public void setCheckIn(LocalDateTime checkIn) { this.checkIn = checkIn; }

    public LocalDateTime getCheckOut() { return checkOut; }
    public void setCheckOut(LocalDateTime checkOut) { this.checkOut = checkOut; }

    public AttendanceStatus getStatus() { return status; }
    public void setStatus(AttendanceStatus status) { this.status = status; }

    public AttendanceSource getSource() { return source; }
    public void setSource(AttendanceSource source) { this.source = source; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
