package com.hrms.repository;

import com.hrms.entity.DailyWorkLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DailyWorkLogRepository extends JpaRepository<DailyWorkLog, String> {

    Optional<DailyWorkLog> findByEmployeeIdAndDate(String employeeId, LocalDate date);

    @Query("SELECT w FROM DailyWorkLog w WHERE " +
           "(:employeeId IS NULL OR :employeeId = '' OR w.employee.id = :employeeId) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(w.employee.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(w.employee.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(w.employee.employeeId) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(w.summary) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:date IS NULL OR w.date = :date)")
    Page<DailyWorkLog> searchWorkLogs(
            @Param("employeeId") String employeeId,
            @Param("search") String search,
            @Param("date") LocalDate date,
            Pageable pageable);
}
