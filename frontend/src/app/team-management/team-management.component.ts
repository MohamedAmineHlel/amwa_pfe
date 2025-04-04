import { Component, OnInit } from '@angular/core';
import { TeamService } from '../service/team.service';
import { UsersService } from '../users.service';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { TeamModalComponent } from '../team-modal/team-modal.component';
import { TaskService } from '../service/task.service';
import { TaskModalComponent } from '../task-modal/task-modal.component';
import { ProjectService } from '../service/project.service';
import { AirlineService } from '../service/airline.service';

@Component({
  selector: 'app-team-management',
  standalone: true,
  imports: [FormsModule, CommonModule,TeamModalComponent,TaskModalComponent],
  templateUrl: './team-management.component.html',
  styleUrls: ['./team-management.component.css'],
  providers: [TeamService, ProjectService, AirlineService],
})
export class TeamManagementComponent {

  teamName: string = '';
  projectId: string = ''; // New field for project selection
  airlineId: string = ''; // New field for airline selection
  userId: number | null = null;
  teamId: number | null = null;
  message: string = '';
  errorMessage: string = '';
  teams: any[] = []; // Store list of teams
  users: any[] = []; // Store list of users
  projects: any[] = []; // Store list of projects

  selectedTeamName: string = '';
  selectedTeamMembers: any[] | null = null; // Initialize as null
  isAuthenticated: boolean = false;
  isAdmin: boolean = false;
  isUser: boolean = false;
  tasks: any[] = [];
  teamTaskCounts: { [key: number]: number } = {}; // To store task count per team
  selectedTeamTasks: any[] | null = null;; // Store tasks for the modal
  showCreateTeamForm: boolean = false; // Default to true to show the form initially
  isCreatingTeam: boolean = false; // New property to control the modal
  // Properties for Modify Team Modal
  isModifyingTeam: boolean = false;
  selectedTeam: any = null;
  modifyTeamName: string = '';
  modifyProjectId: string = '';
  modifyAirlineId: string = '';
  availableProjects: any[] = []; // Filtered projects not assigned to teams
 
  constructor(private taskService: TaskService,private teamService: TeamService, private router: Router, private userService: UsersService,private projectService: ProjectService,private airlineService: AirlineService){ }


  ngOnInit(): void {
    this.getAllTeams();
    this.loadUsers(); // Ensure users are loaded first
    this.loadTasks();
    this.loadProjects(); // Load projects

    this.isAuthenticated = this.userService.isAuthenticated();
    this.isAdmin = this.userService.isAdmin();
    this.isUser = this.userService.isUser();
  } 

  
  async loadUsers() {
    try {
      const token: any = localStorage.getItem('token');
      const response = await this.userService.getAllUsers(token);
      if (response && response.statusCode === 200 && response.ourUsersList) {
        this.users = response.ourUsersList.map((user:any) => ({
          ...user,
          teamName: this.getTeamName(user.team) // Call getTeamName to fetch the team name for each user
        }));;

      } else {
        this.showError('No users found.');
      }
    } catch (error: any) {
      this.showError(error.message);
    }
  }

 

 
  // Load all projects and filter available ones
  loadProjects() {
    this.projectService.getAllProjects().subscribe({
      next: (response) => {
        if (response.statusCode === 200) {
          this.projects = response.projectsList || [];
          this.updateAvailableProjectsAndAirlines(); // Filter after loading
        } else {
          this.showError('Failed to load projects');
        }
      },
      error: (err) => this.showError('Error fetching projects: ' + err.message)
    });
  }



  // Update available projects and airlines based on team assignments
  updateAvailableProjectsAndAirlines() {
    // Filter projects that are not assigned to any team
    this.availableProjects = this.projects.filter(project => 
      !this.teams.some(team => team.project && team.project.id === project.id)
    );

  
  }
  
  
  
  
  
  // Add a method to count tasks for each team
  countTasksForTeam(teamId: number): void {
    this.taskService.getTasksByTeamId(teamId).subscribe(
      (response) => {
        if (response && response.tasks) {
          // Set the count of tasks for this team
          this.teamTaskCounts[teamId] = response.tasks.length;
        } else {
          this.teamTaskCounts[teamId] = 0;
        }
      },
      (error) => {
        console.error('Error retrieving tasks for team:', error);
        this.teamTaskCounts[teamId] = 0;
      }
    );
  }
  
  
 

  
  

  getAllTeams() {
    this.teamService.getAllTeams().subscribe(
      (response) => {
       // console.log('Teams:', response);  // Log the response to check
        this.teams = response.teamsList;  // Assign the teamsList array to the teams variable
        this.message = 'Teams retrieved successfully!';
        this.teams.forEach((team) => {
          // Fetch tasks for each team after retrieving the teams
          this.countTasksForTeam(team.id);
        });
        this.updateAvailableProjectsAndAirlines(); // Update available lists after fetching teams
      },
      (error) => {
        console.error('Error retrieving teams:', error);  // Log error if any
        this.message = 'Error retrieving teams';
      }
    );
  }

  loadTasks(): void {
    if (this.users.length === 0) {
      return;
    }
    const today = new Date().toISOString().split('T')[0];  // Get today's date in YYYY-MM-DD format

    this.taskService.getAllTasks().subscribe(
      (response) => {

        this.tasks = response.tasks.map((task: any) => {
          task.createdBy = this.getUserName(task.createdBy?.id);
          task.assignedTo = this.getUserName(task.assignedTo?.id);
          task.team = this.getTeamName(task.team);

          // Check if expiryDate is today and update the status to 'DELAYED'
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

  getTeamName(teamId: number | any): string {
    if (!teamId) return 'N/A';
    
    // Check if the teamId is an object or an ID (number)
    if (typeof teamId === 'object' && teamId.teamName) {
      return teamId.teamName; // Return the teamName if it's an object
    } else if (typeof teamId === 'number') {
      const team = this.teams.find((t) => t.id === teamId);
      return team ? team.teamName : 'Unknown'; // Return the team name from the teams list
    }
    
    return 'Unknown'; // Default return value if teamId is invalid
  }






  
  // Get the count of members in a team
  getMembersCount(teamId: number): number {
    return this.users.filter(user => {
      if (typeof user.team === 'object' && user.team !== null) {
        return user.team.id === teamId;
      } else if (typeof user.team === 'number') {
        return user.team === teamId;
      }
      return false;
    }).length;
  }








  // Fetch team members when clicking on a team
  openTeamModal(team: any) {
    this.selectedTeamName = team.teamName;
    this.teamService.getUsersByTeamId(team.id).subscribe(
      (response) => {
        this.selectedTeamMembers = response.members ?? [];
      },
      (error) => {
        this.selectedTeamMembers = [];
        this.showError('Error retrieving team members.');
      }
    );
  }
  openTaskModal(teamId: number, teamName: string) {
    this.taskService.getTasksByTeamId(teamId).subscribe(
      (response) => {
        if (response.statusCode === 200) {
          this.selectedTeamTasks = response.tasks ?? [];
          this.selectedTeamName = teamName;
        } else {
          this.selectedTeamTasks = [];
        }
      },
      (error) => {
        console.error('Error fetching team tasks:', error);
        this.selectedTeamTasks = [];
      }
    );
  }
  

 // Create a new team
 createTeam() {
  if (this.teamName) {
    const teamData = {
      teamName: this.teamName,
      projectId: this.projectId || undefined, // Send undefined if not selected
      airlineId: this.airlineId || undefined // Send undefined if not selected
    };
    this.teamService.createTeam(teamData).subscribe(
      (response) => {
        if (response.statusCode === 201) {
          this.message = 'Team created successfully!';
          this.getAllTeams(); // Refresh team list
          this.isCreatingTeam = false; // Close modal on success
          this.teamName = ''; // Reset form
          this.projectId = '';
          this.airlineId = '';
          this.showCreateTeamForm = false; // Hide form after creation
        } else {
          this.showError(response.message || 'Error creating team');
        }
      },
      (error) => {
        this.showError('Error creating team: ' + error.message);
      }
    );
  } else {
    this.showError('Team name is required');
  }
}
// Open create team modal
openCreateTeamModal() {
  this.isCreatingTeam = true;
  this.teamName = '';
  this.projectId = '';
  this.airlineId = '';
}
// Cancel create team
cancelCreateTeam() {
  this.isCreatingTeam = false;
  this.teamName = '';
  this.projectId = '';
  this.airlineId = '';
}

// Open Modify Team Modal
openModifyTeamModal(team: any) {
  this.isModifyingTeam = true;
  this.selectedTeam = team;
  this.modifyTeamName = team.teamName;
  this.modifyProjectId = team.project ? team.project.id : '';
  this.modifyAirlineId = team.airline ? team.airline.id : '';
}

// Cancel Modify Team
cancelModifyTeam() {
  this.isModifyingTeam = false;
  this.selectedTeam = null;
  this.modifyTeamName = '';
  this.modifyProjectId = '';
  this.modifyAirlineId = '';
}

// Modify an existing team
modifyTeam() {
  if (this.modifyTeamName && this.selectedTeam) {
    const teamData = {
      teamName: this.modifyTeamName,
      projectId: this.modifyProjectId || null, // Send null if not selected
      airlineId: this.modifyAirlineId || null // Send null if not selected
    };
    this.teamService.modifyTeam(this.selectedTeam.id, teamData).subscribe(
      (response) => {
        if (response.statusCode === 200) {
          this.message = 'Team modified successfully!';
          this.getAllTeams(); // Refresh team list
          this.isModifyingTeam = false; // Close modal on success
          this.selectedTeam = null;
          this.modifyTeamName = '';
          this.modifyProjectId = '';
          this.modifyAirlineId = '';
        } else {
          this.showError(response.message || 'Error modifying team');
        }
      },
      (error) => {
        this.showError('Error modifying team: ' + error.message);
      }
    );
  } else {
    this.showError('Team name is required');
  }
}

  // Assign a user to a team
  assignUserToTeam() {
    if (this.userId && this.teamId) {
      this.teamService.assignUserToTeam(this.userId, this.teamId).subscribe(
        (response) => {
          if (response.statusCode === 200) {
            this.message = 'User assigned to team successfully!';
            this.loadUsers(); // Refresh users list to remove assigned users
          } else {
            this.showError(response.message); // Display error message from the backend
          }
        },
        (error) => {
          this.showError('Error assigning user to team');
        }
      );
    }
  }
  

  // Unassign a user from a team
  unassignUserFromTeam() {
    if (this.userId) {
      this.teamService.unassignUserFromTeam(this.userId).subscribe(
        (response) => {
          this.message = 'User unassigned from team successfully!';
          this.loadUsers(); // Refresh users list to remove assigned users

        },
        (error) => {
          this.showError('Error unassigning user from team');
        }
      );
    }
  }

 

  // Delete a team
  deleteTeam() {
    if (this.teamId) {
      this.teamService.deleteTeam(this.teamId).subscribe(
        (response) => {
          this.message = 'Team deleted successfully!';

        },
        (error) => {
          this.message = 'Error deleting team';
        }
      );
    }
  }
// Delete a team
deleteTeam2(teamId: number) {
  if (confirm('Are you sure you want to delete this team?')) {
    this.teamService.deleteTeam(teamId).subscribe(
      (response) => {
        this.message = 'Team deleted successfully!';
        this.getAllTeams(); // Refresh the team list after deletion
        this.loadUsers(); // Refresh users list to remove assigned users

      },
      (error) => {
        this.message = 'Error deleting team';
      }
    );
  }
}
  // Get team by name
  getTeamByName() {
    if (this.teamName) {
      this.teamService.getTeamByName(this.teamName).subscribe(
        (response) => {
          this.message = 'Team found!';
        },
        (error) => {
          this.message = 'Team not found';
        }
      );
    }
  }


  viewTeamUsers(teamId: number, teamName: string) {
    this.teamService.getUsersByTeamId(teamId).subscribe(
      (response) => {
        if (response.statusCode === 200) {
          this.selectedTeamName = teamName;
          this.selectedTeamMembers = response.ourUsersList ?? []; // Use empty array if no members
        } else {
          this.selectedTeamMembers = []; // Set to empty array if no members
        }
      },
      (error) => {
        console.error('Error fetching team users:', error);
        this.selectedTeamMembers = []; // Set to empty array on error
      }
    );
  }

  
  showError(message: string) {
    this.errorMessage = message;
    setTimeout(() => {
      this.errorMessage = ''; // Clear the error message after the specified duration
    }, 3000);
  }
  getUserName(userId: number | any): string {
    if (!userId) return 'N/A';
    
    if (typeof userId === 'number') {
      const user = this.users.find((u) => u.id === userId);
      return user ? user.name : 'Unknown';
    } else if (userId && userId.name) {
      return userId.name;  // Directly return name if it's already an object with name
    }
  
    return 'Unknown';
  }
  toggleCreateTeamForm() {
    this.showCreateTeamForm = !this.showCreateTeamForm; // Toggle the form visibility
  }
}
