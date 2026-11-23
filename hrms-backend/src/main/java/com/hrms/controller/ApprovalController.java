package com.hrms.controller;

import com.hrms.entity.LeaveRequest;
import com.hrms.entity.Performance;
import com.hrms.entity.enums.LeaveStatus;
import com.hrms.entity.enums.PerformanceStatus;
import com.hrms.repository.LeaveRequestRepository;
import com.hrms.repository.PerformanceRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/approvals")
@Tag(name = "Approvals", description = "Aggregated pending approvals (Leaves + Performance reviews)")
public class ApprovalController {

    private final LeaveRequestRepository leaveRequestRepository;
    private final PerformanceRepository performanceRepository;

    public ApprovalController(LeaveRequestRepository leaveRequestRepository, PerformanceRepository performanceRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.performanceRepository = performanceRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'MANAGER')")
    @Operation(summary = "Get all pending leaves and submitted performance reviews")
    public ResponseEntity<?> getPendingApprovals() {
        List<LeaveRequest> pendingLeaves = leaveRequestRepository.findTop50ByStatusOrderByCreatedAtAsc(LeaveStatus.PENDING);
        List<Performance> pendingReviews = performanceRepository.findTop20ByStatusInOrderByCreatedAtAsc(
                List.of(PerformanceStatus.SUBMITTED, PerformanceStatus.REVIEWED));

        Map<String, Object> counts = new HashMap<>();
        counts.put("leaves", pendingLeaves.size());
        counts.put("reviews", pendingReviews.size());
        counts.put("total", pendingLeaves.size() + pendingReviews.size());

        Map<String, Object> response = new HashMap<>();
        response.put("pendingLeaves", pendingLeaves);
        response.put("pendingReviews", pendingReviews);
        response.put("counts", counts);

        return ResponseEntity.ok(response);
    }
}
