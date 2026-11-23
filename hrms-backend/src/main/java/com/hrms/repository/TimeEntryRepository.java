package com.hrms.repository;

import com.hrms.entity.TimeEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TimeEntryRepository extends JpaRepository<TimeEntry, String> {

    List<TimeEntry> findByIssueIdOrderByDateDesc(String issueId);

    @Query("SELECT t FROM TimeEntry t WHERE " +
           "(:employeeId IS NULL OR :employeeId = '' OR t.employee.id = :employeeId) AND " +
           "(:issueId IS NULL OR :issueId = '' OR t.issue.id = :issueId) AND " +
           "(:startDate IS NULL OR t.date >= :startDate) AND " +
           "(:endDate IS NULL OR t.date <= :endDate) " +
           "ORDER BY t.date DESC")
    List<TimeEntry> findTimeEntries(
            @Param("employeeId") String employeeId,
            @Param("issueId") String issueId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    long countByIssueId(String issueId);
}
