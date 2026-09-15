import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/env/environment';
import { CampaignDetails, CampaignSave } from '../models/campaign.model';
import { PlayerRecommendation } from '../models/recommendation.model';

@Injectable({ providedIn: 'root' })
export class CampaignService {
  private baseUrl = `${environment.apiHost}campaigns`;

  constructor(private http: HttpClient) {}

  createCampaign(campaign: CampaignSave): Observable<void> {
    return this.http.post<void>(this.baseUrl, campaign);
  }

  getCampaignDetails(id: number): Observable<CampaignDetails> {
    return this.http.get<CampaignDetails>(`${this.baseUrl}/${id}/details`);
  }

  getCampaignRecommendations(id: number): Observable<PlayerRecommendation[]> {
    return this.http.get<PlayerRecommendation[]>(`${this.baseUrl}/${id}/recommendations`);
  }
}