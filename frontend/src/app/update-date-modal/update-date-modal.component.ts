import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-update-date-modal',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './update-date-modal.component.html',
  styleUrls: ['./update-date-modal.component.css']
})
export class UpdateDateModalComponent {
  @Input() taskId!: number;
  @Input() currentExpiryDate!: string;
  @Output() close = new EventEmitter<void>();
  @Output() save = new EventEmitter<{ taskId: number, expiryDate: string }>();

  expiryDate: string = this.currentExpiryDate;

  closeModalOnOverlayClick(event: MouseEvent) {
    if (event.target === event.currentTarget) {
      this.closeModal();
    }
  }

  closeModal() {
    this.close.emit();
  }

  onSubmit() {
    this.save.emit({ taskId: this.taskId, expiryDate: this.expiryDate });
    this.closeModal();
  }
}