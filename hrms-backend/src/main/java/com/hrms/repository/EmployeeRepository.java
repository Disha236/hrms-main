package com.hrms.repository;

import com.hrms.entity.Employee;
import com.hrms.entity.enums.EmploymentStatus;
import com.hrms.entity.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> {

    Optional<Employee> findByEmail(String email);

    Optional<Employee> findByEmployeeId(String employeeId);

    Optional<Employee> findByBiometricId(String biometricId);

    boolean existsByEmail(String email);

    boolean existsByEmployeeId(String employeeId);

    boolean existsByBiometricId(String biometricId);

    List<Employee> findByEmploymentStatus(EmploymentStatus status);

    List<Employee> findByRoleInAndEmploymentStatus(List<Role> roles, EmploymentStatus status);

    List<Employee> findByDepartmentIdAndEmploymentStatus(String departmentId, EmploymentStatus status);

    long countByEmploymentStatus(EmploymentStatus status);

    long countByDepartmentId(String departmentId);

    List<Employee> findTop5ByDateOfBirthBetweenOrderByDateOfBirthAsc(java.time.LocalDate start, java.time.LocalDate end);

    @Query("SELECT e FROM Employee e WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(e.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.employeeId) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:departmentId IS NULL OR :departmentId = '' OR e.department.id = :departmentId) AND " +
           "(:status IS NULL OR e.employmentStatus = :status)")
    Page<Employee> searchEmployees(
            @Param("search") String search,
            @Param("departmentId") String departmentId,
            @Param("status") EmploymentStatus status,
            Pageable pageable);
}
