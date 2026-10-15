package com.ems.model;

import com.ems.model.enums.ConditionState;
import com.ems.model.enums.EquipmentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "equipment")
@Getter @Setter @NoArgsConstructor
public class Equipment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "equipment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id")
    private School school;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @Column(nullable = false, unique = true)
    private String assetTag;

    @Column(nullable = false)
    private String name;

    private String serialNumber;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDate dateAcquired;
    private BigDecimal cost;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConditionState conditionState = ConditionState.GOOD;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EquipmentStatus status = EquipmentStatus.AVAILABLE;

    /** The text encoded inside the QR code printed on the equipment. */
    @Column(nullable = false, unique = true)
    private String qrCodeValue;
}
