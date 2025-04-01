import { Component, OnInit } from '@angular/core';
import { LeaveService } from '../service/leave.service';
import { Leave } from '../models/leave.model';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgxPaginationModule } from 'ngx-pagination';

@Component({
  selector: 'app-leave-manager',
  templateUrl: './leave-manager.component.html',
  styleUrls: ['./leave-manager.component.css'],
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    NgxPaginationModule
  ],
})
export class LeaveManagerComponent implements OnInit {
  leaves: Leave[] = [];
  filteredLeaves: Leave[] = []; // Pour filtrer les résultats
  searchTerm: string = ''; // Pour la barre de recherche
  currentPage: number = 1;
  itemsPerPage: number = 5;
  errorMessage: string = '';
  showToast: boolean = false; // Contrôle de l'affichage du toast
  showUpdateToast: boolean = false; // Contrôle du toast de mise à jour
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
    this.loadLeaves();
  }

  loadLeaves(): void {
    if (typeof window !== 'undefined') {
      const token = localStorage.getItem('token');
      
      if (token) {  // Vérifie que le token n'est pas null
        this.leaveService.getUserLeaves(token).subscribe({
          next: (data) => {
            this.leaves = data;
            this.filteredLeaves = [...this.leaves];
          },
          error: () => {
            this.errorMessage = 'Erreur lors du chargement des congés';
          }
        });
      } else {
        this.errorMessage = 'Aucun token trouvé, veuillez vous connecter.';
        // Optionnel : rediriger l'utilisateur vers la page de connexion si le token est manquant
        // this.router.navigate(['/login']);
      }
    } else {
      this.errorMessage = 'L\'environnement n\'est pas un navigateur.';
    }
  }
  
  filterLeaves(): void {
    if (this.searchTerm.trim() === '') {
      this.filteredLeaves = [...this.leaves]; // Réinitialiser la liste
    } else {
      const term = this.searchTerm.toLowerCase();
      this.filteredLeaves = this.leaves.filter(leave =>
        leave.description.toLowerCase().includes(term) ||
        leave.status.toLowerCase().includes(term)
      );
    }
    this.currentPage = 1; // Revenir à la première page après filtrage
  }
  changePage(event: number): void {
    this.currentPage = event;
  }
  openToast(): void {
    this.showToast = true;
  }

  // Fermer le toast
  closeToast(): void {
    this.showToast = false;
    this.resetNewLeave();
  }

  // Soumettre une demande de congé
  submitLeave(): void {
    const token: any = localStorage.getItem('token');
    this.leaveService.createLeave(this.newLeave, token).subscribe({
      next: (leave) => {
        this.leaves.push(leave);
        this.filteredLeaves = [...this.leaves]; // Mettre à jour la liste affichée
        this.closeToast(); // Fermer le formulaire après soumission
      },
      error: () => {
        this.errorMessage = "Erreur lors de l'ajout du congé";
      }
    });
  }

  resetNewLeave(): void {
    this.newLeave = {
      id: 0,
      startDate: '',
      endDate: '',
      description: '',
      status: 'PENDING',
    };
  }
  openUpdateToast(leave: Leave): void {
    this.selectedLeave = { ...leave }; // Copiez l'objet congé sélectionné pour la mise à jour
    this.showUpdateToast = true; // Ouvrir le formulaire de mise à jour
  }

  closeUpdateToast(): void {
    this.showUpdateToast = false; // Fermer le formulaire de mise à jour
    this.selectedLeave= {
      id: 0,
      startDate: '',
      endDate: '',
      description: '',
      status: 'PENDING',
    };
  }

  // Soumettre les modifications du congé
  updateLeave(): void {
    const token = localStorage.getItem('token');
    if (this.selectedLeave && token) {
      console.log(this.selectedLeave)
      this.leaveService.updateLeave(this.selectedLeave.id, this.selectedLeave, token).subscribe({
        next: () => {
          const index = this.leaves.findIndex(leave => leave.id === this.selectedLeave?.id);
          if (index !== -1) {
            this.leaves[index] = { ...this.selectedLeave }; // Mettre à jour le congé dans la liste
          }
          this.filteredLeaves = [...this.leaves]; // Mettre à jour la liste affichée
          this.closeUpdateToast(); // Fermer le formulaire après soumission
        },
        error: () => {
          this.errorMessage = "Erreur lors de la mise à jour du congé";
        }
      });
    }
  }
// Supprimer un congé
deleteLeave(leaveId: number): void {
  const token: any = localStorage.getItem('token');
  if (token) {
    this.leaveService.deleteLeave(leaveId, token).subscribe({
      next: () => {
        this.leaves = this.leaves.filter(leave => leave.id !== leaveId); // Enlever le congé supprimé de la liste
        this.filteredLeaves = [...this.leaves]; // Mettre à jour la liste affichée
      },
      error: () => {
        this.errorMessage = "Erreur lors de la suppression du congé";
      }
    });
  }
}
}
