import { Component, OnInit } from '@angular/core';
import { ScoutManagementService, ScoutPerformance, ScoutActivePlayer } from 'src/app/feature-modules/scouting/services/scout-management.service';
import { ReportService } from 'src/app/feature-modules/scouting/services/report.service';
import { Report } from 'src/app/feature-modules/scouting/models/report.model';

@Component({
  selector: 'app-scout-management',
  templateUrl: './scout-management.component.html',
  styleUrls: ['./scout-management.component.css']
})
export class ScoutManagementComponent implements OnInit {
  scouts: ScoutPerformance[] = [];
  filteredScouts: ScoutPerformance[] = [];

  // Filters
  searchQuery: string = '';
  selectedRegionFilter: string = 'ALL';
  regions = ['AFRICA', 'ASIA', 'EUROPE', 'NORTH_AMERICA', 'OCEANIA', 'SOUTH_AMERICA', 'GLOBAL'];

  // Drawer State
  isDrawerOpen: boolean = false;
  selectedScout: ScoutPerformance | null = null;
  selectedScoutReports: Report[] = [];
  selectedScoutPlayers: ScoutActivePlayer[] = [];
  drawerTab: 'PLAYERS' | 'REPORTS' = 'PLAYERS';

  constructor(
    private scoutManagementService: ScoutManagementService,
    private reportService: ReportService
  ) {}

  ngOnInit(): void {
    this.loadScouts();
  }

  loadScouts(): void {
    this.scoutManagementService.getScoutPerformances().subscribe({
      next: (data) => {
        this.scouts = data;
        this.applyFilters();
      },
      error: (err) => console.error('Greška pri učitavanju skauta', err)
    });
  }

  // --- Filters & KPIs ---
  applyFilters(): void {
    const q = this.searchQuery.toLowerCase();
    this.filteredScouts = this.scouts.filter(s => {
      const matchesSearch = (s.name?.toLowerCase().includes(q)) || 
                            (s.surname?.toLowerCase().includes(q)) || 
                            (s.username?.toLowerCase().includes(q));
      const matchesRegion = this.selectedRegionFilter === 'ALL' || 
                            (this.selectedRegionFilter === 'UNASSIGNED' ? !s.region : s.region === this.selectedRegionFilter);
      return matchesSearch && matchesRegion;
    });
  }

  get totalScouts(): number { return this.scouts.length; }
  get unassignedScouts(): number { return this.scouts.filter(s => !s.region).length; }
  get totalReports(): number { return this.scouts.reduce((acc, s) => acc + s.totalReports, 0); }

  get topRegion(): string {
    const counts = this.scouts.reduce((acc, s) => {
      if (s.region) acc[s.region] = (acc[s.region] || 0) + 1;
      return acc;
    }, {} as any);
    const sorted = Object.entries(counts).sort((a: any, b: any) => b[1] - a[1]);
    return sorted.length > 0 ? `${sorted[0][0]} (${sorted[0][1]})` : 'N/A';
  }

  // --- Region Management ---
  onRegionChange(scout: ScoutPerformance, newRegion: string): void {
    this.scoutManagementService.updateScoutRegion(scout.id, newRegion).subscribe({
      next: () => {
        scout.region = newRegion;
        // Optionally show a toast success message here
      },
      error: (err) => console.error('Greška pri izmeni regiona', err)
    });
  }

  // --- Drawer Management ---
  openDrawer(scout: ScoutPerformance): void {
    this.selectedScout = scout;
    this.isDrawerOpen = true;
    this.drawerTab = 'PLAYERS';
    this.selectedScoutReports = [];
    this.selectedScoutPlayers = [];

    // Load active players
    this.scoutManagementService.getActivePlayersForScout(scout.id).subscribe(data => {
      this.selectedScoutPlayers = data;
    });

    // Load recent reports
    this.reportService.getReportsByScout(scout.id).subscribe(data => {
      // Sort to show latest first
      this.selectedScoutReports = data.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
    });
  }

  closeDrawer(): void {
    this.isDrawerOpen = false;
    this.selectedScout = null;
  }
}