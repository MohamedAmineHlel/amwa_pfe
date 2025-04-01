package com.phegondev.usersmanagementsystem.controller;

import com.phegondev.usersmanagementsystem.dto.ReqRes;
import com.phegondev.usersmanagementsystem.entity.Leave;
import com.phegondev.usersmanagementsystem.entity.LeaveStatus;
import com.phegondev.usersmanagementsystem.entity.OurUsers;
import com.phegondev.usersmanagementsystem.repository.UsersRepo;
import com.phegondev.usersmanagementsystem.service.LeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;
    private final UsersRepo userRepo;

    /**
     * Créer une demande de congé
     * /leaves/create
     */
    @PostMapping("/create")
    public ResponseEntity<Leave> requestLeave(@RequestBody ReqRes request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // Récupérer l'utilisateur connecté depuis l'email du token
        Optional<OurUsers> userOptional = userRepo.findByEmail(email);

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        OurUsers user = userOptional.get();

        Leave leave = leaveService.requestLeave(user, request.getStartDate(), request.getEndDate(), request.getDescription());
        return ResponseEntity.ok(leave);
    }

    /**
     * Récupérer les congés de l'utilisateur connecté
     */
    @GetMapping("/user")
    public ResponseEntity<List<Leave>> getLeavesByCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // Trouver l'utilisateur connecté
        Optional<OurUsers> userOptional = userRepo.findByEmail(email);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        OurUsers user = userOptional.get();
        List<Leave> leaves = leaveService.getLeavesByUser(user.getId());
        return ResponseEntity.ok(leaves);
    }
    /**
     * Mettre à jour les détails d'une demande de congé
     */
    @PutMapping("/update/{leaveId}")
    public ResponseEntity<Leave> updateLeave(
            @PathVariable Integer leaveId,
            @RequestBody ReqRes request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // Trouver l'utilisateur connecté
        Optional<OurUsers> userOptional = userRepo.findByEmail(email);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        OurUsers user = userOptional.get();

        // Récupérer la demande de congé existante
        Optional<Leave> leaveOptional = leaveService.getLeaveByIdAndUser(leaveId, user.getId());
        if (leaveOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        // Mise à jour des informations de la demande de congé
        System.out.println("test le donner "+request.getStartDate());
        Leave updatedLeave = leaveService.updateLeaveDetails(leaveOptional.get(), request.getStartDate(), request.getEndDate(), request.getDescription());
        return ResponseEntity.ok(updatedLeave);
    }
    /**

     * Modifier le statut d'une demande de congé
     */
    @PutMapping("/status/{leaveId}")
    public ResponseEntity<Leave> updateLeaveStatus(
            @PathVariable Integer leaveId,
            @RequestParam String status) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // Trouver l'utilisateur connecté
        Optional<OurUsers> userOptional = userRepo.findByEmail(email);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        OurUsers user = userOptional.get();
        System.out.println("done"+status);
        Leave leave = leaveService.updateLeaveStatus(leaveId, status ,user);
        return ResponseEntity.ok(leave);
    }
    /**
     * Supprimer une demande de congé
     */
    @DeleteMapping("/delete/{leaveId}")
    public ResponseEntity<Void> deleteLeave(@PathVariable Integer leaveId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // Trouver l'utilisateur connecté
        Optional<OurUsers> userOptional = userRepo.findByEmail(email);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        OurUsers user = userOptional.get();

        // Vérifier si la demande de congé appartient à l'utilisateur connecté
        Optional<Leave> leaveOptional = leaveService.getLeaveByIdAndUser(leaveId, user.getId());
        if (leaveOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Supprimer la demande de congé
        leaveService.deleteLeave(leaveId);
        return ResponseEntity.noContent().build();
    }
    /**
     * Supprimer une demande de congé
     */
    @DeleteMapping("/{leaveId}")
    public ResponseEntity<Void> deleteLeave1(@PathVariable Integer leaveId) {
        leaveService.deleteLeave(leaveId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Rechercher les congés par statut et date
     */
    @GetMapping("/search")
    public ResponseEntity<List<Leave>> findLeavesByStatusAndDate(
            @RequestParam LeaveStatus status,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        List<Leave> leaves = leaveService.findLeavesByStatusAndDate(status, startDate, endDate);
        return ResponseEntity.ok(leaves);
    }

    /**
     * Récupérer les congés en fonction du rôle de l'utilisateur.
     */
    @GetMapping("/role-based-leaves")
    public ResponseEntity<List<Leave>> getLeavesByRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // Trouver l'utilisateur connecté
        Optional<OurUsers> userOptional = userRepo.findByEmail(email);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        OurUsers user = userOptional.get();
        String role = user.getRole();

      /**  switch (role) {
            case "SUPERVISOR":
                return ResponseEntity.ok(leaveService.getLeavesByOperator());  // Supervisors see only Operator's leaves
            case "MANAGER":
                return ResponseEntity.ok(leaveService.getLeavesBySupervisor());  // Managers see Supervisor's and Operator's leaves
            case "SUPERMANAGER":
                return ResponseEntity.ok(leaveService.getLeavesByManager());  // SuperManagers see all except Admin's leaves
            case "ADMIN":
                return ResponseEntity.ok(leaveService.getLeavesBySuperManager());  // Admin sees all
            default:
                return ResponseEntity.status(403).build();  // Forbidden if role not recognized
        }**/
        switch (role) {
            case "SUPERVISOR":
                return ResponseEntity.ok(leaveService.getLeavesByOperator());
            case "MANAGER":
                return ResponseEntity.ok(leaveService.getLeavesByRoles(Arrays.asList("SUPERVISOR", "OPERATOR")));
            case "SUPERMANAGER":
                return ResponseEntity.ok(leaveService.getLeavesByRoles(Arrays.asList("MANAGER", "SUPERVISOR", "OPERATOR")));
            case "ADMIN":
                return ResponseEntity.ok(leaveService.getAllLeaves());
            default:
                return ResponseEntity.status(403).build();
        }
    }
}
