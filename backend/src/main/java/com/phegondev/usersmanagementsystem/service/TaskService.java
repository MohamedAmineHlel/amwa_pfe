package com.phegondev.usersmanagementsystem.service;

import com.phegondev.usersmanagementsystem.dto.ReqRes;
import com.phegondev.usersmanagementsystem.entity.OurUsers;
import com.phegondev.usersmanagementsystem.entity.Task;
import com.phegondev.usersmanagementsystem.entity.Team;
import com.phegondev.usersmanagementsystem.repository.TaskRepo;
import com.phegondev.usersmanagementsystem.repository.TeamRepo;
import com.phegondev.usersmanagementsystem.repository.UsersRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {
    @Autowired
    private TaskRepo taskRepo;

    @Autowired
    private TeamRepo teamRepo;

    @Autowired
    private UsersRepo userRepo; // Add this repository to fetch users

    public ReqRes createTask(ReqRes request) {
        ReqRes response = new ReqRes();
        try {
            Optional<OurUsers> userOpt = userRepo.findById(request.getCreatedBy());
            if (userOpt.isEmpty()) {
                response.setStatusCode(404);
                response.setMessage("User not found");
                return response;
            }

            Task task = new Task();
            task.setTaskName(request.getTaskName());
            task.setDescription(request.getDescription());
            task.setPriority(request.getPriority());
            task.setStatus(request.getStatus());
            task.setExpiryDate(request.getExpiryDate()); // ✅ Set expiry date

            task.setCreatedBy(userOpt.get());

            // ✅ Assign assignedTo user if provided
            if (request.getAssignedTo() != null) {
                Optional<OurUsers> assignedUserOpt = userRepo.findById(request.getAssignedTo());
                assignedUserOpt.ifPresent(task::setAssignedTo);
            }

            // ✅ Assign team if provided (use teamId in the request)
            if (request.getTeamId() != null) {
                Optional<Team> teamOpt = teamRepo.findById(request.getTeamId());
                if (teamOpt.isPresent()) {
                    task.setTeam(teamOpt.get());
                } else {
                    response.setStatusCode(404);
                    response.setMessage("Team not found");
                    return response;
                }
            }

            Task savedTask = taskRepo.save(task);
            response.setTask(savedTask);
            response.setStatusCode(200);
            response.setMessage("Task created successfully");

        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error: " + e.getMessage());
        }
        return response;
    }

    public ReqRes assignTaskToTeam(Integer taskId, Integer teamId) {
        ReqRes response = new ReqRes();
        try {
            Optional<Task> taskOpt = taskRepo.findById(taskId);
            Optional<Team> teamOpt = teamRepo.findById(teamId);

            if (taskOpt.isPresent() && teamOpt.isPresent()) {
                Task task = taskOpt.get();
                task.setTeam(teamOpt.get());
                taskRepo.save(task);
                response.setStatusCode(200);
                response.setMessage("Task assigned to team successfully");
            } else {
                response.setStatusCode(404);
                response.setMessage("Task or Team not found");
            }
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error: " + e.getMessage());
        }
        return response;
    }

    public ReqRes unassignTaskFromTeam(Integer taskId) {
        ReqRes response = new ReqRes();
        try {
            Optional<Task> taskOpt = taskRepo.findById(taskId);
            if (taskOpt.isPresent()) {
                Task task = taskOpt.get();
                task.setTeam(null);
                taskRepo.save(task);
                response.setStatusCode(200);
                response.setMessage("Task unassigned from team successfully");
            } else {
                response.setStatusCode(404);
                response.setMessage("Task not found");
            }
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error: " + e.getMessage());
        }
        return response;
    }

    public ReqRes getAllTasks() {
        ReqRes response = new ReqRes();
        try {
            List<Task> tasks = taskRepo.findAllTasksWithUsers(); // Fetch tasks with full details
            response.setTasks(tasks); // Ensure full task objects are added
            response.setStatusCode(200);
            response.setMessage("Tasks retrieved successfully");
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error: " + e.getMessage());
        }
        return response;
    }

    public ReqRes getTaskById(Integer taskId) {
        ReqRes response = new ReqRes();
        try {
            Optional<Task> taskOpt = taskRepo.findById(taskId);
            if (taskOpt.isPresent()) {
                Task task = taskOpt.get();
                if (task.getTeam() == null) {
                    response.setMessage("Task found but not assigned to a team");
                }
                response.setTask(task);
                response.setStatusCode(200);
            } else {
                response.setStatusCode(404);
                response.setMessage("Task not found");
            }
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error: " + e.getMessage());
        }
        return response;
    }

    public ReqRes deleteTask(Integer taskId) {
        ReqRes response = new ReqRes();
        try {
            Optional<Task> taskOpt = taskRepo.findById(taskId);
            if (taskOpt.isPresent()) {
                taskRepo.delete(taskOpt.get());
                response.setStatusCode(200);
                response.setMessage("Task deleted successfully");
            } else {
                response.setStatusCode(404);
                response.setMessage("Task not found");
            }
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error: " + e.getMessage());
        }
        return response;
    }

    public ReqRes getTasksByTeamId(Integer teamId) {
        ReqRes response = new ReqRes();
        try {
            Optional<Team> teamOpt = teamRepo.findById(teamId);
            if (teamOpt.isPresent()) {
                List<Task> tasks = taskRepo.findByTeam(teamOpt.get());
                response.setTasks(tasks);  // ✅ Ensure full objects are returned
                response.setStatusCode(200);
                response.setMessage("Tasks retrieved successfully");
            } else {
                response.setStatusCode(404);
                response.setMessage("Team not found");
            }
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error: " + e.getMessage());
        }
        return response;
    }
    public ReqRes updateTaskExpiry(Integer taskId, LocalDate expiryDate) {
        ReqRes response = new ReqRes();
        try {
            Optional<Task> taskOpt = taskRepo.findById(taskId);
            if (taskOpt.isPresent()) {
                Task task = taskOpt.get();
                task.setExpiryDate(expiryDate); // ✅ Set the new expiry date
                taskRepo.save(task); // ✅ Save the updated task
                response.setTask(task);
                response.setStatusCode(200);
                response.setMessage("Expiry date updated successfully");
            } else {
                response.setStatusCode(404);
                response.setMessage("Task not found");
            }
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error: " + e.getMessage());
        }
        return response;
    }

}
