package com.hrms.repository;

import com.hrms.entity.Meeting;
import com.hrms.entity.enums.MeetingStatus;
import com.hrms.entity.enums.MeetingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, String> {

    @Query("SELECT DISTINCT m FROM Meeting m LEFT JOIN m.attendees a WHERE " +
           "(:isManager = true OR m.organizer.id = :userId OR a.employee.id = :userId) AND " +
           "(:status IS NULL OR m.status = :status) AND " +
           "(:type IS NULL OR m.type = :type) AND " +
           "(:afterTime IS NULL OR m.startTime >= :afterTime) AND " +
           "(:beforeTime IS NULL OR m.endTime < :beforeTime) " +
           "ORDER BY m.startTime ASC")
    List<Meeting> findMeetings(
            @Param("userId") String userId,
            @Param("isManager") boolean isManager,
            @Param("status") MeetingStatus status,
            @Param("type") MeetingType type,
            @Param("afterTime") LocalDateTime afterTime,
            @Param("beforeTime") LocalDateTime beforeTime);

    @Query("SELECT DISTINCT m FROM Meeting m LEFT JOIN m.attendees a WHERE " +
           "(:isManager = true OR m.organizer.id = :userId OR a.employee.id = :userId) AND " +
           "(m.status IN ('COMPLETED', 'CANCELLED') OR (m.status = 'SCHEDULED' AND m.endTime < :now)) " +
           "ORDER BY m.startTime DESC")
    List<Meeting> findPastMeetings(
            @Param("userId") String userId,
            @Param("isManager") boolean isManager,
            @Param("now") LocalDateTime now);

    List<Meeting> findTop5ByStartTimeAfterOrderByStartTimeAsc(LocalDateTime now);

    @Query("SELECT DISTINCT m FROM Meeting m LEFT JOIN m.attendees a WHERE (m.organizer.id = :empId OR a.employee.id = :empId) AND m.startTime >= :now ORDER BY m.startTime ASC")
    List<Meeting> findUpcomingForEmployee(@Param("empId") String empId, @Param("now") LocalDateTime now);
}
