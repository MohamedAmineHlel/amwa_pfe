package com.phegondev.usersmanagementsystem.controller;


import com.phegondev.usersmanagementsystem.dto.ReqRes;
import com.phegondev.usersmanagementsystem.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tasks")
public class TaskController {

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
}