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
  activeTab: 'ACTIVE' | 'COMPLETED' = 'ACTIVE';
  campaigns: CampaignDetails[] = [];
  pageMap: { [campaignId: number]: number } = {};
  pageSize: number = 5;

  // Modal & Form State
  isModalOpen: boolean = false;
  isEditMode: boolean = false;
  editingCampaignId: number | null = null;
  campaignForm!: FormGroup;

  // Confirmation Modal State
  showConfirmModal: boolean = false;
  confirmAction: 'DELETE' | 'END' | null = null;
  selectedCampaignId: number | null = null;
  
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
    this.initForm();
    this.loadCampaigns();
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

  // --- Tab & Data Loading Logic ---
  switchTab(tab: 'ACTIVE' | 'COMPLETED'): void {
    this.activeTab = tab;
    this.loadCampaigns();
  }

  loadCampaigns(): void {
    if (this.activeTab === 'ACTIVE') {
      this.campaignService.getMyActiveCampaigns().subscribe({
        next: (data) => this.setupCampaigns(data),
        error: (err) => console.error('Greška pri učitavanju aktivnih kampanja', err)
      });
    } else {
      this.campaignService.getMyCompletedCampaigns().subscribe({
        next: (data) => this.setupCampaigns(data),
        error: (err) => console.error('Greška pri učitavanju završenih kampanja', err)
      });
    }
  }

  setupCampaigns(data: CampaignDetails[]): void {
    this.campaigns = data;
    this.campaigns.forEach(campaign => {
      this.pageMap[campaign.id] = 1;
    });
  }

  // --- Pagination Logic ---
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

  // --- Modal & Campaign Action Logic ---
  openCreateModal(): void {
    this.isEditMode = false;
    this.editingCampaignId = null;
    this.initForm();
    this.selectedCandidates = [];
    this.searchResults = [];
    this.searchQuery = '';
    this.searchPage = 1;
    this.isModalOpen = true;
  }

  openEditModal(campaign: CampaignDetails): void {
    this.isEditMode = true;
    this.editingCampaignId = campaign.id;
    
    this.campaignForm.patchValue({
      name: campaign.name,
      description: campaign.description,
      targetPosition: this.mapToPositionEnum(campaign.targetPosition),
      startDate: campaign.startDate,
      endDate: campaign.endDate,
      region: campaign.region || 'GLOBAL'
    });
    
    this.isModalOpen = true;
  }

  closeModal(): void {
    this.isModalOpen = false;
  }

  saveCampaign(): void {
    if (this.campaignForm.invalid) {
      this.campaignForm.markAllAsTouched();
      return;
    }

    const formValues = this.campaignForm.value;
    const campaignData: CampaignSave = {
      name: formValues.name,
      description: formValues.description,
      targetPosition: formValues.targetPosition,
      startDate: formValues.startDate,
      endDate: formValues.endDate,
      region: formValues.region,
      candidateApiIds: this.isEditMode ? [] : this.selectedCandidates.map(c => c.id)
    };

    if (this.isEditMode && this.editingCampaignId) {
      this.campaignService.updateCampaign(this.editingCampaignId, campaignData).subscribe({
        next: () => {
          this.closeModal();
          this.loadCampaigns();
        },
        error: (err) => console.error('Greška pri izmeni kampanje', err)
      });
    } else {
      this.campaignService.createCampaign(campaignData).subscribe({
        next: () => {
          this.closeModal();
          this.loadCampaigns();
        },
        error: (err) => console.error('Greška pri kreiranju kampanje', err)
      });
    }
  }

  // --- Confirmation Modals ---
  openConfirmModal(action: 'DELETE' | 'END', campaignId: number, event: Event): void {
    event.stopPropagation();
    this.confirmAction = action;
    this.selectedCampaignId = campaignId;
    this.showConfirmModal = true;
  }

  closeConfirmModal(): void {
    this.showConfirmModal = false;
    this.confirmAction = null;
    this.selectedCampaignId = null;
  }

  executeConfirmAction(): void {
    if (!this.selectedCampaignId || !this.confirmAction) return;

    if (this.confirmAction === 'DELETE') {
      this.campaignService.deleteCampaign(this.selectedCampaignId).subscribe({
        next: () => {
          this.closeConfirmModal();
          this.loadCampaigns();
        },
        error: (err) => console.error('Greška pri brisanju kampanje', err)
      });
    } else if (this.confirmAction === 'END') {
      this.campaignService.endCampaign(this.selectedCampaignId).subscribe({
        next: () => {
          this.closeConfirmModal();
          this.loadCampaigns();
        },
        error: (err) => console.error('Greška pri završavanju kampanje', err)
      });
    }
  }

  // --- Search Logic (Unchanged but adapted) ---
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

  addCandidate(player: CandidatePlayer): void {
    if (!this.selectedCandidates.some(c => c.id === player.id)) {
      this.selectedCandidates.push(player);
      this.searchResults = [];
      this.searchQuery = '';
    }
  }

  removeCandidate(playerId: number): void {
    this.selectedCandidates = this.selectedCandidates.filter(c => c.id !== playerId);
  }

  toggleCandidatesCollapse(): void {
    this.isCandidatesCollapsed = !this.isCandidatesCollapsed;
  }

  private mapToPositionEnum(displayString: string): string {
    const map: any = {
      'Striker': 'ST', 'Left Winger': 'LW', 'Right Winger': 'RW', 
      'Attacking Midfielder': 'CAM', 'Central Midfielder': 'CM', 
      'Defensive Midfielder': 'CDM', 'Left Wing-Back': 'LWB', 
      'Right Wing-Back': 'RWB', 'Left-Back': 'LB', 
      'Right-Back': 'RB', 'Center-Back': 'CB', 'Goalkeeper': 'GK'
    };
    return map[displayString] || displayString;
  }
}