package com.ems.dto;

import com.ems.model.enums.ConditionState;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Data typed into the "Register equipment" form (RQ12). */
@Getter @Setter
public class EquipmentForm {
    @NotBlank(message = "Asset tag is required")
    private String assetTag;

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Choose a category")
    private Long categoryId;

    private Long locationId;
    private String serialNumber;
    private String description;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateAcquired;

    private BigDecimal cost;

    @NotNull
    private ConditionState conditionState = ConditionState.GOOD;
}
