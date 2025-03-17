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
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id") // ✅ Prevent recursion

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

    private LocalDate expiryDate; // ✅ New attribute for task expiry date


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = true)
    //@JsonBackReference(value = "task-created-by")  // Back reference for createdBy
    //@JsonManagedReference(value = "task-created-by")  // Use ManagedReference here
    @JsonIgnoreProperties({"createdTasks", "assignedTasks", "team"}) // Avoid recursion

    private OurUsers createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    //@JsonBackReference(value = "task-assigned-to")  // Back reference for assignedTo
    //@JsonManagedReference(value = "task-assigned-to")  // Use ManagedReference here
    @JsonIgnoreProperties({"createdTasks", "assignedTasks", "team"}) // Avoid recursion

    @JoinColumn(name = "assigned_to_id", nullable = true)

    private OurUsers assignedTo;

    @ManyToOne(fetch = FetchType.LAZY)
    //@JsonManagedReference(value = "task-team")  // Back reference for team

    @JoinColumn(name = "team_id", nullable = true)

    private Team team;

    @ElementCollection
    private List<String> attachments; // Store attachment URLs or file paths

    @ElementCollection
    private List<String> comments; // List of comments related to the task
}
