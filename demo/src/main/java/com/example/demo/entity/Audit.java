package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "audits")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Audit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Asset getAsset() {
        return asset;
    }

    public void setAsset(Asset asset) {
        this.asset = asset;
    }

    public String getAuditType() {
        return auditType;
    }

    public void setAuditType(String auditType) {
        this.auditType = auditType;
    }

    public String getAuditorName() {
        return auditorName;
    }

    public void setAuditorName(String auditorName) {
        this.auditorName = auditorName;
    }

    public String getAuditLocation() {
        return auditLocation;
    }

    public void setAuditLocation(String auditLocation) {
        this.auditLocation = auditLocation;
    }

    public Double getAuditLatitude() {
        return auditLatitude;
    }

    public void setAuditLatitude(Double auditLatitude) {
        this.auditLatitude = auditLatitude;
    }

    public Double getAuditLongitude() {
        return auditLongitude;
    }

    public void setAuditLongitude(Double auditLongitude) {
        this.auditLongitude = auditLongitude;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEvidenceImagePath() {
        return evidenceImagePath;
    }

    public void setEvidenceImagePath(String evidenceImagePath) {
        this.evidenceImagePath = evidenceImagePath;
    }

    public String getAiAnalysisResult() {
        return aiAnalysisResult;
    }

    public void setAiAnalysisResult(String aiAnalysisResult) {
        this.aiAnalysisResult = aiAnalysisResult;
    }

    public String getDeviceNumber() {
        return deviceNumber;
    }

    public void setDeviceNumber(String deviceNumber) {
        this.deviceNumber = deviceNumber;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public LocalDateTime getAuditDate() {
        return auditDate;
    }

    public void setAuditDate(LocalDateTime auditDate) {
        this.auditDate = auditDate;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Asset asset;
    
    @Column(nullable = false)
    private String auditType;
    
    @Column
    private String auditorName;
    
    @Column
    private String auditLocation;
    
    @Column
    private Double auditLatitude;
    
    @Column
    private Double auditLongitude;
    
    @Column
    private String condition;
    
//    @Column(columnDefinition = "TEXT")
    @Lob
    @JdbcTypeCode(SqlTypes.CLOB)
    @Column
    private String notes;
    
    @Column
    private String status;
    
    @Column
    private String evidenceImagePath;
    
//    @Column(columnDefinition = "TEXT")
    @Lob
    @JdbcTypeCode(SqlTypes.CLOB) // Hibernate 6
    @Column(name = "ai_analysis_result")
    private String aiAnalysisResult;
    
    @Column
    private String deviceNumber;
    
    @Column
    private String department;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime auditDate;
    
    @Column
    private LocalDateTime completedAt;
}
