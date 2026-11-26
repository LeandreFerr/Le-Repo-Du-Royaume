import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BoutonDeconnexion } from './bouton-deconnexion.component';

describe('BoutonDeconnexion', () => {
  let component: BoutonDeconnexion;
  let fixture: ComponentFixture<BoutonDeconnexion>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BoutonDeconnexion],
    }).compileComponents();

    fixture = TestBed.createComponent(BoutonDeconnexion);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
