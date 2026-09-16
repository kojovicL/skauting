import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/env/environment';
import { PlayerRecommendation, RecommendationRequest } from '../models/recommendation.model';

@Injectable({ providedIn: 'root' })
export class RecommendationService {
  private baseUrl = `${environment.apiHost}recommendations`;

  constructor(private http: HttpClient) {}

  getRecommendations(request: RecommendationRequest): Observable<PlayerRecommendation[]> {
    return this.http.post<PlayerRecommendation[]>(this.baseUrl, request);
  }
}