package com.phegondev.usersmanagementsystem.service;

import com.phegondev.usersmanagementsystem.dto.ReqRes;
import com.phegondev.usersmanagementsystem.entity.OurUsers;
import com.phegondev.usersmanagementsystem.entity.Task;
import com.phegondev.usersmanagementsystem.entity.Team;
import com.phegondev.usersmanagementsystem.repository.TaskRepo;
import com.phegondev.usersmanagementsystem.repository.TeamRepo;
import com.phegondev.usersmanagementsystem.repository.UsersRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TaskService {
    @Autowired
    private TaskRepo taskRepo;

    @Autowired
    private TeamRepo teamRepo;

    @Autowired
    private UsersRepo userRepo; // Add this repository to fetch users


    @Value("${upload.path}")
    private String uploadPath;

    public ReqRes uploadTaskAttachments(Integer taskId, MultipartFile[] files) {
        ReqRes response = new ReqRes();
        try {
            Optional<Task> taskOpt = taskRepo.findById(taskId);
            if (taskOpt.isEmpty()) {
                response.setStatusCode(404);
                response.setMessage("Task not found");
                return response;
            }

            Task task = taskOpt.get();
            List<String> attachmentPaths = task.getAttachments() != null ? task.getAttachments() : new ArrayList<>();

            // Create base uploads directory if it doesn't exist
            File baseUploadDir = new File(uploadPath);
            if (!baseUploadDir.exists()) {
                baseUploadDir.mkdirs();
            }

            // Create task-specific subfolder using the task's unique number
            String taskUniqueNumber = task.getUniqueNumber();
            if (taskUniqueNumber == null || taskUniqueNumber.isEmpty()) {
                taskUniqueNumber = "TASK_" + taskId; // Fallback if uniqueNumber is not set
            }
            String taskUploadPath = uploadPath + File.separator + taskUniqueNumber;
            File taskUploadDir = new File(taskUploadPath);
            if (!taskUploadDir.exists()) {
                taskUploadDir.mkdirs();
            }

            // Process each file
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    // Use original filename
                    String originalFilename = file.getOriginalFilename();
                    if (originalFilename == null) {
                        throw new IOException("File has no name");
                    }

                    // Check for duplicates and append a counter if necessary
                    String filePath = taskUploadPath + File.separator + originalFilename;
                    File destinationFile = new File(filePath);
                    String baseName = originalFilename.substring(0, originalFilename.lastIndexOf("."));
                    String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                    int counter = 1;

                    // Handle filename conflicts
                    while (destinationFile.exists()) {
                        String newFilename = baseName + "_" + counter + extension;
                        filePath = taskUploadPath + File.separator + newFilename;
                        destinationFile = new File(filePath);
                        counter++;
                    }

                    // Save file to task-specific folder
                    Path path = Paths.get(filePath);
                    Files.write(path, file.getBytes());

                    // Add file path to attachments list
                    attachmentPaths.add(filePath);
                }
            }

            // Update task with new attachments
            task.setAttachments(attachmentPaths);
            Task updatedTask = taskRepo.save(task);

            response.setTask(updatedTask);
            response.setStatusCode(200);
            response.setMessage("Attachments uploaded successfully");

        } catch (IOException e) {
            response.setStatusCode(500);
            response.setMessage("Error uploading files: " + e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error: " + e.getMessage());
        }
        return response;
    }
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
            task.setExpiryDate(request.getExpiryDate());
            task.setCreatedBy(userOpt.get());

            if (request.getAssignedTo() != null) {
                Optional<OurUsers> assignedUserOpt = userRepo.findById(request.getAssignedTo());
                assignedUserOpt.ifPresent(task::setAssignedTo);
            }

            if (request.getTeamId() != null) {
                Optional<Team> teamOpt = teamRepo.findById(request.getTeamId());
                if (teamOpt.isPresent()) {
                    Team team = teamOpt.get();
                    task.setTeam(team);

                    // Generate unique number based on max sequence number
                    String projectName = team.getProject() != null ? team.getProject().getAbbr().toUpperCase() : "NO_PROJECT";
                    Integer maxSequence = taskRepo.findMaxSequenceNumberByTeam(team, projectName);
                    int nextSequence = (maxSequence != null ? maxSequence : 0) + 1;
                    String uniqueNumber = String.format("%s-%s-%02d", projectName, nextSequence);
                    task.setUniqueNumber(uniqueNumber);
                } else {
                    response.setStatusCode(404);
                    response.setMessage("Team not found");
                    return response;
                }
            } else {
                // Generate unique number for unassigned tasks
                Integer maxSequence = taskRepo.findMaxSequenceNumberForUnassigned();
                int nextSequence = (maxSequence != null ? maxSequence : 0) + 1;
                String uniqueNumber = String.format("NO_PROJECT-NO_AIRLINE-%02d", nextSequence);
                task.setUniqueNumber(uniqueNumber);
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
                Task task = taskOpt.get();
                String taskUniqueNumber = task.getUniqueNumber() != null ? task.getUniqueNumber() : "TASK_" + taskId;
                String taskFolderPath = uploadPath + File.separator + taskUniqueNumber;

                // Delete the task's attachment folder and its contents
                File taskFolder = new File(taskFolderPath);
                if (taskFolder.exists() && taskFolder.isDirectory()) {
                    deleteDirectory(taskFolder);
                }

                // Delete the task from the database
                taskRepo.delete(task);

                response.setStatusCode(200);
                response.setMessage("Task and its attachments deleted successfully");
            } else {
                response.setStatusCode(404);
                response.setMessage("Task not found");
            }
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error deleting task: " + e.getMessage());
        }
        return response;
    }

    // Helper method to recursively delete a directory and its contents
    private void deleteDirectory(File directory) throws IOException {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file); // Recursively delete subdirectories
                    } else {
                        if (!file.delete()) {
                            throw new IOException("Failed to delete file: " + file.getAbsolutePath());
                        }
                    }
                }
            }
        }
        if (!directory.delete()) {
            throw new IOException("Failed to delete directory: " + directory.getAbsolutePath());
        }
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


    public ReqRes modifyTask(Integer taskId, ReqRes request) {
        ReqRes response = new ReqRes();
        try {
            Optional<Task> taskOpt = taskRepo.findById(taskId);
            if (taskOpt.isEmpty()) {
                response.setStatusCode(404);
                response.setMessage("Task not found");
                return response;
            }

            Task task = taskOpt.get();

            // Update fields if provided
            if (request.getTaskName() != null) {
                task.setTaskName(request.getTaskName());
            }
            if (request.getDescription() != null) {
                task.setDescription(request.getDescription());
            }
            if (request.getPriority() != null) {
                task.setPriority(request.getPriority());
            }
            if (request.getStatus() != null) {
                task.setStatus(request.getStatus());
            }
            if (request.getExpiryDate() != null) {
                task.setExpiryDate(request.getExpiryDate());
            }

            // Update assignedTo if provided
            if (request.getAssignedTo() != null) {
                Optional<OurUsers> assignedUserOpt = userRepo.findById(request.getAssignedTo());
                if (assignedUserOpt.isPresent()) {
                    task.setAssignedTo(assignedUserOpt.get());
                } else {
                    response.setStatusCode(404);
                    response.setMessage("Assigned user not found");
                    return response;
                }
            }

            // Update team if provided and regenerate uniqueNumber only if team changes
            if (request.getTeamId() != null) {
                Optional<Team> teamOpt = teamRepo.findById(request.getTeamId());
                if (teamOpt.isPresent()) {
                    Team newTeam = teamOpt.get();
                    Team currentTeam = task.getTeam();

                    // Check if the team is actually changing
                    if (currentTeam == null || !currentTeam.getId().equals(newTeam.getId())) {
                        task.setTeam(newTeam);

                        // Generate new unique number based on max sequence number
                        String projectName = newTeam.getProject() != null ? newTeam.getProject().getAbbr().toUpperCase() : "NO_PROJECT";


                        Integer maxSequence = taskRepo.findMaxSequenceNumberByTeam(newTeam, projectName);
                        int nextSequence = (maxSequence != null ? maxSequence : 0) + 1;
                        String uniqueNumber = String.format("%s-%s-%02d", projectName, nextSequence);
                        task.setUniqueNumber(uniqueNumber);
                    }
                    // If teamId is the same, keep the existing uniqueNumber
                } else {
                    response.setStatusCode(404);
                    response.setMessage("Team not found");
                    return response;
                }
            } else if (request.getTeamId() == null && task.getTeam() != null) {
                // If teamId is explicitly set to null and there was a team, remove it
                task.setTeam(null);
                // Generate new unique number for unassigned tasks
                Integer maxSequence = taskRepo.findMaxSequenceNumberForUnassigned();
                int nextSequence = (maxSequence != null ? maxSequence : 0) + 1;
                String uniqueNumber = String.format("NO_PROJECT-NO_AIRLINE-%02d", nextSequence);
                task.setUniqueNumber(uniqueNumber);
            }
            // If request.getTeamId() is null and task.getTeam() is already null, keep existing uniqueNumber

            Task updatedTask = taskRepo.save(task);
            response.setTask(updatedTask);
            response.setStatusCode(200);
            response.setMessage("Task modified successfully");

        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error: " + e.getMessage());
        }
        return response;
    }
    public ReqRes deleteTaskAttachment(Integer taskId, String filename) {
        ReqRes response = new ReqRes();
        try {
            Optional<Task> taskOpt = taskRepo.findById(taskId);
            if (taskOpt.isEmpty()) {
                response.setStatusCode(404);
                response.setMessage("Task not found");
                return response;
            }

            Task task = taskOpt.get();
            List<String> attachmentPaths = task.getAttachments() != null ? task.getAttachments() : new ArrayList<>();

            String taskUniqueNumber = task.getUniqueNumber() != null ? task.getUniqueNumber() : "TASK_" + taskId;
            String filePath = uploadPath + File.separator + taskUniqueNumber + File.separator + filename;

            // Check if the attachment exists in the list
            if (!attachmentPaths.contains(filePath)) {
                response.setStatusCode(404);
                response.setMessage("Attachment not found in task");
                return response;
            }

            // Delete the file from the filesystem
            File fileToDelete = new File(filePath);
            if (fileToDelete.exists()) {
                if (!fileToDelete.delete()) {
                    response.setStatusCode(500);
                    response.setMessage("Failed to delete file from filesystem");
                    return response;
                }
            } else {
                response.setStatusCode(404);
                response.setMessage("File not found on server");
                return response;
            }

            // Remove the attachment path from the list
            attachmentPaths.remove(filePath);

            // Update the task
            task.setAttachments(attachmentPaths);
            Task updatedTask = taskRepo.save(task);

            response.setTask(updatedTask);
            response.setStatusCode(200);
            response.setMessage("Attachment deleted successfully");

        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error deleting attachment: " + e.getMessage());
        }
        return response;
    }


}
