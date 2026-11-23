package com.hrms.repository;

import com.hrms.entity.BiometricDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BiometricDeviceRepository extends JpaRepository<BiometricDevice, String> {

    Optional<BiometricDevice> findByDeviceId(String deviceId);

    boolean existsByDeviceId(String deviceId);

    List<BiometricDevice> findByIsActiveTrue();

    List<BiometricDevice> findAllByOrderByCreatedAtDesc();
}
