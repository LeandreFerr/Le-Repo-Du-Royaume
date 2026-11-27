import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router, provideRouter } from '@angular/router';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { SessionService } from '../../../services/session/session.service';
import { PageCreationRoyaume } from './page-creation-royaume.component';

describe('PageCreationRoyaume', () => {
  let component: PageCreationRoyaume;
  let fixture: ComponentFixture<PageCreationRoyaume>;
  let httpMock: HttpTestingController;
  let session: SessionService;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PageCreationRoyaume],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: {
                get: (key: string) => (key === 'partieId' ? '15' : null),
              },
            },
          },
        },
      ],
    }).compileComponents();

    session = TestBed.inject(SessionService);
    session.enregistrer({
      id: 7,
      pseudo: 'Xavier',
      email: 'x@test.fr',
      online: true,
      admin: false,
      royaumeId: null,
      royaumeNom: null,
    });

    fixture = TestBed.createComponent(PageCreationRoyaume);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    fixture.detectChanges();

    const popReq = httpMock.expectOne('/api/populations');
    popReq.flush([]);
  });

  afterEach(() => {
    session.effacer();
    httpMock.verify();
    vi.restoreAllMocks();
  });

  it('should create and update session + navigate to /player/:royaumeId on kingdom creation', () => {
    const navSpy = vi.spyOn(router, 'navigate').mockResolvedValue(true);

    component.creationForm.controls.nom.setValue('Valoria');
    component.creationForm.controls.peupleId.setValue(2);
    component.creerRoyaume();

    const createReq = httpMock.expectOne('/api/royaumes');
    expect(createReq.request.method).toBe('POST');
    createReq.flush({
      id: 99,
      nom: 'Valoria',
    });

    const userReq = httpMock.expectOne('/api/utilisateurs/7');
    userReq.flush({
      id: 7,
      pseudo: 'Xavier',
      email: 'x@test.fr',
      online: true,
      admin: false,
      royaumeId: 99,
      royaumeNom: 'Valoria',
    });

    expect(session.utilisateur()?.royaumeId).toBe(99);
    expect(navSpy).toHaveBeenCalledWith(['/player', 99]);
  });
});
