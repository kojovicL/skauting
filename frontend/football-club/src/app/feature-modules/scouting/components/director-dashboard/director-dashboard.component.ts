import { Component, OnInit } from '@angular/core';
import { CampaignService } from 'src/app/feature-modules/scouting/services/campaign.service';
import { CampaignDetails, MonitoredPlayerBasic } from 'src/app/feature-modules/scouting/models/campaign.model';

@Component({
  selector: 'app-director-dashboard',
  templateUrl: './director-dashboard.component.html',
  styleUrls: ['./director-dashboard.component.css']
})
export class DirectorDashboardComponent implements OnInit {
  campaigns: CampaignDetails[] = [];
  
  // Maps campaign ID to its current pagination page
  pageMap: { [campaignId: number]: number } = {};
  pageSize: number = 5;

  constructor(private campaignService: CampaignService) {}

  ngOnInit(): void {
    this.fetchActiveCampaigns();
  }

  fetchActiveCampaigns(): void {
    this.campaignService.getMyActiveCampaigns().subscribe({
      next: (data) => {
        this.campaigns = data;
        // Initialize page 1 for every campaign
        this.campaigns.forEach(campaign => {
          this.pageMap[campaign.id] = 1;
        });
      },
      error: (err) => console.error('Failed to load campaigns', err)
    });
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
}