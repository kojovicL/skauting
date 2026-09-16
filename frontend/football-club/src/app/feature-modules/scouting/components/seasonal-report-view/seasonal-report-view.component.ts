import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ChartConfiguration } from 'chart.js';
import { SeasonalReportService } from '../../services/seasonal-report.service';
import { SeasonalReport, SeasonalValuedMetric } from '../../models/seasonal-report.model';

@Component({
  selector: 'app-seasonal-report-view',
  templateUrl: './seasonal-report-view.component.html',
  styleUrls: ['./seasonal-report-view.component.css']
})
export class SeasonalReportViewComponent implements OnInit {
  reportId!: number;
  report: SeasonalReport | null = null;
  isLoading: boolean = true;
  errorMessage: string = '';

  // --- Chart Data ---
  hasRadar: boolean = false;
  radarChartData!: ChartConfiguration<'radar'>['data'];
  radarChartOptions: ChartConfiguration<'radar'>['options'] = {
    responsive: true,
    scales: { r: { min: 0, max: 100, ticks: { stepSize: 20 } } },
    plugins: { tooltip: { callbacks: { label: (ctx) => `Percentil: ${ctx.raw}` } } }
  };

  barCharts: { categoryName: string, data: ChartConfiguration<'bar'>['data'], options: ChartConfiguration<'bar'>['options'] }[] = [];

  // Mapa za držanje pravih (apsolutnih) vrednosti za tooltip
  absoluteValuesMap: { [metricName: string]: number } = {};

  constructor(
    private route: ActivatedRoute,
    private seasonalReportService: SeasonalReportService
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.reportId = +id;
        this.loadReport();
      }
    });
  }

  loadReport(): void {
    this.seasonalReportService.getSeasonalReportById(this.reportId).subscribe({
      next: (data) => {
        this.report = data;
        this.processCharts(data);
        this.isLoading = false;
      },
      error: (err) => {
        console.error(err);
        this.errorMessage = 'Izveštaj nije pronađen.';
        this.isLoading = false;
      }
    });
  }

  processCharts(report: SeasonalReport): void {
    const validMetrics = report.metrics.filter(m => m.percentile !== null && m.percentile !== undefined);
    
    // Popuni mapu apsolutnih vrednosti
    validMetrics.forEach(m => this.absoluteValuesMap[m.metricName] = m.aggregatedValue);

    this.buildRadarChart(validMetrics, report.playerPosition);
    this.buildBarCharts(validMetrics);
  }

  buildRadarChart(metrics: SeasonalValuedMetric[], positionStr?: string): void {
    const macroCategory = this.getMacroCategory(positionStr);
    const keyMetrics = this.getKeyMetricsForCategory(macroCategory);

    const radarMetrics = metrics.filter(m => keyMetrics.includes(m.metricName));

    if (radarMetrics.length >= 3) {
      this.hasRadar = true;
      this.radarChartData = {
        labels: radarMetrics.map(m => m.metricName),
        datasets: [{
          label: 'Percentil igrača vs Liga',
          data: radarMetrics.map(m => m.percentile),
          backgroundColor: 'rgba(52, 152, 219, 0.4)',
          borderColor: 'rgba(52, 152, 219, 1)',
          pointBackgroundColor: 'rgba(41, 128, 185, 1)',
          fill: true
        }]
      };
    } else {
      this.hasRadar = false;
    }
  }

  buildBarCharts(metrics: SeasonalValuedMetric[]): void {
    const grouped = new Map<string, SeasonalValuedMetric[]>();
    metrics.forEach(m => {
      if (!grouped.has(m.category)) grouped.set(m.category, []);
      grouped.get(m.category)!.push(m);
    });

    this.barCharts = Array.from(grouped.entries()).map(([category, catMetrics]) => {
      // Sortiramo po percentilu opadajuće za lepši grafikon
      catMetrics.sort((a, b) => b.percentile - a.percentile);
      
      const colors = catMetrics.map(m => this.getPercentileColor(m.percentile));

      return {
        categoryName: this.formatCategoryName(category),
        data: {
          labels: catMetrics.map(m => m.metricName),
          datasets: [{
            label: 'Percentil',
            data: catMetrics.map(m => m.percentile),
            backgroundColor: colors,
            borderWidth: 1
          }]
        },
        options: {
          responsive: true,
          indexAxis: 'y', // Horizontalni bar chart
          scales: { x: { min: 0, max: 100 } },
          plugins: {
            legend: { display: false },
            tooltip: {
              callbacks: {
                label: (context) => {
                  const metricName = context.label;
                  const absVal = this.absoluteValuesMap[metricName];
                  return `Percentil: ${context.raw} (Učinak: ${absVal.toFixed(2)})`;
                }
              }
            }
          }
        }
      };
    });
  }

  // --- Pomocne metode ---
  getPercentileColor(percentile: number): string {
    if (percentile >= 75) return 'rgba(39, 174, 96, 0.8)'; // Zelena
    if (percentile >= 30) return 'rgba(149, 165, 166, 0.8)'; // Siva
    return 'rgba(231, 76, 60, 0.8)'; // Crvena
  }

  getMacroCategory(pos?: string): string {
    if (!pos) return 'MIDFIELDER';
    const defenders = ['CB', 'LB', 'RB', 'LWB', 'RWB'];
    const midfielders = ['CDM', 'CM', 'CAM', 'LM', 'RM'];
    const attackers = ['LW', 'RW', 'ST', 'CF'];
    
    if (pos === 'GK') return 'GOALKEEPER';
    if (defenders.includes(pos)) return 'DEFENDER';
    if (attackers.includes(pos)) return 'ATTACKER';
    return 'MIDFIELDER';
  }

  getKeyMetricsForCategory(category: string): string[] {
    const keys: { [key: string]: string[] } = {
      'ATTACKER': ['Shots Total', 'Shots On Target', 'Successful Dribbles', 'Big Chances Created', 'Expected Goals (xG)', 'Goals', 'Assists'],
      'MIDFIELDER': ['Pass Accuracy', 'Key Passes', 'Progressive Passes', 'Possession Won Final 3rd', 'Successful Dribbles', 'Total Passes'],
      'DEFENDER': ['Tackles', 'Interceptions', 'Clearances', 'Aerial Duels Won', 'Duels Won', 'Blocks'],
      'GOALKEEPER': ['Saves', 'Penalties Saved', 'Pass Accuracy', 'Goals Conceded']
    };
    return keys[category] || keys['MIDFIELDER'];
  }

  formatCategoryName(cat: string): string {
    const dict: any = {
      'PASSING_AND_PROGRESSION': 'Dodavanja i Progresija',
      'ATTACKING_AND_OUTPUT': 'Napad i Realizacija',
      'DEFENSIVE_ACTIONS': 'Defanzivne Akcije',
      'PHYSICAL': 'Fizičke Performanse',
      'IMPACT_AND_EFFICIENCY': 'Uticaj i Efikasnost'
    };
    return dict[cat] || cat;
  }
}