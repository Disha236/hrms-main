package com.hrms.controller;

import com.hrms.dto.PayrollGenerateRequest;
import com.hrms.entity.PaySlip;
import com.hrms.entity.enums.PaySlipStatus;
import com.hrms.security.CustomUserDetails;
import com.hrms.service.PayrollService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payroll")
@Tag(name = "Payroll", description = "Generate monthly payslips, calculate taxes, and publish to employees")
public class PayrollController {

    private final PayrollService payrollService;

    public PayrollController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    @GetMapping
    @Operation(summary = "List payslips for a given month/year (scoped for employees)")
    public ResponseEntity<?> getPayroll(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        int m = (month != null) ? month : LocalDate.now().getMonthValue();
        int y = (year != null) ? year : LocalDate.now().getYear();
        boolean isAdmin = "ADMIN".equals(userDetails.getRole()) || "HR_MANAGER".equals(userDetails.getRole());

        List<PaySlip> slips = payrollService.getPaySlips(m, y, userDetails.getId(), isAdmin);

        Map<String, Object> res = new HashMap<>();
        res.put("paySlips", slips);
        res.put("month", m);
        res.put("year", y);
        return ResponseEntity.ok(res);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    @Operation(summary = "Generate payroll for all or selected employees")
    public ResponseEntity<?> generatePayroll(@Valid @RequestBody PayrollGenerateRequest req) {
        return ResponseEntity.ok(payrollService.generatePayroll(req));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get single payslip with breakdown")
    public ResponseEntity<?> getPaySlipById(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return payrollService.getPaySlipById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    @Operation(summary = "Update payslip status")
    public ResponseEntity<?> updatePaySlip(@PathVariable String id, @RequestBody Map<String, String> body) {
        PaySlipStatus status = PaySlipStatus.valueOf(body.get("status").toUpperCase());
        return ResponseEntity.ok(payrollService.updateStatus(id, status));
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    @Operation(summary = "Publish payslip to employee with notification")
    public ResponseEntity<?> publishPaySlip(@PathVariable String id) {
        PaySlip published = payrollService.publishPaySlip(id);
        return ResponseEntity.ok(published);
    }
}
