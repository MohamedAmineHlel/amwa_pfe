import { Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { RegisterComponent } from './register/register.component';
import { ProfileComponent } from './profile/profile.component';
import { UpdateuserComponent } from './updateuser/updateuser.component';
import { UserslistComponent } from './userslist/userslist.component';
import { usersGuard, adminGuard,authGuard, defaultGuard  } from './users.guard';
import { TeamManagementComponent } from './team-management/team-management.component';
import { TeamModalComponent } from './team-modal/team-modal.component';
import { TaskComponent } from './task/task.component';
import { ProjectManagementComponent } from './project-management/project-management.component';
import { AirlineManagementComponent } from './airline-management/airline-management.component';
import { CalendarComponent } from './calendar/calendar.component';
import { AiFaceLoginComponent } from './ai-face-login/ai-face-login.component';
import { LeaveManagerComponent } from './leave-manager/leave-manager.component';
import { LeavesUsersComponent } from './leaves-users/leaves-users.component';



export const routes: Routes = [
    {path: 'login', component: LoginComponent , canActivate: [authGuard] },
    {path: 'register', component: RegisterComponent, canActivate: [adminGuard]},
    {path: 'profile', component: ProfileComponent, canActivate: [usersGuard]},
    {path: 'update/:id', component: UpdateuserComponent, canActivate: [usersGuard]},
    {path: 'users', component: UserslistComponent, canActivate:[adminGuard]},
    {path: 'teams', component: TeamManagementComponent },
    {path: 'teams/:id/users', component: TeamModalComponent , canActivate: [usersGuard]},
    {path: 'tasks', component: TaskComponent },
    {path: 'projects', component: ProjectManagementComponent },
    {path: 'airlines', component: AirlineManagementComponent },
    {path: 'calendar', component: CalendarComponent },
    {path:'aiLoginFaceId',component:AiFaceLoginComponent},
    {path:'leave',component:LeaveManagerComponent},
    {path:'leaves-users',component:LeavesUsersComponent},





    {path: '**', component: LoginComponent, canActivate: [defaultGuard]},
    {path: '', redirectTo: '/login', pathMatch: 'full'},
];
