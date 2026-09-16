import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/env/environment';
import { SearchTemplate, SearchTemplateSave } from '../models/search-template.model';

@Injectable({ providedIn: 'root' })
export class SearchTemplateService {
  private baseUrl = `${environment.apiHost}search-templates`;

  constructor(private http: HttpClient) {}

  createTemplate(dto: SearchTemplateSave): Observable<SearchTemplate> {
    return this.http.post<SearchTemplate>(this.baseUrl, dto);
  }

  getTemplateById(id: number): Observable<SearchTemplate> {
    return this.http.get<SearchTemplate>(`${this.baseUrl}/${id}`);
  }

  getAllTemplates(): Observable<SearchTemplate[]> {
    return this.http.get<SearchTemplate[]>(this.baseUrl);
  }

  updateTemplate(id: number, dto: SearchTemplateSave): Observable<SearchTemplate> {
    return this.http.put<SearchTemplate>(`${this.baseUrl}/${id}`, dto);
  }

  deleteTemplate(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  getMyTemplates(): Observable<SearchTemplate[]> {
    return this.http.get<SearchTemplate[]>(`${this.baseUrl}/my`);
  }

  getTemplatesByCreator(creatorId: number): Observable<SearchTemplate[]> {
    return this.http.get<SearchTemplate[]>(`${this.baseUrl}/creator/${creatorId}`);
  }
}