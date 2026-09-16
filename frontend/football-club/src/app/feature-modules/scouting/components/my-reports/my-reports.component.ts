import { Component, OnInit } from '@angular/core';
import { ReportService } from 'src/app/feature-modules/scouting/services/report.service';
import { SeasonalReportService } from 'src/app/feature-modules/scouting/services/seasonal-report.service';
import { Report, PendingReportMatch } from 'src/app/feature-modules/scouting/models/report.model';
import { Router } from '@angular/router';

@Component({
  selector: 'app-my-reports',
  templateUrl: './my-reports.component.html',
  styleUrls: ['./my-reports.component.css']
})
export class MyReportsComponent implements OnInit {
  // Pending Tasks State
  pendingTasks: PendingReportMatch[] = [];
  taskPage: number = 1;
  taskPageSize: number = 3;

  // Global State
  scoutedPlayers: any[] = [];
  selectedPlayerId: number | null = null;
  selectedPlayerDetails: any = null;
  activeTab: 'MATCH' | 'SEASONAL' = 'MATCH';

  // Match Reports State
  allMyReports: Report[] = [];
  playerMatchReports: Report[] = [];
  availableSeasons: number[] = [];
  selectedSeason: number | null = null;
  filteredMatchReports: Report[] = [];

  // Seasonal Reports State
  playerSeasonalReports: any[] = [];
  availableLeagues: { id: number, name: string }[] = [];
  selectedLeagueId: number | null = null;
  filteredSeasonalReports: any[] = [];

  constructor(
    private reportService: ReportService,
    private seasonalReportService: SeasonalReportService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadPendingTasks();
    this.loadScoutedPlayers();
    this.loadAllMyReports();
  }

  // --- Pending Tasks Logic ---
  loadPendingTasks(): void {
    this.reportService.getPendingMatches().subscribe(data => {
      this.pendingTasks = data.sort((a, b) => new Date(a.matchDate).getTime() - new Date(b.matchDate).getTime());
    });
  }

  get paginatedPendingTasks(): PendingReportMatch[] {
    const start = (this.taskPage - 1) * this.taskPageSize;
    return this.pendingTasks.slice(start, start + this.taskPageSize);
  }

  get taskTotalPages(): number {
    return Math.ceil(this.pendingTasks.length / this.taskPageSize) || 1;
  }

  nextTaskPage() { if (this.taskPage < this.taskTotalPages) this.taskPage++; }
  prevTaskPage() { if (this.taskPage > 1) this.taskPage--; }

  // --- Players & Tabs Logic ---
  loadScoutedPlayers(): void {
    this.reportService.getMyScoutedPlayers().subscribe(data => {
      this.scoutedPlayers = data;
      if (data.length > 0) {
        this.onPlayerSelect(data[0].id); // Select first by default
      }
    });
  }

  loadAllMyReports(): void {
    this.reportService.getMyReports().subscribe(data => {
      this.allMyReports = data;
      if (this.selectedPlayerId) this.filterMatchReportsForPlayer();
    });
  }

  onPlayerSelect(playerId: number): void {
    this.selectedPlayerId = playerId;
    this.selectedPlayerDetails = this.scoutedPlayers.find(p => p.id == playerId);
    
    // Reset Data
    this.filterMatchReportsForPlayer();
    this.loadSeasonalReportsForPlayer(playerId);
  }

  switchTab(tab: 'MATCH' | 'SEASONAL'): void {
    this.activeTab = tab;
  }

  // --- Match Reports Tab Logic ---
  filterMatchReportsForPlayer(): void {
    if (!this.selectedPlayerId) return;
    this.playerMatchReports = this.allMyReports.filter(r => r.playerId == this.selectedPlayerId);
    
    // Extract unique seasons
    const seasons = new Set<number>();
    this.playerMatchReports.forEach(r => { if (r.seasonYear) seasons.add(r.seasonYear); });
    this.availableSeasons = Array.from(seasons).sort((a, b) => b - a); // Descending

    if (this.availableSeasons.length > 0) {
      this.selectedSeason = this.availableSeasons[0];
      this.onSeasonChange();
    } else {
      this.filteredMatchReports = [];
    }
  }

  onSeasonChange(): void {
    if (!this.selectedSeason) return;
    this.filteredMatchReports = this.playerMatchReports
      .filter(r => r.seasonYear == this.selectedSeason)
      .sort((a, b) => new Date(b.matchDate!).getTime() - new Date(a.matchDate!).getTime());
  }

  // --- Seasonal Reports Tab Logic ---
  loadSeasonalReportsForPlayer(playerId: number): void {
    this.seasonalReportService.getSeasonalReportsByPlayer(playerId).subscribe(data => {
      this.playerSeasonalReports = data;
      
      // Extract unique leagues
      const leaguesMap = new Map<number, string>();
      data.forEach(r => leaguesMap.set(r.leagueId, r.leagueName));
      this.availableLeagues = Array.from(leaguesMap.entries()).map(([id, name]) => ({ id, name }));

      if (this.availableLeagues.length > 0) {
        this.selectedLeagueId = this.availableLeagues[0].id;
        this.onLeagueChange();
      } else {
        this.filteredSeasonalReports = [];
      }
    });
  }

  onLeagueChange(): void {
    if (!this.selectedLeagueId) return;
    this.filteredSeasonalReports = this.playerSeasonalReports
      .filter(r => r.leagueId == this.selectedLeagueId)
      .sort((a, b) => b.seasonYear - a.seasonYear); // Descending year
  }

  translateSource(source: string): string {
    if (source === 'API_HISTORICAL') return 'Istorijski Podaci (API)';
    if (source === 'INTERNAL_CALCULATED') return 'Lokalno Izračunato (Skaut)';
    return source;
  }

  // Navigacija (Rute će se implementirati kasnije)
  viewMatchReport(reportId: number) { this.router.navigate(['/reports/view', reportId]); }
  viewSeasonalReport(reportId: number) { this.router.navigate(['/seasonal-reports/view', reportId]); }
}