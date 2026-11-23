package com.hrms.controller;

import com.hrms.dto.TimeEntryRequest;
import com.hrms.entity.Employee;
import com.hrms.entity.Issue;
import com.hrms.entity.TimeEntry;
import com.hrms.repository.EmployeeRepository;
import com.hrms.repository.IssueRepository;
import com.hrms.repository.TimeEntryRepository;
import com.hrms.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/time-entries")
@Tag(name = "Time Tracking", description = "Log task work hours against issues or general work")
public class TimeEntryController {

    private final TimeEntryRepository timeEntryRepository;
    private final EmployeeRepository employeeRepository;
    private final IssueRepository issueRepository;

    public TimeEntryController(TimeEntryRepository timeEntryRepository,
                               EmployeeRepository employeeRepository,
                               IssueRepository issueRepository) {
        this.timeEntryRepository = timeEntryRepository;
        this.employeeRepository = employeeRepository;
        this.issueRepository = issueRepository;
    }

    @GetMapping
    @Operation(summary = "List time entries with filters")
    public ResponseEntity<?> getTimeEntries(
            @RequestParam(required = false) String employeeId,
            @RequestParam(required = false) String issueId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String empId = employeeId;
        if (empId == null && "EMPLOYEE".equals(userDetails.getRole())) {
            empId = userDetails.getId();
        }

        List<TimeEntry> entries = timeEntryRepository.findTimeEntries(empId, issueId, dateFrom, dateTo);
        double totalHours = entries.stream().mapToDouble(TimeEntry::getHours).sum();

        Map<String, Object> res = new HashMap<>();
        res.put("entries", entries);
        res.put("totalHours", Math.round(totalHours * 100.0) / 100.0);
        return ResponseEntity.ok(res);
    }

    @PostMapping
    @Operation(summary = "Log work hours")
    public ResponseEntity<?> createTimeEntry(
            @Valid @RequestBody TimeEntryRequest req,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String empId = req.getEmployeeId() != null && !req.getEmployeeId().isBlank()
                ? req.getEmployeeId() : userDetails.getId();

        Employee employee = employeeRepository.findById(empId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        TimeEntry entry = new TimeEntry();
        entry.setEmployee(employee);
        entry.setDate(req.getDate());
        entry.setHours(req.getHours());
        entry.setDescription(req.getDescription());

        if (req.getIssueId() != null && !req.getIssueId().isBlank()) {
            issueRepository.findById(req.getIssueId()).ifPresent(entry::setIssue);
        }

        TimeEntry saved = timeEntryRepository.save(entry);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update time entry")
    public ResponseEntity<?> updateTimeEntry(
            @PathVariable String id,
            @RequestBody TimeEntryRequest req) {
        TimeEntry entry = timeEntryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Time entry not found"));

        if (req.getHours() != null) entry.setHours(req.getHours());
        if (req.getDate() != null) entry.setDate(req.getDate());
        if (req.getDescription() != null) entry.setDescription(req.getDescription());

        return ResponseEntity.ok(timeEntryRepository.save(entry));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete time entry")
    public ResponseEntity<?> deleteTimeEntry(@PathVariable String id) {
        timeEntryRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
