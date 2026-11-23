package com.hrms.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrms.dto.ShiftCreateRequest;
import com.hrms.entity.Department;
import com.hrms.entity.Employee;
import com.hrms.entity.Shift;
import com.hrms.entity.ShiftAssignment;
import com.hrms.entity.enums.NotificationType;
import com.hrms.repository.DepartmentRepository;
import com.hrms.repository.EmployeeRepository;
import com.hrms.repository.ShiftAssignmentRepository;
import com.hrms.repository.ShiftRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class ShiftService {

    private final ShiftRepository shiftRepository;
    private final ShiftAssignmentRepository assignmentRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ShiftService(ShiftRepository shiftRepository,
                        ShiftAssignmentRepository assignmentRepository,
                        EmployeeRepository employeeRepository,
                        DepartmentRepository departmentRepository,
                        NotificationService notificationService) {
        this.shiftRepository = shiftRepository;
        this.assignmentRepository = assignmentRepository;
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.notificationService = notificationService;
    }

    public List<Map<String, Object>> getAllShifts() {
        List<Shift> shifts = shiftRepository.findAllByOrderByNameAsc();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Shift s : shifts) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", s.getId());
            map.put("name", s.getName());
            map.put("startTime", s.getStartTime());
            map.put("endTime", s.getEndTime());
            map.put("breakMinutes", s.getBreakMinutes());
            map.put("department", s.getDepartment());
            map.put("isActive", s.getIsActive());
            map.put("assignments", assignmentRepository.findByShiftId(s.getId()));

            List<String> days = new ArrayList<>();
            try {
                if (s.getWorkingDays() != null) {
                    days = objectMapper.readValue(s.getWorkingDays(), new TypeReference<List<String>>() {});
                }
            } catch (Exception ignored) {}
            map.put("workingDays", days);
            result.add(map);
        }

        return result;
    }

    @Transactional
    public Shift createShift(ShiftCreateRequest req) {
        Shift shift = new Shift();
        shift.setName(req.getName().trim());
        shift.setStartTime(req.getStartTime());
        shift.setEndTime(req.getEndTime());
        shift.setBreakMinutes(req.getBreakMinutes() != null ? req.getBreakMinutes() : 60);

        List<String> days = req.getWorkingDays() != null && !req.getWorkingDays().isEmpty()
                ? req.getWorkingDays() : List.of("MON", "TUE", "WED", "THU", "FRI");
        try {
            shift.setWorkingDays(objectMapper.writeValueAsString(days));
        } catch (Exception e) {
            shift.setWorkingDays("[\"MON\",\"TUE\",\"WED\",\"THU\",\"FRI\"]");
        }

        if (req.getDepartmentId() != null && !req.getDepartmentId().isBlank()) {
            departmentRepository.findById(req.getDepartmentId()).ifPresent(shift::setDepartment);
        }

        shift.setIsActive(req.getIsActive() != null ? req.getIsActive() : true);
        return shiftRepository.save(shift);
    }

    @Transactional
    public Shift updateShift(String id, ShiftCreateRequest req) {
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Shift not found"));

        if (req.getName() != null) shift.setName(req.getName().trim());
        if (req.getStartTime() != null) shift.setStartTime(req.getStartTime());
        if (req.getEndTime() != null) shift.setEndTime(req.getEndTime());
        if (req.getBreakMinutes() != null) shift.setBreakMinutes(req.getBreakMinutes());
        if (req.getWorkingDays() != null) {
            try {
                shift.setWorkingDays(objectMapper.writeValueAsString(req.getWorkingDays()));
            } catch (Exception ignored) {}
        }
        if (req.getDepartmentId() != null) {
            if (req.getDepartmentId().isBlank()) shift.setDepartment(null);
            else departmentRepository.findById(req.getDepartmentId()).ifPresent(shift::setDepartment);
        }
        if (req.getIsActive() != null) shift.setIsActive(req.getIsActive());

        return shiftRepository.save(shift);
    }

    @Transactional
    public void deactivateShift(String id) {
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Shift not found"));
        shift.setIsActive(false);
        shiftRepository.save(shift);
    }

    @Transactional
    public ShiftAssignment assignShift(String shiftId, String employeeId, LocalDate startDate, LocalDate endDate) {
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new IllegalArgumentException("Shift not found"));
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        ShiftAssignment assignment = new ShiftAssignment(shift, employee, startDate, endDate);
        ShiftAssignment saved = assignmentRepository.save(assignment);

        notificationService.createNotification(employee, "Shift Assigned",
                "You have been assigned to the \"" + shift.getName() + "\" shift.",
                NotificationType.INFO, "/attendance/my-calendar");

        return saved;
    }
}
