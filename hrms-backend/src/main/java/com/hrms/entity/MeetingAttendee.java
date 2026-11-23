package com.hrms.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "meeting_attendees")
public class MeetingAttendee {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    @JsonIgnore
    private Meeting meeting;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonIgnoreProperties({"department"})
    private Employee employee;

    @Column(length = 30)
    private String status = "INVITED";

    public MeetingAttendee() {}

    public MeetingAttendee(Meeting meeting, Employee employee) {
        this.id = java.util.UUID.randomUUID().toString();
        this.meeting = meeting;
        this.employee = employee;
        this.status = "INVITED";
    }

    @PrePersist
    protected void onCreate() {
        if (id == null || id.isBlank()) {
            id = java.util.UUID.randomUUID().toString();
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Meeting getMeeting() { return meeting; }
    public void setMeeting(Meeting meeting) { this.meeting = meeting; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
