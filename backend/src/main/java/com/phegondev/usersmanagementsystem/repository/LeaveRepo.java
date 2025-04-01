package com.phegondev.usersmanagementsystem.repository;

import com.phegondev.usersmanagementsystem.entity.Leave;
import com.phegondev.usersmanagementsystem.entity.LeaveStatus;
import com.phegondev.usersmanagementsystem.entity.OurUsers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveRepo extends JpaRepository<Leave, Integer> {

    List<Leave> findByUser(OurUsers user);

    List<Leave> findByManager(OurUsers manager);

    List<Leave> findByStatus(LeaveStatus status);
    List<Leave> findByUserAndStatus(OurUsers user, LeaveStatus status);

    List<Leave> findByManagerAndStatus(OurUsers manager, LeaveStatus status);

    List<Leave> findByStartDateBetween(LocalDate startDate, LocalDate endDate);

    List<Leave> findByStatusAndStartDateBetween(LeaveStatus status, LocalDate startDate, LocalDate endDate);

    // Méthode personnalisée pour trouver une demande de congé par id et userId
    Optional<Leave> findByIdAndUserId(Integer leaveId, Integer userId);
}
