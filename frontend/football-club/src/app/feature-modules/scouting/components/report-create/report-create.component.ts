import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ReportService } from 'src/app/feature-modules/scouting/services/report.service';
import { ReportDraftData, ReportSave, MetricCategoryGroup, MetricFormItem } from 'src/app/feature-modules/scouting/models/report.model';

@Component({
  selector: 'app-report-create',
  templateUrl: './report-create.component.html',
  styleUrls: ['./report-create.component.css']
})
export class ReportCreateComponent implements OnInit {
  playerId!: number;
  matchId!: number;
  draftData: ReportDraftData | null = null;
  isLoading: boolean = true;
  errorMessage: string = '';

  groupedMetrics: MetricCategoryGroup[] = [];
  overallCommentary: string = '';
  isSaving: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private reportService: ReportService
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      if (params['playerId'] && params['matchId']) {
        this.playerId = +params['playerId'];
        this.matchId = +params['matchId'];
        this.loadDraftData();
      } else {
        this.errorMessage = 'Nedostaju podaci o igraču i utakmici.';
        this.isLoading = false;
      }
    });
  }

  loadDraftData(): void {
    this.reportService.getReportDraftData(this.playerId, this.matchId).subscribe({
      next: (data) => {
        this.draftData = data;
        
        // Brza mapa API vrednosti kako bismo znali šta je već popunjeno
        const apiValuesMap = new Map<number, number>();
        if (data.apiStat && data.apiStat.matchMetrics) {
          data.apiStat.matchMetrics.forEach(m => apiValuesMap.set(m.metricId, m.value));
        }

        // Grupisanje svih metrika po kategorijama
        const groupMap = new Map<string, MetricFormItem[]>();

        data.allSystemMetrics.forEach(sysMetric => {
          const isApiProvided = apiValuesMap.has(sysMetric.id);
          const metricValue = isApiProvided ? apiValuesMap.get(sysMetric.id)! : 5.0; // Default ocena 5.0 za ručne

          const item: MetricFormItem = {
            metricId: sysMetric.id,
            metricName: sysMetric.name,
            value: metricValue,
            type: sysMetric.type,
            isApi: isApiProvided
          };

          if (!groupMap.has(sysMetric.category)) {
            groupMap.set(sysMetric.category, []);
          }
          groupMap.get(sysMetric.category)!.push(item);
        });

        // Pretvaranje mape u niz radi lakše iteracije u HTML-u
        this.groupedMetrics = Array.from(groupMap.keys()).map(category => ({
          categoryName: this.formatCategoryName(category),
          metrics: groupMap.get(category)!
        }));

        this.isLoading = false;
      },
      error: (err) => {
        console.error('Greška pri učitavanju nacrta izveštaja', err);
        this.errorMessage = 'Došlo je do greške pri povezivanju sa serverom.';
        this.isLoading = false;
      }
    });
  }

  submitReport(): void {
    if (!this.overallCommentary || this.overallCommentary.trim().length < 5) {
      this.errorMessage = 'Taktički komentar mora imati bar 5 karaktera.';
      return;
    }

    this.isSaving = true;
    this.errorMessage = '';

    // "Razvijamo" (flatten) grupe nazad u jednu listu za slanje na backend
    const allMetricsToSave: { metricId: number, value: number }[] = [];
    this.groupedMetrics.forEach(group => {
      group.metrics.forEach(m => {
        allMetricsToSave.push({ metricId: m.metricId, value: m.value });
      });
    });

    const reportToSave: ReportSave = {
      playerId: this.playerId,
      matchId: this.matchId,
      overallCommentary: this.overallCommentary,
      minutesPlayed: this.draftData?.apiStat?.minutesPlayed || 0,
      shirtNumber: this.draftData?.apiStat?.shirtNumber || 0,
      isSubstitute: this.draftData?.apiStat?.isSubstitute || false,
      isCaptain: this.draftData?.apiStat?.isCaptain || false,
      goals: this.draftData?.apiStat?.goals || 0,
      assists: this.draftData?.apiStat?.assists || 0,
      rawRating: this.draftData?.apiStat?.rawRating || 0,
      metrics: allMetricsToSave
    };

    this.reportService.createReport(reportToSave).subscribe({
      next: () => {
        this.isSaving = false;
        this.router.navigate(['/scout-dashboard']);
      },
      error: (err) => {
        console.error('Greška pri čuvanju izveštaja', err);
        this.errorMessage = 'Greška pri čuvanju izveštaja. Pokušajte ponovo.';
        this.isSaving = false;
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/scout-dashboard']);
  }

  // Pomoćna funkcija za lepši ispis kategorija
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
}