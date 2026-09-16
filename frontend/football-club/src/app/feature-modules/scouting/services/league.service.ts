import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/env/environment';
import { League, LeagueSave } from '../models/league.model';

@Injectable({ providedIn: 'root' })
export class LeagueService {
  private baseUrl = `${environment.apiHost}leagues`;

  constructor(private http: HttpClient) {}

  createLeague(dto: LeagueSave): Observable<League> {
    return this.http.post<League>(this.baseUrl, dto);
  }

  getLeagueById(id: number): Observable<League> {
    return this.http.get<League>(`${this.baseUrl}/${id}`);
  }

  getAllLeagues(): Observable<League[]> {
    return this.http.get<League[]>(this.baseUrl);
  }

  updateLeague(id: number, dto: LeagueSave): Observable<League> {
    return this.http.put<League>(`${this.baseUrl}/${id}`, dto);
  }

  deleteLeague(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  addClubToLeague(leagueId: number, clubId: number): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${leagueId}/clubs/${clubId}`, {});
  }

  updateMultipliers(multipliers: { [id: number]: number }): Observable<void> {
    return this.http.patch<void>(`${this.baseUrl}/bulk-multipliers`, multipliers);
  }
}