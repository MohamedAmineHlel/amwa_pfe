package com.phegondev.usersmanagementsystem.service;

import com.phegondev.usersmanagementsystem.dto.ReqRes;
import com.phegondev.usersmanagementsystem.entity.Project;
import com.phegondev.usersmanagementsystem.repository.ProjectRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepo projectRepository;

    // Create a new project
    public ReqRes createProject(ReqRes request) {
        ReqRes response = new ReqRes();
        try {
            Project project = new Project();
            project.setName(request.getNameProject());
            project.setDescription(request.getDescriptionProject());
            project.setAbbr(request.getAbbrProject());
            project.setArchived(false); // Default to not archived

            Project savedProject = projectRepository.save(project);
            response.setProject(savedProject);
            response.setStatusCode(201);
            response.setMessage("Project created successfully");
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setError("Error occurred while creating project: " + e.getMessage());
        }
        return response;
    }

    // Update an existing project
    public ReqRes updateProject(UUID projectId, ReqRes request) {
        ReqRes response = new ReqRes();
        try {
            Optional<Project> projectOptional = projectRepository.findById(projectId);
            if (projectOptional.isPresent()) {
                Project project = projectOptional.get();
                project.setName(request.getNameProject() != null ? request.getNameProject() : project.getName());
                project.setDescription(request.getDescriptionProject() != null ? request.getDescriptionProject() : project.getDescription());
                project.setAbbr(request.getAbbrProject() != null ? request.getAbbrProject() : project.getAbbr());

                Project updatedProject = projectRepository.save(project);
                response.setProject(updatedProject);
                response.setStatusCode(200);
                response.setMessage("Project updated successfully");
            } else {
                response.setStatusCode(404);
                response.setError("Project not found with ID: " + projectId);
            }
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setError("Error occurred while updating project: " + e.getMessage());
        }
        return response;
    }

    // Display all projects
    public ReqRes getAllProjects() {
        ReqRes response = new ReqRes();
        try {
            List<Project> projects = projectRepository.findAll();
            response.setProjectsList(projects);
            response.setStatusCode(200);
            response.setMessage("Projects retrieved successfully");
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setError("Error occurred while retrieving projects: " + e.getMessage());
        }
        return response;
    }

    // Display a single project by ID
    public ReqRes getProjectById(UUID projectId) {
        ReqRes response = new ReqRes();
        try {
            Optional<Project> projectOptional = projectRepository.findById(projectId);
            if (projectOptional.isPresent()) {
                response.setProject(projectOptional.get());
                response.setStatusCode(200);
                response.setMessage("Project retrieved successfully");
            } else {
                response.setStatusCode(404);
                response.setError("Project not found with ID: " + projectId);
            }
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setError("Error occurred while retrieving project: " + e.getMessage());
        }
        return response;
    }

    // Delete a project
    public ReqRes deleteProject(UUID projectId) {
        ReqRes response = new ReqRes();
        try {
            Optional<Project> projectOptional = projectRepository.findById(projectId);
            if (projectOptional.isPresent()) {
                projectRepository.deleteById(projectId);
                response.setStatusCode(200);
                response.setMessage("Project deleted successfully");
            } else {
                response.setStatusCode(404);
                response.setError("Project not found with ID: " + projectId);
            }
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setError("Error occurred while deleting project: " + e.getMessage());
        }
        return response;
    }

    // Archive a project
    public ReqRes archiveProject(UUID projectId) {
        ReqRes response = new ReqRes();
        try {
            Optional<Project> projectOptional = projectRepository.findById(projectId);
            if (projectOptional.isPresent()) {
                Project project = projectOptional.get();
                project.setArchived(true);
                Project archivedProject = projectRepository.save(project);
                response.setProject(archivedProject);
                response.setStatusCode(200);
                response.setMessage("Project archived successfully");
            } else {
                response.setStatusCode(404);
                response.setError("Project not found with ID: " + projectId);
            }
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setError("Error occurred while archiving project: " + e.getMessage());
        }
        return response;
    }

    // Unarchive a project
    public ReqRes unarchiveProject(UUID projectId) {
        ReqRes response = new ReqRes();
        try {
            Optional<Project> projectOptional = projectRepository.findById(projectId);
            if (projectOptional.isPresent()) {
                Project project = projectOptional.get();
                project.setArchived(false);
                Project unarchivedProject = projectRepository.save(project);
                response.setProject(unarchivedProject);
                response.setStatusCode(200);
                response.setMessage("Project unarchived successfully");
            } else {
                response.setStatusCode(404);
                response.setError("Project not found with ID: " + projectId);
            }
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setError("Error occurred while unarchiving project: " + e.getMessage());
        }
        return response;
    }
}