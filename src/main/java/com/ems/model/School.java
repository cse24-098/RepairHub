package com.ems.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "schools")
@Getter @Setter @NoArgsConstructor
public class School {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "school_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;
    private String address;
    private String contactEmail;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}
