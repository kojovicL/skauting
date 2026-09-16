import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/env/environment';

export interface ScoutPerformance {
  id: number;
  name: string;
  surname: string;
  username: string;
  email: string;
  region: string | null;
  totalReports: number;
  activeMonitoredPlayers: number;
}

export interface ScoutActivePlayer {
  playerId: number;
  playerName: string;
  playerSurname: string;
  photoUrl: string;
  campaignName: string;
  currentTeamName: string;
}

@Injectable({ providedIn: 'root' })
export class ScoutManagementService {
  private baseUrl = `${environment.apiHost}scout-management`;

  constructor(private http: HttpClient) {}

  getScoutPerformances(): Observable<ScoutPerformance[]> {
    return this.http.get<ScoutPerformance[]>(`${this.baseUrl}/performances`);
  }

  getActivePlayersForScout(scoutId: number): Observable<ScoutActivePlayer[]> {
    return this.http.get<ScoutActivePlayer[]>(`${this.baseUrl}/scouts/${scoutId}/active-players`);
  }

  updateScoutRegion(scoutId: number, region: string): Observable<string> {
    let params = new HttpParams().set('region', region);
    return this.http.patch(`${this.baseUrl}/scouts/${scoutId}/region`, null, { 
      params: params,
      responseType: 'text' 
    });
  }
}