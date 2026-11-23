package com.hrms.controller;

import com.hrms.dto.DailyWorkLogRequest;
import com.hrms.entity.DailyWorkLog;
import com.hrms.security.CustomUserDetails;
import com.hrms.service.WorkLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/work-logs")
@Tag(name = "Daily Work Logs", description = "Submit daily work logs, check today's status, and review history")
public class WorkLogController {

    private final WorkLogService workLogService;

    public WorkLogController(WorkLogService workLogService) {
        this.workLogService = workLogService;
    }

    @GetMapping
    @Operation(summary = "List work logs with pagination and filters")
    public ResponseEntity<?> getWorkLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String empId = null;
        if ("EMPLOYEE".equals(userDetails.getRole())) {
            empId = userDetails.getId();
        }
        return ResponseEntity.ok(workLogService.getWorkLogs(empId, page, limit, search, date));
    }

    @PostMapping
    @Operation(summary = "Submit today's daily work log")
    public ResponseEntity<?> createWorkLog(
            @Valid @RequestBody DailyWorkLogRequest req,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        DailyWorkLog log = workLogService.createWorkLog(userDetails.getId(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(log);
    }

    @GetMapping("/status")
    @Operation(summary = "Check if current employee submitted today's work log")
    public ResponseEntity<?> getStatus(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(workLogService.getWorkLogStatus(userDetails.getId()));
    }
}
