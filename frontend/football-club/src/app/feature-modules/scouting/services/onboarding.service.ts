import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/env/environment';
import { ApiPlayer, OnboardPlayerRequest } from '../models/onboarding.model';

@Injectable({ providedIn: 'root' })
export class OnboardingService {
  private baseUrl = `${environment.apiHost}onboarding`;

  constructor(private http: HttpClient) {}

  searchPlayers(name: string): Observable<ApiPlayer[]> {
    let params = new HttpParams().set('name', name);
    return this.http.get<ApiPlayer[]>(`${this.baseUrl}/search`, { params });
  }

  onboardPlayerToCampaign(request: OnboardPlayerRequest): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/add`, request);
  }
}