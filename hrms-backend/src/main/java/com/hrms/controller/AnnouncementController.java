package com.hrms.controller;

import com.hrms.dto.AnnouncementRequest;
import com.hrms.entity.Announcement;
import com.hrms.entity.Employee;
import com.hrms.entity.enums.EmploymentStatus;
import com.hrms.entity.enums.NotificationType;
import com.hrms.repository.AnnouncementRepository;
import com.hrms.repository.DepartmentRepository;
import com.hrms.repository.EmployeeRepository;
import com.hrms.security.CustomUserDetails;
import com.hrms.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/announcements")
@Tag(name = "Announcements", description = "Company announcements and department notices")
public class AnnouncementController {

    private final AnnouncementRepository announcementRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final NotificationService notificationService;

    public AnnouncementController(AnnouncementRepository announcementRepository,
                                  EmployeeRepository employeeRepository,
                                  DepartmentRepository departmentRepository,
                                  NotificationService notificationService) {
        this.announcementRepository = announcementRepository;
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.notificationService = notificationService;
    }

    @GetMapping
    @Operation(summary = "List active announcements relevant to the employee")
    public ResponseEntity<?> getAnnouncements(@AuthenticationPrincipal CustomUserDetails userDetails) {
        LocalDateTime now = LocalDateTime.now();
        List<Announcement> announcements = announcementRepository.findActiveAnnouncements(userDetails.getDepartmentId(), now);
        return ResponseEntity.ok(Map.of("announcements", announcements));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    @Transactional
    @Operation(summary = "Create an announcement (ADMIN / HR_MANAGER only)")
    public ResponseEntity<?> createAnnouncement(
            @Valid @RequestBody AnnouncementRequest req,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Employee author = employeeRepository.findById(userDetails.getId())
                .orElseThrow(() -> new IllegalArgumentException("Author not found"));

        Announcement a = new Announcement();
        a.setTitle(req.getTitle().trim());
        a.setContent(req.getContent().trim());
        a.setAuthor(author);
        a.setPriority(req.getPriority());
        a.setExpiresAt(req.getExpiresAt());
        a.setIsActive(req.getIsActive() != null ? req.getIsActive() : true);

        if (req.getDepartmentId() != null && !req.getDepartmentId().isBlank()) {
            departmentRepository.findById(req.getDepartmentId()).ifPresent(a::setDepartment);
        }

        Announcement saved = announcementRepository.save(a);

        List<Employee> targetEmployees;
        if (saved.getDepartment() != null) {
            targetEmployees = employeeRepository.findByDepartmentIdAndEmploymentStatus(
                    saved.getDepartment().getId(), EmploymentStatus.ACTIVE);
        } else {
            targetEmployees = employeeRepository.findByEmploymentStatus(EmploymentStatus.ACTIVE);
        }

        for (Employee emp : targetEmployees) {
            notificationService.createNotification(emp, "New Announcement", saved.getTitle(),
                    NotificationType.SYSTEM, "/announcements");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    @Operation(summary = "Update announcement status or content")
    public ResponseEntity<?> updateAnnouncement(@PathVariable String id, @RequestBody Map<String, Object> body) {
        Announcement a = announcementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Announcement not found"));

        if (body.containsKey("isActive")) a.setIsActive((Boolean) body.get("isActive"));
        if (body.containsKey("title")) a.setTitle(((String) body.get("title")).trim());
        if (body.containsKey("content")) a.setContent(((String) body.get("content")).trim());

        return ResponseEntity.ok(announcementRepository.save(a));
    }
}
