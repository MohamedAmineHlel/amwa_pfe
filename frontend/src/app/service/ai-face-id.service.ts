import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
@Injectable({
  providedIn: 'root'
})
export class AiFaceIdService {

  private apiUrl = 'http://127.0.0.1:5000/';

  constructor(private http: HttpClient) { }
  faceLogin(formData: FormData): Observable<FormData> {
    return this.http.post<any>(`${this.apiUrl}/upload`, formData);
  }
}
