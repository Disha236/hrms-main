package com.hrms.service;

import com.hrms.dto.EmployeeCreateRequest;
import com.hrms.dto.EmployeeUpdateRequest;
import com.hrms.entity.Department;
import com.hrms.entity.Employee;
import com.hrms.entity.enums.EmploymentStatus;
import com.hrms.repository.DepartmentRepository;
import com.hrms.repository.EmployeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.hrms.repository.AttendanceRepository attendanceRepository;
    private final com.hrms.repository.LeaveRequestRepository leaveRequestRepository;
    private final com.hrms.repository.PerformanceRepository performanceRepository;
    private final com.hrms.repository.DocumentRepository documentRepository;

    public EmployeeService(EmployeeRepository employeeRepository,
                           DepartmentRepository departmentRepository,
                           PasswordEncoder passwordEncoder,
                           com.hrms.repository.AttendanceRepository attendanceRepository,
                           com.hrms.repository.LeaveRequestRepository leaveRequestRepository,
                           com.hrms.repository.PerformanceRepository performanceRepository,
                           com.hrms.repository.DocumentRepository documentRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.attendanceRepository = attendanceRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.performanceRepository = performanceRepository;
        this.documentRepository = documentRepository;
    }

    public Optional<Map<String, Object>> getEmployeeDetail(String id) {
        Optional<Employee> empOpt = employeeRepository.findById(id);
        if (empOpt.isEmpty()) return Optional.empty();
        Employee emp = empOpt.get();

        Map<String, Object> map = new HashMap<>();
        map.put("id", emp.getId());
        map.put("employeeId", emp.getEmployeeId());
        map.put("firstName", emp.getFirstName());
        map.put("lastName", emp.getLastName());
        map.put("email", emp.getEmail());
        map.put("phone", emp.getPhone());
        map.put("dateOfBirth", emp.getDateOfBirth());
        map.put("gender", emp.getGender());
        map.put("address", emp.getAddress());
        map.put("department", emp.getDepartment());
        map.put("departmentId", emp.getDepartment() != null ? emp.getDepartment().getId() : null);
        map.put("position", emp.getPosition());
        map.put("hireDate", emp.getHireDate());
        map.put("salary", emp.getSalary());
        map.put("employmentStatus", emp.getEmploymentStatus());
        map.put("role", emp.getRole());
        map.put("bankAccount", emp.getBankAccount());
        map.put("emergencyContact", emp.getEmergencyContact());
        map.put("biometricId", emp.getBiometricId());
        map.put("createdAt", emp.getCreatedAt());
        map.put("updatedAt", emp.getUpdatedAt());

        map.put("attendances", attendanceRepository.findTop30ByEmployeeIdOrderByDateDesc(id));
        map.put("leaveRequests", leaveRequestRepository.findTop20ByEmployeeIdOrderByCreatedAtDesc(id));
        map.put("performances", performanceRepository.findTop10ByEmployeeIdOrderByReviewDateDesc(id));
        map.put("documents", documentRepository.findByEmployeeIdOrderByUploadedAtDesc(id));

        return Optional.of(map);
    }

    public Map<String, Object> getEmployees(int page, int limit, String search, String departmentId, String status) {
        PageRequest pageable = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());

        EmploymentStatus empStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                empStatus = EmploymentStatus.valueOf(status.toUpperCase().trim());
            } catch (IllegalArgumentException ignored) {}
        }

        Page<Employee> result = employeeRepository.searchEmployees(search, departmentId, empStatus, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("employees", result.getContent());
        response.put("total", result.getTotalElements());
        response.put("totalPages", result.getTotalPages());
        response.put("currentPage", page);
        return response;
    }

    public Optional<Employee> getEmployeeById(String id) {
        return employeeRepository.findById(id);
    }

    @Transactional
    public Employee createEmployee(EmployeeCreateRequest req) {
        if (employeeRepository.existsByEmail(req.getEmail().toLowerCase().trim())) {
            throw new IllegalArgumentException("Employee with this email already exists");
        }
        if (employeeRepository.existsByEmployeeId(req.getEmployeeId().trim())) {
            throw new IllegalArgumentException("Employee with this ID already exists");
        }
        if (req.getBiometricId() != null && !req.getBiometricId().isBlank() &&
                employeeRepository.existsByBiometricId(req.getBiometricId().trim())) {
            throw new IllegalArgumentException("Employee with this Biometric ID already exists");
        }

        Department department = null;
        if (req.getDepartmentId() != null && !req.getDepartmentId().isBlank()) {
            department = departmentRepository.findById(req.getDepartmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Selected department not found"));
        }

        Employee emp = new Employee();
        emp.setEmployeeId(req.getEmployeeId().trim());
        emp.setFirstName(req.getFirstName().trim());
        emp.setLastName(req.getLastName().trim());
        emp.setEmail(req.getEmail().toLowerCase().trim());

        String plainPassword = (req.getPassword() != null && !req.getPassword().isBlank())
                ? req.getPassword().trim() : "changeme123";
        emp.setPassword(passwordEncoder.encode(plainPassword));

        emp.setPhone(req.getPhone());
        emp.setDateOfBirth(req.getDateOfBirth());
        emp.setGender(req.getGender());
        emp.setAddress(req.getAddress());
        emp.setDepartment(department);
        emp.setPosition(req.getPosition().trim());
        emp.setHireDate(req.getHireDate());
        emp.setSalary(req.getSalary());
        emp.setBankAccount(req.getBankAccount());
        emp.setEmergencyContact(req.getEmergencyContact());
        emp.setEmergencyPhone(req.getEmergencyPhone());
        emp.setBiometricId(req.getBiometricId() != null && !req.getBiometricId().isBlank() ? req.getBiometricId().trim() : null);
        emp.setRole(req.getRole());
        emp.setEmploymentStatus(req.getEmploymentStatus());

        return employeeRepository.save(emp);
    }

    @Transactional
    public Employee updateEmployee(String id, EmployeeUpdateRequest req) {
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        if (req.getEmail() != null && !req.getEmail().equalsIgnoreCase(emp.getEmail())) {
            if (employeeRepository.existsByEmail(req.getEmail().toLowerCase().trim())) {
                throw new IllegalArgumentException("Email already taken");
            }
            emp.setEmail(req.getEmail().toLowerCase().trim());
        }

        if (req.getEmployeeId() != null && !req.getEmployeeId().equalsIgnoreCase(emp.getEmployeeId())) {
            if (employeeRepository.existsByEmployeeId(req.getEmployeeId().trim())) {
                throw new IllegalArgumentException("Employee ID already taken");
            }
            emp.setEmployeeId(req.getEmployeeId().trim());
        }

        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            emp.setPassword(passwordEncoder.encode(req.getPassword().trim()));
        }

        if (req.getFirstName() != null) emp.setFirstName(req.getFirstName().trim());
        if (req.getLastName() != null) emp.setLastName(req.getLastName().trim());
        if (req.getPhone() != null) emp.setPhone(req.getPhone());
        if (req.getDateOfBirth() != null) emp.setDateOfBirth(req.getDateOfBirth());
        if (req.getGender() != null) emp.setGender(req.getGender());
        if (req.getAddress() != null) emp.setAddress(req.getAddress());

        if (req.getDepartmentId() != null) {
            if (req.getDepartmentId().isBlank() || req.getDepartmentId().equals("none")) {
                emp.setDepartment(null);
            } else {
                Department department = departmentRepository.findById(req.getDepartmentId())
                        .orElseThrow(() -> new IllegalArgumentException("Selected department not found"));
                emp.setDepartment(department);
            }
        }

        if (req.getPosition() != null) emp.setPosition(req.getPosition().trim());
        if (req.getHireDate() != null) emp.setHireDate(req.getHireDate());
        if (req.getSalary() != null) emp.setSalary(req.getSalary());
        if (req.getBankAccount() != null) emp.setBankAccount(req.getBankAccount());
        if (req.getEmergencyContact() != null) emp.setEmergencyContact(req.getEmergencyContact());
        if (req.getEmergencyPhone() != null) emp.setEmergencyPhone(req.getEmergencyPhone());
        if (req.getBiometricId() != null) emp.setBiometricId(req.getBiometricId().isBlank() ? null : req.getBiometricId().trim());
        if (req.getRole() != null) emp.setRole(req.getRole());
        if (req.getEmploymentStatus() != null) emp.setEmploymentStatus(req.getEmploymentStatus());

        return employeeRepository.save(emp);
    }

    @Transactional
    public void deleteEmployee(String id) {
        employeeRepository.deleteById(id);
    }
}
