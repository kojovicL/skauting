import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/env/environment';
import { 
  PendingReportMatch, 
  Report, 
  ReportDraftData, 
  ReportSave, 
  UpcomingMatchTask 
} from '../models/report.model';

@Injectable({ providedIn: 'root' })
export class ReportService {
  private baseUrl = `${environment.apiHost}reports`;

  constructor(private http: HttpClient) {}

  getReportDraftData(playerId: number, matchId: number): Observable<ReportDraftData> {
    let params = new HttpParams()
      .set('playerId', playerId.toString())
      .set('matchId', matchId.toString());
    return this.http.get<ReportDraftData>(`${this.baseUrl}/draft-data`, { params });
  }

  createReport(dto: ReportSave): Observable<Report> {
    return this.http.post<Report>(this.baseUrl, dto);
  }

  getReportById(id: number): Observable<Report> {
    return this.http.get<Report>(`${this.baseUrl}/${id}`);
  }

  getAllReports(): Observable<Report[]> {
    return this.http.get<Report[]>(this.baseUrl);
  }

  updateReport(id: number, dto: ReportSave): Observable<Report> {
    return this.http.put<Report>(`${this.baseUrl}/${id}`, dto);
  }

  deleteReport(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  getMyReports(): Observable<Report[]> {
    return this.http.get<Report[]>(`${this.baseUrl}/my`);
  }

  getReportsByScout(scoutId: number): Observable<Report[]> {
    return this.http.get<Report[]>(`${this.baseUrl}/scout/${scoutId}`);
  }

  getReportsByPlayer(playerId: number): Observable<Report[]> {
    return this.http.get<Report[]>(`${this.baseUrl}/player/${playerId}`);
  }

  getLatestReportForPlayer(playerId: number): Observable<Report> {
    return this.http.get<Report>(`${this.baseUrl}/player/${playerId}/latest`);
  }

  getPendingMatches(): Observable<PendingReportMatch[]> {
    return this.http.get<PendingReportMatch[]>(`${this.baseUrl}/pending-matches`);
  }

  getUpcomingTasks(): Observable<UpcomingMatchTask[]> {
    return this.http.get<UpcomingMatchTask[]>(`${this.baseUrl}/upcoming-tasks`);
  }

  getMyScoutedPlayers(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/my/players`);
  }
}