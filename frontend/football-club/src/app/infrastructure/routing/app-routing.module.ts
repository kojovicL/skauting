import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { AuthGuard } from '../auth/auth.guard';
import { LoginComponent } from '../auth/components/login/login.component';
import { RegistrationComponent } from '../auth/components/registration/registration.component';

import { SearchTemplateManagementComponent } from 'src/app/feature-modules/scouting/components/search-template-management/search-template-management.component';
import { MetricsDashboardComponent } from 'src/app/feature-modules/scouting/components/metrics-dashboard/metrics-dashboard.component';
import { DirectorDashboardComponent } from 'src/app/feature-modules/scouting/components/director-dashboard/director-dashboard.component';
import { ScoutDashboardComponent } from 'src/app/feature-modules/scouting/components/scout-dashboard/scout-dashboard.component';
import { ScoutManagementComponent } from 'src/app/feature-modules/scouting/components/scout-management/scout-management.component';
import { ReportCreateComponent } from 'src/app/feature-modules/scouting/components/report-create/report-create.component';
import { MyReportsComponent } from 'src/app/feature-modules/scouting/components/my-reports/my-reports.component';

const routes: Routes = [
  { path: '',                     redirectTo: 'login', pathMatch: 'full' },

  // Auth
  { path: 'login',               component: LoginComponent },
  { path: 'register',            component: RegistrationComponent },

  // Skaut/Direktor

  // Skaut
  { path: 'scout-dashboard',        component: ScoutDashboardComponent },
  { path: 'reports/create', component: ReportCreateComponent },
  { path: 'my-reports', component: MyReportsComponent },

  // Direktor
  { path: 'director-dashboard',     component: DirectorDashboardComponent },
  { path: 'metrics-dashboard',      component: MetricsDashboardComponent },
  { path: 'search-templates',       component: SearchTemplateManagementComponent },
  { path: 'scout-management',        component: ScoutManagementComponent },

  { path: '**',                      redirectTo: 'matches' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
