package com.hrms.service;

import com.hrms.dto.DailyWorkLogRequest;
import com.hrms.entity.DailyWorkLog;
import com.hrms.entity.Employee;
import com.hrms.entity.enums.EmploymentStatus;
import com.hrms.entity.enums.NotificationType;
import com.hrms.repository.DailyWorkLogRepository;
import com.hrms.repository.EmployeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class WorkLogService {

    private final DailyWorkLogRepository workLogRepository;
    private final EmployeeRepository employeeRepository;
    private final NotificationService notificationService;

    public WorkLogService(DailyWorkLogRepository workLogRepository,
                          EmployeeRepository employeeRepository,
                          NotificationService notificationService) {
        this.workLogRepository = workLogRepository;
        this.employeeRepository = employeeRepository;
        this.notificationService = notificationService;
    }

    public Map<String, Object> getWorkLogs(String employeeId, int page, int limit, String search, LocalDate date) {
        PageRequest pageable = PageRequest.of(page - 1, limit, Sort.by("date").descending());
        Page<DailyWorkLog> result = workLogRepository.searchWorkLogs(employeeId, search, date, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("logs", result.getContent());
        response.put("total", result.getTotalElements());
        response.put("totalPages", result.getTotalPages());
        response.put("currentPage", page);
        return response;
    }

    @Transactional
    public DailyWorkLog createWorkLog(String currentUserId, DailyWorkLogRequest req) {
        String empId = (req.getEmployeeId() != null && !req.getEmployeeId().isBlank())
                ? req.getEmployeeId() : currentUserId;

        Employee employee = employeeRepository.findById(empId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        LocalDate date = req.getDate() != null ? req.getDate() : LocalDate.now();

        Optional<DailyWorkLog> existing = workLogRepository.findByEmployeeIdAndDate(empId, date);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("A work log for this date already exists");
        }

        DailyWorkLog log = new DailyWorkLog();
        log.setEmployee(employee);
        log.setDate(date);
        log.setSummary(req.getSummary().trim());
        log.setTasksDone(req.getTasksDone());
        log.setBlockers(req.getBlockers());
        log.setNextSteps(req.getNextSteps());
        log.setHoursWorked(req.getHoursWorked());

        return workLogRepository.save(log);
    }

    public Map<String, Object> getWorkLogStatus(String employeeId) {
        LocalDate today = LocalDate.now();
        Optional<DailyWorkLog> log = workLogRepository.findByEmployeeIdAndDate(employeeId, today);

        Map<String, Object> res = new HashMap<>();
        res.put("submittedToday", log.isPresent());
        res.put("todayLog", log.orElse(null));
        return res;
    }

    @Transactional
    public Map<String, Object> sendReminders() {
        LocalDate today = LocalDate.now();
        List<Employee> activeEmployees = employeeRepository.findByEmploymentStatus(EmploymentStatus.ACTIVE);

        int remindersSent = 0;
        for (Employee emp : activeEmployees) {
            Optional<DailyWorkLog> log = workLogRepository.findByEmployeeIdAndDate(emp.getId(), today);
            if (log.isEmpty()) {
                notificationService.createNotification(emp, "Daily Work Log Reminder",
                        "Please remember to submit your daily work log for today (" + today + ").",
                        NotificationType.INFO, "/work-logs/submit");
                remindersSent++;
            }
        }

        Map<String, Object> res = new HashMap<>();
        res.put("remindersSent", remindersSent);
        res.put("date", today.toString());
        return res;
    }
}
