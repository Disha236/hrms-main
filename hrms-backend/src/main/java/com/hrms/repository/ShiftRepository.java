package com.hrms.repository;

import com.hrms.entity.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, String> {

    List<Shift> findAllByOrderByNameAsc();

    List<Shift> findByIsActiveTrueOrderByNameAsc();
}
