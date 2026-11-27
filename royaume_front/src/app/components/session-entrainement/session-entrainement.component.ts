import { Component, Input, OnInit, inject} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { SessionEntrainementService } from '../../services/attaque/entrainement.service';
import { Royaume } from '../../models/royaume.model';

@Component({
  imports: [CommonModule, FormsModule],
  standalone: true,
  selector: 'app-session-entrainement',
  styleUrl: './session-entrainement.component.css',
  templateUrl: './session-entrainement.component.html',
})
export class SessionEntrainement implements OnInit{
  private entrainementService = inject(SessionEntrainementService);
  
  @Input({ required: true }) royaume!: Royaume;

  typesTroupes = [
    { code: 'ARCHER', label: 'Archers', image: 'assets/imago/archer.png' },
    { code: 'CAVALIER', label: 'Cavaliers', image: 'assets/imago/cavalier.png' },
    { code: 'LANCIER', label: 'Lanciers', image: 'assets/imago/lancier.png' },
    { code: 'SPECIAL', label: 'Troupes spéciales', image: 'assets/imago/star.png' },
  ];

  quantitesAEntrainer: Record<string, number> = {
    ARCHER: 0,
    CAVALIER: 0,
    LANCIER: 0,
    SPECIAL: 0,
  };

  garnison: any[] = [];
  sessionsEnCours: any[] = [];
  sessionsFinalisees: any[] = [];

  chargement = false;
  actionEnCours = false;
  message = '';
  erreur = false;

  ngOnInit(): void {
    this.chargerDonnees();
  }

  get populationDisponible(): number {
    return this.royaume.populationDisponible;
  }

  get populationEnEntrainement(): number {
    return this.royaume.populationEnEntrainement;
  }

  getGarnisonParType(type: string): number {
    const contingent = this.garnison.find((c) => c.type === type);
    return contingent ? contingent.quantite : 0;
  }

  get totalSelectionne(): number {
    return Object.values(this.quantitesAEntrainer).reduce((acc, v) => acc + (Number(v) || 0), 0);
  }

  chargerDonnees(): void {
    if (!this.royaume?.id) return;
    this.chargement = true;

    this.entrainementService.getGarnison(this.royaume.id).subscribe({
      next: (data) => (this.garnison = data),
      error: () => {
        this.erreur = true;
        this.message = 'Erreur lors du chargement de la garnison.';
      },
    });

    this.entrainementService.getSessions(this.royaume.id, false).subscribe({
      next: (data) => {
        this.sessionsEnCours = data;
        this.chargement = false;
      },
      error: () => {
        this.erreur = true;
        this.message = 'Erreur lors du chargement des sessions en cours.';
        this.chargement = false;
      },
    });

    this.entrainementService.getSessions(this.royaume.id, true).subscribe({
      next: (data) => (this.sessionsFinalisees = data),
      error: () => {},
    });
  }

  verifierQuantite(type: string, valeur: number): void {
    const autres = this.totalSelectionne - (Number(valeur) || 0);
    const maxAutorise = Math.max(0, this.populationDisponible - autres);
    const propre = Math.floor(Number(valeur) || 0);
    this.quantitesAEntrainer[type] = Math.min(maxAutorise, Math.max(0, propre));
  }

  lancerEntrainement(type: string): void {
    const quantite = Number(this.quantitesAEntrainer[type]) || 0;
    if (quantite <= 0) return;

    this.actionEnCours = true;
    this.message = '';
    this.erreur = false;

    this.entrainementService.entrainer(this.royaume.id, type, quantite).subscribe({
      next: (sessionCreee) => {
        this.sessionsEnCours.push(sessionCreee);
        this.royaume.populationDisponible -= quantite;
        this.royaume.populationEnEntrainement += quantite;
        this.quantitesAEntrainer[type] = 0;

        this.message = `Entraînement lancé : ${quantite} ${type}.`;
        this.actionEnCours = false;
      },
      error: (err) => {
        this.erreur = true;
        this.message = err?.error?.message || "Entraînement impossible : population ou ressources insuffisantes.";
        this.actionEnCours = false;
      },
    });
  }
}





