package com.hrms.controller;

import com.hrms.dto.PerformanceRequest;
import com.hrms.entity.Employee;
import com.hrms.entity.KPI;
import com.hrms.entity.Performance;
import com.hrms.entity.enums.PerformanceStatus;
import com.hrms.repository.EmployeeRepository;
import com.hrms.repository.KPIRepository;
import com.hrms.repository.PerformanceRepository;
import com.hrms.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/performance")
@Tag(name = "Performance Reviews", description = "Employee evaluations, goals, strengths, and KPI scores")
public class PerformanceController {

    private final PerformanceRepository performanceRepository;
    private final KPIRepository kpiRepository;
    private final EmployeeRepository employeeRepository;

    public PerformanceController(PerformanceRepository performanceRepository,
                                 KPIRepository kpiRepository,
                                 EmployeeRepository employeeRepository) {
        this.performanceRepository = performanceRepository;
        this.kpiRepository = kpiRepository;
        this.employeeRepository = employeeRepository;
    }

    @GetMapping
    @Operation(summary = "List performance reviews with pagination")
    public ResponseEntity<?> getReviews(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String period) {
        PageRequest pageable = PageRequest.of(page - 1, limit, Sort.by("reviewDate").descending());

        PerformanceStatus perfStatus = null;
        if (status != null && !status.isBlank()) {
            try { perfStatus = PerformanceStatus.valueOf(status.toUpperCase().trim()); } catch (Exception ignored) {}
        }

        Page<Performance> result = performanceRepository.searchPerformances(search, perfStatus, period, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("reviews", result.getContent());
        response.put("total", result.getTotalElements());
        response.put("totalPages", result.getTotalPages());
        response.put("currentPage", page);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Transactional
    @Operation(summary = "Create a performance review with KPIs")
    public ResponseEntity<?> createReview(
            @Valid @RequestBody PerformanceRequest req,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Employee employee = employeeRepository.findById(req.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        String reviewerId = req.getReviewerId() != null && !req.getReviewerId().isBlank()
                ? req.getReviewerId() : userDetails.getId();
        Employee reviewer = employeeRepository.findById(reviewerId)
                .orElseThrow(() -> new IllegalArgumentException("Reviewer not found"));

        Performance p = new Performance();
        p.setEmployee(employee);
        p.setReviewer(reviewer);
        p.setReviewPeriod(req.getReviewPeriod().trim());
        p.setReviewDate(req.getReviewDate());
        p.setOverallRating(req.getOverallRating());
        p.setGoals(req.getGoals());
        p.setAchievements(req.getAchievements());
        p.setStrengths(req.getStrengths());
        p.setImprovements(req.getImprovements());
        p.setComments(req.getComments());
        p.setStatus(req.getStatus() != null ? req.getStatus() : PerformanceStatus.DRAFT);

        Performance saved = performanceRepository.save(p);

        if (req.getKpis() != null) {
            for (PerformanceRequest.KpiItem item : req.getKpis()) {
                KPI kpi = new KPI(item.getName(), item.getTarget(), item.getActual(), item.getWeight(), item.getRating());
                kpi.setPerformance(saved);
                kpiRepository.save(kpi);
                saved.getKpis().add(kpi);
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get performance review by ID")
    public ResponseEntity<?> getReviewById(@PathVariable String id) {
        return performanceRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PutMapping("/{id}")
    @Transactional
    @Operation(summary = "Update performance review and replace KPIs")
    public ResponseEntity<?> updateReview(@PathVariable String id, @RequestBody PerformanceRequest req) {
        Performance p = performanceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Performance review not found"));

        if (req.getReviewPeriod() != null) p.setReviewPeriod(req.getReviewPeriod().trim());
        if (req.getReviewDate() != null) p.setReviewDate(req.getReviewDate());
        if (req.getOverallRating() != null) p.setOverallRating(req.getOverallRating());
        if (req.getGoals() != null) p.setGoals(req.getGoals());
        if (req.getAchievements() != null) p.setAchievements(req.getAchievements());
        if (req.getStrengths() != null) p.setStrengths(req.getStrengths());
        if (req.getImprovements() != null) p.setImprovements(req.getImprovements());
        if (req.getComments() != null) p.setComments(req.getComments());
        if (req.getStatus() != null) p.setStatus(req.getStatus());

        if (req.getKpis() != null) {
            kpiRepository.deleteByPerformanceId(id);
            p.getKpis().clear();
            for (PerformanceRequest.KpiItem item : req.getKpis()) {
                KPI kpi = new KPI(item.getName(), item.getTarget(), item.getActual(), item.getWeight(), item.getRating());
                kpi.setPerformance(p);
                kpiRepository.save(kpi);
                p.getKpis().add(kpi);
            }
        }

        return ResponseEntity.ok(performanceRepository.save(p));
    }

    @DeleteMapping("/{id}")
    @Transactional
    @Operation(summary = "Delete a performance review")
    public ResponseEntity<?> deleteReview(@PathVariable String id) {
        kpiRepository.deleteByPerformanceId(id);
        performanceRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
