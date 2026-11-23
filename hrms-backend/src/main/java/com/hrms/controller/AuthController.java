package com.hrms.controller;

import com.hrms.dto.AuthRequest;
import com.hrms.dto.AuthResponse;
import com.hrms.dto.UserSessionDto;
import com.hrms.security.CustomUserDetails;
import com.hrms.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Email and password login")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping("/login")
    @Operation(summary = "Email and password login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest req) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail().toLowerCase().trim(), req.getPassword().trim()));

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String token = tokenProvider.generateToken(userDetails);

        UserSessionDto userDto = new UserSessionDto(
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getName(),
                userDetails.getRole(),
                userDetails.getDepartmentId()
        );

        return ResponseEntity.ok(new AuthResponse(token, userDto));
    }
}
