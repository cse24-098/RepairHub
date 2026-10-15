package com.ems.model;

import com.ems.model.enums.ReportStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** One row per status change: this is the audit trail (RQ04 / RQ06). */
@Entity
@Table(name = "report_status_history")
@Getter @Setter @NoArgsConstructor
public class ReportStatusHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "report_id")
    private MaintenanceReport report;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "changed_by")
    private User changedBy;

    @Enumerated(EnumType.STRING)
    private ReportStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus newStatus;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String comment;

    @Column(nullable = false)
    private LocalDateTime changedAt;
}
