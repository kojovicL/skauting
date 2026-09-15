import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MetricsDashboardComponent } from './components/metrics-dashboard/metrics-dashboard.component';
import { SearchTemplateManagementComponent } from './components/search-template-management/search-template-management.component';



@NgModule({
  declarations: [
    MetricsDashboardComponent,
    SearchTemplateManagementComponent,
  ],
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    FormsModule
  ]
})
export class ScoutingModule { }
