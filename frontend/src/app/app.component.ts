import { Component } from '@angular/core';
import { Router, RouterOutlet,NavigationEnd  } from '@angular/router';
import { NavComponent } from './nav/nav.component';
import { FooterComponent } from './footer/footer.component';
import { NgIf } from '@angular/common'; 
import { UsersService } from './users.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet,NavComponent, FooterComponent,NgIf],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent  {
  title = 'users-management-angular';

  constructor(private usersService: UsersService) {}

  isAuthenticated(): boolean {
    return this.usersService.isAuthenticated();
  }



}
