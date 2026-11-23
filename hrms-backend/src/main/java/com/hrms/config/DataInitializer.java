package com.hrms.config;

import com.hrms.entity.AppSetting;
import com.hrms.entity.Department;
import com.hrms.entity.Employee;
import com.hrms.entity.Shift;
import com.hrms.entity.enums.EmploymentStatus;
import com.hrms.entity.enums.Role;
import com.hrms.repository.AppSettingRepository;
import com.hrms.repository.DepartmentRepository;
import com.hrms.repository.EmployeeRepository;
import com.hrms.repository.ShiftRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final ShiftRepository shiftRepository;
    private final AppSettingRepository settingRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(EmployeeRepository employeeRepository,
                           DepartmentRepository departmentRepository,
                           ShiftRepository shiftRepository,
                           AppSettingRepository settingRepository,
                           PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.shiftRepository = shiftRepository;
        this.settingRepository = settingRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        Department engineering = null;
        if (departmentRepository.count() == 0) {
            Department d1 = new Department();
            d1.setName("Engineering");
            d1.setDescription("Software development and IT operations");
            engineering = departmentRepository.save(d1);

            Department d2 = new Department();
            d2.setName("Human Resources");
            d2.setDescription("People operations and recruitment");
            departmentRepository.save(d2);

            Department d3 = new Department();
            d3.setName("Finance");
            d3.setDescription("Accounting and financial reporting");
            departmentRepository.save(d3);
        } else {
            engineering = departmentRepository.findByName("Engineering").orElse(null);
        }

        Optional<Employee> existingCompanyAdmin = employeeRepository.findByEmail("admin@company.com");
        if (existingCompanyAdmin.isEmpty()) {
            Employee admin = new Employee();
            admin.setEmployeeId("EMP-ADM01");
            admin.setFirstName("System");
            admin.setLastName("Admin");
            admin.setEmail("admin@company.com");
            admin.setPassword(passwordEncoder.encode("Admin@123"));
            admin.setPosition("System Administrator");
            admin.setHireDate(LocalDate.now());
            admin.setSalary(75000.0);
            admin.setRole(Role.ADMIN);
            admin.setEmploymentStatus(EmploymentStatus.ACTIVE);
            admin.setDepartment(engineering);
            employeeRepository.save(admin);
            System.out.println("Default Admin created: admin@company.com / Admin@123");
        } else {
            Employee admin = existingCompanyAdmin.get();
            admin.setPassword(passwordEncoder.encode("Admin@123"));
            employeeRepository.save(admin);
        }

        if (employeeRepository.findByEmail("admin@example.com").isEmpty()) {
            Employee admin2 = new Employee();
            admin2.setEmployeeId("EMP-ADM02");
            admin2.setFirstName("System");
            admin2.setLastName("Admin");
            admin2.setEmail("admin@example.com");
            admin2.setPassword(passwordEncoder.encode("changeme123"));
            admin2.setPosition("System Administrator");
            admin2.setHireDate(LocalDate.now());
            admin2.setSalary(75000.0);
            admin2.setRole(Role.ADMIN);
            admin2.setEmploymentStatus(EmploymentStatus.ACTIVE);
            admin2.setDepartment(engineering);
            employeeRepository.save(admin2);
            System.out.println("Default Admin created: admin@example.com / changeme123");
        }

        if (shiftRepository.count() == 0) {
            Shift s = new Shift();
            s.setName("Standard General Shift");
            s.setStartTime("09:00");
            s.setEndTime("18:00");
            s.setBreakMinutes(60);
            s.setWorkingDays("[\"MON\",\"TUE\",\"WED\",\"THU\",\"FRI\"]");
            s.setIsActive(true);
            shiftRepository.save(s);
        }

        if (settingRepository.count() == 0) {
            settingRepository.save(new AppSetting("company_name", "HRMS Enterprise"));
            settingRepository.save(new AppSetting("worklog_reminder_enabled", "true"));
            settingRepository.save(new AppSetting("doc_storage_enabled", "false"));
        }
    }
}
