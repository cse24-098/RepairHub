package com.ems.model;

import com.ems.model.enums.LocationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "locations")
@Getter @Setter @NoArgsConstructor
public class Location {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "location_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id")
    private School school;

    @Column(nullable = false)
    private String building;

    @Column(nullable = false)
    private String room;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LocationType locationType = LocationType.CLASSROOM;

    /** Text shown in dropdowns, e.g. "Science Block - Lab 2". */
    public String getDisplayName() {
        return building + " - " + room;
    }
}
