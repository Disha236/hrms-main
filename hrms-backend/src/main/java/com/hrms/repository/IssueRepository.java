package com.hrms.repository;

import com.hrms.entity.Issue;
import com.hrms.entity.enums.IssueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IssueRepository extends JpaRepository<Issue, String> {

    Optional<Issue> findTopByProjectIdOrderByIssueNumberDesc(String projectId);

    List<Issue> findByProjectIdOrderByStatusAscPriorityDescIssueNumberDesc(String projectId);

    List<Issue> findBySprintIdOrderByIssueNumberAsc(String sprintId);

    List<Issue> findByProjectIdAndSprintIdIsNullOrderByIssueNumberDesc(String projectId);

    @Query("SELECT i FROM Issue i WHERE i.project.id = :projectId AND (i.sprint IS NULL OR i.sprint.id <> :sprintId) ORDER BY i.issueNumber DESC")
    List<Issue> findBacklogIssues(@Param("projectId") String projectId, @Param("sprintId") String sprintId);

    @Query("SELECT i FROM Issue i WHERE " +
           "(:projectId IS NULL OR :projectId = '' OR i.project.id = :projectId) AND " +
           "(:assigneeId IS NULL OR :assigneeId = '' OR i.assignee.id = :assigneeId) AND " +
           "(:status IS NULL OR i.status = :status) " +
           "ORDER BY i.priority DESC, i.updatedAt DESC")
    List<Issue> searchIssues(
            @Param("projectId") String projectId,
            @Param("assigneeId") String assigneeId,
            @Param("status") IssueStatus status);

    long countByProjectId(String projectId);
    long countBySprintId(String sprintId);
}
