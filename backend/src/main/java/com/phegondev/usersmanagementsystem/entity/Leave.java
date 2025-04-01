package com.phegondev.usersmanagementsystem.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "leaves")
@Data
public class Leave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private LocalDate startDate;
    private LocalDate endDate;
    private String description;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private OurUsers user;

    @ManyToOne
    @JoinColumn(name = "manager_id", nullable = true)
    private OurUsers manager;

    @Enumerated(EnumType.STRING)
    private LeaveStatus status;
}


