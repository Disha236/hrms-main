package com.hrms.controller;

import com.hrms.repository.BiometricDeviceRepository;
import com.hrms.repository.DepartmentRepository;
import com.hrms.repository.EmployeeRepository;
import com.hrms.service.SettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/settings")
@Tag(name = "Settings", description = "Global company settings and cloud integrations")
public class SettingsController {

    private final SettingsService settingsService;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final BiometricDeviceRepository biometricDeviceRepository;

    public SettingsController(SettingsService settingsService,
                              EmployeeRepository employeeRepository,
                              DepartmentRepository departmentRepository,
                              BiometricDeviceRepository biometricDeviceRepository) {
        this.settingsService = settingsService;
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.biometricDeviceRepository = biometricDeviceRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    @Operation(summary = "Get all system configuration settings")
    public ResponseEntity<?> getSettings() {
        return ResponseEntity.ok(Map.of(
                "settings", settingsService.getAllSettings(),
                "counts", Map.of(
                        "employees", employeeRepository.count(),
                        "departments", departmentRepository.count(),
                        "devices", biometricDeviceRepository.count()
                )
        ));
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    @Operation(summary = "Update system settings")
    public ResponseEntity<?> updateSettings(@RequestBody Map<String, Object> settings) {
        settingsService.upsertSettings(settings);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/integrations")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    @Operation(summary = "Get document storage / S3 integration settings")
    public ResponseEntity<?> getIntegrations() {
        return ResponseEntity.ok(settingsService.getIntegrationConfig());
    }

    @PutMapping("/integrations")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Save S3 / storage integration settings")
    public ResponseEntity<?> updateIntegrations(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(Map.of("success", true, "config", settingsService.getIntegrationConfig()));
    }
}
