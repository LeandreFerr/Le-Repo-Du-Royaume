import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { VisuAttaques } from './visu-attaques.component';

describe('VisuAttaques', () => {
  let component: VisuAttaques;
  let fixture: ComponentFixture<VisuAttaques>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VisuAttaques],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(VisuAttaques);
    component = fixture.componentInstance;
    component.royaumeId = 18;
    httpMock = TestBed.inject(HttpTestingController);
    fixture.detectChanges();

    const req = httpMock.expectOne('/api/attaques');
    req.flush([]);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create and bind royaumeId', () => {
    expect(component).toBeTruthy();
    expect(component.idUtilisateur()).toBe(18);
  });
});
