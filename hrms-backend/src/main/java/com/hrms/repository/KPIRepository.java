package com.hrms.repository;

import com.hrms.entity.KPI;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KPIRepository extends JpaRepository<KPI, String> {

    List<KPI> findByPerformanceId(String performanceId);

    void deleteByPerformanceId(String performanceId);
}
