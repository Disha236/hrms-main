package com.hrms.repository;

import com.hrms.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, String> {

    List<Document> findByEmployeeIdOrderByUploadedAtDesc(String employeeId);

    List<Document> findTop100ByOrderByUploadedAtDesc();

    List<Document> findTop20ByEmployeeIdOrderByUploadedAtDesc(String employeeId);
}
