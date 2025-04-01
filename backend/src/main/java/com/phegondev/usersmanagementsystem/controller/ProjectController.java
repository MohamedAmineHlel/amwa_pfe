package com.phegondev.usersmanagementsystem.controller;

import com.phegondev.usersmanagementsystem.dto.ReqRes;
import com.phegondev.usersmanagementsystem.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    // Create a new project
    @PostMapping("/create")
    public ResponseEntity<ReqRes> createProject(@RequestBody ReqRes request) {
        ReqRes response = projectService.createProject(request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    // Update an existing project
    @PutMapping("/update/{projectId}")
    public ResponseEntity<ReqRes> updateProject(@PathVariable UUID projectId, @RequestBody ReqRes request) {
        ReqRes response = projectService.updateProject(projectId, request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    // Get all projects
    @GetMapping
    public ResponseEntity<ReqRes> getAllProjects() {
        ReqRes response = projectService.getAllProjects();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    // Get a single project by ID
    @GetMapping("/{projectId}")
    public ResponseEntity<ReqRes> getProjectById(@PathVariable UUID projectId) {
        ReqRes response = projectService.getProjectById(projectId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    // Delete a project
    @DeleteMapping("/delete/{projectId}")
    public ResponseEntity<ReqRes> deleteProject(@PathVariable UUID projectId) {
        ReqRes response = projectService.deleteProject(projectId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    // Archive a project
    @PutMapping("/archive/{projectId}")
    public ResponseEntity<ReqRes> archiveProject(@PathVariable UUID projectId) {
        ReqRes response = projectService.archiveProject(projectId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    // Unarchive a project
    @PutMapping("/unarchive/{projectId}")
    public ResponseEntity<ReqRes> unarchiveProject(@PathVariable UUID projectId) {
        ReqRes response = projectService.unarchiveProject(projectId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}