package com.phegondev.usersmanagementsystem.controller;

import com.phegondev.usersmanagementsystem.dto.ReqRes;
import com.phegondev.usersmanagementsystem.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/teams")
public class TeamController {

    @Autowired
    private TeamService teamService;

    @PostMapping("/create")
    public ReqRes createTeam(@RequestBody ReqRes request) {
        return teamService.createTeam(request);
    }

    @PostMapping("/assign")
    public ReqRes assignUserToTeam(@RequestParam Integer userId, @RequestParam Integer teamId) {
        return teamService.assignUserToTeam(userId, teamId);
    }
    @PostMapping("/unassign")
    public ReqRes unassignUserFromTeam(@RequestParam Integer userId) {
        return teamService.unassignUserFromTeam(userId);
    }

    @GetMapping("/all")
    public ReqRes getAllTeams() {
        return teamService.getAllTeams();
    }
    @GetMapping("/name")
    public ReqRes getTeamByName(@RequestParam String teamName) {
        return teamService.getTeamByName(teamName);
    }

    @DeleteMapping("/delete")
    public ReqRes deleteTeam(@RequestParam Integer teamId) {
        return teamService.deleteTeam(teamId);
    }
    @GetMapping("/{teamId}/users")
    public ResponseEntity<ReqRes> getUsersByTeam(@PathVariable Integer teamId) {
        return ResponseEntity.ok(teamService.getUsersByTeamId(teamId));
    }

}
