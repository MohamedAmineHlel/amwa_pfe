import { Component, OnInit, Pipe, PipeTransform } from '@angular/core';
import { ProjectService } from '../service/project.service'; // Adjust path as needed
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Pipe({
  name: 'filter',
  standalone: true
})
export class FilterPipe implements PipeTransform {
  transform(items: any[], filter: any): any[] {
    if (!items || !filter) {
      return items;
    }
    return items.filter(item => item.archived === filter.archived);
  }
}

@Component({
  selector: 'app-project-management',
  standalone: true,
  imports: [FormsModule, CommonModule, FilterPipe],
  templateUrl: './project-management.component.html',
  styleUrls: ['./project-management.component.css']
})
export class ProjectManagementComponent implements OnInit {
  projects: any[] = [];
  newProject: any = { nameProject: '', descriptionProject: '', abbrProject: '' };
  updateProjectData: any = { name: '', description: '', abbr: '' };
  errorMessage: string = '';
  isModifying: boolean = false;
  isCreating: boolean = false;
  isViewingArchived: boolean = false;
  selectedProjectId: string | null = null;

  constructor(private projectService: ProjectService) {}

  ngOnInit() {
    this.loadAllProjects();
  }

  // Load all projects
  loadAllProjects() {
    this.projectService.getAllProjects().subscribe({
      next: (response) => {
        if (response.statusCode === 200) {
          this.projects = response.projectsList || [];
          this.errorMessage = '';
        } else {
          this.errorMessage = response.error || 'Failed to load projects';
        }
      },
      error: (err) => {
        this.errorMessage = 'Error fetching projects: ' + err.message;
      }
    });
  }

  // Open create modal
  openCreateModal() {
    this.isCreating = true;
    this.newProject = { nameProject: '', descriptionProject: '', abbrProject: '' };
  }

  // Create a new project
  createProject() {
    this.projectService.createProject(this.newProject).subscribe({
      next: (response) => {
        if (response.statusCode === 201) {
          this.loadAllProjects();

          this.projects.push(response.project);
          this.isCreating = false;
          this.errorMessage = '';
        } else {
          this.errorMessage = response.error || 'Failed to create project';
        }
      },
      error: (err) => {
        this.errorMessage = 'Error creating project: ' + err.message;
      }
    });
  }

  // Cancel create
  cancelCreate() {
    this.isCreating = false;
    this.newProject = { nameProject: '', descriptionProject: '', abbrProject: '' };
  }

  // Select a project for updating
  selectProjectForUpdate(projectId: string) {
    this.projectService.getProjectById(projectId).subscribe({
      next: (response) => {
        if (response.statusCode === 200) {
          this.selectedProjectId = projectId;
          this.updateProjectData = {
            name: response.project.name,
            description: response.project.description,
            abbr: response.project.abbr
          };
          this.isModifying = true;
          this.errorMessage = '';
        } else {
          this.errorMessage = response.error || 'Project not found';
        }
      },
      error: (err) => {
        this.errorMessage = 'Error fetching project: ' + err.message;
      }
    });
  }

  // Update a project
  updateProject(projectId: string | null) {
    if (!projectId) {
      this.errorMessage = 'No project selected for update';
      return;
    }
    const updatePayload = {
      nameProject: this.updateProjectData.name,
      descriptionProject: this.updateProjectData.description,
      abbrProject: this.updateProjectData.abbr
    };
    this.projectService.updateProject(projectId, updatePayload).subscribe({
      next: (response) => {
        if (response.statusCode === 200) {
          this.loadAllProjects();

          const index = this.projects.findIndex(p => p.id === projectId);
          if (index !== -1) {
            this.projects[index] = response.project;
          }
          this.isModifying = false;
          this.selectedProjectId = null;
          this.errorMessage = '';
        } else {
          this.errorMessage = response.error || 'Failed to update project';
        }
      },
      error: (err) => {
        this.errorMessage = 'Error updating project: ' + err.message;
      }
    });
  }

  // Cancel update
  cancelUpdate() {
    this.isModifying = false;
    this.selectedProjectId = null;
    this.updateProjectData = { name: '', description: '', abbr: '' };
  }

  // Delete a project
  deleteProject(projectId: string) {
    this.projectService.deleteProject(projectId).subscribe({
      next: (response) => {
        if (response.statusCode === 200) {
          this.projects = this.projects.filter(p => p.id !== projectId);
          if (this.selectedProjectId === projectId) {
            this.isModifying = false;
            this.selectedProjectId = null;
          }
          this.errorMessage = '';
        } else {
          this.errorMessage = response.error || 'Failed to delete project';
        }
      },
      error: (err) => {
        this.errorMessage = 'Error deleting project: ' + err.message;
      }
    });
  }

  // Archive a project
  archiveProject(projectId: string) {
    this.projectService.archiveProject(projectId).subscribe({
      next: (response) => {
        if (response.statusCode === 200) {
          this.loadAllProjects();
          const index = this.projects.findIndex(p => p.id === projectId);
          if (index !== -1) {
            this.projects[index] = response.project;
          }
          if (this.selectedProjectId === projectId) {
            this.updateProjectData = { ...response.project };
          }
          this.errorMessage = '';
        } else {
          this.errorMessage = response.error || 'Failed to archive project';
        }
      },
      error: (err) => {
        this.errorMessage = 'Error archiving project: ' + err.message;
      }
    });
  }

  // Unarchive a project
  unarchiveProject(projectId: string) {
    this.projectService.unarchiveProject(projectId).subscribe({
      next: (response) => {
        if (response.statusCode === 200) {
          this.loadAllProjects();
          const index = this.projects.findIndex(p => p.id === projectId);
          if (index !== -1) {
            this.projects[index] = response.project;
          }
          if (this.selectedProjectId === projectId) {
            this.updateProjectData = { ...response.project };
          }
          this.errorMessage = '';
        } else {
          this.errorMessage = response.error || 'Failed to unarchive project';
        }
      },
      error: (err) => {
        this.errorMessage = 'Error unarchiving project: ' + err.message;
      }
    });
  }

  // Open archived projects modal
  openArchivedModal() {
    this.isViewingArchived = true;
  }

  // Close archived projects modal
  closeArchivedModal() {
    this.isViewingArchived = false;
  }
}