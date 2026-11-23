package com.hrms.repository;

import com.hrms.entity.Sprint;
import com.hrms.entity.enums.SprintStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SprintRepository extends JpaRepository<Sprint, String> {

    List<Sprint> findByProjectIdOrderByStartDateDesc(String projectId);

    @Modifying
    @Query("UPDATE Sprint s SET s.status = 'PLANNED' WHERE s.project.id = :projectId AND s.status = 'ACTIVE' AND s.id <> :excludeId")
    void deactivateOtherActiveSprints(@Param("projectId") String projectId, @Param("excludeId") String excludeId);
}
