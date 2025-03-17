package com.phegondev.usersmanagementsystem.service;

import com.phegondev.usersmanagementsystem.dto.ReqRes;
import com.phegondev.usersmanagementsystem.entity.Team;
import com.phegondev.usersmanagementsystem.entity.OurUsers;
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

    public ReqRes createTeam(ReqRes request) {
        ReqRes response = new ReqRes();
        try {
            Team team = new Team();
            team.setTeamName(request.getTeamName());
            Team savedTeam = teamRepo.save(team);

            response.setTeam(savedTeam);
            response.setStatusCode(200);
            response.setMessage("Team created successfully");
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
