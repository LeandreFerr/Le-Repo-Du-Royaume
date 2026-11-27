import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { SessionService } from '../../services/session/session.service';
import { PageLobby } from './page-lobby.component';

describe('PageLobby', () => {
  let component: PageLobby;
  let fixture: ComponentFixture<PageLobby>;
  let httpMock: HttpTestingController;
  let session: SessionService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PageLobby],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    session = TestBed.inject(SessionService);
    session.enregistrer({
      id: 5,
      pseudo: 'JoueurTest',
      email: 'j@test.fr',
      online: true,
      admin: false,
      royaumeId: 88,
      royaumeNom: 'Valoria',
    });

    fixture = TestBed.createComponent(PageLobby);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    fixture.detectChanges();

    const req = httpMock.expectOne('/api/parties/statut/EN_ATTENTE');
    req.flush([]);
  });

  afterEach(() => {
    session.effacer();
    httpMock.verify();
  });

  it('should create and display return-to-kingdom button when user has a royaumeId', () => {
    expect(component).toBeTruthy();
    const compiled = fixture.nativeElement as HTMLElement;
    const btn = compiled.querySelector('[data-testid="retour-royaume-btn"]');
    expect(btn).not.toBeNull();
    expect(btn?.textContent).toContain('Valoria');
  });
});
