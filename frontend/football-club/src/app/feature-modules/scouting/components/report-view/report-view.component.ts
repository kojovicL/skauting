import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ReportService } from '../../services/report.service';
import { AuthService } from '../../../../infrastructure/auth/auth.service';
import { Report, ReportSave, ValuedMetric } from '../../models/report.model';

export interface MetricCategoryGroup {
  categoryName: string;
  metrics: ValuedMetric[];
}

@Component({
  selector: 'app-report-view',
  templateUrl: './report-view.component.html',
  styleUrls: ['./report-view.component.css']
})
export class ReportViewComponent implements OnInit {
  reportId!: number;
  report: Report | null = null;
  isLoading: boolean = true;
  errorMessage: string = '';

  // Access Control
  currentUserId: number | null = null;
  isOwner: boolean = false;

  // Edit State
  isEditing: boolean = false;
  editData: Partial<Report> = {};
  isSaving: boolean = false;

  // Groups
  groupedMetrics: MetricCategoryGroup[] = [];

  // Delete Modal
  showDeleteConfirm: boolean = false;
  isDeleting: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private reportService: ReportService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.authService.checkIfUserExists();
    this.currentUserId = this.authService.user$.getValue()?.id || null;
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.reportId = +id;
        this.loadReport();
      }
    });
  }

  loadReport(): void {
    this.isLoading = true;
    this.reportService.getReportById(this.reportId).subscribe({
      next: (data) => {
        this.report = data;
        this.isOwner = this.currentUserId === data.scoutId;
        this.groupMetrics(this.report.valuedMetrics);
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Greška pri učitavanju izveštaja', err);
        this.errorMessage = 'Izveštaj nije pronađen ili nemate pravo pristupa.';
        this.isLoading = false;
      }
    });
  }

  groupMetrics(metrics: ValuedMetric[]): void {
    const groupMap = new Map<string, ValuedMetric[]>();
    metrics.forEach(metric => {
      const cat = metric.category || 'UNCATEGORIZED';
      if (!groupMap.has(cat)) groupMap.set(cat, []);
      groupMap.get(cat)!.push(metric);
    });

    this.groupedMetrics = Array.from(groupMap.keys()).map(cat => ({
      categoryName: this.formatCategoryName(cat),
      metrics: groupMap.get(cat)!
    }));
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

  // --- EDIT LOGIC ---
  startEdit(): void {
    this.isEditing = true;
    // Deep clone za edit formu
    this.editData = JSON.parse(JSON.stringify(this.report));
    // Regrupišemo metrike iz editData kako bi se menjale referencirane vrednosti
    this.groupMetrics(this.editData.valuedMetrics as ValuedMetric[]);
  }

  cancelEdit(): void {
    this.isEditing = false;
    this.editData = {};
    this.errorMessage = '';
    // Vraćamo originalne metrike u prikaz
    this.groupMetrics(this.report!.valuedMetrics);
  }

  saveEdit(): void {
    if (!this.validateForm()) return;

    this.isSaving = true;
    const payload: ReportSave = {
      playerId: this.editData.playerId!,
      matchId: this.editData.matchId!,
      overallCommentary: this.editData.overallCommentary!,
      minutesPlayed: this.editData.minutesPlayed!,
      shirtNumber: this.editData.shirtNumber!,
      isSubstitute: this.editData.isSubstitute!,
      isCaptain: this.editData.isCaptain!,
      goals: this.editData.goals!,
      assists: this.editData.assists!,
      rawRating: this.editData.rawRating!,
      metrics: this.editData.valuedMetrics!.map(m => ({ metricId: m.metricId, value: m.value }))
    };

    this.reportService.updateReport(this.reportId, payload).subscribe({
      next: (updatedReport) => {
        this.report = updatedReport;
        this.isEditing = false;
        this.isSaving = false;
        this.groupMetrics(this.report.valuedMetrics);
      },
      error: (err) => {
        console.error('Greška pri čuvanju izmena', err);
        this.errorMessage = 'Došlo je do greške prilikom čuvanja izmena.';
        this.isSaving = false;
      }
    });
  }

  validateForm(): boolean {
    if (
      this.editData.minutesPlayed === null || this.editData.minutesPlayed === undefined ||
      this.editData.shirtNumber === null || this.editData.shirtNumber === undefined ||
      this.editData.goals === null || this.editData.goals === undefined ||
      this.editData.assists === null || this.editData.assists === undefined ||
      this.editData.rawRating === null || this.editData.rawRating === undefined
    ) {
      this.errorMessage = 'Sva osnovna polja (minuti, broj, golovi, asistencije, ocena) su obavezna.';
      return false;
    }
    if (!this.editData.overallCommentary || this.editData.overallCommentary.trim().length < 5) {
      this.errorMessage = 'Taktički komentar mora imati bar 5 karaktera.';
      return false;
    }
    this.errorMessage = '';
    return true;
  }

  // --- DELETE LOGIC ---
  openDeleteConfirm(): void { this.showDeleteConfirm = true; }
  closeDeleteConfirm(): void { this.showDeleteConfirm = false; }

  confirmDelete(): void {
    this.isDeleting = true;
    this.reportService.deleteReport(this.reportId).subscribe({
      next: () => {
        this.isDeleting = false;
        this.closeDeleteConfirm();
        this.router.navigate(['/my-reports']);
      },
      error: (err) => {
        console.error('Greška pri brisanju', err);
        this.isDeleting = false;
        this.closeDeleteConfirm();
      }
    });
  }
}