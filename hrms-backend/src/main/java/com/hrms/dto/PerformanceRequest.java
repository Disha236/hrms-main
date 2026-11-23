package com.hrms.dto;

import com.hrms.entity.enums.PerformanceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public class PerformanceRequest {

    @NotBlank(message = "employeeId is required")
    private String employeeId;

    private String reviewerId;

    @NotBlank(message = "reviewPeriod is required")
    private String reviewPeriod;

    @NotNull(message = "reviewDate is required")
    private LocalDate reviewDate;

    @NotNull(message = "overallRating is required")
    private Double overallRating;

    private String goals;
    private String achievements;
    private String strengths;
    private String improvements;
    private String comments;
    private PerformanceStatus status = PerformanceStatus.DRAFT;

    @NotEmpty(message = "kpis must not be empty")
    private List<KpiItem> kpis;

    public static class KpiItem {
        private String name;
        private Double target;
        private Double actual;
        private Double weight = 1.0;
        private Double rating;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public Double getTarget() { return target; }
        public void setTarget(Double target) { this.target = target; }

        public Double getActual() { return actual; }
        public void setActual(Double actual) { this.actual = actual; }

        public Double getWeight() { return weight; }
        public void setWeight(Double weight) { this.weight = weight; }

        public Double getRating() { return rating; }
        public void setRating(Double rating) { this.rating = rating; }
    }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getReviewerId() { return reviewerId; }
    public void setReviewerId(String reviewerId) { this.reviewerId = reviewerId; }

    public String getReviewPeriod() { return reviewPeriod; }
    public void setReviewPeriod(String reviewPeriod) { this.reviewPeriod = reviewPeriod; }

    public LocalDate getReviewDate() { return reviewDate; }
    public void setReviewDate(LocalDate reviewDate) { this.reviewDate = reviewDate; }

    public Double getOverallRating() { return overallRating; }
    public void setOverallRating(Double overallRating) { this.overallRating = overallRating; }

    public String getGoals() { return goals; }
    public void setGoals(String goals) { this.goals = goals; }

    public String getAchievements() { return achievements; }
    public void setAchievements(String achievements) { this.achievements = achievements; }

    public String getStrengths() { return strengths; }
    public void setStrengths(String strengths) { this.strengths = strengths; }

    public String getImprovements() { return improvements; }
    public void setImprovements(String improvements) { this.improvements = improvements; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public PerformanceStatus getStatus() { return status; }
    public void setStatus(PerformanceStatus status) { this.status = status; }

    public List<KpiItem> getKpis() { return kpis; }
    public void setKpis(List<KpiItem> kpis) { this.kpis = kpis; }
}
