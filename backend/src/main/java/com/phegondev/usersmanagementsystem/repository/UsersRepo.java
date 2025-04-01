package com.phegondev.usersmanagementsystem.repository;


import com.phegondev.usersmanagementsystem.entity.OurUsers;
import com.phegondev.usersmanagementsystem.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UsersRepo extends JpaRepository<OurUsers, Integer> {

    Optional<OurUsers> findByEmail(String email);
    List<OurUsers> findByTeam(Team team);
    @Query("SELECT u FROM OurUsers u LEFT JOIN FETCH u.team")
    List<OurUsers> findAllWithTeams();  // Custom method to fetch users with teams
    List<OurUsers> findByRole(String role);
    List<OurUsers> findByRoleIn(List<String> roles);
}
