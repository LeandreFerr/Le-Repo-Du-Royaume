import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PlaceDuMarche } from './place-du-marche.component';

describe('PlaceDuMarche', () => {
  let component: PlaceDuMarche;
  let fixture: ComponentFixture<PlaceDuMarche>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PlaceDuMarche],
    }).compileComponents();

    fixture = TestBed.createComponent(PlaceDuMarche);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
