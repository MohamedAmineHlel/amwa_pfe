import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class TaskService {

  private apiUrl = 'http://localhost:1010/tasks';

  constructor(private http: HttpClient) { }

  // Create a new task
  createTask(task: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}`, task);
  }

  // Assign a task to a team
  assignTaskToTeam(taskId: number, teamId: number): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/${taskId}/assign/${teamId}`, {});
  }

  // Unassign a task from a team
  unassignTaskFromTeam(taskId: number): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/${taskId}/unassign`, {});
  }

  // Get all tasks
  getAllTasks(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}`);
  }

  // Get task by ID
  getTaskById(taskId: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${taskId}`);
  }

  // Delete a task
  deleteTask(taskId: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/${taskId}`);
  }

  // Get tasks by team ID
  getTasksByTeamId(teamId: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/team/${teamId}`);
  }
  updateTaskExpiry(taskId: number, expiryDate: string): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${taskId}/updateExpiry`, { expiryDate });
  }

  modifyTask(taskId: number, task: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${taskId}`, task);
  }

  // Upload attachments for a task
  uploadTaskAttachments(taskId: number, files: File[]): Observable<any> {
    const formData = new FormData();
    
    // Append each file to the FormData object with the key 'files'
    files.forEach(file => {
      formData.append('files', file, file.name);
    });

    return this.http.post<any>(`${this.apiUrl}/${taskId}/attachments`, formData);
  }
  // Updated method to include taskId
  downloadAttachment(taskId: number, filename: string): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${taskId}/attachments/${encodeURIComponent(filename)}`, {
      responseType: 'blob'
    });
  }
  // Delete an attachment from a task
  deleteTaskAttachment(taskId: number, filename: string): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/${taskId}/attachments/${filename}`);
  }
  
}
