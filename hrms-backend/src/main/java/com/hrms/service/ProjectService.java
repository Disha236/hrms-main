package com.hrms.service;

import com.hrms.dto.*;
import com.hrms.entity.*;
import com.hrms.entity.enums.IssueStatus;
import com.hrms.entity.enums.NotificationType;
import com.hrms.entity.enums.ProjectRole;
import com.hrms.entity.enums.SprintStatus;
import com.hrms.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository memberRepository;
    private final SprintRepository sprintRepository;
    private final IssueRepository issueRepository;
    private final IssueCommentRepository commentRepository;
    private final EmployeeRepository employeeRepository;
    private final NotificationService notificationService;

    public ProjectService(ProjectRepository projectRepository,
                          ProjectMemberRepository memberRepository,
                          SprintRepository sprintRepository,
                          IssueRepository issueRepository,
                          IssueCommentRepository commentRepository,
                          EmployeeRepository employeeRepository,
                          NotificationService notificationService) {
        this.projectRepository = projectRepository;
        this.memberRepository = memberRepository;
        this.sprintRepository = sprintRepository;
        this.issueRepository = issueRepository;
        this.commentRepository = commentRepository;
        this.employeeRepository = employeeRepository;
        this.notificationService = notificationService;
    }

    public List<Map<String, Object>> getAllProjects() {
        List<Project> list = projectRepository.findAllByOrderByUpdatedAtDesc();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Project p : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", p.getId());
            map.put("name", p.getName());
            map.put("key", p.getKey());
            map.put("description", p.getDescription());
            map.put("status", p.getStatus());
            map.put("lead", p.getLead());
            map.put("createdAt", p.getCreatedAt());
            map.put("updatedAt", p.getUpdatedAt());
            long issueCount = issueRepository.countByProjectId(p.getId());
            long memberCount = memberRepository.countByProjectId(p.getId());
            map.put("_count", Map.of("issues", issueCount, "members", memberCount));
            result.add(map);
        }
        return result;
    }

    public Optional<Project> getProjectById(String id) {
        return projectRepository.findById(id);
    }

    public Optional<Map<String, Object>> getProjectDetail(String id) {
        Optional<Project> pOpt = projectRepository.findById(id);
        if (pOpt.isEmpty()) return Optional.empty();
        Project p = pOpt.get();

        Map<String, Object> map = new HashMap<>();
        map.put("id", p.getId());
        map.put("name", p.getName());
        map.put("key", p.getKey());
        map.put("description", p.getDescription());
        map.put("status", p.getStatus());
        map.put("lead", p.getLead());
        map.put("createdAt", p.getCreatedAt());
        map.put("updatedAt", p.getUpdatedAt());
        map.put("members", memberRepository.findByProjectId(id));
        map.put("sprints", sprintRepository.findByProjectIdOrderByStartDateDesc(id));
        map.put("issues", issueRepository.findByProjectIdOrderByStatusAscPriorityDescIssueNumberDesc(id));

        return Optional.of(map);
    }

    @Transactional
    public Project createProject(String currentUserId, ProjectCreateRequest req) {
        String key = req.getKey().toUpperCase().replaceAll("[^A-Z0-9]", "");
        if (key.length() < 2) {
            throw new IllegalArgumentException("Project key must be at least 2 characters");
        }
        if (projectRepository.existsByKey(key)) {
            throw new IllegalArgumentException("Project key already exists");
        }

        String leadId = req.getLeadId() != null && !req.getLeadId().isBlank() ? req.getLeadId() : currentUserId;
        Employee lead = employeeRepository.findById(leadId)
                .orElseThrow(() -> new IllegalArgumentException("Project lead not found"));

        Project project = new Project();
        project.setKey(key);
        project.setName(req.getName().trim());
        project.setDescription(req.getDescription());
        project.setLead(lead);
        project.setStatus(req.getStatus() != null ? req.getStatus() : "ACTIVE");

        Project saved = projectRepository.save(project);

        memberRepository.save(new ProjectMember(saved, lead, ProjectRole.LEAD));

        if (req.getMemberIds() != null) {
            for (String memId : req.getMemberIds()) {
                if (!memId.equals(lead.getId())) {
                    employeeRepository.findById(memId).ifPresent(e ->
                            memberRepository.save(new ProjectMember(saved, e, ProjectRole.MEMBER)));
                }
            }
        }

        return saved;
    }

    @Transactional
    public Project updateProject(String id, ProjectCreateRequest req) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        if (req.getName() != null) project.setName(req.getName().trim());
        if (req.getDescription() != null) project.setDescription(req.getDescription());
        if (req.getStatus() != null) project.setStatus(req.getStatus());
        if (req.getLeadId() != null) {
            employeeRepository.findById(req.getLeadId()).ifPresent(project::setLead);
        }

        return projectRepository.save(project);
    }

    @Transactional
    public void deleteProject(String id) {
        projectRepository.deleteById(id);
    }

    public List<ProjectMember> getProjectMembers(String projectId) {
        return memberRepository.findByProjectId(projectId);
    }

    @Transactional
    public ProjectMember addMember(String projectId, String employeeId, ProjectRole role) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        ProjectMember member = memberRepository.findByProjectIdAndEmployeeId(projectId, employeeId)
                .orElseGet(() -> new ProjectMember(project, employee, role));
        member.setRole(role != null ? role : ProjectRole.MEMBER);
        return memberRepository.save(member);
    }

    @Transactional
    public void removeMember(String projectId, String employeeId) {
        memberRepository.deleteByProjectIdAndEmployeeId(projectId, employeeId);
    }

    public List<Sprint> getProjectSprints(String projectId) {
        return sprintRepository.findByProjectIdOrderByStartDateDesc(projectId);
    }

    @Transactional
    public Sprint createSprint(String projectId, SprintCreateRequest req) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        Sprint sprint = new Sprint();
        sprint.setProject(project);
        sprint.setName(req.getName().trim());
        sprint.setGoal(req.getGoal());
        sprint.setStartDate(req.getStartDate());
        sprint.setEndDate(req.getEndDate());
        sprint.setStatus(req.getStatus() != null ? req.getStatus() : SprintStatus.PLANNED);

        if (sprint.getStatus() == SprintStatus.ACTIVE) {
            sprintRepository.deactivateOtherActiveSprints(projectId, "dummy-id");
        }

        return sprintRepository.save(sprint);
    }

    public List<Issue> getIssues(String projectId, String assigneeId, String status) {
        IssueStatus issueStatus = null;
        if (status != null && !status.isBlank()) {
            try { issueStatus = IssueStatus.valueOf(status.toUpperCase().trim()); } catch (Exception ignored) {}
        }
        return issueRepository.searchIssues(projectId, assigneeId, issueStatus);
    }

    public Optional<Issue> getIssueById(String id) {
        return issueRepository.findById(id);
    }

    @Transactional
    public Issue createIssue(String reporterId, IssueCreateRequest req) {
        Project project = projectRepository.findById(req.getProjectId())
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        Employee reporter = employeeRepository.findById(reporterId)
                .orElseThrow(() -> new IllegalArgumentException("Reporter not found"));

        int nextNum = issueRepository.findTopByProjectIdOrderByIssueNumberDesc(project.getId())
                .map(i -> i.getIssueNumber() + 1).orElse(1);

        Issue issue = new Issue();
        issue.setProject(project);
        issue.setIssueNumber(nextNum);
        issue.setTitle(req.getTitle().trim());
        issue.setDescription(req.getDescription());
        issue.setType(req.getType());
        issue.setStatus(req.getStatus());
        issue.setPriority(req.getPriority());
        issue.setReporter(reporter);
        issue.setDueDate(req.getDueDate());
        issue.setEstimatedHours(req.getEstimatedHours());

        if (req.getAssigneeId() != null && !req.getAssigneeId().isBlank()) {
            employeeRepository.findById(req.getAssigneeId()).ifPresent(issue::setAssignee);
        }

        if (req.getSprintId() != null && !req.getSprintId().isBlank()) {
            sprintRepository.findById(req.getSprintId()).ifPresent(issue::setSprint);
        }

        Issue saved = issueRepository.save(issue);

        if (saved.getAssignee() != null && !saved.getAssignee().getId().equals(reporter.getId())) {
            notificationService.createNotification(saved.getAssignee(), "New Issue Assigned",
                    reporter.getFullName() + " assigned you: " + project.getKey() + "-" + nextNum + " " + issue.getTitle(),
                    NotificationType.INFO, "/projects/" + project.getId() + "/issues/" + saved.getId());
        }

        return saved;
    }

    @Transactional
    public IssueComment addComment(String issueId, String authorId, String content) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new IllegalArgumentException("Issue not found"));
        Employee author = employeeRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("Author not found"));

        IssueComment comment = new IssueComment(issue, author, content.trim());
        return commentRepository.save(comment);
    }

    public List<IssueComment> getComments(String issueId) {
        return commentRepository.findByIssueIdOrderByCreatedAtAsc(issueId);
    }
}
