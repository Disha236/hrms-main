package com.hrms.repository;

import com.hrms.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, String> {

    Optional<Department> findByName(String name);

    boolean existsByName(String name);

    List<Department> findAllByOrderByNameAsc();

    @Query("SELECT d.id, COUNT(e) FROM Department d LEFT JOIN Employee e ON e.department.id = d.id GROUP BY d.id")
    List<Object[]> countEmployeesPerDepartment();
}
