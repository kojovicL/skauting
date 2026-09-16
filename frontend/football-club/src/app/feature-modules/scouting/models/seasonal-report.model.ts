export interface SeasonalValuedMetric {
  id: number;
  metricId: number;
  metricName: string;
  category: string;
  type: string;
  aggregatedValue: number;
  percentile: number;
}

export interface SeasonalReport {
  id: number;
  playerId: number;
  playerName: string;
  playerSurname: string;
  playerPhotoUrl?: string;
  playerPosition?: string;
  leagueId: number;
  leagueName: string;
  difficultyMultiplier: number;
  seasonYear: number;
  source: string;
  avgScoutScore: number;
  minutesPlayed: number;
  goalsPer90: number;
  assistsPer90: number;
  avgWeightedRating: number;
  seasonSummary?: string;
  metrics: SeasonalValuedMetric[];
}