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



@NgModule({
  declarations: [
    MetricsDashboardComponent,
    SearchTemplateManagementComponent,
    DirectorDashboardComponent,
    ScoutDashboardComponent,
    ScoutManagementComponent,
    ReportCreateComponent,
  ],
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    FormsModule
  ]
})
export class ScoutingModule { }
