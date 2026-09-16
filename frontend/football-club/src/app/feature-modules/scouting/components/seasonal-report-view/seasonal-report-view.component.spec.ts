import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SeasonalReportViewComponent } from './seasonal-report-view.component';

describe('SeasonalReportViewComponent', () => {
  let component: SeasonalReportViewComponent;
  let fixture: ComponentFixture<SeasonalReportViewComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [SeasonalReportViewComponent]
    });
    fixture = TestBed.createComponent(SeasonalReportViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
