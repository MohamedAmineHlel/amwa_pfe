import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class TeamService {

  private apiUrl = "http://localhost:1010/teams";

  constructor(private http: HttpClient) { }

  // Create a new team
  createTeam(teamName: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/create`, { teamName });
  }

  // Assign a user to a team
  assignUserToTeam(userId: number, teamId: number): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/assign?userId=${userId}&teamId=${teamId}`, {});
  }

  unassignUserFromTeam(userId: number): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/unassign?userId=${userId}`, {});
  }

  // Get all teams
  getAllTeams(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/all`);
  }

  // Delete a team
  deleteTeam(teamId: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/delete?teamId=${teamId}`,{});
  }

  // Get team by name
  getTeamByName(teamName: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/name?teamName=${teamName}`);
  }
  // Fetch users by team ID
  getUsersByTeamId(teamId: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${teamId}/users`);
  }
}
