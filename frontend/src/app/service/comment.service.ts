import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class CommentService {

  private apiUrl = 'http://localhost:1010/comments'; // Adjust port if necessary

  constructor(private http: HttpClient) { }

  // Create a new comment
  createComment(commentData: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}`, commentData);
  }

  // Get all comments for a specific task
  getCommentsByTaskId(taskId: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/task/${taskId}`);
  }

  // Update an existing comment
  updateComment(commentId: number, commentData: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${commentId}`, commentData);
  }

  // Delete a comment
  deleteComment(commentId: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/${commentId}`);
  }
}