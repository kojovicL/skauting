export interface WeightedMetric {
  metricId: number;
  weight: number;
}

export interface RecommendationRequest {
  position: string;
  metricWeights: WeightedMetric[];
}

export interface PlayerRecommendation {
  playerId: number;
  name: string;
  surname: string;
  photoUrl?: string;
  score: number;
  source: string;
}