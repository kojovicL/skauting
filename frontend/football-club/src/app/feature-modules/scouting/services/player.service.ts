import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/env/environment';

export interface LocalPlayerSearch {
  id: number;
  name: string;
  surname: string;
  age: number;
  nationality: string;
  photoUrl: string;
  position: string;
}

@Injectable({ providedIn: 'root' })
export class PlayerService {
  private baseUrl = `${environment.apiHost}players`;

  constructor(private http: HttpClient) {}

  searchLocalPlayers(query: string): Observable<LocalPlayerSearch[]> {
    let params = new HttpParams().set('query', query);
    return this.http.get<LocalPlayerSearch[]>(`${this.baseUrl}/search`, { params });
  }

  getPlayerDetails(id: number): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/${id}/details`);
  }

  getRecentMatches(id: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/${id}/recent-matches`);
  }
}