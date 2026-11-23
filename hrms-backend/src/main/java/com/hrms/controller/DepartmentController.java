package com.hrms.controller;

import com.hrms.dto.DepartmentRequest;
import com.hrms.entity.Department;
import com.hrms.entity.Employee;
import com.hrms.repository.DepartmentRepository;
import com.hrms.repository.EmployeeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/departments")
@Tag(name = "Departments", description = "Department organization management")
public class DepartmentController {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    public DepartmentController(DepartmentRepository departmentRepository, EmployeeRepository employeeRepository) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
    }

    @GetMapping
    @Operation(summary = "List all departments with manager and count")
    public ResponseEntity<?> getAllDepartments() {
        List<Department> departments = departmentRepository.findAllByOrderByNameAsc();
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (Department d : departments) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", d.getId());
            map.put("name", d.getName());
            map.put("description", d.getDescription());
            map.put("manager", d.getManager());
            map.put("createdAt", d.getCreatedAt());
            map.put("updatedAt", d.getUpdatedAt());
            long count = employeeRepository.countByDepartmentId(d.getId());
            map.put("_count", Map.of("employees", count));
            result.add(map);
        }
        Map<String, Object> res = new HashMap<>();
        res.put("departments", result);
        return ResponseEntity.ok(res);
    }

    @PostMapping
    @Operation(summary = "Create a new department")
    public ResponseEntity<?> createDepartment(@Valid @RequestBody DepartmentRequest req) {
        if (departmentRepository.existsByName(req.getName().trim())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Department already exists"));
        }

        Department d = new Department();
        d.setName(req.getName().trim());
        d.setDescription(req.getDescription());

        if (req.getManagerId() != null && !req.getManagerId().isBlank()) {
            employeeRepository.findById(req.getManagerId()).ifPresent(d::setManager);
        }

        Department saved = departmentRepository.save(d);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}
