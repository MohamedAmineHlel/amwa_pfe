import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink,Router } from '@angular/router';
import { UsersService } from '../users.service';

@Component({
  selector: 'app-nav',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink],
  templateUrl: './nav.component.html',
  styleUrl: './nav.component.css'
})
export class NavComponent implements OnInit {
  constructor(private readonly userService: UsersService, private router: Router) {}

  isAuthenticated: boolean = false;
  isAdmin: boolean = false;
  isUser: boolean = false;
  isSidebarCollapsed: boolean = false;
  isUserManagementOpen: boolean = false;
  isLeaveOpen: boolean = false;
  isDropdownOpen: boolean = false; // New property for dropdown state
  profileInfo: any;
  errorMessage: string = '';
  role=localStorage.getItem('role')


  async ngOnInit() {
    try {
      const token = localStorage.getItem('token')
      if(!token){
        throw new Error("No Token Found")
      }

      this.profileInfo = await this.userService.getYourProfile(token);
    } catch (error:any) {
      this.showError(error.message)
    }
    this.isAuthenticated = this.userService.isAuthenticated();
    this.isAdmin = this.userService.isAdmin();
    this.isUser = this.userService.isUser();
  }

  logout(): void {
    this.userService.logOut();
    this.isAuthenticated = false;
    this.isAdmin = false;
    this.isUser = false;
    this.isDropdownOpen = false; // Close dropdown on logout
    this.router.navigate(['/login']).then(() => {
      window.location.reload();
    });
    }

  toggleSidebar(): void {
    this.isSidebarCollapsed = !this.isSidebarCollapsed;
  }
  toggleUserManagement() {
    this.isUserManagementOpen = !this.isUserManagementOpen;
  }
  toggleLeave() {
    this.isLeaveOpen = !this.isLeaveOpen;
  }
  toggleDropdown(): void {
    this.isDropdownOpen = !this.isDropdownOpen;
  }

  closeDropdown(): void {
    this.isDropdownOpen = false;
  }
  showError(message: string) {
    this.errorMessage = message;
    setTimeout(() => {
      this.errorMessage = ''; // Clear the error message after the specified duration
    }, 3000);
  }
}