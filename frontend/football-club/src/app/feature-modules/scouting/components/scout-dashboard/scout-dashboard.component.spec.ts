import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ScoutDashboardComponent } from './scout-dashboard.component';

describe('ScoutDashboardComponent', () => {
  let component: ScoutDashboardComponent;
  let fixture: ComponentFixture<ScoutDashboardComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ScoutDashboardComponent]
    });
    fixture = TestBed.createComponent(ScoutDashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
