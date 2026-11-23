package com.hrms.repository;

import com.hrms.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {

    List<Notification> findByEmployeeIdOrderByCreatedAtDesc(String employeeId, Pageable pageable);

    List<Notification> findByEmployeeIdAndIsReadFalseOrderByCreatedAtDesc(String employeeId, Pageable pageable);

    long countByEmployeeIdAndIsReadFalse(String employeeId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.employee.id = :employeeId AND n.isRead = false")
    void markAllAsRead(@Param("employeeId") String employeeId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id = :id AND n.employee.id = :employeeId")
    void markAsReadById(@Param("id") String id, @Param("employeeId") String employeeId);
}
