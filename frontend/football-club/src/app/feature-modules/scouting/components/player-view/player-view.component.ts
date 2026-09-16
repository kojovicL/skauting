import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ChartConfiguration } from 'chart.js';
import { forkJoin } from 'rxjs';
import { PlayerService } from '../../services/player.service';
import { ContractService } from '../../services/contract.service';
import { ReportService } from '../../services/report.service';
import { SeasonalReportService } from '../../services/seasonal-report.service';
import { Contract } from '../../models/contract.model';
import { Report } from '../../models/report.model';
import { SeasonalReport, SeasonalValuedMetric } from '../../models/seasonal-report.model';

@Component({
  selector: 'app-player-view',
  templateUrl: './player-view.component.html',
  styleUrls: ['./player-view.component.css']
})
export class PlayerViewComponent implements OnInit {
  playerId!: number;
  playerDetails: any = null;
  contracts: Contract[] = [];
  recentMatches: any[] = [];
  isLoading: boolean = true;

  // Tabs & Reports Logic
  activeTab: 'MATCH' | 'SEASONAL' = 'MATCH';
  
  allMatchReports: Report[] = [];
  filteredMatchReports: Report[] = [];
  availableSeasons: number[] = [];
  selectedSeason: number | null = null;
  matchPage: number = 1;

  allSeasonalReports: SeasonalReport[] = [];
  filteredSeasonalReports: SeasonalReport[] = [];
  availableLeagues: { id: number, name: string }[] = [];
  selectedLeagueId: number | null = null;
  seasonalPage: number = 1;

  pageSize: number = 5;

  // Chart Data
  hasCharts: boolean = false;
  formChartData!: ChartConfiguration<'line'>['data'];
  progressChartData!: ChartConfiguration<'bar'>['data'];
  radarChartData!: ChartConfiguration<'radar'>['data'];
  outputChartData!: ChartConfiguration<'bar'>['data'];

  baseChartOptions: any = { responsive: true, maintainAspectRatio: false };
  radarChartOptions: any = { responsive: true, maintainAspectRatio: false, scales: { r: { min: 0, max: 100 } } };

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private playerService: PlayerService,
    private contractService: ContractService,
    private reportService: ReportService,
    private seasonalService: SeasonalReportService
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.playerId = +id;
        this.loadAllData();
      }
    });
  }

  loadAllData(): void {
    this.isLoading = true;
    forkJoin({
      details: this.playerService.getPlayerDetails(this.playerId),
      contracts: this.contractService.getPlayerContracts(this.playerId),
      recent: this.playerService.getRecentMatches(this.playerId),
      matchReports: this.reportService.getReportsByPlayer(this.playerId),
      seasonalReports: this.seasonalService.getSeasonalReportsByPlayer(this.playerId)
    }).subscribe({
      next: (data) => {
        this.playerDetails = data.details;
        this.contracts = data.contracts;
        this.recentMatches = data.recent;
        
        // Setup Match Reports
        this.allMatchReports = data.matchReports.sort((a, b) => new Date(b.matchDate!).getTime() - new Date(a.matchDate!).getTime());
        const seasons = new Set<number>();
        this.allMatchReports.forEach(r => { if (r.seasonYear) seasons.add(r.seasonYear); });
        this.availableSeasons = Array.from(seasons).sort((a, b) => b - a);
        if (this.availableSeasons.length > 0) {
          this.selectedSeason = this.availableSeasons[0];
          this.filterMatchReports();
        }

        // Setup Seasonal Reports
        this.allSeasonalReports = data.seasonalReports.sort((a, b) => b.seasonYear - a.seasonYear);
        const leaguesMap = new Map<number, string>();
        this.allSeasonalReports.forEach(r => leaguesMap.set(r.leagueId, r.leagueName));
        this.availableLeagues = Array.from(leaguesMap.entries()).map(([id, name]) => ({ id, name }));
        if (this.availableLeagues.length > 0) {
          this.selectedLeagueId = this.availableLeagues[0].id;
          this.filterSeasonalReports();
        }

        this.processCharts();
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Greška pri učitavanju profila', err);
        this.isLoading = false;
      }
    });
  }

  // --- Filtering & Pagination ---
  switchTab(tab: 'MATCH' | 'SEASONAL'): void { this.activeTab = tab; }

  filterMatchReports(): void {
    this.filteredMatchReports = this.allMatchReports.filter(r => r.seasonYear === this.selectedSeason);
    this.matchPage = 1;
  }
  get paginatedMatchReports() {
    const start = (this.matchPage - 1) * this.pageSize;
    return this.filteredMatchReports.slice(start, start + this.pageSize);
  }
  get matchTotalPages() { return Math.ceil(this.filteredMatchReports.length / this.pageSize) || 1; }

  filterSeasonalReports(): void {
    const targetLeagueId = Number(this.selectedLeagueId);
    this.filteredSeasonalReports = this.allSeasonalReports.filter(r => r.leagueId === targetLeagueId);
    this.seasonalPage = 1;
  }
  get paginatedSeasonalReports() {
    const start = (this.seasonalPage - 1) * this.pageSize;
    return this.filteredSeasonalReports.slice(start, start + this.pageSize);
  }
  get seasonalTotalPages() { return Math.ceil(this.filteredSeasonalReports.length / this.pageSize) || 1; }

  viewMatchReport(id: number) { this.router.navigate(['/reports/view', id]); }
  viewSeasonalReport(id: number) { this.router.navigate(['/seasonal-reports/view', id]); }

  // --- Charts Construction ---
  processCharts(): void {
    if (this.allMatchReports.length === 0 && this.allSeasonalReports.length === 0) {
      this.hasCharts = false;
      return;
    }
    this.hasCharts = true;

    // 1. Kriva forme (Line Chart)
    const formReports = [...this.allMatchReports].reverse(); // Hronološki rast
    this.formChartData = {
      labels: formReports.map(r => new Date(r.matchDate!).toLocaleDateString('sr-RS', { day: '2-digit', month: '2-digit' })),
      datasets: [{
        label: 'Ocena na meču',
        data: formReports.map(r => r.rawRating),
        borderColor: '#3498db',
        backgroundColor: 'rgba(52, 152, 219, 0.2)',
        fill: true,
        tension: 0.3
      }]
    };

    // 2. Godišnji napredak (Bar Chart)
    const seasonalAsc = [...this.allSeasonalReports].reverse(); // Hronološki
    this.progressChartData = {
      labels: seasonalAsc.map(r => `${r.seasonYear} (${r.leagueName})`),
      datasets: [{
        label: 'Prosečna težinska ocena',
        data: seasonalAsc.map(r => r.avgWeightedRating),
        backgroundColor: '#27ae60'
      }]
    };

    // 3. Učinak po 90 min (Grouped Bar Chart)
    this.outputChartData = {
      labels: seasonalAsc.map(r => r.seasonYear.toString()),
      datasets: [
        { label: 'Golovi / 90', data: seasonalAsc.map(r => r.goalsPer90), backgroundColor: '#3498db' },
        { label: 'Asistencije / 90', data: seasonalAsc.map(r => r.assistsPer90), backgroundColor: '#f39c12' }
      ]
    };

    // 4. Uporedni Profil (Overlapping Radar)
    this.buildRadarChart();
  }

  buildRadarChart(): void {
    if (this.allSeasonalReports.length === 0) return;
    
    // 1. Grupišemo izveštaje po godini i biramo onaj sa najviše odigranih minuta za tu godinu
    const reportsByYear = new Map<number, SeasonalReport>();
    this.allSeasonalReports.forEach(report => {
      const existing = reportsByYear.get(report.seasonYear);
      if (!existing || report.minutesPlayed > existing.minutesPlayed) {
        reportsByYear.set(report.seasonYear, report);
      }
    });

    // 2. Pretvaramo mapu nazad u niz, sortiramo opadajuće po godini i uzimamo prve dve različite sezone
    const topSeasons = Array.from(reportsByYear.values())
      .sort((a, b) => b.seasonYear - a.seasonYear)
      .slice(0, 2);

    const keyMetrics = this.getKeyMetrics(this.playerDetails.position);
    
    const datasets: any[] = [];
    const colors = [
      { border: '#3498db', bg: 'rgba(52, 152, 219, 0.4)' },
      { border: '#95a5a6', bg: 'rgba(149, 165, 166, 0.4)' }
    ];

    topSeasons.forEach((sr, index) => {
      const dataPoints = keyMetrics.map(km => {
        const metric = sr.metrics.find(m => m.metricName === km);
        return metric?.percentile || 0;
      });

      datasets.push({
        // Dodajemo i naziv lige u labelu radi još veće jasnoće na grafikonu
        label: `Sezona ${sr.seasonYear} (${sr.leagueName})`,
        data: dataPoints,
        borderColor: colors[index].border,
        backgroundColor: colors[index].bg,
        fill: true
      });
    });

    this.radarChartData = {
      labels: keyMetrics,
      datasets: datasets
    };
  }

  getKeyMetrics(pos: string): string[] {
    const defenders = ['CB', 'LB', 'RB', 'LWB', 'RWB'];
    const attackers = ['LW', 'RW', 'ST', 'CF'];
    if (pos === 'GK') return ['Saves', 'Penalties Saved', 'Pass Accuracy'];
    if (defenders.includes(pos)) return ['Tackles', 'Interceptions', 'Clearances', 'Aerial Duels Won', 'Duels Won'];
    if (attackers.includes(pos)) return ['Shots Total', 'Shots On Target', 'Successful Dribbles', 'Expected Goals (xG)', 'Dribble Attempts'];
    return ['Pass Accuracy', 'Key Passes', 'Progressive Passes', 'Possession Won Final 3rd', 'Successful Dribbles'];
  }
}