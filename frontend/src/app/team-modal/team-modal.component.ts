import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output, AfterViewInit, OnDestroy } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-team-modal',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './team-modal.component.html',
  styleUrl: './team-modal.component.css',
})
export class TeamModalComponent implements AfterViewInit, OnDestroy {
  @Input() teamName!: string;
  @Input() users: any[] = [];
  @Input()   selectedTeamMembers: any[] | null = null; // Initialize as null

  @Output() close = new EventEmitter<void>(); // Emit an event to parent

  ngAfterViewInit(): void {
    // Disable scroll on body when modal is open
    document.body.style.overflow = 'hidden';
  }

  ngOnDestroy(): void {
    // Enable scroll back when modal is closed
    document.body.style.overflow = 'auto';
  }

  // Close modal when clicking outside of it
  closeModalOnOverlayClick(event: MouseEvent) {
    if (event.target === event.currentTarget) {  // Check if the click is on the overlay
      this.closeModal();
    }
  }

  closeModal() {
    this.close.emit(); // Notify parent to close modal
    this.selectedTeamMembers = null; // Reset to null to hide the modal

  }
}
