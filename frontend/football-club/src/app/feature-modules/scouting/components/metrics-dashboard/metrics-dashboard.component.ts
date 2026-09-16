import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MetricService } from '../../services/metric.service';
import { Metric } from '../../models/metric.model';

@Component({
  selector: 'app-metrics-dashboard',
  templateUrl: './metrics-dashboard.component.html',
  styleUrls: ['./metrics-dashboard.component.css']
})
export class MetricsDashboardComponent implements OnInit {
  metricsByCategory: { [category: string]: Metric[] } = {};
  categories: string[] = [];
  isLoading = true;
  
  isModalOpen = false;
  isSubmitting = false;
  metricForm!: FormGroup;
  selectedMetricId: number | null = null;

  constructor(
    private metricService: MetricService,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadMetrics();
  }

  private initForm(): void {
    this.metricForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3)]],
      category: [{ value: '', disabled: true }, Validators.required],
      type: ['', Validators.required]
    });
  }

  loadMetrics(): void {
    this.isLoading = true;
    this.metricService.getAllMetrics().subscribe({
      next: (metrics) => {
        this.groupMetrics(metrics);
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Failed to load metrics', err);
        this.isLoading = false;
      }
    });
  }

  private groupMetrics(metrics: Metric[]): void {
    this.metricsByCategory = {};

    this.categories = [
      "PASSING_AND_PROGRESSION",
      "ATTACKING_AND_OUTPUT",
      "DEFENSIVE_ACTIONS",
      "PHYSICAL",
      "IMPACT_AND_EFFICIENCY"
    ];

    this.categories.forEach(category => {
      this.metricsByCategory[category] = [];
    });
    
    metrics.forEach(metric => {
      const cat = metric.category || 'UNCATEGORIZED';
      if (this.metricsByCategory[cat]) {
        this.metricsByCategory[cat].push(metric);
      }
    });
  }

  openModal(category: string): void {
    this.selectedMetricId = null;
    this.isModalOpen = true;
    this.metricForm.patchValue({
      name: '',
      category: category,
      type: ''
    });
  }

  openEditModal(metric: Metric): void {
    this.selectedMetricId = metric.id;
    this.isModalOpen = true;
    this.metricForm.patchValue({
      name: metric.name,
      category: metric.category,
      type: metric.type
    });
  }

  closeModal(): void {
    this.isModalOpen = false;
    this.metricForm.reset();
  }

  onSubmit(): void {
    if (this.metricForm.invalid) return;

    this.isSubmitting = true;
    const payload = this.metricForm.getRawValue();

    const request = this.selectedMetricId 
      ? this.metricService.updateMetric(this.selectedMetricId, payload)
      : this.metricService.createMetric(payload);

    request.subscribe({
      next: () => {
        this.isSubmitting = false;
        this.closeModal();
        this.loadMetrics();
      },
      error: (err) => {
        console.error('Greška pri čuvanju metrike', err);
        this.isSubmitting = false;
      }
    });
  }

  formatCategoryName(category: string): string {
    const dict: any = {
      'PASSING_AND_PROGRESSION': 'Dodavanja i Progresija',
      'ATTACKING_AND_OUTPUT': 'Napad i Realizacija',
      'DEFENSIVE_ACTIONS': 'Defanzivne Akcije',
      'PHYSICAL': 'Fizičke Performanse',
      'IMPACT_AND_EFFICIENCY': 'Uticaj i Efikasnost'
    };
    return dict[category] || category;
  }

  formatTypeName(type: string): string {
    if (!type) return '';
    switch (type.toUpperCase()) {
      case 'POSITIVE': return 'Pozitivna';
      case 'NEGATIVE': return 'Negativna';
      case 'NEUTRAL': return 'Neutralna';
      default: return type;
    }
  }
}