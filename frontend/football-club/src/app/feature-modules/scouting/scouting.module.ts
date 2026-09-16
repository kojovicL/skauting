import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MetricsDashboardComponent } from './components/metrics-dashboard/metrics-dashboard.component';
import { SearchTemplateManagementComponent } from './components/search-template-management/search-template-management.component';
import { DirectorDashboardComponent } from './components/director-dashboard/director-dashboard.component';
import { ScoutDashboardComponent } from './components/scout-dashboard/scout-dashboard.component';
import { ScoutManagementComponent } from './components/scout-management/scout-management.component';
import { ReportCreateComponent } from './components/report-create/report-create.component';
import { MyReportsComponent } from './components/my-reports/my-reports.component';
import { PlayerRecommendationComponent } from './components/player-recommendation/player-recommendation.component';
import { ReportViewComponent } from './components/report-view/report-view.component';
import { SeasonalReportViewComponent } from './components/seasonal-report-view/seasonal-report-view.component';
import { NgChartsModule } from 'ng2-charts';
import { MarkdownModule } from 'ngx-markdown';
import { PlayerViewComponent } from './components/player-view/player-view.component';



@NgModule({
  declarations: [
    MetricsDashboardComponent,
    SearchTemplateManagementComponent,
    DirectorDashboardComponent,
    ScoutDashboardComponent,
    ScoutManagementComponent,
    ReportCreateComponent,
    MyReportsComponent,
    PlayerRecommendationComponent,
    ReportViewComponent,
    SeasonalReportViewComponent,
    PlayerViewComponent,
  ],
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    FormsModule,
    NgChartsModule,
    MarkdownModule.forChild()
  ]
})
export class ScoutingModule { }
