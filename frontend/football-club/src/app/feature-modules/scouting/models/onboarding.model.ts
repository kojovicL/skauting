export interface OnboardPlayerRequest {
  apiPlayerId: number;
  campaignId: number;
  season?: number;
}

// Represents the external API-Football player response structure
export interface ApiPlayer {
  id: number;
  name: string;
  firstname: string;
  lastname: string;
  age: number;
  nationality: string;
  photo: string;
}