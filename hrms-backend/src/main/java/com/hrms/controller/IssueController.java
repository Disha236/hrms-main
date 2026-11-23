package com.hrms.controller;

import com.hrms.dto.IssueCreateRequest;
import com.hrms.entity.Issue;
import com.hrms.entity.IssueComment;
import com.hrms.repository.EmployeeRepository;
import com.hrms.repository.IssueRepository;
import com.hrms.repository.SprintRepository;
import com.hrms.security.CustomUserDetails;
import com.hrms.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/issues")
@Tag(name = "Issues (Tickets)", description = "Task tracking, bug reports, and discussion comments")
public class IssueController {

    private final ProjectService projectService;
    private final IssueRepository issueRepository;
    private final EmployeeRepository employeeRepository;
    private final SprintRepository sprintRepository;

    public IssueController(ProjectService projectService,
                           IssueRepository issueRepository,
                           EmployeeRepository employeeRepository,
                           SprintRepository sprintRepository) {
        this.projectService = projectService;
        this.issueRepository = issueRepository;
        this.employeeRepository = employeeRepository;
        this.sprintRepository = sprintRepository;
    }

    @GetMapping
    @Operation(summary = "List issues with optional project, assignee, and status filter")
    public ResponseEntity<?> getIssues(
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) String assigneeId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "false") boolean mine,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String targetAssignee = mine ? userDetails.getId() : assigneeId;
        return ResponseEntity.ok(projectService.getIssues(projectId, targetAssignee, status));
    }

    @PostMapping
    @Operation(summary = "Create an issue in a project")
    public ResponseEntity<?> createIssue(
            @Valid @RequestBody IssueCreateRequest req,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Issue issue = projectService.createIssue(userDetails.getId(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(issue);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get issue details with comments")
    public ResponseEntity<?> getIssueById(@PathVariable String id) {
        return projectService.getIssueById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update issue fields")
    public ResponseEntity<?> updateIssue(@PathVariable String id, @RequestBody IssueCreateRequest req) {
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Issue not found"));

        if (req.getTitle() != null) issue.setTitle(req.getTitle().trim());
        if (req.getDescription() != null) issue.setDescription(req.getDescription());
        if (req.getType() != null) issue.setType(req.getType());
        if (req.getStatus() != null) issue.setStatus(req.getStatus());
        if (req.getPriority() != null) issue.setPriority(req.getPriority());
        if (req.getDueDate() != null) issue.setDueDate(req.getDueDate());
        if (req.getEstimatedHours() != null) issue.setEstimatedHours(req.getEstimatedHours());

        if (req.getAssigneeId() != null) {
            if (req.getAssigneeId().isBlank()) issue.setAssignee(null);
            else employeeRepository.findById(req.getAssigneeId()).ifPresent(issue::setAssignee);
        }

        if (req.getSprintId() != null) {
            if (req.getSprintId().isBlank()) issue.setSprint(null);
            else sprintRepository.findById(req.getSprintId()).ifPresent(issue::setSprint);
        }

        return ResponseEntity.ok(issueRepository.save(issue));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an issue")
    public ResponseEntity<?> deleteIssue(@PathVariable String id) {
        issueRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/{id}/comments")
    @Operation(summary = "Get comments for an issue")
    public ResponseEntity<?> getComments(@PathVariable String id) {
        return ResponseEntity.ok(projectService.getComments(id));
    }

    @PostMapping("/{id}/comments")
    @Operation(summary = "Add a comment to an issue")
    public ResponseEntity<?> addComment(
            @PathVariable String id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String content = body.get("content");
        if (content == null || content.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Comment content is required"));
        }
        IssueComment comment = projectService.addComment(id, userDetails.getId(), content);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }
}
