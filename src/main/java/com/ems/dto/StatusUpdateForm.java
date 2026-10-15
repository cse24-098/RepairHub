package com.ems.dto;

import com.ems.model.enums.ReportStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/** Staff form for changing a report's status. A comment is mandatory (RQ06). */
@Getter @Setter
public class StatusUpdateForm {
    @NotNull(message = "Please choose a status")
    private ReportStatus newStatus;

    @NotBlank(message = "A comment is required when changing the status")
    private String comment;
}
