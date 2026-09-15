import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/env/environment';
import { TemplatePart, TemplatePartSave } from '../models/search-template.model';

@Injectable({ providedIn: 'root' })
export class TemplatePartService {
  private baseUrl = `${environment.apiHost}template-parts`;

  constructor(private http: HttpClient) {}

  createTemplatePart(dto: TemplatePartSave): Observable<TemplatePart> {
    return this.http.post<TemplatePart>(this.baseUrl, dto);
  }

  getTemplatePartById(id: number): Observable<TemplatePart> {
    return this.http.get<TemplatePart>(`${this.baseUrl}/${id}`);
  }

  getAllTemplateParts(): Observable<TemplatePart[]> {
    return this.http.get<TemplatePart[]>(this.baseUrl);
  }

  updateTemplatePart(id: number, dto: TemplatePartSave): Observable<TemplatePart> {
    return this.http.put<TemplatePart>(`${this.baseUrl}/${id}`, dto);
  }

  deleteTemplatePart(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  getPartsByTemplate(templateId: number): Observable<TemplatePart[]> {
    return this.http.get<TemplatePart[]>(`${this.baseUrl}/template/${templateId}`);
  }
}