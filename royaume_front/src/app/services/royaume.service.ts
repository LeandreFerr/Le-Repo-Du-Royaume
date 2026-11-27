import { Injectable, inject} from '@angular/core';
import { Observable, of, throwError } from 'rxjs';
import { HttpClient } from '@angular/common/http';


// ========================================
// MODÈLES
// ========================================

import { Royaume } from '../models/royaume.model';
import { PalierNiveau } from '../models/palier-niveau.model';
import { ModeleBatiment } from '../models/modele-batiment.model';
import { AmeliorationBatiment } from '../models/amelioration-batiment.model';
import { ReparationBatiment } from '../models/reparation-batiment.model';
import { RoyaumeCreationRequest } from '../models/royaume-creation-request';
import { RoyaumeResponse } from '../models/royaume-response';



// ========================================
// MOCKS
// ========================================

import {
  MOCK_ROYAUME,
  MOCK_PALIERS
} from '../mocks/royaume.mock';

import {
  MOCK_CATALOGUE_BATIMENTS
} from '../mocks/catalogue-batiments.mock';

import {
  MOCK_AMELIORATIONS_BATIMENTS
} from '../mocks/ameliorations-batiments.mock';

import {
  MOCK_REPARATIONS_BATIMENTS
} from '../mocks/reparations-batiments.mock';


// ========================================
// SERVICE
// ========================================

@Injectable({
  providedIn: 'root'
})
export class RoyaumeService {

   private readonly http = inject(HttpClient);

  private readonly apiUrl = '/api/royaumes';

  creer(
    request: RoyaumeCreationRequest
  ): Observable<RoyaumeResponse> {

    return this.http.post<RoyaumeResponse>(
      this.apiUrl,
      request
    );
  }



  // ========================================
  // RÉCUPÉRATION DU ROYAUME
  // ========================================

  getMonRoyaume(): Observable<Royaume> {

    return of(MOCK_ROYAUME);

  }


  // ========================================
  // PALIERS DE DÉVELOPPEMENT
  // ========================================

  getPaliers(): Observable<PalierNiveau[]> {

    return of(MOCK_PALIERS);

  }


  // ========================================
  // CATALOGUE DE CONSTRUCTION
  // ========================================

  getCatalogueBatiments(): Observable<ModeleBatiment[]> {

    return of(MOCK_CATALOGUE_BATIMENTS);

  }


  // ========================================
  // DEMANDE DE CONSTRUCTION
  // ========================================

  construireBatiment(
    royaume: Royaume,
    modele: ModeleBatiment,
    ouvriersAssignes: number
  ): Observable<Royaume> {

    // TODO : connecter cette méthode à l'API backend.

    return throwError(() => new Error(
      'Le service de construction n’est pas encore connecté au backend.'
    ));

  }


  // ========================================
  // RÉCUPÉRATION D'UNE AMÉLIORATION
  // ========================================

  getAmeliorationBatiment(
    batimentId: number
  ): Observable<AmeliorationBatiment> {

    const amelioration = MOCK_AMELIORATIONS_BATIMENTS.find(
      element => element.batimentId === batimentId
    );

    if (!amelioration) {

      return throwError(() => new Error(
        'Aucune amélioration disponible.'
      ));

    }

    return of(amelioration);

  }


  // ========================================
  // DEMANDE D'AMÉLIORATION
  // ========================================

  ameliorerBatiment(
    batimentId: number,
    ouvriersAssignes: number
  ): Observable<Royaume> {

    // TODO : connecter cette méthode à l'API backend.

    return throwError(() => new Error(
      'Le service d’amélioration n’est pas encore connecté au backend.'
    ));

  }


  // ========================================
  // RÉCUPÉRATION D'UNE RÉPARATION
  // ========================================

  getReparationBatiment(
    batimentId: number
  ): Observable<ReparationBatiment> {

    const reparation = MOCK_REPARATIONS_BATIMENTS.find(
      element => element.batimentId === batimentId
    );

    if (!reparation) {

      return throwError(() => new Error(
        'Aucune réparation disponible.'
      ));

    }

    return of(reparation);

  }


  // ========================================
  // DEMANDE DE RÉPARATION
  // ========================================

  reparerBatiment(
    batimentId: number,
    montant: number,
    ouvriersAssignes: number
  ): Observable<Royaume> {

    // TODO : connecter cette méthode à l'API backend.

    return throwError(() => new Error(
      'Le service de réparation n’est pas encore connecté au backend.'
    ));

  }

}