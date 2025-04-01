package com.phegondev.usersmanagementsystem.controller;


import com.phegondev.usersmanagementsystem.dto.ReqRes;
import com.phegondev.usersmanagementsystem.entity.Task;
import com.phegondev.usersmanagementsystem.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;

import java.io.File;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Value("${upload.path}")
    private String uploadPath;

    @Autowired
    private TaskService taskService;

    @PostMapping
    public ReqRes createTask(@RequestBody ReqRes request) {
        return taskService.createTask(request);
    }

    @PostMapping("/{taskId}/assign/{teamId}")
    public ReqRes assignTaskToTeam(@PathVariable Integer taskId, @PathVariable Integer teamId) {
        return taskService.assignTaskToTeam(taskId, teamId);
    }

    @PostMapping("/{taskId}/unassign")
    public ReqRes unassignTaskFromTeam(@PathVariable Integer taskId) {
        return taskService.unassignTaskFromTeam(taskId);
    }

    @GetMapping
    public ReqRes getAllTasks() {
        return taskService.getAllTasks();
    }

    @GetMapping("/{taskId}")
    public ReqRes getTaskById(@PathVariable Integer taskId) {
        return taskService.getTaskById(taskId);
    }

    @DeleteMapping("/{taskId}")
    public ReqRes deleteTask(@PathVariable Integer taskId) {
        return taskService.deleteTask(taskId);
    }

    @GetMapping("/team/{teamId}")
    public ReqRes getTasksByTeamId(@PathVariable Integer teamId) {
        return taskService.getTasksByTeamId(teamId);
    }
    @PutMapping("/{taskId}/updateExpiry")
    public ReqRes updateTaskExpiry(@PathVariable Integer taskId, @RequestBody ReqRes request) {
        return taskService.updateTaskExpiry(taskId, request.getExpiryDate());
    }
    // In TaskController.java
    @PutMapping("/{taskId}")
    public ReqRes modifyTask(@PathVariable Integer taskId, @RequestBody ReqRes request) {
        return taskService.modifyTask(taskId, request);
    }

    @PostMapping("/{taskId}/attachments")
    public ReqRes uploadTaskAttachments(
            @PathVariable Integer taskId,
            @RequestParam("files") MultipartFile[] files) {
        return taskService.uploadTaskAttachments(taskId, files);
    }
    // New endpoint to download files
    // Updated endpoint to download files with taskId
    @GetMapping("/{taskId}/attachments/{filename}")
    public ResponseEntity<Resource> downloadAttachment(
            @PathVariable Integer taskId,
            @PathVariable String filename) {
        try {
            ReqRes taskResponse = taskService.getTaskById(taskId);
            if (taskResponse.getStatusCode() != 200 || taskResponse.getTask() == null) {
                return ResponseEntity.notFound().build();
            }

            Task task = taskResponse.getTask();
            String taskUniqueNumber = task.getUniqueNumber() != null ? task.getUniqueNumber() : "TASK_" + taskId;
            String filePath = uploadPath + File.separator + taskUniqueNumber + File.separator + filename;

            File file = new File(filePath);
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(file);
            String contentType = determineContentType(filename);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }}

    // Helper method to determine content type
    private String determineContentType(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        switch (extension) {
            case "pdf":
                return "application/pdf";
            case "png":
                return "image/png";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            default:
                return "application/octet-stream";
        }
    }
    // New endpoint to delete an attachment
    @DeleteMapping("/{taskId}/attachments/{filename}")
    public ReqRes deleteTaskAttachment(
            @PathVariable Integer taskId,
            @PathVariable String filename) {
        return taskService.deleteTaskAttachment(taskId, filename);
    }

}