package com.hrms.service;

import com.hrms.entity.Employee;
import com.hrms.entity.Notification;
import com.hrms.entity.enums.NotificationType;
import com.hrms.repository.NotificationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void createNotification(Employee employee, String title, String message, NotificationType type, String link) {
        Notification notification = new Notification(employee, title, message, type, link);
        notificationRepository.save(notification);
    }

    public Map<String, Object> getUserNotifications(String employeeId, boolean unreadOnly, int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit);
        List<Notification> notifications;
        if (unreadOnly) {
            notifications = notificationRepository.findByEmployeeIdAndIsReadFalseOrderByCreatedAtDesc(employeeId, pageRequest);
        } else {
            notifications = notificationRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId, pageRequest);
        }
        long unreadCount = notificationRepository.countByEmployeeIdAndIsReadFalse(employeeId);

        Map<String, Object> result = new HashMap<>();
        result.put("notifications", notifications);
        result.put("unreadCount", unreadCount);
        return result;
    }

    @Transactional
    public void markAllRead(String employeeId) {
        notificationRepository.markAllAsRead(employeeId);
    }

    @Transactional
    public void markRead(String notificationId, String employeeId) {
        notificationRepository.markAsReadById(notificationId, employeeId);
    }
}
