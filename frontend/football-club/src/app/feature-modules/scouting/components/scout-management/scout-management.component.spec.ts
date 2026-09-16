import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ScoutManagementComponent } from './scout-management.component';

describe('ScoutManagementComponent', () => {
  let component: ScoutManagementComponent;
  let fixture: ComponentFixture<ScoutManagementComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ScoutManagementComponent]
    });
    fixture = TestBed.createComponent(ScoutManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
