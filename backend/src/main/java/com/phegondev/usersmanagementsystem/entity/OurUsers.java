package com.phegondev.usersmanagementsystem.entity;


import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "ourusers")
@Data
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id") // ✅ Prevent recursion

public class OurUsers implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String email;
    private String name;
    private String password;
    private String city;
    private String role;
    private String image = null;  // Explicitly setting default to null
    private String faceId = null; // Explicitly setting default to null
    @ManyToOne
    @JoinColumn(name = "team_id")
    //@JsonManagedReference  // This annotation ensures the team is serialized correctly
    //@JsonBackReference(value = "tt")  // Back reference for createdBy

    private Team team;

    @OneToMany(mappedBy = "createdBy")
    //@JsonIgnoreProperties({"createdBy", "assignedTo", "team"}) // Add these properties to prevent recursion
    //@JsonBackReference(value = "task-created-by")  // Back reference
    //@JsonManagedReference(value = "task-created-by") // Ensure proper serialization for created tasks
    @JsonBackReference(value = "task-created-by")  // Back reference for createdBy
    @JsonIgnoreProperties({"createdBy", "assignedTo", "team"}) // Avoid recursion

    private List<Task> createdTasks;

    @OneToMany(mappedBy = "assignedTo")
    //@JsonIgnoreProperties({"createdBy", "assignedTo", "team"}) // Add these properties to prevent recursion
    //@JsonBackReference(value = "task-assigned-to")  // Back reference
    //@JsonManagedReference(value = "task-assigned-to") // Ensure proper serialization for assigned tasks
    @JsonBackReference(value = "task-assigned-to")  // Back reference for assignedTo
    @JsonIgnoreProperties({"createdBy", "assignedTo", "team"}) // Avoid recursion

    private List<Task> assignedTasks;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
