import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CampaignService } from 'src/app/feature-modules/scouting/services/campaign.service';
import { OnboardingService } from 'src/app/feature-modules/scouting/services/onboarding.service';
import { PlayerService } from 'src/app/feature-modules/scouting/services/player.service';
import { CampaignDetails, MonitoredPlayerBasic, CampaignSave } from 'src/app/feature-modules/scouting/models/campaign.model';

export interface CandidatePlayer {
  id: number;
  firstName: string;
  lastName: string;
  age: number;
  nationality: string;
  photo: string;
}

@Component({
  selector: 'app-director-dashboard',
  templateUrl: './director-dashboard.component.html',
  styleUrls: ['./director-dashboard.component.css']
})
export class DirectorDashboardComponent implements OnInit {
  campaigns: CampaignDetails[] = [];
  pageMap: { [campaignId: number]: number } = {};
  pageSize: number = 5;

  // Modal & Form State
  isModalOpen: boolean = false;
  campaignForm!: FormGroup;
  
  // Enums for dropdowns
  positions = ['STRIKER', 'LW', 'RW', 'CAM', 'CM', 'CDM', 'LWB', 'RWB', 'LB', 'RB', 'CB', 'GK'];
  regions = ['EUROPE', 'SOUTH_AMERICA', 'NORTH_AMERICA', 'AFRICA', 'ASIA', 'OCEANIA', 'GLOBAL'];

  // Search State
  searchMode: 'LOCAL' | 'API' = 'LOCAL';
  searchQuery: string = '';
  searchResults: CandidatePlayer[] = [];
  searchPage: number = 1;
  searchPageSize: number = 5;
  isSearching: boolean = false;

  // Candidates State
  selectedCandidates: CandidatePlayer[] = [];
  isCandidatesCollapsed: boolean = false;

  constructor(
    private campaignService: CampaignService,
    private onboardingService: OnboardingService,
    private playerService: PlayerService,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.fetchActiveCampaigns();
    this.initForm();
  }

  initForm(): void {
    this.campaignForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3)]],
      description: ['', [Validators.required, Validators.minLength(3)]],
      targetPosition: ['', Validators.required],
      startDate: ['', Validators.required],
      endDate: ['', Validators.required],
      region: ['', Validators.required]
    });
  }

  fetchActiveCampaigns(): void {
    this.campaignService.getMyActiveCampaigns().subscribe({
      next: (data) => {
        this.campaigns = data;
        this.campaigns.forEach(campaign => {
          this.pageMap[campaign.id] = 1;
        });
      },
      error: (err) => console.error('Greška pri učitavanju kampanja', err)
    });
  }

  // Dashboard Pagination Methods
  getPaginatedPlayers(campaignId: number, players: MonitoredPlayerBasic[]): MonitoredPlayerBasic[] {
    const currentPage = this.pageMap[campaignId] || 1;
    const startIndex = (currentPage - 1) * this.pageSize;
    return players.slice(startIndex, startIndex + this.pageSize);
  }

  getTotalPages(totalItems: number): number {
    return Math.ceil(totalItems / this.pageSize);
  }

  nextPage(campaignId: number, totalItems: number): void {
    const maxPage = this.getTotalPages(totalItems);
    if (this.pageMap[campaignId] < maxPage) {
      this.pageMap[campaignId]++;
    }
  }

  prevPage(campaignId: number): void {
    if (this.pageMap[campaignId] > 1) {
      this.pageMap[campaignId]--;
    }
  }

  // --- Modal & Campaign Creation Logic ---
  openModal(): void {
    this.initForm();
    this.selectedCandidates = [];
    this.searchResults = [];
    this.searchQuery = '';
    this.searchPage = 1;
    this.isModalOpen = true;
  }

  closeModal(): void {
    this.isModalOpen = false;
  }

  createCampaign(): void {
    if (this.campaignForm.invalid) {
      this.campaignForm.markAllAsTouched();
      return;
    }

    const formValues = this.campaignForm.value;
    const newCampaign: CampaignSave = {
      name: formValues.name,
      description: formValues.description,
      targetPosition: formValues.targetPosition,
      startDate: formValues.startDate,
      endDate: formValues.endDate,
      region: formValues.region,
      candidateApiIds: this.selectedCandidates.map(c => c.id)
    };

    console.log('Kreiranje kampanje sa podacima:', newCampaign);

    this.campaignService.createCampaign(newCampaign).subscribe({
      next: () => {
        this.closeModal();
        this.fetchActiveCampaigns(); // Refresh the list
      },
      error: (err) => console.error('Greška pri kreiranju kampanje', err)
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

  // --- Search Pagination ---
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

  // --- Candidate Management ---
  addCandidate(player: CandidatePlayer): void {
    if (!this.selectedCandidates.some(c => c.id === player.id)) {
      this.selectedCandidates.push(player);
      this.searchResults = []
      this.searchQuery = ''
    }
  }

  removeCandidate(playerId: number): void {
    this.selectedCandidates = this.selectedCandidates.filter(c => c.id !== playerId);
  }

  toggleCandidatesCollapse(): void {
    this.isCandidatesCollapsed = !this.isCandidatesCollapsed;
  }
}