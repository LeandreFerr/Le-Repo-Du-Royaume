import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PlaceDuMarcheComponent } from './place-du-marche.component';

describe('PlaceDuMarcheComponent', () => {
  let component: PlaceDuMarcheComponent;
  let fixture: ComponentFixture<PlaceDuMarcheComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PlaceDuMarcheComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(PlaceDuMarcheComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
