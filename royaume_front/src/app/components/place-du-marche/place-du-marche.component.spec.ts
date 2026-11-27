import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { PlaceDuMarcheComponent } from './place-du-marche.component';

describe('PlaceDuMarcheComponent', () => {
  let component: PlaceDuMarcheComponent;
  let fixture: ComponentFixture<PlaceDuMarcheComponent>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PlaceDuMarcheComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(PlaceDuMarcheComponent);
    component = fixture.componentInstance;
    component.monRoyaumeId = 12;
    httpMock = TestBed.inject(HttpTestingController);
    fixture.detectChanges();

    const req = httpMock.expectOne((r) => r.url === '/api/marche/offres');
    req.flush([]);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create and use the bound monRoyaumeId', () => {
    expect(component).toBeTruthy();
    expect(component.idUtilisateurEnCours).toBe(12);
  });
});
