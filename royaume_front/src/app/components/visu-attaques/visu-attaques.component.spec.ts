import { ComponentFixture, TestBed } from '@angular/core/testing';
import { VisuAttaques } from './visu-attaques.component';

describe('VisuAttaques', () => {
  let component: VisuAttaques;
  let fixture: ComponentFixture<VisuAttaques>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VisuAttaques],
    }).compileComponents();

    fixture = TestBed.createComponent(VisuAttaques);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
