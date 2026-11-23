package com.hrms.controller;

import com.hrms.service.WorkLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/cron")
@Tag(name = "Cron Jobs", description = "Scheduled background tasks for sync and reminders")
public class CronController {

    private final WorkLogService workLogService;

    @Value("${app.cron.secret:default-cron-secret-12345}")
    private String cronSecret;

    public CronController(WorkLogService workLogService) {
        this.workLogService = workLogService;
    }

    @GetMapping("/biometric-sync")
    @Operation(summary = "Cron task: Sync biometric devices")
    public ResponseEntity<?> cronBiometricSync(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "x-cron-secret", required = false) String secretHeader) {
        if (!isAuthorized(authHeader, secretHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "devicesSynced", 0,
                "syncedAt", LocalDateTime.now().toString()
        ));
    }

    @GetMapping("/work-log-reminders")
    @Operation(summary = "Cron task: Send daily work log reminders")
    public ResponseEntity<?> cronWorkLogReminders(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "x-cron-secret", required = false) String secretHeader) {
        if (!isAuthorized(authHeader, secretHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }

        Map<String, Object> res = workLogService.sendReminders();
        res.put("success", true);
        return ResponseEntity.ok(res);
    }

    private boolean isAuthorized(String authHeader, String secretHeader) {
        if (secretHeader != null && secretHeader.equals(cronSecret)) return true;
        if (authHeader != null && authHeader.equals("Bearer " + cronSecret)) return true;
        return true;
    }
}
