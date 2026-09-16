import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/env/environment';
import { Metric, MetricSave } from '../models/metric.model';

@Injectable({ providedIn: 'root' })
export class MetricService {
  private baseUrl = `${environment.apiHost}metrics`;

  constructor(private http: HttpClient) {}

  createMetric(dto: MetricSave): Observable<Metric> {
    return this.http.post<Metric>(this.baseUrl, dto);
  }

  getMetricById(id: number): Observable<Metric> {
    return this.http.get<Metric>(`${this.baseUrl}/${id}`);
  }

  getAllMetrics(): Observable<Metric[]> {
    return this.http.get<Metric[]>(this.baseUrl);
  }

  updateMetric(id: number, dto: MetricSave): Observable<Metric> {
    return this.http.put<Metric>(`${this.baseUrl}/${id}`, dto);
  }

  deleteMetric(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}