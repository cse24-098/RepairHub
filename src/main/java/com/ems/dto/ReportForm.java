package com.ems.dto;

import com.ems.model.enums.IssueType;
import com.ems.model.enums.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** Data typed into the "Report an issue" form (RQ01, RQ03). */
@Getter @Setter
public class ReportForm {
    @NotNull(message = "Please choose the equipment")
    private Long equipmentId;

    @NotNull(message = "Please choose an issue type")
    private IssueType issueType;

    @NotNull(message = "Please choose a priority")
    private Priority priority = Priority.MEDIUM;

    @NotBlank(message = "Please describe the problem")
    @Size(min = 10, max = 2000, message = "Description must be 10-2000 characters")
    private String description;

    private Long locationId;
    private String locationDetails;
    private Double latitude;     // filled by JavaScript later (RQ03)
    private Double longitude;

    /** Ticked by the teacher to submit even though a similar report exists. */
    private boolean confirmDuplicate;
}
