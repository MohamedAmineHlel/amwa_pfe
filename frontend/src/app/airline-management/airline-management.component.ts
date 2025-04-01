import { Component, OnInit, Pipe, PipeTransform } from '@angular/core';
import { AirlineService } from '../service/airline.service'; // Adjust path as needed
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Pipe({
  name: 'filter',
  standalone: true
})
export class FilterPipe implements PipeTransform {
  transform(items: any[], filter: any): any[] {
    if (!items || !filter) {
      return items;
    }
    return items.filter(item => item.archived === filter.archived);
  }
}

@Component({
  selector: 'app-airline-management',
  standalone: true,
  imports: [FormsModule, CommonModule, FilterPipe],
  templateUrl: './airline-management.component.html',
  styleUrls: ['./airline-management.component.css']
})
export class AirlineManagementComponent implements OnInit {
  airlines: any[] = [];
  newAirline: any = { nameAirline: '', descriptionAirline: '', abbrAirline: '' };
  updateAirlineData: any = { name: '', description: '', abbr: '' };
  errorMessage: string = '';
  isModifying: boolean = false;
  isCreating: boolean = false;
  isViewingArchived: boolean = false;
  selectedAirlineId: string | null = null;

  constructor(private airlineService: AirlineService) {}

  ngOnInit() {
    this.loadAllAirlines();
  }

  // Load all airlines
  loadAllAirlines() {
    this.airlineService.getAllAirlines().subscribe({
      next: (response) => {
        if (response.statusCode === 200) {
          this.airlines = response.airlinesList || [];
          this.errorMessage = '';
        } else {
          this.errorMessage = response.error || 'Failed to load airlines';
        }
      },
      error: (err) => {
        this.errorMessage = 'Error fetching airlines: ' + err.message;
      }
    });
  }

  // Open create modal
  openCreateModal() {
    this.isCreating = true;
    this.newAirline = { nameAirline: '', descriptionAirline: '', abbrAirline: '' };
  }

  // Create a new airline
  createAirline() {
    this.airlineService.createAirline(this.newAirline).subscribe({
      next: (response) => {
        if (response.statusCode === 201) {
          this.loadAllAirlines(); // Refresh list
          this.isCreating = false;
          this.errorMessage = '';
        } else {
          this.errorMessage = response.error || 'Failed to create airline';
        }
      },
      error: (err) => {
        this.errorMessage = 'Error creating airline: ' + err.message;
      }
    });
  }

  // Cancel create
  cancelCreate() {
    this.isCreating = false;
    this.newAirline = { nameAirline: '', descriptionAirline: '', abbrAirline: '' };
  }

  // Select an airline for updating
  selectAirlineForUpdate(airlineId: string) {
    this.airlineService.getAirlineById(airlineId).subscribe({
      next: (response) => {
        if (response.statusCode === 200) {
          this.selectedAirlineId = airlineId;
          this.updateAirlineData = {
            name: response.airline.name,
            description: response.airline.description,
            abbr: response.airline.abbr
          };
          this.isModifying = true;
          this.errorMessage = '';
        } else {
          this.errorMessage = response.error || 'Airline not found';
        }
      },
      error: (err) => {
        this.errorMessage = 'Error fetching airline: ' + err.message;
      }
    });
  }

  // Update an airline
  updateAirline(airlineId: string | null) {
    if (!airlineId) {
      this.errorMessage = 'No airline selected for update';
      return;
    }
    const updatePayload = {
      nameAirline: this.updateAirlineData.name,
      descriptionAirline: this.updateAirlineData.description,
      abbrAirline: this.updateAirlineData.abbr
    };
    this.airlineService.updateAirline(airlineId, updatePayload).subscribe({
      next: (response) => {
        if (response.statusCode === 200) {
          this.loadAllAirlines(); // Refresh list
          this.isModifying = false;
          this.selectedAirlineId = null;
          this.errorMessage = '';
        } else {
          this.errorMessage = response.error || 'Failed to update airline';
        }
      },
      error: (err) => {
        this.errorMessage = 'Error updating airline: ' + err.message;
      }
    });
  }

  // Cancel update
  cancelUpdate() {
    this.isModifying = false;
    this.selectedAirlineId = null;
    this.updateAirlineData = { name: '', description: '', abbr: '' };
  }

  // Delete an airline
  deleteAirline(airlineId: string) {
    this.airlineService.deleteAirline(airlineId).subscribe({
      next: (response) => {
        if (response.statusCode === 200) {
          this.loadAllAirlines(); // Refresh list
          if (this.selectedAirlineId === airlineId) {
            this.isModifying = false;
            this.selectedAirlineId = null;
          }
          this.errorMessage = '';
        } else {
          this.errorMessage = response.error || 'Failed to delete airline';
        }
      },
      error: (err) => {
        this.errorMessage = 'Error deleting airline: ' + err.message;
      }
    });
  }

  // Archive an airline
  archiveAirline(airlineId: string) {
    this.airlineService.archiveAirline(airlineId).subscribe({
      next: (response) => {
        if (response.statusCode === 200) {
          this.loadAllAirlines(); // Refresh list
          if (this.selectedAirlineId === airlineId) {
            this.updateAirlineData = { ...response.airline };
          }
          this.errorMessage = '';
        } else {
          this.errorMessage = response.error || 'Failed to archive airline';
        }
      },
      error: (err) => {
        this.errorMessage = 'Error archiving airline: ' + err.message;
      }
    });
  }

  // Unarchive an airline
  unarchiveAirline(airlineId: string) {
    this.airlineService.unarchiveAirline(airlineId).subscribe({
      next: (response) => {
        if (response.statusCode === 200) {
          this.loadAllAirlines(); // Refresh list
          if (this.selectedAirlineId === airlineId) {
            this.updateAirlineData = { ...response.airline };
          }
          this.errorMessage = '';
        } else {
          this.errorMessage = response.error || 'Failed to unarchive airline';
        }
      },
      error: (err) => {
        this.errorMessage = 'Error unarchiving airline: ' + err.message;
      }
    });
  }

  // Open archived airlines modal
  openArchivedModal() {
    this.isViewingArchived = true;
  }

  // Close archived airlines modal
  closeArchivedModal() {
    this.isViewingArchived = false;
  }
}