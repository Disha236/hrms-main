package com.hrms.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrms.dto.PayrollGenerateRequest;
import com.hrms.entity.Employee;
import com.hrms.entity.Notification;
import com.hrms.entity.PaySlip;
import com.hrms.entity.enums.EmploymentStatus;
import com.hrms.entity.enums.NotificationType;
import com.hrms.entity.enums.PaySlipStatus;
import com.hrms.repository.EmployeeRepository;
import com.hrms.repository.PaySlipRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;

@Service
public class PayrollService {

    private final PaySlipRepository paySlipRepository;
    private final EmployeeRepository employeeRepository;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PayrollService(PaySlipRepository paySlipRepository,
                          EmployeeRepository employeeRepository,
                          NotificationService notificationService) {
        this.paySlipRepository = paySlipRepository;
        this.employeeRepository = employeeRepository;
        this.notificationService = notificationService;
    }

    public List<PaySlip> getPaySlips(int month, int year, String employeeId, boolean isAdmin) {
        if (isAdmin) {
            return paySlipRepository.findAllForPeriod(month, year);
        } else {
            return paySlipRepository.findByPeriodMonthAndPeriodYearAndEmployeeIdAndStatusIn(
                    month, year, employeeId, List.of(PaySlipStatus.FINALIZED, PaySlipStatus.PAID));
        }
    }

    public Optional<PaySlip> getPaySlipById(String id) {
        return paySlipRepository.findById(id);
    }

    @Transactional
    public Map<String, Object> generatePayroll(PayrollGenerateRequest req) {
        List<Employee> targetEmployees;
        if (req.getEmployeeIds() != null && !req.getEmployeeIds().isEmpty()) {
            targetEmployees = employeeRepository.findAllById(req.getEmployeeIds());
        } else {
            targetEmployees = employeeRepository.findByEmploymentStatus(EmploymentStatus.ACTIVE);
        }

        int created = 0;
        int updated = 0;

        for (Employee emp : targetEmployees) {
            double basic = emp.getSalary() != null ? emp.getSalary() : 3000.0;
            double allowance = Math.round((basic * 0.15) * 100.0) / 100.0;
            double tax = Math.round((basic * 0.10) * 100.0) / 100.0;
            double net = basic + allowance - tax;

            Map<String, Object> breakdown = new HashMap<>();
            breakdown.put("basicSalary", basic);
            breakdown.put("allowance", allowance);
            breakdown.put("tax", tax);
            breakdown.put("netSalary", net);

            String jsonBreakdown;
            try {
                jsonBreakdown = objectMapper.writeValueAsString(breakdown);
            } catch (Exception e) {
                jsonBreakdown = "{}";
            }

            Optional<PaySlip> existing = paySlipRepository.findByEmployeeIdAndPeriodMonthAndPeriodYear(
                    emp.getId(), req.getMonth(), req.getYear());

            PaySlip slip = existing.orElseGet(PaySlip::new);
            slip.setEmployee(emp);
            slip.setPeriodMonth(req.getMonth());
            slip.setPeriodYear(req.getYear());
            slip.setBasicSalary(basic);
            slip.setAllowances(allowance);
            slip.setDeductions(tax);
            slip.setNetSalary(net);
            slip.setBreakdown(jsonBreakdown);
            if (existing.isEmpty()) {
                slip.setStatus(PaySlipStatus.DRAFT);
                created++;
            } else {
                updated++;
            }

            paySlipRepository.save(slip);
        }

        Map<String, Object> res = new HashMap<>();
        res.put("total", targetEmployees.size());
        res.put("created", created);
        res.put("updated", updated);
        res.put("month", req.getMonth());
        res.put("year", req.getYear());
        return res;
    }

    @Transactional
    public PaySlip publishPaySlip(String id) {
        PaySlip slip = paySlipRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payslip not found"));

        if (slip.getStatus() == PaySlipStatus.FINALIZED || slip.getStatus() == PaySlipStatus.PAID) {
            throw new IllegalStateException("Payslip is already published");
        }

        slip.setStatus(PaySlipStatus.FINALIZED);
        PaySlip saved = paySlipRepository.save(slip);

        String monthName = Month.of(slip.getPeriodMonth()).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        notificationService.createNotification(slip.getEmployee(), "Payslip Available",
                "Your payslip for " + monthName + " " + slip.getPeriodYear() + " is ready to view.",
                NotificationType.PAYSLIP, "/documents/payslips/" + slip.getId());

        return saved;
    }

    @Transactional
    public PaySlip updateStatus(String id, PaySlipStatus status) {
        PaySlip slip = paySlipRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payslip not found"));
        slip.setStatus(status);
        if (status == PaySlipStatus.PAID) {
            slip.setPaidAt(LocalDateTime.now());
        }
        return paySlipRepository.save(slip);
    }
}
