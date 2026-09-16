export interface TemplatePart {
  id: number;
  searchTemplateId: number;
  metricId: number;
  metricName: string;
  weight: number;
}

export interface TemplatePartSave {
  searchTemplateId?: number;
  metricId: number;
  weight: number;
}

export interface SearchTemplate {
  id: number;
  templateName: string;
  creatorId: number;
  creatorName: string;
  parts: TemplatePart[];
}

export interface SearchTemplateSave {
  templateName: string;
  parts: TemplatePartSave[];
}