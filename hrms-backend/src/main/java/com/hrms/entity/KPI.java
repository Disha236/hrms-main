package com.hrms.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "performance_kpis")
public class KPI {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performance_id", nullable = false)
    @JsonIgnore
    private Performance performance;

    @Column(nullable = false, length = 150)
    private String name;

    private Double target;

    private Double actual;

    private Double weight = 1.0;

    private Double rating;

    public KPI() {}

    public KPI(String name, Double target, Double actual, Double weight, Double rating) {
        this.id = java.util.UUID.randomUUID().toString();
        this.name = name;
        this.target = target;
        this.actual = actual;
        this.weight = weight != null ? weight : 1.0;
        this.rating = rating;
    }

    @PrePersist
    protected void onCreate() {
        if (id == null || id.isBlank()) {
            id = java.util.UUID.randomUUID().toString();
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Performance getPerformance() { return performance; }
    public void setPerformance(Performance performance) { this.performance = performance; }

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
