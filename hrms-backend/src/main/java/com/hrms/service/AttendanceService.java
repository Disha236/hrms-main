package com.hrms.service;

import com.hrms.dto.AttendanceCreateRequest;
import com.hrms.entity.Attendance;
import com.hrms.entity.Employee;
import com.hrms.entity.TimeEntry;
import com.hrms.entity.enums.AttendanceSource;
import com.hrms.entity.enums.AttendanceStatus;
import com.hrms.repository.AttendanceRepository;
import com.hrms.repository.EmployeeRepository;
import com.hrms.repository.TimeEntryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final TimeEntryRepository timeEntryRepository;

    public AttendanceService(AttendanceRepository attendanceRepository,
                             EmployeeRepository employeeRepository,
                             TimeEntryRepository timeEntryRepository) {
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
        this.timeEntryRepository = timeEntryRepository;
    }

    public Map<String, Object> getAttendanceRecords(int page, int limit, String search, String status, LocalDate dateFrom, LocalDate dateTo) {
        PageRequest pageable = PageRequest.of(page - 1, limit, Sort.by("date").descending());

        AttendanceStatus attStatus = null;
        if (status != null && !status.isBlank()) {
            try { attStatus = AttendanceStatus.valueOf(status.toUpperCase().trim()); } catch (Exception ignored) {}
        }

        Page<Attendance> result = attendanceRepository.searchAttendance(search, attStatus, dateFrom, dateTo, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("records", result.getContent());
        response.put("total", result.getTotalElements());
        response.put("totalPages", result.getTotalPages());
        response.put("currentPage", page);
        return response;
    }

    public Map<String, Object> getMonthlyCalendar(String employeeId, int month, int year) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        List<Attendance> records = attendanceRepository.findByEmployeeIdAndDateBetweenOrderByDateAsc(employeeId, start, end);

        Map<String, Object> res = new HashMap<>();
        res.put("records", records);
        res.put("month", month);
        res.put("year", year);
        return res;
    }

    public List<Attendance> getRecentRecords(int limit) {
        return attendanceRepository.findTop10ByOrderByUpdatedAtDesc();
    }

    @Transactional
    public Attendance punch(String employeeId, String type) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        LocalDateTime now = LocalDateTime.now();
        Attendance attendance = attendanceRepository.findByEmployeeIdAndDate(employee.getId(), now.toLocalDate())
                .orElseGet(Attendance::new);

        attendance.setEmployee(employee);
        attendance.setDate(now.toLocalDate());
        attendance.setSource(AttendanceSource.MANUAL);
        attendance.setStatus(AttendanceStatus.PRESENT);

        if ("CHECK_OUT".equalsIgnoreCase(type)) {
            attendance.setCheckOut(now);
        } else if (attendance.getCheckIn() == null) {
            attendance.setCheckIn(now);
        }

        if (attendance.getCheckIn() != null && attendance.getCheckOut() != null) {
            long minutes = Duration.between(attendance.getCheckIn(), attendance.getCheckOut()).toMinutes();
            int breakMins = attendance.getBreakMinutes() != null ? attendance.getBreakMinutes() : 0;
            double hours = Math.max(0.0, (minutes - breakMins) / 60.0);
            attendance.setWorkHours(Math.round(hours * 100.0) / 100.0);
            attendance.setTotalHours(Math.round((minutes / 60.0) * 100.0) / 100.0);
            attendance.setOvertimeHours(Math.max(0.0, attendance.getWorkHours() - 8.0));
        }

        return attendanceRepository.save(attendance);
    }

    @Transactional
    public Attendance upsertAttendance(AttendanceCreateRequest req) {
        Employee employee = employeeRepository.findById(req.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        LocalDate date = req.getDate() != null ? req.getDate() : LocalDate.now();

        Optional<Attendance> existing = attendanceRepository.findByEmployeeIdAndDate(employee.getId(), date);
        Attendance attendance = existing.orElseGet(Attendance::new);

        attendance.setEmployee(employee);
        attendance.setDate(date);

        if (req.getCheckIn() != null) attendance.setCheckIn(req.getCheckIn());
        if (req.getCheckOut() != null) attendance.setCheckOut(req.getCheckOut());

        if (req.getStatus() != null) attendance.setStatus(req.getStatus());
        if (req.getSource() != null) attendance.setSource(req.getSource());
        if (req.getDeviceId() != null) attendance.setDeviceId(req.getDeviceId());
        if (req.getLocation() != null) attendance.setLocation(req.getLocation());
        if (req.getNotes() != null) attendance.setNotes(req.getNotes());

        if (attendance.getCheckIn() != null && attendance.getCheckOut() != null) {
            long minutes = Duration.between(attendance.getCheckIn(), attendance.getCheckOut()).toMinutes();
            int breakMins = attendance.getBreakMinutes() != null ? attendance.getBreakMinutes() : 0;
            double hours = Math.max(0.0, (minutes - breakMins) / 60.0);
            attendance.setWorkHours(Math.round(hours * 100.0) / 100.0);
            attendance.setTotalHours(Math.round((minutes / 60.0) * 100.0) / 100.0);
            attendance.setOvertimeHours(Math.max(0.0, attendance.getWorkHours() - 8.0));
        }

        return attendanceRepository.save(attendance);
    }

    public Map<String, Object> generateReport(String employeeId, LocalDate from, LocalDate to, String groupBy) {
        if (from == null) from = LocalDate.now().withDayOfMonth(1);
        if (to == null) to = LocalDate.now();

        List<Attendance> attendanceRecords;
        if (employeeId != null && !employeeId.isBlank()) {
            attendanceRecords = attendanceRepository.findByEmployeeIdAndDateBetweenOrderByDateAsc(employeeId, from, to);
        } else {
            attendanceRecords = attendanceRepository.findByDateBetweenOrderByDateDesc(from, to);
        }

        List<TimeEntry> timeEntries = timeEntryRepository.findTimeEntries(employeeId, null, from, to);

        double totalAttendanceHours = 0;
        int daysPresent = 0;
        for (Attendance a : attendanceRecords) {
            if (a.getWorkHours() != null && a.getWorkHours() > 0) {
                totalAttendanceHours += a.getWorkHours();
                daysPresent++;
            }
        }

        double totalTaskHours = 0;
        for (TimeEntry te : timeEntries) {
            totalTaskHours += te.getHours();
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalAttendanceHours", Math.round(totalAttendanceHours * 100.0) / 100.0);
        summary.put("totalTaskHours", Math.round(totalTaskHours * 100.0) / 100.0);
        summary.put("daysPresent", daysPresent);

        Map<String, Object> response = new HashMap<>();
        response.put("dateFrom", from.toString());
        response.put("dateTo", to.toString());
        response.put("summary", summary);
        response.put("records", attendanceRecords);
        response.put("timeEntries", timeEntries);

        return response;
    }
}
