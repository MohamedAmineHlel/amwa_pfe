import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';import { Observable } from 'rxjs';
@Injectable({
  providedIn: 'root'
})
export class ProjectService {

  private apiUrl = 'http://localhost:1010/projects'; // Adjust port if necessary

  constructor(private http: HttpClient) { }

  // Create a new project
  createProject(project: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/create`, project);
  }

  // Update an existing project
  updateProject(projectId: string, project: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/update/${projectId}`, project);
  }

  // Get all projects
  getAllProjects(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}`);
  }

  // Get a single project by ID
  getProjectById(projectId: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${projectId}`);
  }

  // Delete a project
  deleteProject(projectId: string): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/delete/${projectId}`);
  }

  // Archive a project
  archiveProject(projectId: string): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/archive/${projectId}`, {});
  }

  // Unarchive a project
  unarchiveProject(projectId: string): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/unarchive/${projectId}`, {});
  }
}
