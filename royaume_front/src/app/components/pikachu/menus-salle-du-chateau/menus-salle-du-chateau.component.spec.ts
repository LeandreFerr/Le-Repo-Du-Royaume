import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import {
  MenusSalleDuChateauComponent,
  resoudrePersonnageDepuisPeuple,
} from './menus-salle-du-chateau.component';

describe('MenusSalleDuChateau', () => {
  let component: MenusSalleDuChateauComponent;
  let fixture: ComponentFixture<MenusSalleDuChateauComponent>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MenusSalleDuChateauComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(MenusSalleDuChateauComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    fixture.detectChanges();

    const req = httpMock.expectOne('/api/attaques');
    req.flush([]);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create and resolve all 5 game peoples automatically when opened in modal mode', () => {
    expect(component).toBeTruthy();

    expect(resoudrePersonnageDepuisPeuple('Canards')).toBe('canard');
    expect(resoudrePersonnageDepuisPeuple('Gobelins')).toBe('gobelin');
    expect(resoudrePersonnageDepuisPeuple('Licornes')).toBe('licorne');
    expect(resoudrePersonnageDepuisPeuple('Princesses')).toBe('princesse');
    expect(resoudrePersonnageDepuisPeuple('Teuteus')).toBe('teuteu');

    component.royaumeId = 14;
    component.peupleNom = 'Princesses';
    component.modeModal = true;
    component.ngOnInit();

    expect(component.vendeurId).toBe(14);
    expect(component.nomPersonnage).toBe('princesse');
    expect(component.demarre).toBe(true);
    expect(component.spriteActuel).toBe('assets/pikachu/img/princesseDown.png');
    expect(component.fondEau).toBe('assets/pikachu/img/water-princesse.png');
  });
});
