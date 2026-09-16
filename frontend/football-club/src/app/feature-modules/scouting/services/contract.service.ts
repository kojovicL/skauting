import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/env/environment';
import { Contract } from '../models/contract.model';

@Injectable({ providedIn: 'root' })
export class ContractService {
  private baseUrl = `${environment.apiHost}contracts`;

  constructor(private http: HttpClient) {}

  getPlayerContracts(playerId: number): Observable<Contract[]> {
    return this.http.get<Contract[]>(`${this.baseUrl}/player/${playerId}`);
  }
}