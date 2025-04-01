package com.phegondev.usersmanagementsystem.service;

import com.phegondev.usersmanagementsystem.dto.ReqRes;
import com.phegondev.usersmanagementsystem.entity.Project;
import com.phegondev.usersmanagementsystem.entity.Team;
import com.phegondev.usersmanagementsystem.entity.OurUsers;
import com.phegondev.usersmanagementsystem.repository.ProjectRepo;
import com.phegondev.usersmanagementsystem.repository.TeamRepo;
import com.phegondev.usersmanagementsystem.repository.UsersRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TeamService {

    @Autowired
    private TeamRepo teamRepo;

    @Autowired
    private UsersRepo usersRepo;

    @Autowired
    private ProjectRepo projectRepo;



    public ReqRes createTeam(ReqRes request) {
        ReqRes response = new ReqRes();
        try {
            // Validate required fields
            if (request.getTeamName() == null || request.getTeamName().isEmpty()) {
                response.setStatusCode(400);
                response.setMessage("Team name is required");
                return response;
            }

            Team team = new Team();
            team.setTeamName(request.getTeamName());

            // Assign Project if projectId is provided
            if (request.getProjectId() != null) {
                Optional<Project> projectOpt = projectRepo.findById(request.getProjectId());
                if (projectOpt.isPresent()) {
                    Project project = projectOpt.get();
                    // Check if the project is already assigned to another team
                    if (project.getTeam() != null) {
                        response.setStatusCode(400);
                        response.setMessage("Project is already assigned to another team");
                        return response;
                    }
                    team.setProject(project);
                } else {
                    response.setStatusCode(404);
                    response.setMessage("Project not found with ID: " + request.getProjectId());
                    return response;
                }
            }



            Team savedTeam = teamRepo.save(team);
            response.setTeam(savedTeam);
            response.setStatusCode(201); // Changed to 201 for resource creation
            response.setMessage("Team created successfully");
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error: " + e.getMessage());
        }
        return response;
    }

    public ReqRes modifyTeam(Integer teamId, ReqRes request) {
        ReqRes response = new ReqRes();
        try {
            Optional<Team> teamOpt = teamRepo.findById(teamId);

            if (!teamOpt.isPresent()) {
                response.setStatusCode(404);
                response.setMessage("Team not found with ID: " + teamId);
                return response;
            }

            Team team = teamOpt.get();

            // Update team name if provided
            if (request.getTeamName() != null && !request.getTeamName().isEmpty()) {
                team.setTeamName(request.getTeamName());
            }

            // Handle Project assignment/unassignment
            if (request.getProjectId() != null) {
                Optional<Project> projectOpt = projectRepo.findById(request.getProjectId());
                if (projectOpt.isPresent()) {
                    Project newProject = projectOpt.get();
                    // Check if the project is already assigned to another team
                    if (newProject.getTeam() != null && !newProject.getTeam().getId().equals(teamId)) {
                        response.setStatusCode(400);
                        response.setMessage("Project is already assigned to another team");
                        return response;
                    }
                    // Unassign the current project if it exists
                    if (team.getProject() != null && !team.getProject().getId().equals(request.getProjectId())) {
                        Project oldProject = team.getProject();
                        oldProject.setTeam(null);
                        projectRepo.save(oldProject);
                    }
                    team.setProject(newProject);
                    newProject.setTeam(team);
                    projectRepo.save(newProject);
                } else {
                    response.setStatusCode(404);
                    response.setMessage("Project not found with ID: " + request.getProjectId());
                    return response;
                }
            } else if (request.getProjectId() == null && team.getProject() != null) {
                // Remove project assignment if explicitly set to null
                Project oldProject = team.getProject();
                oldProject.setTeam(null);
                projectRepo.save(oldProject);
                team.setProject(null);
            }



            // Save the updated team
            Team updatedTeam = teamRepo.save(team);
            response.setTeam(updatedTeam);
            response.setStatusCode(200);
            response.setMessage("Team modified successfully");
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error: " + e.getMessage());
        }
        return response;
    }


    public ReqRes assignUserToTeam(Integer userId, Integer teamId) {
        ReqRes response = new ReqRes();
        try {
            Optional<OurUsers> userOpt = usersRepo.findById(userId);
            Optional<Team> teamOpt = teamRepo.findById(teamId);

            if (userOpt.isPresent() && teamOpt.isPresent()) {
                OurUsers user = userOpt.get();
                Team team = teamOpt.get();

                // Check if the user is already in a team
                if (user.getTeam() != null) {
                    response.setStatusCode(400);
                    response.setMessage("User is already assigned to a team");
                    return response;
                }

                // Fetch all users in the team
                List<OurUsers> teamUsers = usersRepo.findByTeam(team);

                // Check if the team already has a MANAGER or SUPERVISOR
                if (user.getRole().equals("MANAGER") || user.getRole().equals("SUPERVISOR")) {
                    for (OurUsers teamUser : teamUsers) {
                        if (teamUser.getRole().equals(user.getRole())) {
                            response.setStatusCode(400);
                            response.setMessage("Team already has a " + user.getRole());
                            return response;
                        }
                    }
                }

                // Assign the user to the team
                user.setTeam(team);
                usersRepo.save(user);

                response.setStatusCode(200);
                response.setMessage("User assigned to team successfully");
            } else {
                response.setStatusCode(404);
                response.setMessage("User or team not found");
            }
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error: " + e.getMessage());
        }
        return response;
    }





    public ReqRes unassignUserFromTeam(Integer userId) {
        ReqRes response = new ReqRes();
        try {
            Optional<OurUsers> userOpt = usersRepo.findById(userId);

            if (userOpt.isPresent()) {
                OurUsers user = userOpt.get();
                user.setTeam(null);
                usersRepo.save(user);

                response.setStatusCode(200);
                response.setMessage("User unassigned from team successfully");
            } else {
                response.setStatusCode(404);
                response.setMessage("User not found");
            }
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error: " + e.getMessage());
        }
        return response;
    }

    public ReqRes getAllTeams() {
        ReqRes response = new ReqRes();
        try {
            List<Team> teams = teamRepo.findAll();
            response.setTeamsList(teams);
            response.setStatusCode(200);
            response.setMessage("Teams retrieved successfully");
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error: " + e.getMessage());
        }
        return response;
    }
    public ReqRes getTeamByName(String teamName) {
        ReqRes response = new ReqRes();
        try {
            Optional<Team> teamOpt = teamRepo.findByTeamName(teamName);

            if (teamOpt.isPresent()) {
                response.setTeam(teamOpt.get());
                response.setStatusCode(200);
                response.setMessage("Team retrieved successfully");
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

    public ReqRes deleteTeam(Integer teamId) {
        ReqRes response = new ReqRes();
        try {
            Optional<Team> teamOpt = teamRepo.findById(teamId);

            if (teamOpt.isPresent()) {
                Team team = teamOpt.get();

                // Unassign all users from this team
                List<OurUsers> users = usersRepo.findByTeam(team);
                for (OurUsers user : users) {
                    user.setTeam(null);
                    usersRepo.save(user);
                }

                // Unset the team reference in the associated project
                if (team.getProject() != null) {
                    Project project = team.getProject();
                    project.setTeam(null);
                    projectRepo.save(project);
                }


                // Now delete the team
                teamRepo.delete(team);
                response.setStatusCode(200);
                response.setMessage("Team deleted successfully");
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
    public ReqRes getUsersByTeamId(Integer teamId) {
        ReqRes response = new ReqRes();
        try {
            Optional<Team> teamOpt = teamRepo.findById(teamId);

            if (teamOpt.isPresent()) {
                List<OurUsers> users = usersRepo.findByTeam(teamOpt.get());
                response.setOurUsersList(users);
                response.setStatusCode(200);
                response.setMessage("Users retrieved successfully");
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
}
