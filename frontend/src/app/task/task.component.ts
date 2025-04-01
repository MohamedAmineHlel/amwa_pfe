import { Component, OnInit } from '@angular/core';
import { TaskService } from '../service/task.service';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { UsersService } from '../users.service';
import { TeamService } from '../service/team.service';
import { RouterLink, RouterOutlet } from '@angular/router';
import { CommentService } from '../service/comment.service';

@Component({
  selector: 'app-task',
  standalone: true,
  imports: [FormsModule, CommonModule, RouterLink],
  templateUrl: './task.component.html',
  styleUrls: ['./task.component.css']
})
export class TaskComponent implements OnInit {
  tasks: any[] = [];
  filteredTasks: any[] = []; // Add this to store filtered tasks
  // Search properties
  searchTaskName: string = '';
  searchUniqueNumber: string = '';
  searchStatus: string | null = null;
  message: string = '';
  errorMessage: string = '';
  users: any[] = [];
  isAuthenticated: boolean = false;
  isAdmin: boolean = false;
  selectedTeamFilter: number | null = null; // Filter by team ID, null for all teams
  tasksByTeamAndStatus: { [teamId: number]: { [status: string]: any[] } } = {};
  isUser: boolean = false;
  teams: any[] = [];
  selectedTeamName: string = '';
  selectedTeamMembers: any[] | null = null;
  profileInfo: any;
  showTaskForm: boolean = false;
  tasksByTeam: { [teamId: number]: any[] } = {};
  teamTasksByStatus: { [teamId: number]: { [status: string]: any[] } } = {};
  showDateForm: boolean = false;
  taskToUpdate: any = null;
  teamsWithTasks: any[] = [];
  selectedTeamId: number | null = null;
  filteredTeams: any[] = []; // Add this to dynamically filter teams
  visibleTasks: { [key: string]: number } = {
    DISPATCHED: 4,
    STARTED: 4,
    ONGOING: 4,
    FINISHED: 4,
    DELAYED: 4,
  };
  showTaskModal: boolean = false;
  selectedTask: any = null;
  
  teamMembers: any[] = [];
  newTask = {
    taskName: '',
    description: '',
    priority: 'LOW',
    status: 'DISPATCHED',
    createdBy: null,
    assignedTo: null,
    teamId: null as number | null,
    expiryDate: ''
  };
  showModifyForm: boolean = false;
  taskToModify: any = {
    taskId: null,
    taskName: '',
    description: '',
    priority: 'LOW',
    status: 'DISPATCHED',
    assignedTo: null,
    teamId: null,
    expiryDate: ''
  };
  selectedFiles: File[] = [];
  uploadMessage: string = '';
  uploadError: string = '';
// Comments-related properties
comments: any[] = []; // Store comments for the selected task
newCommentContent: string = ''; // For creating a new comment
editingCommentId: number | null = null; // Track the comment being edited
editingCommentContent: string = ''; // Content of the comment being edited
  constructor(private taskService: TaskService, private userService: UsersService, private teamService: TeamService,private commentService: CommentService) {}

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
          task.teamId = task.team?.id || task.team || null;
          task.teamName = this.getTeamName(task.teamId);
          if (task.expiryDate <= today && task.status !== 'COMPLETED' && task.status !== 'FINISHED') {
            task.status = 'DELAYED';
          }
          return task;
        });
        this.filteredTasks = [...this.tasks]; // Initialize filteredTasks with all tasks
        this.applySearchFilter(); // Apply search and team filters after loading
      },
      (error) => {
        console.error('Error fetching tasks', error);
      }
    );
  }

  applySearchFilter(): void {
    this.filteredTasks = this.tasks.filter(task => {
      const matchesTaskName = this.searchTaskName
        ? task.taskName.toLowerCase().includes(this.searchTaskName.toLowerCase())
        : true;
      const matchesUniqueNumber = this.searchUniqueNumber
        ? task.uniqueNumber.toLowerCase().includes(this.searchUniqueNumber.toLowerCase())
        : true;
      const matchesStatus = this.searchStatus !== null
        ? task.status === this.searchStatus
        : true; // Include all tasks if searchStatus is null
      const matchesTeam = this.selectedTeamFilter
        ? task.teamId === this.selectedTeamFilter
        : true;
  
      return matchesTaskName && matchesUniqueNumber && matchesStatus && matchesTeam;
    });
  
    this.tasksByTeamAndStatus = this.groupTasksByTeamAndStatus(this.filteredTasks);
    this.applyTeamFilter();
  }

  groupTasksByTeamAndStatus(tasks: any[]): { [teamId: number]: { [status: string]: any[] } } {
    const groupedTasks: { [teamId: number]: { [status: string]: any[] } } = {};

    tasks.forEach(task => {
      const teamId = task.teamId ? Number(task.teamId) : null;
      const status = task.status;

      if (!teamId) {
        console.warn('Task with undefined or null teamId:', task);
      }

      const effectiveTeamId = teamId || 0;

      if (!groupedTasks[effectiveTeamId]) {
        groupedTasks[effectiveTeamId] = {};
      }

      if (!groupedTasks[effectiveTeamId][status]) {
        groupedTasks[effectiveTeamId][status] = [];
      }

      groupedTasks[effectiveTeamId][status].push(task);
    });

    return groupedTasks;
  }

  applyTeamFilter(): void {
    if (this.selectedTeamFilter === null) {
      // Show all teams
      this.filteredTeams = [...this.teams];
    } else {
      // Show only the selected team
      this.filteredTeams = this.teams.filter(team => team.id === this.selectedTeamFilter);
    }
  }
  modifyTask(task: any): void {
    this.showModifyForm = true;
    this.taskToModify = {
      taskId: task.id,
      taskName: task.taskName,
      description: task.description,
      priority: task.priority,
      status: task.status,
      assignedTo: task.assignedTo?.id || task.assignedTo,
      teamId: task.teamId,
      expiryDate: task.expiryDate
    };
    if (task.teamId) {
      this.loadTeamMembers(task.teamId);
    }
  }

  // Add method to save modified task
  saveModifiedTask(): void {
    if (this.taskToModify && this.taskToModify.taskId) {
      this.taskService.modifyTask(this.taskToModify.taskId, this.taskToModify).subscribe(
        (response) => {
          if (response.statusCode === 200) {
            this.message = 'Task modified successfully!';
            this.loadTasks();
            this.showModifyForm = false;
            this.clearModifyForm();
          } else {
            this.showError(response.message);
          }
        },
        (error) => {
          console.error('Error modifying task', error);
          this.showError('Error modifying task');
        }
      );
    }
  }

  // Add method to cancel modification
  cancelModify(): void {
    this.showModifyForm = false;
    this.clearModifyForm();
  }

  // Add method to clear modify form
  clearModifyForm(): void {
    this.taskToModify = {
      taskId: null,
      taskName: '',
      description: '',
      priority: 'LOW',
      status: 'DISPATCHED',
      assignedTo: null,
      teamId: null,
      expiryDate: ''
    };
    this.teamMembers = [];
  }
  // Updated onTeamFilterChange method
  onTeamFilterChange(event: Event): void {
    const teamId = (event.target as HTMLSelectElement).value === 'null' ? null : Number((event.target as HTMLSelectElement).value);
    this.selectedTeamFilter = teamId;
    this.applySearchFilter(); // Apply both team and search filters
      }

  showError(message: string) {
    this.errorMessage = message;
    setTimeout(() => {
      this.errorMessage = '';
    }, 3000);
  }

  loadTeamMembers(teamId: number): void {
    if (teamId) {
      this.teamService.getUsersByTeamId(teamId).subscribe(
        (response) => {
          if (response.statusCode === 200 && response.ourUsersList) {
            this.teamMembers = response.ourUsersList;
          } else {
            this.teamMembers = [];
            this.showError('No members found for this team');
          }
        },
        (error) => {
          console.error('Error fetching team members:', error);
          this.teamMembers = [];
          this.showError('Error loading team members');
        }
      );
    } else {
      this.teamMembers = [];
    }
  }

  onTeamChange(event: Event, isModify: boolean = false): void {
    const teamId = Number((event.target as HTMLSelectElement).value);
    if (isModify) {
      this.taskToModify.teamId = teamId || null;
      this.taskToModify.assignedTo = null;
    } else {
      this.newTask.teamId = teamId || null;
      this.newTask.assignedTo = null;
    }
    this.loadTeamMembers(teamId);
  }

  createTask(): void {
    this.newTask.createdBy = this.profileInfo?.ourUsers?.id;
    this.taskService.createTask(this.newTask).subscribe(
      (response) => {
        if (response.statusCode === 200) {
          this.message = response.message;
          this.loadTasks();
          this.clearForm();
          this.showTaskForm = false;
          this.teamMembers = [];
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
    this.teamMembers = [];
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
    if (!teamId) return 'Unassigned';
    if (typeof teamId === 'number') {
      const team = this.teams.find((t) => t.id === teamId);
      return team ? team.teamName : 'Unknown';
    } else if (typeof teamId === 'object' && teamId.teamName) {
      return teamId.teamName;
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

  openTaskModal(task: any): void {
    this.selectedTask = task;
    this.showTaskModal = true;
    this.loadComments(task.id); // Load comments when opening the modal
  }

  closeTaskModal(): void {
    this.showTaskModal = false;
    this.selectedTask = null;
    this.comments = []; // Clear comments when closing
    this.newCommentContent = '';
    this.editingCommentId = null;
    this.editingCommentContent = '';
  }

  // Load comments for the selected task
  loadComments(taskId: number): void {
    if (this.users.length === 0) {
      this.loadUsers().then(() => this.loadComments(taskId));
      return;
    }

    this.commentService.getCommentsByTaskId(taskId).subscribe(
      (response) => {
        if (response.statusCode === 200) {
          this.comments = (response.commentss || []).map((comment: any) => {
            const userId = typeof comment.user === 'object' && comment.user.id ? comment.user.id : comment.user;
            comment.user = {
              id: userId, // Preserve the ID for ownership checks
              name: this.getUserName(comment.user)
            };
            return comment;
          });
        } else {
          this.showError(response.message);
        }
      },
      (error) => {
        console.error('Error fetching comments', error);
        this.showError('Failed to load comments');
      }
    );
  }

  addComment(): void {
    if (!this.newCommentContent.trim()) {
      this.showError('Comment cannot be empty');
      return;
    }

    const commentData = {
      commentContent: this.newCommentContent,
      commentUserId: this.profileInfo?.ourUsers?.id,
      commentTaskId: this.selectedTask.id
    };

    this.commentService.createComment(commentData).subscribe(
      (response) => {
        if (response.statusCode === 200) {
          this.message = response.message;
          this.newCommentContent = '';
          this.loadComments(this.selectedTask.id);
        } else {
          this.showError(response.message);
        }
      },
      (error) => {
        console.error('Error creating comment', error);
        this.showError('Failed to add comment');
      }
    );
  }

  editComment(comment: any): void {
    const currentUserId = this.profileInfo?.ourUsers?.id;
    if (!this.isAdmin && comment.user.id !== currentUserId) {
      this.showError('You can only edit your own comments');
      return;
    }
    this.editingCommentId = comment.id;
    this.editingCommentContent = comment.content;
  }

  saveEditedComment(commentId: number): void {
    if (!this.editingCommentContent.trim()) {
      this.showError('Comment cannot be empty');
      return;
    }

    const comment = this.comments.find(c => c.id === commentId);
    const currentUserId = this.profileInfo?.ourUsers?.id;
    if (!this.isAdmin && comment.user.id !== currentUserId) {
      this.showError('You can only edit your own comments');
      return;
    }

    const commentData = {
      commentContent: this.editingCommentContent
    };

    this.commentService.updateComment(commentId, commentData).subscribe(
      (response) => {
        if (response.statusCode === 200) {
          this.message = response.message;
          this.editingCommentId = null;
          this.editingCommentContent = '';
          this.loadComments(this.selectedTask.id);
        } else {
          this.showError(response.message);
        }
      },
      (error) => {
        console.error('Error updating comment', error);
        this.showError('Failed to update comment');
      }
    );
  }

  deleteComment(commentId: number): void {
    const comment = this.comments.find(c => c.id === commentId);
    const currentUserId = this.profileInfo?.ourUsers?.id;
    if (!this.isAdmin && comment.user.id !== currentUserId) {
      this.showError('You can only delete your own comments');
      return;
    }

    if (confirm('Are you sure you want to delete this comment?')) {
      this.commentService.deleteComment(commentId).subscribe(
        (response) => {
          if (response.statusCode === 200) {
            this.message = response.message;
            this.loadComments(this.selectedTask.id);
          } else {
            this.showError(response.message);
          }
        },
        (error) => {
          console.error('Error deleting comment', error);
          this.showError('Failed to delete comment');
        }
      );
    }
  }

  viewTeamUsers(teamId: number, teamName: string) {
    this.teamService.getUsersByTeamId(teamId).subscribe(
      (response) => {
        if (response.statusCode === 200) {
          this.selectedTeamName = teamName;
          this.selectedTeamMembers = response.ourUsersList ?? [];
        } else {
          this.selectedTeamMembers = [];
        }
      },
      (error) => {
        console.error('Error fetching team users:', error);
        this.selectedTeamMembers = [];
      }
    );
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

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files) {
      this.selectedFiles = Array.from(input.files);
    }
  }

  // Add method to upload attachments
  uploadAttachments(): void {
    if (!this.selectedTask || this.selectedFiles.length === 0) {
      this.uploadError = 'Please select files to upload';
      setTimeout(() => this.uploadError = '', 3000);
      return;
    }

    this.taskService.uploadTaskAttachments(this.selectedTask.id, this.selectedFiles).subscribe(
      (response) => {
        if (response.statusCode === 200) {
          this.uploadMessage = 'Files uploaded successfully!';
          this.selectedTask = response.task; // Update the task with new attachments
          this.loadTasks(); // Refresh the task list
          this.selectedFiles = []; // Clear selected files
          setTimeout(() => this.uploadMessage = '', 3000);
        } else {
          this.uploadError = response.message;
          setTimeout(() => this.uploadError = '', 3000);
        }
      },
      (error) => {
        console.error('Error uploading attachments', error);
        this.uploadError = 'Error uploading files';
        setTimeout(() => this.uploadError = '', 3000);
      }
    );
  }
  downloadAttachment(attachment: string): void {
    const filename = attachment.split('\\').pop() || attachment.split('/').pop();
    if (!filename) {
      this.showError('Invalid attachment filename');
      return;
    }

    if (!this.selectedTask || !this.selectedTask.id) {
      this.showError('No task selected for download');
      return;
    }

    this.taskService.downloadAttachment(this.selectedTask.id, filename).subscribe(
      (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = filename; // Set the filename for download
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url); // Clean up
      },
      (error) => {
        console.error('Error downloading attachment', error);
        this.showError('Failed to download attachment');
      }
    );
  }
  // New method to delete an attachment
  deleteAttachment(attachment: any): void {
    if (!this.selectedTask) {
      this.showError('No task selected');
      return;
    }

    const filename = typeof attachment === 'string' ? (attachment.split('\\').pop() || attachment.split('/').pop()) : attachment.filename;
    if (!filename) {
      this.showError('Invalid attachment filename');
      return;
    }

    const currentUserId = this.profileInfo?.ourUsers?.id;
    const uploadedBy = attachment.uploadedBy || this.selectedTask.attachments.find((a: any) => a.filename === filename)?.uploadedBy;

    if (!this.isAdmin && uploadedBy !== currentUserId) {
      this.showError('You can only delete your own attachments');
      return;
    }

    if (confirm(`Are you sure you want to delete ${filename}?`)) {
      this.taskService.deleteTaskAttachment(this.selectedTask.id, filename).subscribe(
        (response) => {
          if (response.statusCode === 200) {
            this.uploadMessage = 'Attachment deleted successfully!';
            this.selectedTask = response.task;
            this.loadTasks();
            setTimeout(() => this.uploadMessage = '', 3000);
          } else {
            this.showError(response.message);
          }
        },
        (error) => {
          console.error('Error deleting attachment', error);
          this.showError('Failed to delete attachment');
        }
      );
    }
  }
// Cancel editing a comment
cancelEdit(): void {
  this.editingCommentId = null;
  this.editingCommentContent = '';
}
  // Helper method to check if a comment belongs to the current user or if the user is an admin
  canEditOrDeleteComment(comment: any): boolean {
    const currentUserId = this.profileInfo?.ourUsers?.id;
    return this.isAdmin || comment.user.id === currentUserId;
  }

  // Helper method to check if an attachment belongs to the current user or if the user is an admin
  canDeleteAttachment(attachment: any): boolean {
    const currentUserId = this.profileInfo?.ourUsers?.id;
    const uploadedBy = typeof attachment === 'string' ? 
      this.selectedTask.attachments.find((a: any) => (a.split('\\').pop() || a.split('/').pop()) === attachment)?.uploadedBy : 
      attachment.uploadedBy;
    return this.isAdmin || uploadedBy === currentUserId;
  }

  // In TaskComponent class
resetFilters(): void {
  this.searchTaskName = '';
  this.searchUniqueNumber = '';
  this.searchStatus = null;
  this.selectedTeamFilter = null;
  this.applySearchFilter(); // Re-apply filters to update the view
}
 
}