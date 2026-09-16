import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/env/environment';
import { SeasonalReport } from '../models/seasonal-report.model';

@Injectable({ providedIn: 'root' })
export class SeasonalReportService {
  private baseUrl = `${environment.apiHost}seasonal-reports`;
  constructor(private http: HttpClient) {}

  getSeasonalReportsByPlayer(playerId: number): Observable<SeasonalReport[]> {
    return this.http.get<SeasonalReport[]>(`${this.baseUrl}/player/${playerId}`);
  }

  getSeasonalReportById(id: number): Observable<SeasonalReport> {
    return this.http.get<SeasonalReport>(`${this.baseUrl}/${id}`);
  }
}