import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PlayerRecommendationComponent } from './player-recommendation.component';

describe('PlayerRecommendationComponent', () => {
  let component: PlayerRecommendationComponent;
  let fixture: ComponentFixture<PlayerRecommendationComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [PlayerRecommendationComponent]
    });
    fixture = TestBed.createComponent(PlayerRecommendationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
