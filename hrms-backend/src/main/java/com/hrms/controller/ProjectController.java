package com.hrms.controller;

import com.hrms.dto.ProjectCreateRequest;
import com.hrms.dto.ProjectMemberRequest;
import com.hrms.dto.SprintCreateRequest;
import com.hrms.entity.Project;
import com.hrms.entity.ProjectMember;
import com.hrms.entity.Sprint;
import com.hrms.entity.enums.ProjectRole;
import com.hrms.security.CustomUserDetails;
import com.hrms.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
@Tag(name = "Projects", description = "Project management, members, and sprints")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    @Operation(summary = "List all projects")
    public ResponseEntity<?> getProjects() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    @PostMapping
    @Operation(summary = "Create a new project")
    public ResponseEntity<?> createProject(
            @Valid @RequestBody ProjectCreateRequest req,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Project project = projectService.createProject(userDetails.getId(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(project);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project details")
    public ResponseEntity<?> getProjectById(@PathVariable String id) {
        return projectService.getProjectDetail(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update project")
    public ResponseEntity<?> updateProject(@PathVariable String id, @RequestBody ProjectCreateRequest req) {
        return ResponseEntity.ok(projectService.updateProject(id, req));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete project")
    public ResponseEntity<?> deleteProject(@PathVariable String id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/{id}/members")
    @Operation(summary = "Add or update project member")
    public ResponseEntity<?> addMember(@PathVariable String id, @RequestBody Map<String, String> body) {
        String empId = body.get("employeeId");
        ProjectRole role = body.get("role") != null ? ProjectRole.valueOf(body.get("role").toUpperCase()) : ProjectRole.MEMBER;
        ProjectMember member = projectService.addMember(id, empId, role);
        return ResponseEntity.status(HttpStatus.CREATED).body(member);
    }

    @DeleteMapping("/{id}/members")
    @Operation(summary = "Remove project member")
    public ResponseEntity<?> removeMember(@PathVariable String id, @RequestParam String employeeId) {
        projectService.removeMember(id, employeeId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/{id}/sprints")
    @Operation(summary = "List sprints for project")
    public ResponseEntity<?> getProjectSprints(@PathVariable String id) {
        return ResponseEntity.ok(projectService.getProjectSprints(id));
    }

    @PostMapping("/{id}/sprints")
    @Operation(summary = "Create sprint for project")
    public ResponseEntity<?> createSprint(@PathVariable String id, @Valid @RequestBody SprintCreateRequest req) {
        Sprint sprint = projectService.createSprint(id, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(sprint);
    }
}
