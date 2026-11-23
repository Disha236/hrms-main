package com.hrms.service;

import com.hrms.dto.LeaveCreateRequest;
import com.hrms.entity.Attendance;
import com.hrms.entity.Employee;
import com.hrms.entity.LeaveRequest;
import com.hrms.entity.enums.*;
import com.hrms.repository.AttendanceRepository;
import com.hrms.repository.EmployeeRepository;
import com.hrms.repository.LeaveRequestRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final NotificationService notificationService;

    private static final Map<LeaveType, Double> DEFAULT_ALLOCATIONS = Map.of(
            LeaveType.ANNUAL, 20.0,
            LeaveType.SICK, 10.0,
            LeaveType.CASUAL, 5.0,
            LeaveType.MATERNITY, 90.0,
            LeaveType.PATERNITY, 10.0,
            LeaveType.UNPAID, 365.0
    );

    public LeaveService(LeaveRequestRepository leaveRequestRepository,
                        EmployeeRepository employeeRepository,
                        AttendanceRepository attendanceRepository,
                        NotificationService notificationService) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.employeeRepository = employeeRepository;
        this.attendanceRepository = attendanceRepository;
        this.notificationService = notificationService;
    }

    public Map<String, Object> getLeaveRequests(String employeeId, int page, int limit, String search, String status, String type) {
        PageRequest pageable = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());

        LeaveStatus leaveStatus = null;
        if (status != null && !status.isBlank()) {
            try { leaveStatus = LeaveStatus.valueOf(status.toUpperCase().trim()); } catch (Exception ignored) {}
        }

        LeaveType leaveType = null;
        if (type != null && !type.isBlank()) {
            try { leaveType = LeaveType.valueOf(type.toUpperCase().trim()); } catch (Exception ignored) {}
        }

        Page<LeaveRequest> result = leaveRequestRepository.searchLeaveRequests(employeeId, search, leaveStatus, leaveType, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("requests", result.getContent());
        response.put("total", result.getTotalElements());
        response.put("totalPages", result.getTotalPages());
        response.put("currentPage", page);
        return response;
    }

    public Map<String, Object> getLeaveBalances(String employeeId, int year) {
        List<LeaveRequest> approved = leaveRequestRepository.findApprovedLeavesForYear(employeeId, year);

        Map<LeaveType, Double> used = new EnumMap<>(LeaveType.class);
        for (LeaveRequest lr : approved) {
            used.merge(lr.getLeaveType(), lr.getTotalDays(), Double::sum);
        }

        List<Map<String, Object>> balances = new ArrayList<>();
        for (LeaveType type : LeaveType.values()) {
            double allocated = DEFAULT_ALLOCATIONS.getOrDefault(type, 15.0);
            double taken = used.getOrDefault(type, 0.0);
            double remaining = Math.max(0.0, allocated - taken);

            Map<String, Object> item = new HashMap<>();
            item.put("type", type.name());
            item.put("allocated", allocated);
            item.put("used", taken);
            item.put("remaining", remaining);
            balances.add(item);
        }

        Map<String, Object> res = new HashMap<>();
        res.put("balances", balances);
        res.put("year", year);
        return res;
    }

    @Transactional
    public LeaveRequest submitLeaveRequest(String currentUserId, LeaveCreateRequest req) {
        String empId = (req.getEmployeeId() != null && !req.getEmployeeId().isBlank())
                ? req.getEmployeeId() : currentUserId;

        Employee employee = employeeRepository.findById(empId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        if (req.getEndDate().isBefore(req.getStartDate())) {
            throw new IllegalArgumentException("End date must be on or after start date");
        }

        double days = req.getTotalDays() != null && req.getTotalDays() > 0
                ? req.getTotalDays()
                : (ChronoUnit.DAYS.between(req.getStartDate(), req.getEndDate()) + 1);

        List<LeaveRequest> overlapping = leaveRequestRepository.findOverlappingRequests(
                empId, req.getStartDate(), req.getEndDate());
        if (!overlapping.isEmpty()) {
            throw new IllegalArgumentException("Employee already has an active leave request for this period");
        }

        LeaveRequest lr = new LeaveRequest();
        lr.setEmployee(employee);
        lr.setLeaveType(req.getLeaveType());
        lr.setStartDate(req.getStartDate());
        lr.setEndDate(req.getEndDate());
        lr.setTotalDays(days);
        lr.setReason(req.getReason());
        lr.setStatus(LeaveStatus.PENDING);

        LeaveRequest saved = leaveRequestRepository.save(lr);

        notificationService.createNotification(employee, "Leave Request Submitted",
                "Your " + req.getLeaveType().name().toLowerCase() + " request has been submitted.",
                NotificationType.LEAVE_REQUEST, "/leaves");

        List<Employee> managers = employeeRepository.findByRoleInAndEmploymentStatus(
                List.of(Role.ADMIN, Role.HR_MANAGER, Role.MANAGER), EmploymentStatus.ACTIVE);
        for (Employee manager : managers) {
            notificationService.createNotification(manager, "New Leave Request",
                    employee.getFullName() + " requested " + req.getLeaveType().name().toLowerCase() + " leave.",
                    NotificationType.LEAVE_REQUEST, "/approvals");
        }

        return saved;
    }

    @Transactional
    public LeaveRequest approveLeave(String leaveId, String reviewerId) {
        LeaveRequest lr = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found"));

        if (lr.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalStateException("Leave request is not pending");
        }

        Employee reviewer = employeeRepository.findById(reviewerId).orElse(null);
        lr.setStatus(LeaveStatus.APPROVED);
        lr.setApprovedBy(reviewer);
        lr.setApprovedAt(LocalDateTime.now());
        LeaveRequest updated = leaveRequestRepository.save(lr);

        LocalDate curr = lr.getStartDate();
        while (!curr.isAfter(lr.getEndDate())) {
            Optional<Attendance> existing = attendanceRepository.findByEmployeeIdAndDate(lr.getEmployee().getId(), curr);
            Attendance attendance = existing.orElseGet(Attendance::new);
            attendance.setEmployee(lr.getEmployee());
            attendance.setDate(curr);
            attendance.setStatus(AttendanceStatus.ON_LEAVE);
            attendance.setSource(AttendanceSource.MANUAL);
            attendance.setNotes("Leave: " + lr.getLeaveType().name());
            attendanceRepository.save(attendance);
            curr = curr.plusDays(1);
        }

        notificationService.createNotification(lr.getEmployee(), "Leave Request Approved",
                "Your leave request from " + lr.getStartDate() + " to " + lr.getEndDate() + " has been approved.",
                NotificationType.LEAVE_APPROVED, "/leaves");

        return updated;
    }

    @Transactional
    public LeaveRequest rejectLeave(String leaveId, String reviewerId, String rejectedReason) {
        LeaveRequest lr = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found"));

        if (lr.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalStateException("Leave request is not pending");
        }

        Employee reviewer = employeeRepository.findById(reviewerId).orElse(null);
        lr.setStatus(LeaveStatus.REJECTED);
        lr.setApprovedBy(reviewer);
        lr.setApprovedAt(LocalDateTime.now());
        lr.setRejectedReason(rejectedReason != null ? rejectedReason : "No reason provided");
        LeaveRequest updated = leaveRequestRepository.save(lr);

        notificationService.createNotification(lr.getEmployee(), "Leave Request Rejected",
                "Your leave request has been rejected. Reason: " + lr.getRejectedReason(),
                NotificationType.LEAVE_REJECTED, "/leaves");

        return updated;
    }

    @Transactional
    public LeaveRequest cancelLeave(String leaveId, String currentUserId, boolean isAdmin) {
        LeaveRequest lr = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found"));

        if (!isAdmin && !lr.getEmployee().getId().equals(currentUserId)) {
            throw new SecurityException("Forbidden");
        }

        if (lr.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalStateException("Only pending leave requests can be cancelled");
        }

        lr.setStatus(LeaveStatus.CANCELLED);
        return leaveRequestRepository.save(lr);
    }
}
