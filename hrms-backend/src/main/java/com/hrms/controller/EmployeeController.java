package com.hrms.controller;

import com.hrms.dto.EmployeeCreateRequest;
import com.hrms.dto.EmployeeUpdateRequest;
import com.hrms.entity.Employee;
import com.hrms.security.CustomUserDetails;
import com.hrms.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/employees")
@Tag(name = "Employees", description = "Employee profile management and search")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    @Operation(summary = "List employees with pagination, search, and department filter")
    public ResponseEntity<?> getEmployees(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(employeeService.getEmployees(page, limit, search, department, status));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    @Operation(summary = "Create a new employee (ADMIN / HR_MANAGER only)")
    public ResponseEntity<?> createEmployee(@Valid @RequestBody EmployeeCreateRequest req) {
        Employee employee = employeeService.createEmployee(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(employee);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated employee profile")
    public ResponseEntity<?> getCurrentEmployee(@org.springframework.security.core.annotation.AuthenticationPrincipal com.hrms.security.CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return employeeService.getEmployeeDetail(userDetails.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get full employee profile")
    public ResponseEntity<?> getEmployeeById(@PathVariable String id) {
        return employeeService.getEmployeeDetail(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    @Operation(summary = "Update employee details (ADMIN / HR_MANAGER only)")
    public ResponseEntity<?> updateEmployee(@PathVariable String id, @RequestBody EmployeeUpdateRequest req) {
        Employee updated = employeeService.updateEmployee(id, req);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    @Operation(summary = "Delete an employee (ADMIN / HR_MANAGER only)")
    public ResponseEntity<?> deleteEmployee(@PathVariable String id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
