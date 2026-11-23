package com.hrms.security;

import com.hrms.entity.Employee;
import com.hrms.entity.enums.EmploymentStatus;
import com.hrms.entity.enums.Role;
import com.hrms.repository.EmployeeRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider tokenProvider;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.frontend.redirect-url:http://localhost:5173}")
    private String redirectUrl;

    public OAuth2LoginSuccessHandler(JwtTokenProvider tokenProvider,
                                     EmployeeRepository employeeRepository,
                                     @org.springframework.context.annotation.Lazy PasswordEncoder passwordEncoder) {
        this.tokenProvider = tokenProvider;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");

        if (email == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Email not found from OAuth provider");
            return;
        }

        email = email.toLowerCase().trim();
        Optional<Employee> existing = employeeRepository.findByEmail(email);

        Employee employee;
        if (existing.isPresent()) {
            employee = existing.get();
            if (picture != null && employee.getAvatarUrl() == null) {
                employee.setAvatarUrl(picture);
                employeeRepository.save(employee);
            }
        } else {
            String[] nameParts = name != null ? name.split(" ", 2) : new String[]{"User", ""};
            String firstName = nameParts[0];
            String lastName = nameParts.length > 1 ? nameParts[1] : "";

            employee = new Employee();
            employee.setEmail(email);
            employee.setFirstName(firstName);
            employee.setLastName(lastName);
            employee.setEmployeeId("EMP-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
            employee.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            employee.setPosition("Employee");
            employee.setHireDate(LocalDate.now());
            employee.setRole(Role.EMPLOYEE);
            employee.setEmploymentStatus(EmploymentStatus.ACTIVE);
            employee.setAvatarUrl(picture);
            employee = employeeRepository.save(employee);
        }

        CustomUserDetails userDetails = new CustomUserDetails(employee);
        String token = tokenProvider.generateToken(userDetails);

        String targetUrl = redirectUrl + "?token=" + token;
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
