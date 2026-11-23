package com.hrms.service;

import com.hrms.dto.BiometricDeviceRequest;
import com.hrms.dto.BiometricPunchRequest;
import com.hrms.dto.BiometricSyncRequest;
import com.hrms.dto.GoogleFormPunchRequest;
import com.hrms.entity.Attendance;
import com.hrms.entity.BiometricDevice;
import com.hrms.entity.BiometricLog;
import com.hrms.entity.Employee;
import com.hrms.entity.enums.AttendanceSource;
import com.hrms.entity.enums.AttendanceStatus;
import com.hrms.entity.enums.DeviceType;
import com.hrms.repository.AttendanceRepository;
import com.hrms.repository.BiometricDeviceRepository;
import com.hrms.repository.BiometricLogRepository;
import com.hrms.repository.EmployeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class BiometricService {

    private final BiometricDeviceRepository deviceRepository;
    private final BiometricLogRepository logRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;

    public BiometricService(BiometricDeviceRepository deviceRepository,
                            BiometricLogRepository logRepository,
                            EmployeeRepository employeeRepository,
                            AttendanceRepository attendanceRepository) {
        this.deviceRepository = deviceRepository;
        this.logRepository = logRepository;
        this.employeeRepository = employeeRepository;
        this.attendanceRepository = attendanceRepository;
    }

    public List<BiometricDevice> getAllDevices() {
        return deviceRepository.findAllByOrderByCreatedAtDesc();
    }

    public Optional<BiometricDevice> getDeviceById(String id) {
        return deviceRepository.findById(id);
    }

    @Transactional
    public BiometricDevice createDevice(BiometricDeviceRequest req) {
        if (deviceRepository.existsByDeviceId(req.getDeviceId())) {
            throw new IllegalArgumentException("Device with this ID already exists");
        }

        BiometricDevice dev = new BiometricDevice();
        dev.setDeviceId(req.getDeviceId().trim());
        dev.setName(req.getName().trim());
        dev.setDeviceType(req.getDeviceType() != null ? req.getDeviceType() : DeviceType.ZK_DEVICE);
        dev.setLocation(req.getLocation());
        dev.setIpAddress(req.getIpAddress());
        dev.setPort(req.getPort() != null ? req.getPort() : 4370);
        dev.setGoogleFormUrl(req.getGoogleFormUrl());
        dev.setIsActive(req.getIsActive() != null ? req.getIsActive() : true);

        if (dev.getDeviceType() == DeviceType.GOOGLE_FORM) {
            dev.setWebhookSecret(generateSecret());
        }

        return deviceRepository.save(dev);
    }

    @Transactional
    public BiometricDevice updateDevice(String id, BiometricDeviceRequest req) {
        BiometricDevice dev = deviceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Device not found"));

        if (req.getName() != null) dev.setName(req.getName().trim());
        if (req.getLocation() != null) dev.setLocation(req.getLocation());
        if (req.getIpAddress() != null) dev.setIpAddress(req.getIpAddress());
        if (req.getPort() != null) dev.setPort(req.getPort());
        if (req.getGoogleFormUrl() != null) dev.setGoogleFormUrl(req.getGoogleFormUrl());
        if (req.getIsActive() != null) dev.setIsActive(req.getIsActive());

        return deviceRepository.save(dev);
    }

    @Transactional
    public void deleteDevice(String id) {
        deviceRepository.deleteById(id);
    }

    @Transactional
    public String regenerateSecret(String deviceId) {
        BiometricDevice dev = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("Device not found"));

        if (dev.getDeviceType() != DeviceType.GOOGLE_FORM) {
            throw new IllegalArgumentException("Only Google Form integrations have webhook secrets");
        }

        String newSecret = generateSecret();
        dev.setWebhookSecret(newSecret);
        deviceRepository.save(dev);
        return newSecret;
    }

    public Map<String, Object> getLogs(int page, int limit, String deviceId, String employeeId, Boolean processed) {
        PageRequest pageable = PageRequest.of(page - 1, limit, Sort.by("timestamp").descending());
        Page<BiometricLog> result = logRepository.searchLogs(deviceId, employeeId, processed, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("logs", result.getContent());
        response.put("total", result.getTotalElements());
        response.put("totalPages", result.getTotalPages());
        response.put("currentPage", page);
        return response;
    }

    @Transactional
    public Map<String, Object> processPunch(BiometricPunchRequest req) {
        Employee employee = employeeRepository.findByBiometricId(req.getBiometricId())
                .orElseGet(() -> employeeRepository.findByEmployeeId(req.getBiometricId())
                        .orElseThrow(() -> new IllegalArgumentException("Employee not found for biometric ID: " + req.getBiometricId())));

        LocalDateTime punchTime = req.getTimestamp() != null ? req.getTimestamp() : LocalDateTime.now();
        LocalDate punchDate = punchTime.toLocalDate();

        Attendance attendance = attendanceRepository.findByEmployeeIdAndDate(employee.getId(), punchDate)
                .orElseGet(Attendance::new);

        attendance.setEmployee(employee);
        attendance.setDate(punchDate);
        attendance.setSource(AttendanceSource.BIOMETRIC);
        if (req.getDeviceId() != null) attendance.setDeviceId(req.getDeviceId());
        if (req.getLocation() != null) attendance.setLocation(req.getLocation());

        if ("CHECK_IN".equalsIgnoreCase(req.getType())) {
            if (attendance.getCheckIn() == null || punchTime.isBefore(attendance.getCheckIn())) {
                attendance.setCheckIn(punchTime);
            }
        } else if ("CHECK_OUT".equalsIgnoreCase(req.getType())) {
            if (attendance.getCheckOut() == null || punchTime.isAfter(attendance.getCheckOut())) {
                attendance.setCheckOut(punchTime);
            }
        }

        if (attendance.getCheckIn() != null && attendance.getCheckOut() != null) {
            long minutes = Duration.between(attendance.getCheckIn(), attendance.getCheckOut()).toMinutes();
            double hours = Math.max(0.0, minutes / 60.0);
            attendance.setWorkHours(Math.round(hours * 100.0) / 100.0);
            attendance.setTotalHours(attendance.getWorkHours());
        }

        attendance.setStatus(AttendanceStatus.PRESENT);
        attendanceRepository.save(attendance);

        BiometricLog log = new BiometricLog();
        if (req.getDeviceId() != null) {
            deviceRepository.findByDeviceId(req.getDeviceId()).ifPresent(log::setDevice);
        }
        log.setEmployee(employee);
        log.setBiometricId(req.getBiometricId());
        log.setTimestamp(punchTime);
        log.setEventType(req.getType().toUpperCase());
        log.setProcessed(true);
        logRepository.save(log);

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("employee", employee.getFullName());
        res.put("type", req.getType());
        res.put("timestamp", punchTime);
        return res;
    }

    @Transactional
    public Map<String, Object> handleGoogleFormWebhook(String secret, GoogleFormPunchRequest req) {
        BiometricDevice dev = deviceRepository.findByDeviceId(req.getDeviceId())
                .orElseThrow(() -> new IllegalArgumentException("Device not found"));

        if (!dev.getIsActive()) {
            throw new IllegalStateException("Device is inactive");
        }

        if (dev.getWebhookSecret() == null || !dev.getWebhookSecret().equals(secret)) {
            throw new SecurityException("Invalid webhook secret");
        }

        BiometricPunchRequest punch = new BiometricPunchRequest();
        punch.setBiometricId(req.getEmployeeId());
        punch.setDeviceId(req.getDeviceId());
        punch.setType("CHECK_IN");
        if (req.getTimestamp() != null) {
            try { punch.setTimestamp(LocalDateTime.parse(req.getTimestamp())); } catch (Exception ignored) {}
        }

        return processPunch(punch);
    }

    @Transactional
    public Map<String, Object> syncDeviceLogs(String deviceId, List<BiometricSyncRequest.LogItem> logs) {
        int processed = 0;
        List<String> errors = new ArrayList<>();

        for (BiometricSyncRequest.LogItem item : logs) {
            try {
                BiometricPunchRequest punch = new BiometricPunchRequest();
                punch.setDeviceId(deviceId);
                punch.setBiometricId(item.getBiometricId());
                punch.setType(item.getEventType());
                punch.setTimestamp(item.getTimestamp());
                processPunch(punch);
                processed++;
            } catch (Exception e) {
                errors.add(e.getMessage());
            }
        }

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("processed", processed);
        res.put("errors", errors);
        return res;
    }

    private String generateSecret() {
        byte[] bytes = new byte[24];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
