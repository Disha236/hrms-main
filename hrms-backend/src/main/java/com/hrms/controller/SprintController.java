package com.hrms.controller;

import com.hrms.dto.SprintCreateRequest;
import com.hrms.entity.Issue;
import com.hrms.entity.Sprint;
import com.hrms.entity.enums.SprintStatus;
import com.hrms.repository.IssueRepository;
import com.hrms.repository.SprintRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sprints")
@Tag(name = "Sprints", description = "Sprint board, backlog, and issue assignment")
public class SprintController {

    private final SprintRepository sprintRepository;
    private final IssueRepository issueRepository;

    public SprintController(SprintRepository sprintRepository, IssueRepository issueRepository) {
        this.sprintRepository = sprintRepository;
        this.issueRepository = issueRepository;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get sprint details with active issues and project backlog")
    public ResponseEntity<?> getSprintById(@PathVariable String id) {
        Sprint sprint = sprintRepository.findById(id).orElse(null);
        if (sprint == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        List<Issue> issues = issueRepository.findBySprintIdOrderByIssueNumberAsc(id);
        List<Issue> backlog = issueRepository.findBacklogIssues(sprint.getProject().getId(), id);

        Map<String, Object> res = new HashMap<>();
        res.put("sprint", sprint);
        res.put("issues", issues);
        res.put("backlog", backlog);
        return ResponseEntity.ok(res);
    }

    @PutMapping("/{id}")
    @Transactional
    @Operation(summary = "Update sprint details or status")
    public ResponseEntity<?> updateSprint(@PathVariable String id, @RequestBody SprintCreateRequest req) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sprint not found"));

        if (req.getName() != null) sprint.setName(req.getName().trim());
        if (req.getGoal() != null) sprint.setGoal(req.getGoal());
        if (req.getStartDate() != null) sprint.setStartDate(req.getStartDate());
        if (req.getEndDate() != null) sprint.setEndDate(req.getEndDate());
        if (req.getStatus() != null) {
            sprint.setStatus(req.getStatus());
            if (req.getStatus() == SprintStatus.ACTIVE) {
                sprintRepository.deactivateOtherActiveSprints(sprint.getProject().getId(), id);
            }
        }

        return ResponseEntity.ok(sprintRepository.save(sprint));
    }

    @DeleteMapping("/{id}")
    @Transactional
    @Operation(summary = "Delete sprint and unlink its issues")
    public ResponseEntity<?> deleteSprint(@PathVariable String id) {
        List<Issue> issues = issueRepository.findBySprintIdOrderByIssueNumberAsc(id);
        for (Issue issue : issues) {
            issue.setSprint(null);
            issueRepository.save(issue);
        }
        sprintRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PatchMapping("/{id}/issues")
    @Transactional
    @Operation(summary = "Add or remove an issue from a sprint")
    public ResponseEntity<?> updateSprintIssues(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        String issueId = body.get("issueId");
        String action = body.get("action");

        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sprint not found"));
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new IllegalArgumentException("Issue not found"));

        if ("add".equalsIgnoreCase(action)) {
            issue.setSprint(sprint);
        } else if ("remove".equalsIgnoreCase(action)) {
            issue.setSprint(null);
        }

        return ResponseEntity.ok(issueRepository.save(issue));
    }
}
