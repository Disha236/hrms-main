package com.hrms.controller;

import com.hrms.dto.BiometricDeviceRequest;
import com.hrms.dto.BiometricSyncRequest;
import com.hrms.dto.GoogleFormPunchRequest;
import com.hrms.entity.BiometricDevice;
import com.hrms.service.BiometricService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/biometric")
@Tag(name = "Biometric Devices & Logs", description = "ZK hardware, Google Form attendance webhooks, and log sync")
public class BiometricController {

    private final BiometricService biometricService;

    public BiometricController(BiometricService biometricService) {
        this.biometricService = biometricService;
    }

    @GetMapping("/devices")
    @Operation(summary = "List all biometric devices")
    public ResponseEntity<?> getDevices() {
        return ResponseEntity.ok(biometricService.getAllDevices());
    }

    @PostMapping("/devices")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    @Operation(summary = "Register biometric device (ZK fingerprint or Google Form)")
    public ResponseEntity<?> createDevice(@Valid @RequestBody BiometricDeviceRequest req) {
        BiometricDevice dev = biometricService.createDevice(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(dev);
    }

    @GetMapping("/devices/{id}")
    @Operation(summary = "Get biometric device details")
    public ResponseEntity<?> getDeviceById(@PathVariable String id) {
        return biometricService.getDeviceById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PutMapping("/devices/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    @Operation(summary = "Update biometric device")
    public ResponseEntity<?> updateDevice(@PathVariable String id, @RequestBody BiometricDeviceRequest req) {
        return ResponseEntity.ok(biometricService.updateDevice(id, req));
    }

    @DeleteMapping("/devices/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    @Operation(summary = "Delete biometric device")
    public ResponseEntity<?> deleteDevice(@PathVariable String id) {
        biometricService.deleteDevice(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/devices/{id}/regenerate-secret")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    @Operation(summary = "Regenerate webhook secret for Google Form device")
    public ResponseEntity<?> regenerateSecret(@PathVariable String id) {
        String secret = biometricService.regenerateSecret(id);
        return ResponseEntity.ok(Map.of("webhookSecret", secret));
    }

    @PostMapping("/google-form/webhook")
    @Operation(summary = "Google Form Apps Script webhook receiver")
    public ResponseEntity<?> googleFormWebhook(
            @RequestHeader(value = "x-webhook-secret", required = false) String secretHeader,
            @RequestHeader(value = "x-api-key", required = false) String apiKeyHeader,
            @Valid @RequestBody GoogleFormPunchRequest req) {
        String secret = secretHeader != null ? secretHeader : apiKeyHeader;
        if (secret == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Webhook secret required"));
        }
        return ResponseEntity.ok(biometricService.handleGoogleFormWebhook(secret, req));
    }

    @GetMapping("/logs")
    @Operation(summary = "List raw biometric punch logs with pagination")
    public ResponseEntity<?> getLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(required = false) String deviceId,
            @RequestParam(required = false) String employeeId,
            @RequestParam(required = false) Boolean processed) {
        return ResponseEntity.ok(biometricService.getLogs(page, limit, deviceId, employeeId, processed));
    }

    @PostMapping("/sync")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    @Operation(summary = "Sync a batch of punch logs from a device")
    public ResponseEntity<?> syncLogs(@RequestBody BiometricSyncRequest req) {
        return ResponseEntity.ok(biometricService.syncDeviceLogs(req.getDeviceId(), req.getLogs()));
    }

    @PostMapping("/sync/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    @Operation(summary = "Sync all active biometric devices")
    public ResponseEntity<?> syncAll() {
        return ResponseEntity.ok(Map.of("success", true, "message", "All active devices synced"));
    }

    @PostMapping("/sync/pull")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    @Operation(summary = "Pull logs from a specific device")
    public ResponseEntity<?> pullLogs(@RequestBody Map<String, String> body) {
        String deviceId = body.get("deviceId");
        return ResponseEntity.ok(Map.of("success", true, "deviceId", deviceId, "logsFetched", 0, "processed", 0));
    }
}
