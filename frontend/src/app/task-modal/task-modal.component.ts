import { CommonModule } from '@angular/common';
import { Component, Input, Output, EventEmitter, AfterViewInit, OnDestroy } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-task-modal',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './task-modal.component.html',
  styleUrl: './task-modal.component.css'
})
export class TaskModalComponent implements AfterViewInit, OnDestroy{
  @Input() teamName!: string;
  @Input() tasks: any[] = []; // List of tasks assigned to the team
  @Input()   selectedTeamTasks: any[] | null = null;; // Store tasks for the modal

  @Output() close = new EventEmitter<void>(); // Emit close event to parent


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
    this.close.emit(); // Close the modal
    this.selectedTeamTasks = null; // Reset to null to hide the modal

  }
  
}
