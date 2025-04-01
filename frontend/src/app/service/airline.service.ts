import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AirlineService {

  private apiUrl = 'http://localhost:1010/airlines'; // Adjust port if necessary

  constructor(private http: HttpClient) { }

  // Create a new airline
  createAirline(airline: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/create`, airline);
  }

  // Update an existing airline
  updateAirline(airlineId: string, airline: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/update/${airlineId}`, airline);
  }

  // Get all airlines
  getAllAirlines(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}`);
  }

  // Get a single airline by ID
  getAirlineById(airlineId: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${airlineId}`);
  }

  // Delete an airline
  deleteAirline(airlineId: string): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/delete/${airlineId}`);
  }

  // Archive an airline
  archiveAirline(airlineId: string): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/archive/${airlineId}`, {});
  }

  // Unarchive an airline
  unarchiveAirline(airlineId: string): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/unarchive/${airlineId}`, {});
  }
}