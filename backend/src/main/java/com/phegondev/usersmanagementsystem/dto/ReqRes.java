package com.phegondev.usersmanagementsystem.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.phegondev.usersmanagementsystem.entity.*;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReqRes {

    private int statusCode;
    private String error;
    private String message;
    private String token;
    private String refreshToken;
    private String expirationTime;
    private String name;
    private String city;
    private String role;
    private String email;
    private String password;
    private OurUsers ourUsers;
    private List<OurUsers> ourUsersList;
    private Team team;
    private List<Team> teamsList;
    private String taskName;
    private String description;
    private Priority priority;
    private Status status;
    private String teamName; // For creating teams
    private Task task;
    private String image;
    private String faceId;
    private Object data; // This will store any data like the list of image paths
    private List<Task> tasks;
    private Integer teamId; // Add this field
    private Integer assignedTo;  // 🔹 Add this field
    private Integer createdBy;  // 🔹 Added this field to accept user ID
    private LocalDate expiryDate; // ✅ Added expiryDate field



}
