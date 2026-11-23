package com.hrms.controller;

import com.hrms.dto.AttendanceCreateRequest;
import com.hrms.dto.BiometricPunchRequest;
import com.hrms.entity.Attendance;
import com.hrms.security.CustomUserDetails;
import com.hrms.service.AttendanceService;
import com.hrms.service.BiometricService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
@Tag(name = "Attendance", description = "Attendance records, monthly calendar, punching, and reports")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final BiometricService biometricService;

    public AttendanceController(AttendanceService attendanceService, BiometricService biometricService) {
        this.attendanceService = attendanceService;
        this.biometricService = biometricService;
    }

    @GetMapping
    @Operation(summary = "List attendance records with pagination and date filter")
    public ResponseEntity<?> getAttendance(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "15") int limit,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        return ResponseEntity.ok(attendanceService.getAttendanceRecords(page, limit, search, status, dateFrom, dateTo));
    }

    @PostMapping
    @Operation(summary = "Upsert attendance record")
    public ResponseEntity<?> upsertAttendance(@RequestBody AttendanceCreateRequest req) {
        Attendance attendance = attendanceService.upsertAttendance(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(attendance);
    }

    @GetMapping("/calendar")
    @Operation(summary = "Get monthly attendance calendar records")
    public ResponseEntity<?> getCalendar(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String employeeId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        int m = (month != null) ? month : LocalDate.now().getMonthValue();
        int y = (year != null) ? year : LocalDate.now().getYear();
        String targetEmpId = (employeeId != null && !employeeId.isBlank()) ? employeeId : userDetails.getId();

        return ResponseEntity.ok(attendanceService.getMonthlyCalendar(targetEmpId, m, y));
    }

    @PostMapping("/punch")
    @Operation(summary = "Check in or check out for the signed-in employee")
    public ResponseEntity<?> punch(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody Map<String, String> body) {
        String type = body.get("type");
        Attendance attendance = attendanceService.punch(userDetails.getId(), type);
        return ResponseEntity.ok(attendance);
    }

    @PostMapping("/checkin")
    @Operation(summary = "Process biometric punch (CHECK_IN or CHECK_OUT)")
    public ResponseEntity<?> checkIn(@RequestBody BiometricPunchRequest req) {
        return ResponseEntity.ok(biometricService.processPunch(req));
    }

    @GetMapping("/recent")
    @Operation(summary = "Get recent 10 attendance records across company")
    public ResponseEntity<?> getRecent(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(Map.of("records", attendanceService.getRecentRecords(limit)));
    }

    @GetMapping("/reports")
    @Operation(summary = "Generate attendance summary reports")
    public ResponseEntity<?> getReports(
            @RequestParam(required = false) String employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(defaultValue = "employee") String groupBy,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String empId = employeeId;
        if (empId == null && "EMPLOYEE".equals(userDetails.getRole())) {
            empId = userDetails.getId();
        }
        return ResponseEntity.ok(attendanceService.generateReport(empId, dateFrom, dateTo, groupBy));
    }
}
