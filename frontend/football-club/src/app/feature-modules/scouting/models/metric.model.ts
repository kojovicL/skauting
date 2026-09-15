export interface Metric {
  id: number;
  name: string;
  category: string;
  type: string;
}

export interface MetricSave {
  name: string;
  category: string;
  type: string;
}