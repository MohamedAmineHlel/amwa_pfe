package com.phegondev.usersmanagementsystem.service;

import com.phegondev.usersmanagementsystem.entity.Leave;
import com.phegondev.usersmanagementsystem.entity.LeaveStatus;
import com.phegondev.usersmanagementsystem.entity.OurUsers;
import com.phegondev.usersmanagementsystem.repository.LeaveRepo;
import com.phegondev.usersmanagementsystem.repository.UsersRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveService {

    private final LeaveRepo leaveRepo;
    private final UsersRepo userRepo;

    /**
     * Créer une demande de congé
     */
    public Leave requestLeave1(Integer userId, Integer managerId, LocalDate startDate, LocalDate endDate, String description) {
        Optional<OurUsers> userOpt = userRepo.findById(userId);
        Optional<OurUsers> managerOpt = userRepo.findById(managerId);

        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Utilisateur ou manager non trouvé.");
        }

        Leave leave = new Leave();
        leave.setUser(userOpt.get());
        if ( !managerOpt.isEmpty()){
            leave.setManager(managerOpt.get());
        }else{
            leave.setManager(null);
        }

        leave.setStartDate(startDate);
        leave.setEndDate(endDate);
        leave.setDescription(description);
        leave.setStatus(LeaveStatus.PENDING);

        return leaveRepo.save(leave);
    }
    public Leave requestLeave(OurUsers user, LocalDate startDate, LocalDate endDate, String description) {
        Leave leave = new Leave();
        leave.setUser(user);
        leave.setManager(null); // Manager est toujours null
        leave.setStartDate(startDate);
        leave.setEndDate(endDate);
        leave.setDescription(description);
        leave.setStatus(LeaveStatus.PENDING);

        return leaveRepo.save(leave);
    }
    /**
     * Récupérer les congés d'un utilisateur
     */
    public List<Leave> getLeavesByUser(Integer userId) {
        Optional<OurUsers> user = userRepo.findById(userId);
        return user.map(leaveRepo::findByUser).orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé."));
    }

    /**
     * Récupérer les congés d'un manager
     */
    public List<Leave> getLeavesByManager(Integer managerId) {
        Optional<OurUsers> manager = userRepo.findById(managerId);
        return manager.map(leaveRepo::findByManager).orElseThrow(() -> new IllegalArgumentException("Manager non trouvé."));
    }

    /**
     * Modifier le statut d'une demande de congé
     */
    public Leave updateLeaveStatus(Integer leaveId, String status, OurUsers user) {
        Leave leave = leaveRepo.findById(leaveId)
                .orElseThrow(() -> new IllegalArgumentException("Demande de congé non trouvée."));
        if ("ACCEPT".equals(status)) {
            leave.setStatus(LeaveStatus.ACCEPT);
        } else if ("REFUSE".equals(status)) {
            leave.setStatus(LeaveStatus.REFUSE);
        } else {
            leave.setStatus(LeaveStatus.PENDING);
        }
        leave.setManager(user);
        return leaveRepo.save(leave);
    }

    /**
     * Supprimer une demande de congé
     */
    public void deleteLeave(Integer leaveId) {
        leaveRepo.deleteById(leaveId);
    }

    /**
     * Rechercher les congés par statut et dates
     */
    public List<Leave> findLeavesByStatusAndDate(LeaveStatus status, LocalDate startDate, LocalDate endDate) {
        return leaveRepo.findByStatusAndStartDateBetween(status, startDate, endDate);
    }

    /**
     * Modifier les donners  de congé
     */
    public Leave updateLeaveDetails(Leave leave, LocalDate startDate, LocalDate endDate, String description) {
        if (startDate != null) leave.setStartDate(startDate);
        if (endDate != null) leave.setEndDate(endDate);
        if (description != null) leave.setDescription(description);
        System.out.println(description);
        leave.setStatus(LeaveStatus.PENDING);
        return leaveRepo.save(leave);
    }
    public Optional<Leave> getLeaveByIdAndUser(Integer leaveId, Integer userId) {
        return leaveRepo.findByIdAndUserId(leaveId, userId);
    }

    /**
     * Récupérer les congés des opérateurs
     */
    public List<Leave> getLeavesByOperator() {
        // Trouver tous les utilisateurs ayant le rôle "OPERATOR"
        List<OurUsers> operators = userRepo.findByRole("OPERATOR");

        // Récupérer les congés de chaque opérateur
        List<Leave> leaves = operators.stream()
                .flatMap(operator -> leaveRepo.findByUser(operator).stream())
                .collect(Collectors.toList());

        return leaves;
    }

    /**
     * Récupérer les congés des superviseurs
     */
    public List<Leave> getLeavesBySupervisor() {
        // Trouver tous les utilisateurs ayant le rôle "SUPERVISOR"
        List<OurUsers> supervisors = userRepo.findByRole("SUPERVISOR");

        // Récupérer les congés de chaque superviseur
        List<Leave> leaves = supervisors.stream()
                .flatMap(supervisor -> leaveRepo.findByUser(supervisor).stream())
                .collect(Collectors.toList());

        return leaves;
    }

    /**
     * Récupérer les congés des managers
     */
    public List<Leave> getLeavesByManager() {
        // Trouver tous les utilisateurs ayant le rôle "MANAGER"
        List<OurUsers> managers = userRepo.findByRole("MANAGER");

        // Récupérer les congés de chaque manager
        List<Leave> leaves = managers.stream()
                .flatMap(manager -> leaveRepo.findByUser(manager).stream())
                .collect(Collectors.toList());

        return leaves;
    }

    /**
     * Récupérer les congés des supermanagers
     */
    public List<Leave> getLeavesBySuperManager() {
        // Trouver tous les utilisateurs ayant le rôle "SUPERMANAGER"
        List<OurUsers> superManagers = userRepo.findByRole("SUPERMANAGER");

        // Récupérer les congés de chaque supermanager
        List<Leave> leaves = superManagers.stream()
                .flatMap(superManager -> leaveRepo.findByUser(superManager).stream())
                .collect(Collectors.toList());

        return leaves;
    }

    public List<Leave> getAllLeaves() {
        return leaveRepo.findAll();
    }

    public List<Leave> getLeavesByRoles(List<String> roles) {
        List<OurUsers> users = userRepo.findByRoleIn(roles);
        return users.stream()
                .flatMap(user -> leaveRepo.findByUser(user).stream())
                .collect(Collectors.toList());
    }
}
