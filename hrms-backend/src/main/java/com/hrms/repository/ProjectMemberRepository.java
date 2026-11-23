package com.hrms.repository;

import com.hrms.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, String> {

    List<ProjectMember> findByProjectId(String projectId);

    Optional<ProjectMember> findByProjectIdAndEmployeeId(String projectId, String employeeId);

    void deleteByProjectIdAndEmployeeId(String projectId, String employeeId);

    long countByProjectId(String projectId);
}
