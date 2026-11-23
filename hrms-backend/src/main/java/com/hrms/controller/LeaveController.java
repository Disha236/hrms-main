package com.hrms.controller;

import com.hrms.dto.LeaveCreateRequest;
import com.hrms.entity.LeaveRequest;
import com.hrms.security.CustomUserDetails;
import com.hrms.service.LeaveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/leaves")
@Tag(name = "Leaves", description = "Leave requests, remaining balance, and approvals")
public class LeaveController {

    private final LeaveService leaveService;

    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    @GetMapping
    @Operation(summary = "List leave requests with pagination and filter (scoped to self for employees)")
    public ResponseEntity<?> getLeaves(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String empId = null;
        if ("EMPLOYEE".equals(userDetails.getRole())) {
            empId = userDetails.getId();
        }
        return ResponseEntity.ok(leaveService.getLeaveRequests(empId, page, limit, search, status, type));
    }

    @PostMapping
    @Operation(summary = "Submit a new leave request")
    public ResponseEntity<?> submitLeave(
            @Valid @RequestBody LeaveCreateRequest req,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        LeaveRequest lr = leaveService.submitLeaveRequest(userDetails.getId(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(lr);
    }

    @GetMapping("/balance")
    @Operation(summary = "Get leave balance breakdown by type")
    public ResponseEntity<?> getBalance(
            @RequestParam(required = false) String employeeId,
            @RequestParam(required = false) Integer year,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String targetEmpId = (employeeId != null && !employeeId.isBlank()) ? employeeId : userDetails.getId();
        int targetYear = (year != null) ? year : LocalDate.now().getYear();

        if ("EMPLOYEE".equals(userDetails.getRole()) && !targetEmpId.equals(userDetails.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(leaveService.getLeaveBalances(targetEmpId, targetYear));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'MANAGER')")
    @Operation(summary = "Approve a leave request")
    public ResponseEntity<?> approveLeave(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        LeaveRequest approved = leaveService.approveLeave(id, userDetails.getId());
        return ResponseEntity.ok(Map.of("success", true, "leaveRequest", approved));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'MANAGER')")
    @Operation(summary = "Reject a leave request")
    public ResponseEntity<?> rejectLeave(
            @PathVariable String id,
            @RequestBody(required = false) Map<String, String> body,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String reason = body != null ? body.get("rejectedReason") : "No reason provided";
        LeaveRequest rejected = leaveService.rejectLeave(id, userDetails.getId(), reason);
        return ResponseEntity.ok(Map.of("success", true, "leaveRequest", rejected));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel a pending leave request")
    public ResponseEntity<?> cancelLeave(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        boolean isAdmin = "ADMIN".equals(userDetails.getRole()) || "HR_MANAGER".equals(userDetails.getRole());
        LeaveRequest cancelled = leaveService.cancelLeave(id, userDetails.getId(), isAdmin);
        return ResponseEntity.ok(cancelled);
    }
}
