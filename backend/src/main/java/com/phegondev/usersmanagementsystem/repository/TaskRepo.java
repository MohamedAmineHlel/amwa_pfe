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

   // @Query("SELECT t FROM Task t LEFT JOIN FETCH t.createdBy LEFT JOIN FETCH t.assignedTo LEFT JOIN FETCH t.team")
    //List<Task> findAllTasksWithUsers();
}
