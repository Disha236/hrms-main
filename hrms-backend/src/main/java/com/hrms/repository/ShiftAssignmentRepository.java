package com.hrms.repository;

import com.hrms.entity.ShiftAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, String> {

    List<ShiftAssignment> findByShiftId(String shiftId);

    List<ShiftAssignment> findByEmployeeId(String employeeId);

    Optional<ShiftAssignment> findFirstByEmployeeIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            String employeeId, LocalDate date1, LocalDate date2);
}
