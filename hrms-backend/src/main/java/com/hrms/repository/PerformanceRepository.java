package com.hrms.repository;

import com.hrms.entity.Performance;
import com.hrms.entity.enums.PerformanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PerformanceRepository extends JpaRepository<Performance, String> {

    List<Performance> findTop20ByStatusInOrderByCreatedAtAsc(List<PerformanceStatus> statuses);

    List<Performance> findTop10ByEmployeeIdOrderByReviewDateDesc(String employeeId);

    @Query("SELECT p FROM Performance p WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(p.employee.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.employee.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.employee.employeeId) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.reviewPeriod) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:period IS NULL OR :period = '' OR p.reviewPeriod = :period)")
    Page<Performance> searchPerformances(
            @Param("search") String search,
            @Param("status") PerformanceStatus status,
            @Param("period") String period,
            Pageable pageable);
}
