package com.hrms.repository;

import com.hrms.entity.LeaveRequest;
import com.hrms.entity.enums.LeaveStatus;
import com.hrms.entity.enums.LeaveType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, String> {

    List<LeaveRequest> findByEmployeeIdAndStatus(String employeeId, LeaveStatus status);

    List<LeaveRequest> findTop50ByStatusOrderByCreatedAtAsc(LeaveStatus status);

    List<LeaveRequest> findTop20ByEmployeeIdOrderByCreatedAtDesc(String employeeId);

    long countByStatus(LeaveStatus status);

    long countByEmployeeIdAndStatus(String employeeId, LeaveStatus status);

    @Query("SELECT r FROM LeaveRequest r WHERE " +
           "(:employeeId IS NULL OR :employeeId = '' OR r.employee.id = :employeeId) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(r.employee.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(r.employee.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(r.employee.employeeId) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:type IS NULL OR r.leaveType = :type)")
    Page<LeaveRequest> searchLeaveRequests(
            @Param("employeeId") String employeeId,
            @Param("search") String search,
            @Param("status") LeaveStatus status,
            @Param("type") LeaveType type,
            Pageable pageable);

    @Query("SELECT r FROM LeaveRequest r WHERE r.employee.id = :employeeId AND " +
           "r.status IN ('PENDING', 'APPROVED') AND " +
           "((r.startDate <= :endDate) AND (r.endDate >= :startDate))")
    List<LeaveRequest> findOverlappingRequests(
            @Param("employeeId") String employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT r FROM LeaveRequest r WHERE r.employee.id = :employeeId AND " +
           "r.status = 'APPROVED' AND " +
           "YEAR(r.startDate) = :year")
    List<LeaveRequest> findApprovedLeavesForYear(
            @Param("employeeId") String employeeId,
            @Param("year") int year);
}
