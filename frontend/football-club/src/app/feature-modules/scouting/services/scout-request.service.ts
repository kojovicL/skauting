import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/env/environment';
import { ScoutRequest } from '../models/scout-request.model';

@Injectable({ providedIn: 'root' })
export class ScoutRequestService {
  private baseUrl = `${environment.apiHost}scout-requests`;

  constructor(private http: HttpClient) {}

  getPendingRequests(): Observable<ScoutRequest[]> {
    return this.http.get<ScoutRequest[]>(`${this.baseUrl}/pending`);
  }

  getRequestsByRegion(region: string): Observable<ScoutRequest[]> {
    return this.http.get<ScoutRequest[]>(`${this.baseUrl}/region/${region}`);
  }

  getAllRequests(): Observable<ScoutRequest[]> {
    return this.http.get<ScoutRequest[]>(this.baseUrl);
  }

  getRequestById(id: number): Observable<ScoutRequest> {
    return this.http.get<ScoutRequest>(`${this.baseUrl}/${id}`);
  }

  claimRequest(id: number): Observable<ScoutRequest> {
    return this.http.post<ScoutRequest>(`${this.baseUrl}/${id}/claim`, {});
  }

  deleteRequest(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}