import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { MetricService } from '../../services/metric.service';
import { Metric } from '../../models/metric.model';
import { RecommendationService } from '../../services/recommendation.service';
import { SelectedMetric, PlayerRecommendation, RecommendationRequest } from '../../models/recommendation.model';
import { SearchTemplate, SearchTemplateSave } from '../../models/search-template.model';
import { SearchTemplateService } from '../../services/search-template.service';

@Component({
  selector: 'app-player-recommendation',
  templateUrl: './player-recommendation.component.html',
  styleUrls: ['./player-recommendation.component.css']
})
export class PlayerRecommendationComponent implements OnInit {
  allMetrics: Metric[] = [];
  
  // State
  selectedPosition: string = 'ST';
  selectedMetrics: SelectedMetric[] = [];
  recommendations: PlayerRecommendation[] = [];
  isLoadingRecommendations: boolean = false;

  // Modals
  showModal: boolean = false;
  showSaveTemplateModal: boolean = false;
  showLoadTemplateModal: boolean = false;
  
  // Templates
  newTemplateName: string = '';
  savedTemplates: SearchTemplate[] = [];

  // Pozicije koje backend prepoznaje (iz tvog Position enum-a)
  playerPositions = [
    { value: 'GK', label: 'Goalkeeper' },
    { value: 'CB', label: 'Center-Back' },
    { value: 'LB', label: 'Left-Back' },
    { value: 'RB', label: 'Right-Back' },
    { value: 'LWB', label: 'Left Wing-Back' },
    { value: 'RWB', label: 'Right Wing-Back' },
    { value: 'CDM', label: 'Defensive Midfielder' },
    { value: 'CM', label: 'Central Midfielder' },
    { value: 'CAM', label: 'Attacking Midfielder' },
    { value: 'LM', label: 'Left Midfielder' },
    { value: 'RM', label: 'Right Midfielder' },
    { value: 'LW', label: 'Left Winger' },
    { value: 'RW', label: 'Right Winger' },
    { value: 'ST', label: 'Striker' }
  ];

  constructor(
    private metricService: MetricService,
    private recommendationService: RecommendationService,
    private templateService: SearchTemplateService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.metricService.getAllMetrics().subscribe(metrics => {
      this.allMetrics = metrics;
    });
  }

  // --- Metrics Selection ---
  openModal(): void { this.showModal = true; }
  closeModal(): void { this.showModal = false; }

  selectMetric(metric: Metric): void {
    this.selectedMetrics.push({ metric, weight: 3 });
    this.closeModal();
  }

  removeMetric(metricId: number): void {
    this.selectedMetrics = this.selectedMetrics.filter(m => m.metric.id !== metricId);
  }

  getAvailableMetricsByCategory(): { [key: string]: Metric[] } {
    const available = this.allMetrics.filter(
      m => !this.selectedMetrics.some(sm => sm.metric.id === m.id)
    );
    
    return available.reduce((acc, metric) => {
      if (!acc[metric.category]) {
        acc[metric.category] = [];
      }
      acc[metric.category].push(metric);
      return acc;
    }, {} as { [key: string]: Metric[] });
  }

  getCategoryKeys(obj: object): string[] {
    return Object.keys(obj);
  }

  // --- Template Management ---
  openSaveTemplateModal(): void {
    this.newTemplateName = '';
    this.showSaveTemplateModal = true;
  }
  closeSaveTemplateModal(): void { this.showSaveTemplateModal = false; }

  saveTemplate(): void {
    if (!this.newTemplateName.trim() || this.selectedMetrics.length === 0) return;

    const templatePayload: SearchTemplateSave = {
      templateName: this.newTemplateName,
      parts: this.selectedMetrics.map(sm => ({
        metricId: sm.metric.id,
        weight: sm.weight
      }))
    };

    this.templateService.createTemplate(templatePayload).subscribe({
      next: () => {
        this.closeSaveTemplateModal();
      },
      error: (err) => console.error('Greška pri čuvanju šablona:', err)
    });
  }

  openLoadTemplateModal(): void {
    this.templateService.getMyTemplates().subscribe({
      next: (templates) => {
        this.savedTemplates = templates;
        this.showLoadTemplateModal = true;
      },
      error: (err) => console.error('Greška pri dobavljanju šablona:', err)
    });
  }
  closeLoadTemplateModal(): void { this.showLoadTemplateModal = false; }

  loadTemplate(template: SearchTemplate): void {
    const loadedMetrics: SelectedMetric[] = [];
    
    template.parts.forEach(part => {
      const fullMetric = this.allMetrics.find(m => m.id === part.metricId);
      if (fullMetric) {
        loadedMetrics.push({
          metric: fullMetric,
          weight: part.weight
        });
      }
    });

    this.selectedMetrics = loadedMetrics;
    this.closeLoadTemplateModal();
  }

  // --- Core Recommendation Logic ---
  submitRecommendation(): void {
    if (this.selectedMetrics.length === 0) return;
    
    this.isLoadingRecommendations = true;
    this.recommendations = [];

    const request: RecommendationRequest = {
      position: this.selectedPosition,
      metricWeights: this.selectedMetrics.map(sm => ({
        metricId: sm.metric.id, // NAPOMENA: Ispravljeno sa metricdId
        weight: sm.weight
      }))
    };

    this.recommendationService.getRecommendations(request).subscribe({
      next: (results) => {
        this.recommendations = results;
        this.isLoadingRecommendations = false;
      },
      error: (err) => {
        console.error(err);
        this.isLoadingRecommendations = false;
      }
    });
  }

  viewPlayer(playerId: number | undefined): void {
    if (playerId) {
      this.router.navigate(['/players', playerId]);
    }
  }

  formatCategoryName(category: string): string {
    const dict: any = {
      'PASSING_AND_PROGRESSION': 'Dodavanja i Progresija',
      'ATTACKING_AND_OUTPUT': 'Napad i Realizacija',
      'DEFENSIVE_ACTIONS': 'Defanzivne Akcije',
      'PHYSICAL': 'Fizičke Performanse',
      'IMPACT_AND_EFFICIENCY': 'Uticaj i Efikasnost',
      'SEASONAL_PERCENTILE': 'Mesečni presek (Percentili)',
      'CAMPAIGN_AVERAGE': 'Zbirni presek skauta'
    };
    return dict[category] || category;
  }
}