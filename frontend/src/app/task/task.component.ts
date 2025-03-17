import { Component, OnInit } from '@angular/core';
import { TaskService } from '../service/task.service';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { UsersService } from '../users.service';
import { TeamService } from '../service/team.service';

@Component({
  selector: 'app-task',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './task.component.html',
  styleUrls: ['./task.component.css']
})
export class TaskComponent implements OnInit {
  tasks: any[] = [];
  message: string = '';
  errorMessage: string = '';
  users: any[] = [];
  isAuthenticated: boolean = false;
  isAdmin: boolean = false;
  isUser: boolean = false;
  teams: any[] = [];
  profileInfo: any;
  showTaskForm: boolean = false;
  showDateForm: boolean = false;
  taskToUpdate: any = null;
  visibleTasks: { [key: string]: number } = {
    DISPATCHED: 4,
    STARTED: 4,
    ONGOING: 4,
    FINISHED: 4,
    DELAYED: 4,
  };
  showTaskModal: boolean = false; // Flag to toggle task details modal
  selectedTask: any = null; // Store the selected task for the modal

  newTask = {
    taskName: '',
    description: '',
    priority: 'LOW',
    status: 'DISPATCHED',
    createdBy: null,
    assignedTo: null,
    teamId: null,
    expiryDate: ''
  };

  constructor(private taskService: TaskService, private userService: UsersService, private teamService: TeamService) {}

  async ngOnInit() {
    try {
      const token = localStorage.getItem('token');
      if (!token) {
        throw new Error("No Token Found");
      }
      this.profileInfo = await this.userService.getYourProfile(token);
    } catch (error: any) {
      this.showError(error.message);
    }
    this.loadUsers();
    this.getAllTeams();
    this.loadTasks();
    this.isAuthenticated = this.userService.isAuthenticated();
    this.isAdmin = this.userService.isAdmin();
    this.isUser = this.userService.isUser();
  }

  toggleTaskForm() {
    this.showTaskForm = !this.showTaskForm;
  }

  async loadUsers() {
    try {
      const token: any = localStorage.getItem('token');
      const response = await this.userService.getAllUsers(token);
      if (response && response.statusCode === 200 && response.ourUsersList) {
        this.users = response.ourUsersList;
        this.loadTasks();
      } else {
        this.showError('No users found.');
      }
    } catch (error: any) {
      this.showError(error.message);
    }
  }

  loadTasks(): void {
    if (this.users.length === 0) {
      return;
    }
    const today = new Date().toISOString().split('T')[0];
    this.taskService.getAllTasks().subscribe(
      (response) => {
        this.tasks = response.tasks.map((task: any) => {
          task.createdBy = this.getUserName(task.createdBy);
          task.assignedTo = this.getUserName(task.assignedTo);
          task.team = this.getTeamName(task.team);
          if (task.expiryDate <= today && task.status !== 'COMPLETED' && task.status !== 'FINISHED') {
            task.status = 'DELAYED';
          }
          return task;
        });
      },
      (error) => {
        console.error('Error fetching tasks', error);
      }
    );
  }

  showError(message: string) {
    this.errorMessage = message;
    setTimeout(() => {
      this.errorMessage = '';
    }, 3000);
  }

  createTask(): void {
    this.newTask.createdBy = this.profileInfo?.ourUsers?.id;
    this.taskService.createTask(this.newTask).subscribe(
      (response) => {
        if (response.statusCode === 200) {
          this.message = response.message;
          this.loadTasks();
          this.clearForm();
          this.showTaskForm = false; // Close the modal on success
        } else {
          this.showError(response.message);
        }
      },
      (error) => {
        console.error('Error creating task', error);
        this.showError('Error creating task');
      }
    );
  }

  clearForm() {
    this.newTask = {
      taskName: '',
      description: '',
      priority: 'LOW',
      status: 'DISPATCHED',
      createdBy: null,
      assignedTo: null,
      teamId: null,
      expiryDate: ''
    };
  }

  getAllTeams(): Promise<void> {
    return new Promise((resolve, reject) => {
      this.teamService.getAllTeams().subscribe(
        (response) => {
          this.teams = response.teamsList;
          resolve();
        },
        (error) => {
          console.error('Error retrieving teams:', error);
          this.showError('Error retrieving teams');
          reject(error);
        }
      );
    });
  }

  getTeamName(teamId: number | any): string {
    if (!teamId) return 'N/A';
    if (typeof teamId === 'object' && teamId.teamName) {
      return teamId.teamName;
    } else if (typeof teamId === 'number') {
      const team = this.teams.find((t) => t.id === teamId);
      return team ? team.teamName : 'Unknown';
    }
    return 'Unknown';
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

  updateTaskExpiry(taskId: number, currentExpiryDate: string): void {
    this.showDateForm = true;
    this.taskToUpdate = { taskId, expiryDate: currentExpiryDate };
  }

  saveUpdatedExpiryDate(): void {
    if (this.taskToUpdate && this.taskToUpdate.expiryDate) {
      this.taskService.updateTaskExpiry(this.taskToUpdate.taskId, this.taskToUpdate.expiryDate).subscribe(
        (response) => {
          if (response.statusCode === 200) {
            this.message = 'Expiry date updated successfully!';
            this.loadTasks();
            this.showDateForm = false;
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
  }

  cancelDateUpdate(): void {
    this.showDateForm = false;
  }

  filterTasksByStatus(status: string) {
    const tasks = this.tasks.filter(task => task.status === status);
    return tasks.slice(0, this.visibleTasks[status]);
  }

  showMoreTasks(status: string) {
    const totalTasks = this.tasks.filter(task => task.status === status).length;
    if (this.visibleTasks[status] < totalTasks) {
      this.visibleTasks[status] += 4;
    }
  }

  showLessTasks(status: string) {
    if (this.visibleTasks[status] > 4) {
      this.visibleTasks[status] -= 4;
    }
  }

  get showMoreFinishedTasks() {
    return this.visibleTasks['FINISHED'] < this.tasks.filter(task => task.status === 'FINISHED').length;
  }
  get showMoreDispatchedTasks() {
    return this.visibleTasks['DISPATCHED'] < this.tasks.filter(task => task.status === 'DISPATCHED').length;
  }
  get showMoreOngoingTasks() {
    return this.visibleTasks['ONGOING'] < this.tasks.filter(task => task.status === 'ONGOING').length;
  }
  get showMoreDelayedTasks() {
    return this.visibleTasks['DELAYED'] < this.tasks.filter(task => task.status === 'DELAYED').length;
  }
  get showMoreStartedTasks() {
    return this.visibleTasks['STARTED'] < this.tasks.filter(task => task.status === 'STARTED').length;
  }

  // Modal methods
  openTaskModal(task: any): void {
    this.selectedTask = task;
    this.showTaskModal = true;
  }

  closeTaskModal(): void {
    this.showTaskModal = false;
    this.selectedTask = null;
  }
}