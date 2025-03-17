package com.phegondev.usersmanagementsystem.entity;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id") // ✅ Prevent recursion

public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String teamName;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonBackReference  // Unique value for this reference
    private List<OurUsers> users;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    //@JsonIgnoreProperties("team") // Allows full serialization while avoiding recursion
    //@JsonBackReference(value = "team-tasks")  // Unique value for this reference
    //@JsonBackReference(value = "task-team")  // Back reference for team-related tasks
    @JsonBackReference(value = "task-team")  // Manage task-team serialization

    private List<Task> tasks;
}
