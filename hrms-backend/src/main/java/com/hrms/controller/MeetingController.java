package com.hrms.controller;

import com.hrms.dto.MeetingCreateRequest;
import com.hrms.entity.Employee;
import com.hrms.entity.Meeting;
import com.hrms.entity.MeetingAttendee;
import com.hrms.entity.enums.MeetingStatus;
import com.hrms.entity.enums.MeetingType;
import com.hrms.entity.enums.NotificationType;
import com.hrms.repository.EmployeeRepository;
import com.hrms.repository.MeetingAttendeeRepository;
import com.hrms.repository.MeetingRepository;
import com.hrms.repository.ProjectRepository;
import com.hrms.security.CustomUserDetails;
import com.hrms.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/meetings")
@Tag(name = "Meetings", description = "Schedule meetings, manage invitees, and cancel")
public class MeetingController {

    private final MeetingRepository meetingRepository;
    private final MeetingAttendeeRepository attendeeRepository;
    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;
    private final NotificationService notificationService;

    public MeetingController(MeetingRepository meetingRepository,
                             MeetingAttendeeRepository attendeeRepository,
                             EmployeeRepository employeeRepository,
                             ProjectRepository projectRepository,
                             NotificationService notificationService) {
        this.meetingRepository = meetingRepository;
        this.attendeeRepository = attendeeRepository;
        this.employeeRepository = employeeRepository;
        this.projectRepository = projectRepository;
        this.notificationService = notificationService;
    }

    @GetMapping
    @Operation(summary = "List upcoming or past meetings")
    public ResponseEntity<?> getMeetings(
            @RequestParam(defaultValue = "upcoming") String filter,
            @RequestParam(required = false) String type,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        boolean isManager = !"EMPLOYEE".equals(userDetails.getRole());
        LocalDateTime now = LocalDateTime.now();

        MeetingType meetingType = null;
        if (type != null && !type.isBlank()) {
            try { meetingType = MeetingType.valueOf(type.toUpperCase().trim()); } catch (Exception ignored) {}
        }

        List<Meeting> meetings;
        if ("past".equalsIgnoreCase(filter)) {
            meetings = meetingRepository.findPastMeetings(userDetails.getId(), isManager, now);
        } else {
            meetings = meetingRepository.findMeetings(userDetails.getId(), isManager, MeetingStatus.SCHEDULED, meetingType, now, null);
        }

        return ResponseEntity.ok(Map.of("meetings", meetings));
    }

    @PostMapping
    @Transactional
    @Operation(summary = "Create and schedule a new meeting")
    public ResponseEntity<?> createMeeting(
            @Valid @RequestBody MeetingCreateRequest req,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (!req.getEndTime().isAfter(req.getStartTime())) {
            return ResponseEntity.badRequest().body(Map.of("error", "End time must be after start time"));
        }

        Employee organizer = employeeRepository.findById(userDetails.getId())
                .orElseThrow(() -> new IllegalArgumentException("Organizer not found"));

        Meeting meeting = new Meeting();
        meeting.setTitle(req.getTitle().trim());
        meeting.setDescription(req.getDescription());
        meeting.setStartTime(req.getStartTime());
        meeting.setEndTime(req.getEndTime());
        meeting.setType(req.getType() != null ? req.getType() : MeetingType.TEAM);
        meeting.setLocation(req.getLocation());
        meeting.setMeetingLink(req.getMeetingLink());
        meeting.setClientName(req.getClientName());
        meeting.setOrganizer(organizer);
        meeting.setStatus(MeetingStatus.SCHEDULED);

        if (req.getProjectId() != null && !req.getProjectId().isBlank()) {
            projectRepository.findById(req.getProjectId()).ifPresent(meeting::setProject);
        }

        Meeting saved = meetingRepository.save(meeting);

        if (req.getAttendeeIds() != null) {
            for (String attId : req.getAttendeeIds()) {
                if (!attId.equals(organizer.getId())) {
                    employeeRepository.findById(attId).ifPresent(emp -> {
                        MeetingAttendee ma = new MeetingAttendee(saved, emp);
                        attendeeRepository.save(ma);

                        notificationService.createNotification(emp, "Meeting Scheduled",
                                organizer.getFullName() + " invited you to \"" + saved.getTitle() + "\" on " + saved.getStartTime(),
                                NotificationType.INFO, "/meetings/" + saved.getId());
                    });
                }
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get meeting details")
    public ResponseEntity<?> getMeetingById(@PathVariable String id) {
        return meetingRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PutMapping("/{id}")
    @Transactional
    @Operation(summary = "Update meeting details")
    public ResponseEntity<?> updateMeeting(@PathVariable String id, @RequestBody MeetingCreateRequest req) {
        Meeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Meeting not found"));

        if (req.getTitle() != null) meeting.setTitle(req.getTitle().trim());
        if (req.getDescription() != null) meeting.setDescription(req.getDescription());
        if (req.getStartTime() != null) meeting.setStartTime(req.getStartTime());
        if (req.getEndTime() != null) meeting.setEndTime(req.getEndTime());
        if (req.getType() != null) meeting.setType(req.getType());
        if (req.getLocation() != null) meeting.setLocation(req.getLocation());
        if (req.getMeetingLink() != null) meeting.setMeetingLink(req.getMeetingLink());
        if (req.getClientName() != null) meeting.setClientName(req.getClientName());

        return ResponseEntity.ok(meetingRepository.save(meeting));
    }

    @DeleteMapping("/{id}")
    @Transactional
    @Operation(summary = "Cancel a meeting")
    public ResponseEntity<?> cancelMeeting(@PathVariable String id) {
        Meeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Meeting not found"));

        meeting.setStatus(MeetingStatus.CANCELLED);
        Meeting saved = meetingRepository.save(meeting);

        for (MeetingAttendee att : saved.getAttendees()) {
            notificationService.createNotification(att.getEmployee(), "Meeting Cancelled",
                    "\"" + saved.getTitle() + "\" has been cancelled.",
                    NotificationType.INFO, "/meetings");
        }

        return ResponseEntity.ok(saved);
    }
}
