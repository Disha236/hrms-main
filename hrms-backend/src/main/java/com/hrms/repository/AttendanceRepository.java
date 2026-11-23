package com.hrms.repository;

import com.hrms.entity.Attendance;
import com.hrms.entity.enums.AttendanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, String> {

    Optional<Attendance> findByEmployeeIdAndDate(String employeeId, LocalDate date);

    List<Attendance> findByEmployeeIdAndDateBetweenOrderByDateAsc(String employeeId, LocalDate start, LocalDate end);

    List<Attendance> findByDateBetweenOrderByDateDesc(LocalDate start, LocalDate end);

    List<Attendance> findTop10ByOrderByUpdatedAtDesc();

    List<Attendance> findTop30ByEmployeeIdOrderByDateDesc(String employeeId);

    long countByDateAndStatusIn(LocalDate date, List<AttendanceStatus> statuses);

    long countByDateAndStatus(LocalDate date, AttendanceStatus status);

    List<Attendance> findTop5ByDateOrderByCheckInDesc(LocalDate date);

    List<Attendance> findByEmployeeIdAndDateBetweenOrderByDateDesc(String employeeId, LocalDate start, LocalDate end);

    @Query("SELECT a FROM Attendance a WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(a.employee.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.employee.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.employee.employeeId) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:status IS NULL OR a.status = :status) AND " +
           "(:startDate IS NULL OR a.date >= :startDate) AND " +
           "(:endDate IS NULL OR a.date <= :endDate)")
    Page<Attendance> searchAttendance(
            @Param("search") String search,
            @Param("status") AttendanceStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);
}
