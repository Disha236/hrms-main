package com.hrms.controller;

import com.hrms.entity.Document;
import com.hrms.entity.Employee;
import com.hrms.entity.enums.DocumentType;
import com.hrms.entity.enums.NotificationType;
import com.hrms.repository.DocumentRepository;
import com.hrms.repository.EmployeeRepository;
import com.hrms.security.CustomUserDetails;
import com.hrms.service.DocumentStorageService;
import com.hrms.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
@Tag(name = "Documents", description = "Upload, download, and manage employee documents (contracts, certificates, ID)")
public class DocumentController {

    private final DocumentRepository documentRepository;
    private final EmployeeRepository employeeRepository;
    private final DocumentStorageService storageService;
    private final NotificationService notificationService;

    public DocumentController(DocumentRepository documentRepository,
                              EmployeeRepository employeeRepository,
                              DocumentStorageService storageService,
                              NotificationService notificationService) {
        this.documentRepository = documentRepository;
        this.employeeRepository = employeeRepository;
        this.storageService = storageService;
        this.notificationService = notificationService;
    }

    @GetMapping
    @Operation(summary = "List documents for an employee or all (for managers)")
    public ResponseEntity<?> getDocuments(
            @RequestParam(required = false) String employeeId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String empId = employeeId;
        if (empId == null && "EMPLOYEE".equals(userDetails.getRole())) {
            empId = userDetails.getId();
        }

        List<Document> docs;
        if (empId != null && !empId.isBlank()) {
            docs = documentRepository.findByEmployeeIdOrderByUploadedAtDesc(empId);
        } else {
            docs = documentRepository.findTop100ByOrderByUploadedAtDesc();
        }

        Map<String, Object> res = new HashMap<>();
        res.put("documents", docs);
        return ResponseEntity.ok(res);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    @Operation(summary = "Upload a document for an employee")
    public ResponseEntity<?> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "type", defaultValue = "OTHER") String type,
            @RequestParam(value = "employeeId", required = false) String employeeId,
            @RequestParam(value = "expiryDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiryDate,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String targetEmpId = (employeeId != null && !employeeId.isBlank()) ? employeeId : userDetails.getId();

        Employee employee = employeeRepository.findById(targetEmpId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        String storedFileName = storageService.storeFile(file);

        Document doc = new Document();
        doc.setEmployee(employee);
        doc.setTitle(title.trim());
        try { doc.setType(DocumentType.valueOf(type.toUpperCase())); } catch (Exception e) { doc.setType(DocumentType.OTHER); }
        doc.setFileUrl(storedFileName);
        doc.setExpiryDate(expiryDate);
        doc.setStatus("ACTIVE");

        Document saved = documentRepository.save(doc);

        if (!targetEmpId.equals(userDetails.getId())) {
            notificationService.createNotification(employee, "New Document",
                    "A new document \"" + title + "\" has been shared with you.",
                    NotificationType.DOCUMENT_EXPIRY, "/documents");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get document metadata by ID")
    public ResponseEntity<?> getDocumentById(@PathVariable String id) {
        return documentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/{id}/file")
    @Operation(summary = "Download or stream document file")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int download) {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));

        Resource resource = storageService.loadFileAsResource(doc.getFileUrl());
        String mimeType = storageService.getMimeType(doc.getFileUrl());

        String disposition = (download == 1)
                ? "attachment; filename=\"" + doc.getTitle() + "\""
                : "inline; filename=\"" + doc.getTitle() + "\"";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mimeType))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                .body(resource);
    }
}
