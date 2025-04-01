import { Component, OnInit } from '@angular/core';
import { FullCalendarModule } from '@fullcalendar/angular';
import dayGridPlugin from '@fullcalendar/daygrid';
import timeGridPlugin from '@fullcalendar/timegrid';
import interactionPlugin from '@fullcalendar/interaction';
import { TaskService } from '../service/task.service';
import { UsersService } from '../users.service';
import { TeamService } from '../service/team.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-calendar',
  standalone: true,
  imports: [FullCalendarModule, CommonModule],
  templateUrl: './calendar.component.html',
  styleUrls: ['./calendar.component.css']
})
export class CalendarComponent implements OnInit {
  tasks: any[] = [];
  calendarOptions: any = {
    plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
    initialView: 'dayGridMonth',
    headerToolbar: {
      left: 'prev,next today',
      center: 'title',
      right: 'dayGridMonth,timeGridWeek,timeGridDay'
    },
    selectable: true,
    editable: true,
    eventClick: this.handleEventClick.bind(this),
    eventDrop: this.handleEventDrop.bind(this),
    events: []
  };
  users: any[] = [];
  teams: any[] = [];
  profileInfo: any;
  errorMessage: string = '';
  selectedTask: any = null;
  showModal: boolean = false;

  constructor(
    private taskService: TaskService,
    private userService: UsersService,
    private teamService: TeamService
  ) {}

  async ngOnInit() {
    await this.loadProfile();
    await this.loadUsers();
    await this.loadTeams();
    this.loadTasks();
  }

  async loadProfile() {
    try {
      const token = localStorage.getItem('token');
      if (!token) {
        throw new Error('No Token Found');
      }
      this.profileInfo = await this.userService.getYourProfile(token);
    } catch (error: any) {
      this.showError(error.message);
    }
  }

  async loadUsers() {
    try {
      const token = localStorage.getItem('token');
      const response = await this.userService.getAllUsers(token!);
      if (response && response.statusCode === 200 && response.ourUsersList) {
        this.users = response.ourUsersList;
      } else {
        this.showError('No users found.');
      }
    } catch (error: any) {
      this.showError(error.message);
    }
  }

  async loadTeams() {
    try {
      const response = await this.teamService.getAllTeams().toPromise();
      this.teams = response.teamsList;
    } catch (error: any) {
      this.showError('Error retrieving teams');
    }
  } 

  loadTasks(): void {
    if (this.users.length === 0 || !this.profileInfo) {
      return;
    }
    const today = new Date().toISOString().split('T')[0];
    this.taskService.getAllTasks().subscribe(
      (response) => {
        let filteredTasks = response.tasks;

        // Filter tasks based on user role
        const userRole = this.profileInfo.ourUsers.role;
        const userId = this.profileInfo.ourUsers.id;
        const userTeamId = this.profileInfo.ourUsers.team?.id || this.profileInfo.ourUsers.team;

        if (userRole === 'ADMIN') {
          // Admin sees all tasks
          filteredTasks = response.tasks;
        } else if (userRole === 'MANAGER' || userRole === 'SUPERVISOR') {
          // Managers and Supervisors see only their team's tasks
          if (userTeamId) {
            filteredTasks = response.tasks.filter((task: any) => 
              task.team?.id === userTeamId || task.team === userTeamId
            );
          } else {
            filteredTasks = []; // No team assigned, show no tasks
          }
        } else if (userRole === 'OPERATOR') {
          // Operators see only their own tasks
          filteredTasks = response.tasks.filter((task: any) => 
            task.assignedTo?.id === userId || task.assignedTo === userId
          );
        } else {
          // Default to no tasks for unrecognized roles
          filteredTasks = [];
        }

        this.tasks = filteredTasks.map((task: any) => {
          task.createdBy = this.getUserName(task.createdBy);
          task.assignedTo = this.getUserName(task.assignedTo);
          task.teamId = task.team?.id || task.team || null;
          task.teamName = this.getTeamName(task.teamId);
          if (task.expiryDate <= today && task.status !== 'COMPLETED' && task.status !== 'FINISHED') {
            task.status = 'DELAYED';
          }
          return task;
        });
        this.prepareCalendarEvents();
      },
      (error) => {
        console.error('Error fetching tasks', error);
        this.showError('Error loading tasks');
      }
    );
  }

  prepareCalendarEvents(): void {
    const events = this.tasks.map(task => ({
      id: task.id,
      title: `${task.taskName} (${task.uniqueNumber})`,
      start: task.expiryDate,
      extendedProps: { data: task }
    }));
    this.calendarOptions.events = events;
  }

  handleEventClick(eventInfo: any): void {
    this.selectedTask = eventInfo.event.extendedProps.data;
    this.showModal = true;
  }

  handleEventDrop(eventDropInfo: any): void {
    const taskId = eventDropInfo.event.id;
    const newDate = eventDropInfo.event.start.toISOString().split('T')[0];

    const updatedTask = this.tasks.find(task => task.id === taskId);
    if (updatedTask) {
      updatedTask.expiryDate = newDate;
      this.updateTaskExpiry(updatedTask);
    }
  }

  updateTaskExpiry(task: any): void {
    this.taskService.updateTaskExpiry(task.id, task.expiryDate).subscribe(
      (response) => {
        if (response.statusCode === 200) {
          console.log('Task expiry date updated successfully.');
          this.loadTasks();
        } else {
          this.showError(response.message);
        }
      },
      (error) => {
        console.error('Error updating task expiry date', error);
        this.showError('Error updating expiry date');
      }
    );
  }

  closeModal(): void {
    this.showModal = false;
    this.selectedTask = null;
  }

  getUserName(userId: number | any): string {
    if (!userId) return 'N/A';
    if (typeof userId === 'number') {
      const user = this.users.find((u) => u.id === userId);
      return user ? user.name : 'Unknown';
    } else if (userId && userId.name) {
      return userId.name;
    }
    return 'Unknown';
  }

  getTeamName(teamId: number | any): string {
    if (!teamId) return 'Unassigned';
    if (typeof teamId === 'number') {
      const team = this.teams.find((t) => t.id === teamId);
      return team ? team.teamName : 'Unknown';
    } else if (typeof teamId === 'object' && teamId.teamName) {
      return teamId.teamName;
    }
    return 'Unknown';
  }  

  showError(message: string) {
    this.errorMessage = message;
    setTimeout(() => {
      this.errorMessage = '';
    }, 3000);
  }
}