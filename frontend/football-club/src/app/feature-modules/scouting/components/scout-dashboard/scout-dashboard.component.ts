import { Component, OnInit } from '@angular/core';
import { CampaignService } from 'src/app/feature-modules/scouting/services/campaign.service';
import { ReportService } from 'src/app/feature-modules/scouting/services/report.service';
import { OnboardingService } from 'src/app/feature-modules/scouting/services/onboarding.service';
import { PlayerService } from 'src/app/feature-modules/scouting/services/player.service';
import { CampaignDetails, MonitoredPlayerBasic } from 'src/app/feature-modules/scouting/models/campaign.model';
import { UpcomingMatchTask } from 'src/app/feature-modules/scouting/models/report.model';

export interface CandidatePlayer {
  id: number;
  firstName: string;
  lastName: string;
  age: number;
  nationality: string;
  photo: string;
}

@Component({
  selector: 'app-scout-dashboard',
  templateUrl: './scout-dashboard.component.html',
  styleUrls: ['./scout-dashboard.component.css']
})
export class ScoutDashboardComponent implements OnInit {
  upcomingTasks: UpcomingMatchTask[] = [];
  campaigns: CampaignDetails[] = [];

  // Tasks Pagination
  taskPage: number = 1;
  taskPageSize: number = 3;
  
  // Pagination for campaigns' players
  pageMap: { [campaignId: number]: number } = {};
  pageSize: number = 5;

  // Onboarding Modal State
  isModalOpen: boolean = false;
  selectedCampaignId: number | null = null;
  
  // Search State
  searchMode: 'LOCAL' | 'API' = 'LOCAL';
  searchQuery: string = '';
  searchResults: CandidatePlayer[] = [];
  searchPage: number = 1;
  searchPageSize: number = 5;
  isSearching: boolean = false;

  // Single Player Selection
  selectedCandidate: CandidatePlayer | null = null;

  constructor(
    private reportService: ReportService,
    private campaignService: CampaignService,
    private onboardingService: OnboardingService,
    private playerService: PlayerService
  ) {}

  ngOnInit(): void {
    this.loadUpcomingTasks();
    this.loadCampaigns();
  }

  // --- Upcoming Tasks ---
  loadUpcomingTasks(): void {
    this.reportService.getUpcomingTasks().subscribe({
      next: (data) => {
        // Sort tasks by date ascending (closest matches first)
        this.upcomingTasks = data.sort((a, b) => new Date(a.matchDate).getTime() - new Date(b.matchDate).getTime());
        this.taskPage = 1; // Resetuj na prvu stranu pri učitavanju
      },
      error: (err) => console.error('Greška pri učitavanju zadataka', err)
    });
  }

  // --- Campaigns & Pagination ---
  loadCampaigns(): void {
    this.campaignService.getActiveCampaignsForScoutRegion().subscribe({
      next: (data) => {
        this.campaigns = data;
        this.campaigns.forEach(campaign => {
          this.pageMap[campaign.id] = 1;
        });
      },
      error: (err) => console.error('Greška pri učitavanju kampanja', err)
    });
  }

  get paginatedTasks(): UpcomingMatchTask[] {
    const startIndex = (this.taskPage - 1) * this.taskPageSize;
    return this.upcomingTasks.slice(startIndex, startIndex + this.taskPageSize);
  }

  get taskTotalPages(): number {
    return Math.ceil(this.upcomingTasks.length / this.taskPageSize) || 1;
  }

  nextTaskPage(): void {
    if (this.taskPage < this.taskTotalPages) {
      this.taskPage++;
    }
  }

  prevTaskPage(): void {
    if (this.taskPage > 1) {
      this.taskPage--;
    }
  }

  getPaginatedPlayers(campaignId: number, players: MonitoredPlayerBasic[]): MonitoredPlayerBasic[] {
    const currentPage = this.pageMap[campaignId] || 1;
    const startIndex = (currentPage - 1) * this.pageSize;
    return players.slice(startIndex, startIndex + this.pageSize);
  }

  getTotalPages(totalItems: number): number {
    return Math.ceil(totalItems / this.pageSize);
  }

  nextPage(campaignId: number, totalItems: number): void {
    if (this.pageMap[campaignId] < this.getTotalPages(totalItems)) {
      this.pageMap[campaignId]++;
    }
  }

  prevPage(campaignId: number): void {
    if (this.pageMap[campaignId] > 1) {
      this.pageMap[campaignId]--;
    }
  }

  // --- Onboarding Modal Logic ---
  openOnboardModal(campaignId: number): void {
    this.selectedCampaignId = campaignId;
    this.selectedCandidate = null;
    this.searchResults = [];
    this.searchQuery = '';
    this.searchPage = 1;
    this.isModalOpen = true;
  }

  closeModal(): void {
    this.isModalOpen = false;
    this.selectedCampaignId = null;
  }

  confirmOnboarding(): void {
    if (!this.selectedCandidate || !this.selectedCampaignId) return;

    const request = {
      apiPlayerId: this.selectedCandidate.id,
      campaignId: this.selectedCampaignId
    };

    this.onboardingService.onboardPlayerToCampaign(request).subscribe({
      next: () => {
        this.closeModal();
        this.loadCampaigns(); // Refresh campaigns to show the new player
      },
      error: (err) => console.error('Greška pri dodavanju igrača', err)
    });
  }

  // --- Search Logic ---
  setSearchMode(mode: 'LOCAL' | 'API'): void {
    this.searchMode = mode;
    this.searchResults = [];
    this.searchPage = 1;
  }

  executeSearch(): void {
    if (!this.searchQuery.trim()) return;
    this.isSearching = true;
    this.searchPage = 1;

    if (this.searchMode === 'LOCAL') {
      this.playerService.searchLocalPlayers(this.searchQuery).subscribe({
        next: (data) => {
          this.searchResults = data.map(p => ({
            id: p.id,
            firstName: p.name,
            lastName: p.surname,
            age: p.age,
            nationality: p.nationality,
            photo: p.photoUrl
          }));
          this.isSearching = false;
        },
        error: () => this.isSearching = false
      });
    } else {
      this.onboardingService.searchPlayers(this.searchQuery).subscribe({
        next: (data) => {
          this.searchResults = data.map(p => ({
            id: p.id,
            firstName: p.firstname || p.name,
            lastName: p.lastname || '',
            age: p.age,
            nationality: p.nationality,
            photo: p.photo
          }));
          this.isSearching = false;
        },
        error: () => this.isSearching = false
      });
    }
  }

  get paginatedSearchResults(): CandidatePlayer[] {
    const startIndex = (this.searchPage - 1) * this.searchPageSize;
    return this.searchResults.slice(startIndex, startIndex + this.searchPageSize);
  }

  get searchTotalPages(): number {
    return Math.ceil(this.searchResults.length / this.searchPageSize);
  }

  nextSearchPage(): void {
    if (this.searchPage < this.searchTotalPages) this.searchPage++;
  }

  prevSearchPage(): void {
    if (this.searchPage > 1) this.searchPage--;
  }

  selectCandidate(player: CandidatePlayer): void {
    this.selectedCandidate = player;
    this.searchResults = []; // Clear results to emphasize the selected player
    this.searchQuery = '';
  }

  removeSelectedCandidate(): void {
    this.selectedCandidate = null;
  }
}