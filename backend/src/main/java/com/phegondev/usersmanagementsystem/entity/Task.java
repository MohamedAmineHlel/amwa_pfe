package com.phegondev.usersmanagementsystem.entity;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")

public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String taskName;
    private String description;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDate expiryDate;
    private String uniqueNumber; // New field


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = true)
    @JsonIgnoreProperties({"createdTasks", "assignedTasks", "team"})

    private OurUsers createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"createdTasks", "assignedTasks", "team"})

    @JoinColumn(name = "assigned_to_id", nullable = true)

    private OurUsers assignedTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = true)

    private Team team;

    @ElementCollection
    private List<String> attachments;

    @ElementCollection
    private List<String> comments;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "task-commentss") // Updated reference name
    private List<Comment> commentss; // Renamed from comments to commentss
}
