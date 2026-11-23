package com.hrms.repository;

import com.hrms.entity.BiometricLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BiometricLogRepository extends JpaRepository<BiometricLog, String> {

    List<BiometricLog> findTop100ByDeviceIdOrderByTimestampDesc(String deviceId);

    List<BiometricLog> findByProcessedFalse();

    @Query("SELECT l FROM BiometricLog l WHERE " +
           "(:deviceId IS NULL OR :deviceId = '' OR l.device.id = :deviceId OR l.device.deviceId = :deviceId) AND " +
           "(:employeeId IS NULL OR :employeeId = '' OR l.employee.id = :employeeId) AND " +
           "(:processed IS NULL OR l.processed = :processed)")
    Page<BiometricLog> searchLogs(
            @Param("deviceId") String deviceId,
            @Param("employeeId") String employeeId,
            @Param("processed") Boolean processed,
            Pageable pageable);

    long countByDeviceId(String deviceId);
}
