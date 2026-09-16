export interface MatchDraftInfo {
  matchId: number;
  date: string;
  status: string;
  homeTeamName: string;
  homeTeamLogo: string;
  homeGoals: number;
  awayTeamName: string;
  awayTeamLogo: string;
  awayGoals: number;
  leagueName: string;
  difficultyMultiplier: number;
}

export interface PlayerDraftInfo {
  playerId: number;
  name: string;
  surname: string;
  photoUrl: string;
  currentTeamName: string;
}

export interface ApiValuedMetric {
  metricId: number;
  metricName: string;
  value: number;
}

export interface ApiMatchStatSummary {
  minutesPlayed: number;
  shirtNumber: number;
  isSubstitute: boolean;
  isCaptain: boolean;
  goals: number;
  assists: number;
  rawRating: number;
  matchMetrics: ApiValuedMetric[];
}

export interface ReportDraftData {
  match: MatchDraftInfo;
  player: PlayerDraftInfo;
  apiStat: ApiMatchStatSummary;
  allSystemMetrics: SystemMetricDTO[];
}

export interface MetricFormItem {
  metricId: number;
  metricName: string;
  value: number;
  type: string;
  isApi: boolean;
}

export interface MetricCategoryGroup {
  categoryName: string;
  metrics: MetricFormItem[];
}

export interface ValuedMetric {
  id: number;
  reportId: number;
  metricId: number;
  metricName: string;
  type: string;
  value: number;
}

export interface ValuedMetricSave {
  metricId: number;
  value: number;
}

export interface SystemMetricDTO {
  id: number;
  name: string;
  category: string;
  type: string;
}

export interface Report {
  id: number;
  playerId: number;
  playerName: string;
  playerSurname: string;
  scoutId: number;
  scoutUsername: string;
  createdAt: string;
  overallCommentary: string;
  teamAtTimeId: number;
  teamAtTimeName: string;
  leagueMultiplierAtTime: number;
  matchId: number;
  homeTeamName: string;
  awayTeamName: string;
  minutesPlayed: number;
  shirtNumber: number;
  isSubstitute: boolean;
  isCaptain: boolean;
  goals: number;
  assists: number;
  rawRating: number;
  playerPhotoUrl?: string;
  seasonYear?: number;
  matchDate?: string;
  weightedRating: number;
  valuedMetrics: ValuedMetric[];
}

export interface ReportSave {
  playerId: number;
  matchId: number;
  overallCommentary: string;
  minutesPlayed: number;
  shirtNumber: number;
  isSubstitute: boolean;
  isCaptain: boolean;
  goals: number;
  assists: number;
  rawRating: number;
  metrics: ValuedMetricSave[];
}

export interface PendingReportMatch {
  matchId: number;
  matchDate: string;
  homeTeamName: string;
  homeTeamLogo: string;
  homeGoals: number;
  awayTeamName: string;
  awayTeamLogo: string;
  awayGoals: number;
  leagueName: string;
  playerId: number;
  playerName: string;
  playerSurname: string;
  playerPhotoUrl: string;
  playerPosition: string;
  minutesPlayed: number;
  rating: number;
}

export interface ScoutMatchPlayer {
  playerId: number;
  name: string;
  surname: string;
  photoUrl: string;
  teamName: string;
}

export interface UpcomingMatchTask {
  matchId: number;
  matchDate: string;
  homeTeamName: string;
  homeTeamLogo: string;
  awayTeamName: string;
  awayTeamLogo: string;
  leagueName: string;
  players: ScoutMatchPlayer[];
}