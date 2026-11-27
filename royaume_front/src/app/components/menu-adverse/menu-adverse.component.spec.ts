import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { MenuAdverseComponent } from './menu-adverse.component';

describe('MenuAdverseComponent', () => {
  let component: MenuAdverseComponent;
  let fixture: ComponentFixture<MenuAdverseComponent>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MenuAdverseComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(MenuAdverseComponent);
    component = fixture.componentInstance;
    component.monRoyaumeId = 3;
    component.monUtilisateurId = 10;
    component.partieId = 7;
    component.royaumeAdverse = {
      id: 9,
      nom: 'Val-Enchante',
      pseudoJoueur: 'ReineKat',
      utilisateurId: 25,
      peupleNom: 'Licornes',
      niveau: 4,
    };
    httpMock = TestBed.inject(HttpTestingController);
    fixture.detectChanges();

    const userReq = httpMock.expectOne('/api/utilisateurs/25');
    userReq.flush({
      id: 25,
      pseudo: 'ReineKat',
      email: 'kat@test.fr',
      online: true,
      admin: false,
      royaumeId: 9,
      royaumeNom: 'Val-Enchante',
    });
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create and target the clicked opponent kingdom for trade and messages', () => {
    expect(component).toBeTruthy();
    expect(component.attaquantId).toBe(3);
    expect(component.defenseurId).toBe(9);

    component.proposerEchange(new Event('submit'), 'OR', '10', 'BOIS', '20');
    const tradeReq = httpMock.expectOne('/api/propositions-echange');
    expect(tradeReq.request.method).toBe('POST');
    expect(tradeReq.request.body.expediteurId).toBe(3);
    expect(tradeReq.request.body.destinataireId).toBe(9);
    tradeReq.flush({});

    component.contenuMessage = 'Salutations !';
    component.envoyerMessage();
    const msgReq = httpMock.expectOne('/api/messages/envoyer');
    expect(msgReq.request.body.expediteurId).toBe(10);
    expect(msgReq.request.body.destinataireId).toBe(25);
    expect(msgReq.request.body.partieId).toBe(7);
    msgReq.flush({});
  });
});
