package com.phegondev.usersmanagementsystem.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.phegondev.usersmanagementsystem.entity.*;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
    private String faceId;
    private Object data; // This will store any data like the list of image paths
    private List<Task> tasks;
    private Integer teamId; // Add this field
    private Integer assignedTo;  //  Add this field
    private Integer createdBy;  //  Added this field to accept user ID
    private LocalDate expiryDate; //  Added expiryDate field
    private MultipartFile[] attachments; //  Added for file uploads (can be null or empty)



    private String nameProject;
    private String descriptionProject;
    private String abbrProject;
    private boolean archivedProject;
    private Project project;
    private List<Project> projectsList;
    private UUID projectId;  // For referencing Project by ID
    private String uniqueNumber; // Add this field

    // Comment-related fields
    private Integer commentId;
    private String commentContent;
    private LocalDateTime commentTimestamp;
    private Integer commentUserId; // ID of the user creating the comment
    private Integer commentTaskId; // ID of the task the comment belongs to
    private List<Comment> commentss;

    // Leave-related fields
    private Integer leaveId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String leaveDescription;
    private LeaveStatus leaveStatus;
    private Integer leaveUserId; // User requesting leave
    private Integer leaveManagerId; // Manager approving/rejecting leave
    private List<Leave> leavesList;

}
