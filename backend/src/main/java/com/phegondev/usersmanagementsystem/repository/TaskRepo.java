package com.phegondev.usersmanagementsystem.repository;

import com.phegondev.usersmanagementsystem.entity.Task;
import com.phegondev.usersmanagementsystem.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TaskRepo extends JpaRepository<Task, Integer> {
    List<Task> findByTeam(Team team);
    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.createdBy LEFT JOIN FETCH t.assignedTo LEFT JOIN FETCH t.team")
    List<Task> findAllTasksWithUsers();

    int countByTeam(Team team); // Add this method
    // Find the maximum sequence number for a specific team
    @Query("SELECT MAX(CAST(SUBSTRING(t.uniqueNumber, LENGTH(t.uniqueNumber) - 1, 2) AS INTEGER)) " +
            "FROM Task t WHERE t.team = :team AND t.uniqueNumber LIKE :prefixPattern")
    Integer findMaxSequenceNumberByTeam(Team team, String prefixPattern);

    // Find the maximum sequence number for unassigned tasks
    @Query("SELECT MAX(CAST(SUBSTRING(t.uniqueNumber, LENGTH(t.uniqueNumber) - 1, 2) AS INTEGER)) " +
            "FROM Task t WHERE t.team IS NULL AND t.uniqueNumber LIKE 'NO_PROJECT-NO_AIRLINE-%'")
    Integer findMaxSequenceNumberForUnassigned();


}
