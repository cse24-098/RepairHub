package com.ems.model;

import java.time.LocalDateTime;

/** One line of a report's audit trail: who changed the status, when, and why. */
public class StatusHistory {
    private String oldStatus;     // null for the first entry
    private String newStatus;
    private String comment;
    private String changedBy;
    private LocalDateTime changedAt;

    public String getOldStatus() { return oldStatus; }
    public void setOldStatus(String oldStatus) { this.oldStatus = oldStatus; }
    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public String getChangedBy() { return changedBy; }
    public void setChangedBy(String changedBy) { this.changedBy = changedBy; }
    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }
}
