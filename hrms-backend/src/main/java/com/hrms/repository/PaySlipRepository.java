package com.hrms.repository;

import com.hrms.entity.PaySlip;
import com.hrms.entity.enums.PaySlipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaySlipRepository extends JpaRepository<PaySlip, String> {

    List<PaySlip> findByPeriodMonthAndPeriodYear(Integer month, Integer year);

    List<PaySlip> findByPeriodMonthAndPeriodYearAndEmployeeIdAndStatusIn(
            Integer month, Integer year, String employeeId, List<PaySlipStatus> statuses);

    List<PaySlip> findByEmployeeIdAndStatusInOrderByPeriodYearDescPeriodMonthDesc(
            String employeeId, List<PaySlipStatus> statuses);

    List<PaySlip> findTop3ByEmployeeIdAndStatusInOrderByPeriodYearDescPeriodMonthDesc(
            String employeeId, List<PaySlipStatus> statuses);

    Optional<PaySlip> findByEmployeeIdAndPeriodMonthAndPeriodYear(
            String employeeId, Integer month, Integer year);

    @Query("SELECT p FROM PaySlip p WHERE p.periodMonth = :month AND p.periodYear = :year ORDER BY p.employee.lastName ASC")
    List<PaySlip> findAllForPeriod(@Param("month") Integer month, @Param("year") Integer year);
}
