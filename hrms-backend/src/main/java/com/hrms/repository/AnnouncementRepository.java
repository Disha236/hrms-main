package com.hrms.repository;

import com.hrms.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, String> {

    @Query("SELECT a FROM Announcement a WHERE a.isActive = true AND " +
           "(a.expiresAt IS NULL OR a.expiresAt > :now) AND " +
           "(a.department IS NULL OR a.department.id = :deptId) " +
           "ORDER BY a.priority DESC, a.createdAt DESC")
    List<Announcement> findActiveAnnouncements(
            @Param("deptId") String deptId,
            @Param("now") LocalDateTime now);
}
