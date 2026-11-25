import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';

import { Royaume } from '../models/royaume.model';
import { PalierNiveau } from '../models/palier-niveau.model';
import { ModeleBatiment } from '../models/modele-batiment.model';
import { MOCK_CATALOGUE_BATIMENTS } from '../mocks/catalogue-batiments.mock';

import {
  MOCK_ROYAUME,
  MOCK_PALIERS
} from '../mocks/royaume.mock';

/* Pour connexion Back, remplacer "return of (MOCK)" par une requête HTTP */

@Injectable({
  providedIn: 'root'
})
export class RoyaumeService {

  getMonRoyaume(): Observable<Royaume> {

    return of(MOCK_ROYAUME);

  }

  getPaliers(): Observable<PalierNiveau[]> {

    return of(MOCK_PALIERS);

  }

  getCatalogueBatiments(): Observable<ModeleBatiment[]> {

    return of(MOCK_CATALOGUE_BATIMENTS);

  }
}