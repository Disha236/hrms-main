package com.hrms.controller;

import com.hrms.entity.Attendance;
import com.hrms.entity.Employee;
import com.hrms.entity.PaySlip;
import com.hrms.entity.enums.AttendanceStatus;
import com.hrms.entity.enums.EmploymentStatus;
import com.hrms.entity.enums.LeaveStatus;
import com.hrms.entity.enums.PaySlipStatus;
import com.hrms.repository.AttendanceRepository;
import com.hrms.repository.EmployeeRepository;
import com.hrms.repository.LeaveRequestRepository;
import com.hrms.repository.MeetingRepository;
import com.hrms.repository.PaySlipRepository;
import com.hrms.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Admin and employee dashboard metrics")
public class DashboardController {

    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final PaySlipRepository paySlipRepository;
    private final MeetingRepository meetingRepository;

    public DashboardController(EmployeeRepository employeeRepository,
                               AttendanceRepository attendanceRepository,
                               LeaveRequestRepository leaveRequestRepository,
                               PaySlipRepository paySlipRepository,
                               MeetingRepository meetingRepository) {
        this.employeeRepository = employeeRepository;
        this.attendanceRepository = attendanceRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.paySlipRepository = paySlipRepository;
        this.meetingRepository = meetingRepository;
    }

    @GetMapping("/stats")
    @Operation(summary = "Get admin dashboard statistics")
    public ResponseEntity<?> getAdminStats() {
        LocalDate today = LocalDate.now();

        long totalEmployees = employeeRepository.countByEmploymentStatus(EmploymentStatus.ACTIVE);
        long presentToday = attendanceRepository.countByDateAndStatusIn(today, List.of(AttendanceStatus.PRESENT, AttendanceStatus.LATE));
        long onLeaveToday = attendanceRepository.countByDateAndStatus(today, AttendanceStatus.ON_LEAVE);
        long pendingLeaves = leaveRequestRepository.countByStatus(LeaveStatus.PENDING);

        List<Attendance> recentAttendances = attendanceRepository.findTop5ByDateOrderByCheckInDesc(today);
        List<Employee> upcomingBirthdays = employeeRepository.findTop5ByDateOfBirthBetweenOrderByDateOfBirthAsc(today, today.plusMonths(6));
        List<?> upcomingMeetings = meetingRepository.findTop5ByStartTimeAfterOrderByStartTimeAsc(LocalDateTime.now());

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEmployees", totalEmployees);
        stats.put("presentToday", presentToday);
        stats.put("onLeaveToday", onLeaveToday);
        stats.put("pendingLeaves", pendingLeaves);
        stats.put("recentAttendances", recentAttendances);
        stats.put("upcomingBirthdays", upcomingBirthdays);
        stats.put("upcomingMeetings", upcomingMeetings);

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/employee")
    @Operation(summary = "Get employee personal dashboard data")
    public ResponseEntity<?> getEmployeeStats(
            @RequestParam(required = false) String employeeId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String empId = (employeeId != null && !employeeId.isBlank()) ? employeeId : (userDetails != null ? userDetails.getId() : null);
        if (empId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Employee ID required"));
        }

        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());

        Optional<Employee> empOpt = employeeRepository.findById(empId);
        List<Attendance> attendances = attendanceRepository.findByEmployeeIdAndDateBetweenOrderByDateDesc(empId, monthStart, monthEnd);
        Optional<Attendance> todayAttendance = attendanceRepository.findByEmployeeIdAndDate(empId, today);
        long pendingLeaves = leaveRequestRepository.countByEmployeeIdAndStatus(empId, LeaveStatus.PENDING);
        List<PaySlip> recentPaySlips = paySlipRepository.findTop3ByEmployeeIdAndStatusInOrderByPeriodYearDescPeriodMonthDesc(
                empId, List.of(PaySlipStatus.FINALIZED, PaySlipStatus.PAID));
        List<?> upcomingMeetings = meetingRepository.findUpcomingForEmployee(empId, LocalDateTime.now());

        long onTime = attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT || a.getStatus() == AttendanceStatus.REMOTE).count();
        long late = attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.LATE).count();
        long absentLeaves = attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.ABSENT || a.getStatus() == AttendanceStatus.ON_LEAVE || a.getStatus() == AttendanceStatus.HALF_DAY || a.getStatus() == AttendanceStatus.EARLY_LEAVE).count();

        Map<String, Object> statsMap = new HashMap<>();
        statsMap.put("onTime", onTime);
        statsMap.put("late", late);
        statsMap.put("absentLeaves", absentLeaves);
        statsMap.put("todayStatus", todayAttendance.map(a -> a.getStatus().name()).orElse(null));

        Map<String, Object> response = new HashMap<>();
        response.put("employeeName", empOpt.map(e -> e.getFirstName() + " " + e.getLastName()).orElse("Employee"));
        response.put("monthLabel", today.getMonth().name() + " " + today.getYear());
        response.put("stats", statsMap);
        response.put("attendances", attendances);
        response.put("pendingLeaves", pendingLeaves);
        response.put("recentPaySlips", recentPaySlips);
        response.put("upcomingMeetings", upcomingMeetings);

        return ResponseEntity.ok(response);
    }
}
