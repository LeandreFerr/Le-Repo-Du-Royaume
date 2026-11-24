import { Component, OnInit } from '@angular/core';

import { CarteRoyaumeComponent } from '../../components/carte-royaume/carte-royaume.component';

import {
  CarteDto,
  EmplacementCarteDto,
  createDefaultCarte,
} from '../../models/carte.model';

import {
  createDefaultRoyaume,
  genererEmplacementsCercle,
} from '../../services/placement-royaumes.util';

@Component({
  selector: 'app-player',
  standalone: true,
  imports: [CarteRoyaumeComponent],
  templateUrl: './player.component.html',
  styleUrl: './player.component.css',
})
export class PlayerComponent implements OnInit {

  seed = 'royaume-1';

  carte: CarteDto = createDefaultCarte();

  selectedEmplacementNumero: number | null = null;

  ngOnInit(): void {
    const emplacements = genererEmplacementsCercle(6, this.seed)
      .map((emp, index) => ({
        ...emp,
        occupe: index < 4,
        royaume: createDefaultRoyaume(emp.numero),
      }));

    this.carte = createDefaultCarte(emplacements);
  }

  onEmplacementSelectionne(
    emplacement: EmplacementCarteDto | null
  ): void {
    this.selectedEmplacementNumero = emplacement?.numero ?? null;

    if (emplacement) {
      console.log('Royaume sélectionné :', emplacement);
    }
  }


  activePage: string | null = null;

  togglePage(page: string): void {
    if (this.activePage === page) {
      this.activePage = null;
      console.log('Fermeture :', page);
    } else {
      this.activePage = page;
      console.log('Ouverture :', page);
    }
  }
}