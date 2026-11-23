package com.hrms.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class ProjectCreateRequest {

    @NotBlank(message = "Project key is required")
    private String key;

    @NotBlank(message = "Project name is required")
    private String name;

    private String description;
    private String leadId;
    private List<String> memberIds;
    private String status = "ACTIVE";

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLeadId() { return leadId; }
    public void setLeadId(String leadId) { this.leadId = leadId; }

    public List<String> getMemberIds() { return memberIds; }
    public void setMemberIds(List<String> memberIds) { this.memberIds = memberIds; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
