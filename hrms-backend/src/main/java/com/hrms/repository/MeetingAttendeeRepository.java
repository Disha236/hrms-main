package com.hrms.repository;

import com.hrms.entity.MeetingAttendee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MeetingAttendeeRepository extends JpaRepository<MeetingAttendee, String> {

    List<MeetingAttendee> findByMeetingId(String meetingId);

    void deleteByMeetingId(String meetingId);
}
