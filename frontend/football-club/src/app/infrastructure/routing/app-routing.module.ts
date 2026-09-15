import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { AuthGuard } from '../auth/auth.guard';
import { LoginComponent } from '../auth/components/login/login.component';
import { RegistrationComponent } from '../auth/components/registration/registration.component';

import { SearchTemplateManagementComponent } from 'src/app/feature-modules/scouting/components/search-template-management/search-template-management.component';
import { MetricsDashboardComponent } from 'src/app/feature-modules/scouting/components/metrics-dashboard/metrics-dashboard.component';

const routes: Routes = [
  { path: '',                     redirectTo: 'login', pathMatch: 'full' },

  // Auth
  { path: 'login',               component: LoginComponent },
  { path: 'register',            component: RegistrationComponent },

  // Skaut/Direktor

  // Skaut

  // Direktor
  { path: 'metrics-dashboard',      component: MetricsDashboardComponent },
  { path: 'search-templates',       component: SearchTemplateManagementComponent },

  { path: '**',                      redirectTo: 'matches' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
