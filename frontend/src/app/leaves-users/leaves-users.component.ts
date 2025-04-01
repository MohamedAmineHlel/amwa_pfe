import { Component, OnInit } from '@angular/core';
import { Leave } from '../models/leave.model';
import { LeaveService } from '../service/leave.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgxPaginationModule } from 'ngx-pagination';

@Component({
  selector: 'app-leaves-users',
  templateUrl: './leaves-users.component.html',
  styleUrls: ['./leaves-users.component.css'],
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    NgxPaginationModule
  ],
})
export class LeavesUsersComponent implements OnInit {

  leaves: Leave[] = [];
  filteredLeaves: Leave[] = [];
  errorMessage: string = '';
  searchTerm: string = '';
  itemsPerPage: number = 5;
  currentPage: number = 1;
  showToast: boolean = false;
  showUpdateToast: boolean = false;
  newLeave: Leave = {
    id: 0,
    startDate: '',
    endDate: '',
    description: '',
    status: 'PENDING',
  };
   // Initialiser selectedLeave avec des valeurs par défaut
   selectedLeave: Leave = {
    id: 0,  // Valeur par défaut pour l'ID
    startDate: '',
    endDate: '',
    description: '',
    status: 'PENDING',
  };

  constructor(private leaveService: LeaveService) {}

  ngOnInit(): void {
    if (typeof window !== 'undefined') {
      const token = localStorage.getItem('token');
      if (token) {
        this.getLeavesByRole(token);
      } else {
        this.errorMessage = 'Token non trouvé. Veuillez vous connecter.';
      }
    } else {
      this.errorMessage = 'L\'environnement n\'est pas un navigateur.';
    }
  }

  /**
   * Récupère les congés en fonction du rôle de l'utilisateur
   */
  getLeavesByRole(token: string): void {
    this.leaveService.getRoleBasedLeaves(token).subscribe(
      (data: Leave[]) => {
        this.leaves = data;
        this.filteredLeaves = data;
      },
      error => {
        this.errorMessage = 'Erreur lors de la récupération des congés : ' + error.message;
      }
    );
  }

  /**
   * Filtrer les congés en fonction du texte saisi
   */
  filterLeaves(): void {
    this.filteredLeaves = this.leaves.filter(leave =>
      leave.description.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
      leave.status.toLowerCase().includes(this.searchTerm.toLowerCase())
    );
  }

  /**
   * Changer de page pour la pagination
   */
  changePage(event: number): void {
    this.currentPage = event;
  }

  /**
   * Ouvrir le toast pour ajouter un congé
   */
  openToast(): void {
    this.showToast = true;
  }

  /**
   * Fermer le toast de création
   */
  closeToast(): void {
    this.showToast = false;
  }

  /**
   * Ouvrir le toast de mise à jour pour un congé spécifique
   */
  openUpdateToast(leave: Leave): void {
    this.selectedLeave = { ...leave, id: leave.id ?? 0 };
    this.showUpdateToast = true;
  }

  /**
   * Fermer le toast de mise à jour
   */
  closeUpdateToast(): void {
    this.showUpdateToast = false;
    this.selectedLeave = {
      id: 0,  // Valeur par défaut pour l'ID
      startDate: '',
      endDate: '',
      description: '',
      status: 'PENDING',
    };
  }

  /**
   * Ajouter un congé
   */
  submitLeave(): void {
    const token = localStorage.getItem('token');
    if (token) {
      this.leaveService.createLeave(this.newLeave, token).subscribe(
        (leave: Leave) => {
          this.leaves.push(leave);
          this.filteredLeaves = this.leaves;
          this.closeToast();
        },
        error => {
          this.errorMessage = 'Erreur lors de la création du congé : ' + error.message;
        }
      );
    }
  }

  /**
   * Mettre à jour un congé
   */
  updateLeave(): void {
    if (this.selectedLeave && this.selectedLeave.id !== undefined) {  // Vérification stricte
      const token = localStorage.getItem('token');
      if (token) {
        this.leaveService.updateLeave(this.selectedLeave.id, this.selectedLeave, token).subscribe(
          () => {
            const index = this.leaves.findIndex(l => l.id === this.selectedLeave?.id);
            if (index !== -1) {
              this.leaves[index] = { ...this.selectedLeave };
            }
            this.filteredLeaves = [...this.leaves]; // Assurez-vous que la liste se met bien à jour
            this.closeUpdateToast();
          },
          error => {
            this.errorMessage = 'Erreur lors de la mise à jour du congé : ' + error.message;
          }
        );
      }
    }
  }
  
  changeLeaveStatus(leave: any, event: Event): void {
    const token = localStorage.getItem('token');
    const newValue = (event.target as HTMLSelectElement).value;
    leave.status = newValue;
    console.log("Statut modifié:", newValue);
    if (token) {
      this.leaveService.updateLeaveStatus(leave.id, leave.status, token).subscribe(
        (updatedLeave: Leave) => {
          const index = this.leaves.findIndex(l => l.id === updatedLeave.id);
          if (index !== -1) {
            this.leaves[index].status = updatedLeave.status;
          }
          this.filteredLeaves = [...this.leaves];
        },
        error => {
          this.errorMessage = 'Erreur lors de la mise à jour du statut : ' + error.message;
        }
      );
    }
  }
  

  /**
   * Supprimer un congé
   */
  deleteLeave(leaveId: number): void {
    const token = localStorage.getItem('token');
    if (token) {
      this.leaveService.deleteLeave(leaveId, token).subscribe(
        () => {
          this.leaves = this.leaves.filter(leave => leave.id !== leaveId);
          this.filteredLeaves = this.leaves;
        },
        error => {
          this.errorMessage = 'Erreur lors de la suppression du congé : ' + error.message;
        }
      );
    }
  }
}
