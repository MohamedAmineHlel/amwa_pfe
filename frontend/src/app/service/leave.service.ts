import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Leave } from '../models/leave.model';
import { catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';
@Injectable({
  providedIn: 'root'
})
export class LeaveService {

  private apiUrl ='http://localhost:1010/leaves';

  constructor(private http: HttpClient) { }

 /**
   * Récupérer les congés de l'utilisateur connecté
   */
 getUserLeaves(token: string): Observable<Leave[]> {
  const headers = new HttpHeaders({
    Authorization: `Bearer ${token}`,
  });

  return this.http.get<Leave[]>(`${this.apiUrl}/user`, { headers });
}

   /**
   * Créer une nouvelle demande de congé
   */
   createLeave(newLeave: Leave, token: any): Observable<Leave> {
    const headers = new HttpHeaders({
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json',
    });
  
    const body = {
      startDate: newLeave.startDate,
      endDate: newLeave.endDate,
      description: newLeave.description,
    };
  
    return this.http.post<Leave>(`${this.apiUrl}/create`, body, { headers })
      .pipe(
        catchError(error => {
          console.error("Erreur lors de la requête :", error);
          return throwError(() => error);
        })
      );
  }
  /**
 * Mettre à jour le statut d'un congé
 */
  updateLeave(leaveId: number, leave: Leave, token: string): Observable<Leave> {
    const headers = new HttpHeaders({
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json',
    });
  
    const body = {
      startDate: leave.startDate,
      endDate: leave.endDate,
      description: leave.description,
    };
  
    return this.http.put<Leave>(`${this.apiUrl}/update/${leaveId}`, body, { headers })
      .pipe(
        catchError(error => {
          console.error("Erreur lors de la mise à jour du statut du congé :", error);
          return throwError(() => error);
        })
      );
  }
  /**
   * Supprimer une demande de congé 
   */
  deleteLeave(leaveId: number, token: string): Observable<void> {
    const headers = new HttpHeaders({
      Authorization: `Bearer ${token}`,
    });

    return this.http.delete<void>(`${this.apiUrl}/delete/${leaveId}`, { headers });
  }

   /**
   * Récupérer les congés en fonction du rôle de l'utilisateur connecté
   */
   getRoleBasedLeaves(token: string): Observable<Leave[]> {
    const headers = new HttpHeaders({
      Authorization: `Bearer ${token}`,
    });

    return this.http.get<Leave[]>(`${this.apiUrl}/role-based-leaves`, { headers })
      .pipe(
        catchError(error => {
          console.error("Erreur lors de la récupération des congés par rôle:", error);
          return throwError(() => error);
        })
      );
  }

  updateLeaveStatus1(leaveId: number, status: string, token: string) {
    const body = { status: status };
    console.log("Token envoyé:", token);  // Vérifie le token dans la console
    console.log("Requête envoyée:", body); // Vérifie le format du body
  
    const headers = new HttpHeaders({
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json',
    });
  
    return this.http.put<Leave>(`${this.apiUrl}/status/${leaveId}`, body, { headers });
  }
  updateLeaveStatus(leaveId: number, status: string, token: string) {
    const headers = { Authorization: `Bearer ${token}` };
    return this.http.put<Leave>(`${this.apiUrl}/status/${leaveId}?status=${status}`, {}, { headers });
  }
}