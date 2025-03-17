package com.phegondev.usersmanagementsystem.repository;

import com.phegondev.usersmanagementsystem.entity.OurUsers;
import com.phegondev.usersmanagementsystem.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TeamRepo extends JpaRepository<Team, Integer> {
    Optional<Team> findByTeamName(String teamName);


}
