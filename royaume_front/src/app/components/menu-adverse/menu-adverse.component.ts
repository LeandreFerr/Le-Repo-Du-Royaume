
import { Component, HostListener } from '@angular/core';

@Component({
  selector: 'app-menu-adverse',
  standalone: true,
  imports: [],
  templateUrl: './menu-adverse.component.html',
  styleUrls: ['./menu-adverse.component.css']
})
export class MenuAdverseComponent {

  // =====================================================
  // GESTION DES FENÊTRES MODALES
  // =====================================================

  modalEchange = false;
  modalMessage = false;
  modalAttaque = false;

  // =====================================================
  // FERMETURE DES MODALES
  // =====================================================

  fermerModales(): void {

    this.modalEchange = false;
    this.modalMessage = false;
    this.modalAttaque = false;

  }

  // =====================================================
  // OUVERTURE DE LA MODALE ÉCHANGE
  // =====================================================

  ouvrirEchange(): void {

    this.fermerModales();

    this.modalEchange = true;

  }

  // =====================================================
  // OUVERTURE DE LA MODALE MESSAGE
  // =====================================================

  ouvrirMessage(): void {

    this.fermerModales();

    this.modalMessage = true;

  }

  // =====================================================
  // OUVERTURE DE LA MODALE ATTAQUE
  // =====================================================

  ouvrirAttaque(): void {

    this.fermerModales();

    this.modalAttaque = true;

  }

  // =====================================================
  // FERMETURE AVEC LA TOUCHE ÉCHAP
  // =====================================================

  @HostListener('document:keydown', ['$event'])
  gererClavier(event: KeyboardEvent): void {

    if (
      this.modalEchange ||
      this.modalMessage ||
      this.modalAttaque
    ) {

      if (event.key === 'Escape') {
        this.fermerModales();
      }

    }

  }

}
