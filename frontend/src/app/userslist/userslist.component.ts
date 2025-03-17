import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { UsersService } from '../users.service';
import { NgxPaginationModule } from 'ngx-pagination';
import { FormsModule } from '@angular/forms';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';


@Component({
  selector: 'app-userslist',
  standalone: true,
  imports: [CommonModule,NgxPaginationModule,FormsModule],
  templateUrl: './userslist.component.html',
  styleUrl: './userslist.component.css'
})


export class UserslistComponent implements OnInit {

  users: any[] = [];
  errorMessage: string = '';
  currentPage: number = 1;  
  itemsPerPage: number = 5; 
  searchName: string = ''; // Add this for two-way binding
  filteredUsers: any[] = []; // Add this for filtered results
  baseImageUrl: string = 'http://localhost:1010/uploads/image/'; // Adjust based on your backend URL
  constructor(
    private readonly userService: UsersService,
    private readonly router: Router,
    private sanitizer: DomSanitizer
  ) {}

  
  ngOnInit(): void {
    this.loadUsers();

  }

  async loadUsers() {
    try {
      const token: any = localStorage.getItem('token');
      const response = await this.userService.getAllUsers(token);
      if (response && response.statusCode === 200 && response.ourUsersList) {
        this.users = response.ourUsersList.map((user: any) => ({
          ...user,
          imageUrl: this.getImageUrl(user.image)
        }));
        this.filteredUsers = [...this.users]; // Initialize filteredUsers with all users
        console.log(this.users);
      } else {
        this.showError('No users found.');
      }
    } catch (error: any) {
      this.showError(error.message);
    }
  }

  getImageUrl(imagePath: string): SafeUrl {
    if (!imagePath) return '';
    
    // Extract filename from the path
    const filename = imagePath.split('\\').pop() || imagePath.split('/').pop();
    const fullUrl = `${this.baseImageUrl}${filename}`;
    return this.sanitizer.bypassSecurityTrustUrl(fullUrl);
  }

  filterUsers() {
    if (this.searchName.trim() === '') {
      this.filteredUsers = [...this.users]; // Reset to all users when search is empty
    } else {
      const searchTerm = this.searchName.toLowerCase();
      this.filteredUsers = this.users.filter(user =>
        user.name.toLowerCase().includes(searchTerm) ||
        user.role.toLowerCase().includes(searchTerm)
      );
    }
    this.currentPage = 1; // Reset to first page when filtering
  }
  async deleteUser(userId: string) {
    const confirmDelete = confirm('Are you sure you want to delete this user?');
    if (confirmDelete) {
      try {
        const token: any = localStorage.getItem('token');
        await this.userService.deleteUser(userId, token);
        // Refresh the user list after deletion
        this.loadUsers();
      } catch (error: any) {
        this.showError(error.message);
      }
    }
  }
  handleImageError(event: Event) {
    // Handle image loading errors (e.g., show default image)
    (event.target as HTMLImageElement).src = 'path/to/default-image.jpg';
  }
  navigateToUpdate(userId: string) {
    this.router.navigate(['/update', userId]);
  }

  showError(message: string) {
    this.errorMessage = message;
    setTimeout(() => {
      this.errorMessage = ''; // Clear the error message after the specified duration
    }, 3000);
  }
}
