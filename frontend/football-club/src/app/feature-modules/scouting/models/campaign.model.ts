export interface MonitoredPlayerBasic {
  playerId: number;
  name: string;
  surname: string;
  photoUrl: string;
  currentTeamName: string;
  age: number;
}

export interface CampaignDetails {
  id: number;
  name: string;
  description: string;
  targetPosition: string;
  status: string;
  startDate: string;
  endDate: string;
  directorId: number;
  region: string;
  monitoredPlayers: MonitoredPlayerBasic[];
}

export interface CampaignSave {
  name: string;
  description: string;
  targetPosition?: string;
  startDate?: string;
  endDate?: string;
  region?: string;
  candidateApiIds?: number[];
}