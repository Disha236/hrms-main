package com.hrms.controller;

import com.hrms.dto.ShiftCreateRequest;
import com.hrms.entity.Shift;
import com.hrms.entity.ShiftAssignment;
import com.hrms.service.ShiftService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/shifts")
@Tag(name = "Shifts", description = "Work shift scheduling, assignment, and hours configuration")
public class ShiftController {

    private final ShiftService shiftService;

    public ShiftController(ShiftService shiftService) {
        this.shiftService = shiftService;
    }

    @GetMapping
    @Operation(summary = "List all shifts with working days and assignments")
    public ResponseEntity<?> getShifts() {
        return ResponseEntity.ok(Map.of("shifts", shiftService.getAllShifts()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    @Operation(summary = "Create a shift")
    public ResponseEntity<?> createShift(@Valid @RequestBody ShiftCreateRequest req) {
        Shift shift = shiftService.createShift(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(shift);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    @Operation(summary = "Update shift")
    public ResponseEntity<?> updateShift(@PathVariable String id, @RequestBody ShiftCreateRequest req) {
        return ResponseEntity.ok(shiftService.updateShift(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    @Operation(summary = "Deactivate shift")
    public ResponseEntity<?> deleteShift(@PathVariable String id) {
        shiftService.deactivateShift(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    @Operation(summary = "Assign employee to a shift")
    public ResponseEntity<?> assignShift(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        String employeeId = body.get("employeeId");
        LocalDate start = LocalDate.parse(body.get("startDate"));
        LocalDate end = body.get("endDate") != null ? LocalDate.parse(body.get("endDate")) : null;

        ShiftAssignment assignment = shiftService.assignShift(id, employeeId, start, end);
        return ResponseEntity.status(HttpStatus.CREATED).body(assignment);
    }
}
