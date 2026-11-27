import { CommonModule } from '@angular/common';
import {
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
  inject,
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { BoutonDeconnexion } from '../../components/bouton-deconnexion/bouton-deconnexion.component';
import { CarteRoyaumeComponent } from '../../components/carte-royaume/carte-royaume.component';
import {
  MenuAdverseComponent,
  RoyaumeAdverseCible,
} from '../../components/menu-adverse/menu-adverse.component';
import { MenusSalleDuChateauComponent } from '../../components/pikachu/menus-salle-du-chateau/menus-salle-du-chateau.component';
import { PlaceDuMarcheComponent } from '../../components/place-du-marche/place-du-marche.component';
import { KingdomBuildingsComponent } from '../../components/player-panels/kingdom-buildings/kingdom-buildings.component';
import { KingdomDevelopmentComponent } from '../../components/player-panels/kingdom-development/kingdom-development.component';
import { EvenementsReception } from '../../components/player-panels/evenements-reception/evenements-reception.component';
import { KingdomOverviewComponent } from '../../components/player-panels/kingdom-overview/kingdom-overview.component';
import { ReglesDuJeuManuscriptComponent } from '../../components/regles-du-jeu-manuscript/regles-du-jeu-manuscript.component';
import { VisuAttaques } from '../../components/visu-attaques/visu-attaques.component';

import { SessionEntrainement } from '../../components/session-entrainement/session-entrainement.component';

import {
  ARCHETYPES_CARTE,
  ArchetypeCarte,
  CarteDto,
  EmplacementCarteDto,
  createDefaultCarte,
} from '../../models/carte.model';
import { CarteResponse } from '../../models/carte-response';
import { PalierNiveau } from '../../models/palier-niveau.model';
import { PartieResponse } from '../../models/partie-response';
import { Royaume } from '../../models/royaume.model';

import {
  CarteApiService,
  adapterCarteBackVersFront,
} from '../../services/carte/carte-api.service';
import { Partie } from '../../services/partie.service';
import { AmeliorerRoyaumeService } from '../../services/royaume/ameliorer-royaume.service';
import { SessionService } from '../../services/session/session.service';

@Component({
  selector: 'app-player',
  standalone: true,
  imports: [
    CommonModule,
    CarteRoyaumeComponent,
    KingdomOverviewComponent,
    KingdomDevelopmentComponent,
    KingdomBuildingsComponent,
    BoutonDeconnexion,
    MenuAdverseComponent,
    MenusSalleDuChateauComponent,
    PlaceDuMarcheComponent,
    VisuAttaques,
    ReglesDuJeuManuscriptComponent,
    SessionEntrainement,
    EvenementsReception,
  ],
  templateUrl: './player.component.html',
  styleUrl: './player.component.css',
})
export class PlayerComponent implements OnInit, OnDestroy {
  private readonly royaumeService = inject(AmeliorerRoyaumeService);
  private readonly partieService = inject(Partie);
  private readonly carteApiService = inject(CarteApiService);
  private readonly session = inject(SessionService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly cdr = inject(ChangeDetectorRef);

  royaumeId!: number;
  destinataireId!: number;
  seed: string = 'royaume-1';
  archetype: ArchetypeCarte = 'VALLEE_ROYALE';
  carte: CarteDto = createDefaultCarte();
  selectedEmplacementNumero: number | null = null;

  activePage: string | null = 'royaume';

  // Données provenant du backend
  royaume: Royaume | null = null;
  paliers: PalierNiveau[] = [];
  partie: PartieResponse | null = null;
  carteBack: CarteResponse | null = null;

  chargement = true;
  erreur: string | null = null;
  demarrageEnCours = false;
  erreurDemarrage: string | null = null;
  messageVerrouillage: string | null = null;

  // Salon d'attente
  salonReduit = false;

  // Surimpressions d'interaction sur la carte
  salleChateauOuverte = false;
  menuAdverseOuvert = false;
  cibleAdverse: RoyaumeAdverseCible | null = null;
  marcheOuvert = false;
  evenementsOuvert = false;
  reglesOuvertes = false;

  private pollingTimer: ReturnType<typeof setTimeout> | null = null;
  private destroyed = false;
  private lastCarteSignature = '';

  ngOnInit(): void {
    const paramId = Number(this.route.snapshot.paramMap.get('royaumeId'));
    const sessionRoyaumeId = this.session.utilisateur()?.royaumeId ?? 0;
    const id =
      Number.isInteger(paramId) && paramId > 0
        ? paramId
        : Number.isInteger(sessionRoyaumeId) && sessionRoyaumeId > 0
          ? sessionRoyaumeId
          : 0;

    if (id <= 0) {
      this.chargement = false;
      this.router.navigate([this.session.utilisateur() ? '/lobby' : '/login']);
      return;
    }

    this.royaumeId = id;
    this.destinataireId = id;
    this.chargerDonneesRoyaume();
  }

  ngOnDestroy(): void {
    this.destroyed = true;
    this.arreterPolling();
  }

  get utilisateurConnecteId(): number {
    return this.session.utilisateur()?.id ?? 1;
  }

  get estEnAttente(): boolean {
    return this.partie?.statut === 'EN_ATTENTE';
  }

  get estPartieComplete(): boolean {
    if (!this.partie) {
      return false;
    }
    return this.partie.royaumes.length >= this.partie.maxJoueurs;
  }

  get placesRestantes(): number {
    if (!this.partie) {
      return 0;
    }
    return Math.max(0, this.partie.maxJoueurs - this.partie.royaumes.length);
  }

  get archetypeLabel(): string {
    return (
      ARCHETYPES_CARTE.find((a) => a.id === this.archetype)?.label ??
      this.archetype
    );
  }

  get slotsSalon(): Array<{
    numero: number;
    occupe: boolean;
    nomRoyaume?: string;
    pseudoJoueur?: string;
    peupleNom?: string;
    estMoi: boolean;
  }> {
    const max = this.partie?.maxJoueurs ?? this.carte.emplacements.length;
    if (this.carte.emplacements.length > 0) {
      return this.carte.emplacements.map((emp) => ({
        numero: emp.numero,
        occupe: emp.occupe,
        nomRoyaume: emp.royaume?.nom,
        pseudoJoueur: emp.royaume?.pseudoJoueur,
        peupleNom: emp.royaume?.peupleNom,
        estMoi: emp.royaume?.id === this.royaumeId,
      }));
    }

    const royaumes = this.partie?.royaumes ?? [];
    return Array.from({ length: max }, (_, idx) => {
      const r = royaumes[idx];
      const peupleNom =
        r && typeof r.peuple === 'object' && r.peuple ? r.peuple.nom : undefined;
      return {
        numero: idx + 1,
        occupe: Boolean(r),
        nomRoyaume: r?.nom,
        pseudoJoueur: r?.pseudoJoueur,
        peupleNom,
        estMoi: r?.id === this.royaumeId,
      };
    });
  }

  chargerDonneesRoyaume(): void {
    this.chargement = !this.royaume;
    this.erreur = null;

    forkJoin({
      royaume: this.royaumeService.obtenirRoyaume(this.royaumeId),
      palier: this.royaumeService
        .obtenirProchainPalier(this.royaumeId)
        .pipe(catchError(() => of(null))),
      partie: this.partieService
        .trouverParRoyaumeId(this.royaumeId)
        .pipe(catchError(() => of(null))),
    }).subscribe({
      next: ({ royaume, palier, partie }) => {
        this.royaume = royaume;
        this.paliers = palier?.prochainPalier ? [palier.prochainPalier] : [];
        this.partie = partie;
        this.chargement = false;

        if (partie?.statut === 'EN_ATTENTE') {
          this.activePage = null;
          this.fermerToutesSurimpressions();
        }

        if (partie?.id) {
          this.chargerCartePartie(partie);
        }

        this.planifierProchainPolling();
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Erreur chargement royaume :', error);
        this.erreur =
          error.error?.message ||
          error.message ||
          'Impossible de récupérer votre royaume.';
        this.chargement = false;
        this.cdr.detectChanges();
      },
    });
  }

  private chargerCartePartie(partie: PartieResponse): void {
    this.carteApiService.trouverParPartieId(partie.id).subscribe({
      next: (carteBack) => {
        this.carteBack = carteBack;
        const adapted = adapterCarteBackVersFront(
          carteBack,
          partie.royaumes,
          this.royaumeId,
        );
        const signature = JSON.stringify({
          archetype: adapted.archetype,
          seed: adapted.seed,
          emplacements: adapted.carte.emplacements.map((e) => ({
            n: e.numero,
            x: e.coordX,
            y: e.coordY,
            o: e.occupe,
            rid: e.royaume?.id ?? null,
            nom: e.royaume?.nom ?? null,
          })),
        });

        if (signature !== this.lastCarteSignature) {
          this.lastCarteSignature = signature;
          this.archetype = adapted.archetype;
          this.seed = adapted.seed;
          this.carte = adapted.carte;
          if (this.selectedEmplacementNumero === null) {
            const monEmp = adapted.carte.emplacements.find(
              (e) => e.royaume?.id === this.royaumeId,
            );
            this.selectedEmplacementNumero = monEmp?.numero ?? null;
          }
          this.cdr.detectChanges();
        }
      },
      error: (err) => {
        console.error('Erreur chargement carte de la partie :', err);
      },
    });
  }

  private rafraichirEtatPartie(): void {
    if (this.destroyed || !this.royaumeId) {
      return;
    }

    forkJoin({
      partie: this.partieService
        .trouverParRoyaumeId(this.royaumeId)
        .pipe(catchError(() => of(null))),
      royaume: this.royaumeService
        .obtenirRoyaume(this.royaumeId)
        .pipe(catchError(() => of(null))),
    }).subscribe(({ partie, royaume }) => {
      if (this.destroyed) {
        return;
      }
      if (royaume) {
        this.royaume = royaume;
      }
      if (partie) {
        const ancienStatut = this.partie?.statut;
        this.partie = partie;
        if (ancienStatut === 'EN_ATTENTE' && partie.statut === 'EN_COURS') {
          this.activePage = 'royaume';
          this.messageVerrouillage = null;
        }
        this.chargerCartePartie(partie);
      }
      this.planifierProchainPolling();
      this.cdr.detectChanges();
    });
  }

  private planifierProchainPolling(): void {
    this.arreterPolling();
    if (this.destroyed || !this.partie || this.partie.statut === 'TERMINEE') {
      return;
    }
    const delaiMs = this.partie.statut === 'EN_ATTENTE' ? 3000 : 8000;
    this.pollingTimer = setTimeout(() => {
      this.rafraichirEtatPartie();
    }, delaiMs);
  }

  private arreterPolling(): void {
    if (this.pollingTimer !== null) {
      clearTimeout(this.pollingTimer);
      this.pollingTimer = null;
    }
  }

  demarrerPartie(): void {
    if (!this.partie || !this.estPartieComplete || this.demarrageEnCours) {
      return;
    }

    this.demarrageEnCours = true;
    this.erreurDemarrage = null;

    this.partieService.demarrer(this.partie.id).subscribe({
      next: (partieDemarree) => {
        this.demarrageEnCours = false;
        this.partie = partieDemarree;
        this.activePage = 'royaume';
        this.messageVerrouillage = null;
        this.chargerCartePartie(partieDemarree);
        this.planifierProchainPolling();
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.demarrageEnCours = false;
        this.erreurDemarrage =
          err.error?.message ||
          (typeof err.error === 'string' ? err.error : null) ||
          'Impossible de démarrer la partie.';
        this.cdr.detectChanges();
      },
    });
  }

  basculerSalonReduit(): void {
    this.salonReduit = !this.salonReduit;
  }

  onRoyaumeModifie(): void {
    this.chargerDonneesRoyaume();
  }

  onEmplacementSelectionne(emplacement: EmplacementCarteDto | null): void {
    this.selectedEmplacementNumero = emplacement?.numero ?? null;

    if (!emplacement || !emplacement.occupe || !emplacement.royaume?.id) {
      return;
    }

    if (this.estEnAttente) {
      this.signalerAttente();
      return;
    }

    if (emplacement.royaume.id === this.royaumeId) {
      this.ouvrirSalleDuChateau();
      return;
    }

    this.fermerToutesSurimpressions();
    this.cibleAdverse = {
      id: emplacement.royaume.id,
      nom: emplacement.royaume.nom,
      pseudoJoueur: emplacement.royaume.pseudoJoueur,
      utilisateurId: emplacement.royaume.utilisateurId,
      peupleNom: emplacement.royaume.peupleNom,
      niveau: emplacement.royaume.niveau,
    };
    this.menuAdverseOuvert = true;
  }

  onMarcheSelectionne(): void {
    if (this.estEnAttente) {
      this.signalerAttente();
      return;
    }
    this.ouvrirMarche();
  }

  ouvrirSalleDuChateau(): void {
    if (this.estEnAttente) {
      this.signalerAttente();
      return;
    }
    this.fermerToutesSurimpressions();
    this.salleChateauOuverte = true;
  }

  fermerSalleDuChateau(): void {
    this.salleChateauOuverte = false;
  }

  fermerMenuAdverse(): void {
    this.menuAdverseOuvert = false;
    this.cibleAdverse = null;
  }

  ouvrirMarche(): void {
    if (this.estEnAttente) {
      this.signalerAttente();
      return;
    }
    this.fermerToutesSurimpressions();
    this.marcheOuvert = true;
  }

  fermerMarche(): void {
    this.marcheOuvert = false;
  }

  ouvrirEvenements(): void {
    if (this.estEnAttente) {
      this.signalerAttente();
      return;
    }
    this.fermerToutesSurimpressions();
    this.evenementsOuvert = true;
  }

  fermerEvenements(): void {
    this.evenementsOuvert = false;
  }

  ouvrirRegles(): void {
    this.fermerToutesSurimpressions();
    this.reglesOuvertes = true;
  }

  fermerRegles(): void {
    this.reglesOuvertes = false;
  }

  fermerToutesSurimpressions(): void {
    this.salleChateauOuverte = false;
    this.menuAdverseOuvert = false;
    this.cibleAdverse = null;
    this.marcheOuvert = false;
    this.evenementsOuvert = false;
    this.reglesOuvertes = false;
  }

  togglePage(page: string): void {
    if (page === 'regles') {
      this.reglesOuvertes ? this.fermerRegles() : this.ouvrirRegles();
      return;
    }

    if (this.estEnAttente) {
      this.signalerAttente();
      return;
    }

    if (page === 'ressources') {
      this.marcheOuvert ? this.fermerMarche() : this.ouvrirMarche();
      return;
    }

    if (page === 'evenements') {
      this.evenementsOuvert ? this.fermerEvenements() : this.ouvrirEvenements();
      return;
    }

    if (page === 'chateau') {
      this.salleChateauOuverte
        ? this.fermerSalleDuChateau()
        : this.ouvrirSalleDuChateau();
      return;
    }

    this.activePage = this.activePage === page ? null : page;
  }

  private signalerAttente(): void {
    this.messageVerrouillage =
      'La partie est en attente de joueurs. Cliquez sur « Démarrer la partie » dès que toutes les places sont remplies.';
    setTimeout(() => {
      if (!this.destroyed) {
        this.messageVerrouillage = null;
        this.cdr.detectChanges();
      }
    }, 3500);
  }
}
