import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { TypeBatiment } from '../../models/batiment.model';
// ========================================
// MODÈLES
// ========================================

import { AmeliorationBatiment } from '../../models/amelioration-batiment.model';
import { ChantierConstructionResponse } from '../../models/chantier.model';
import { ModeleBatiment } from '../../models/modele-batiment.model';
import { ReparationBatiment } from '../../models/reparation-batiment.model';
import { Ressource } from '../../models/ressource.model';
import { Royaume } from '../../models/royaume.model';

// ========================================
// TYPES DES RÉPONSES ET REQUÊTES
// ========================================

export interface BatimentResponse {
  nom: string;
  type: TypeBatiment;
  niveau: number;
  pointsDeVie: number;
  pointsDeVieMax: number;
  enConstruction: boolean;
  montantReparationEnCours: number;
  royaumeId: number;

  typeRessource: Ressource | null;
  productionBaseHeure: number | null;

  bonusPropre: number | null;

  niveauPalierDebloque: number | null;
  bonusDefenseRoyaume: number | null;
}

export interface MaisonRequest {
  royaumeId: number;
  nom: string;
  ouvriersAssignes: number;
}

export interface BatimentProductionRequest {
  royaumeId: number;
  nom: string;
  pointsDeVie: number;
  typeRessource: Ressource;
  productionBaseHeure: number;
  ouvriersAssignes: number;
}

export interface AmeliorationBatimentRequest {
  ouvriersAssignes: number;
}

export interface ReparationBatimentRequest {
  montant: number;
  ouvriersAssignes: number;
}

// ========================================
// SERVICE
// ========================================

@Injectable({
  providedIn: 'root'
})
export class RoyaumeService {

  private readonly apiUrl =
    'http://localhost:8080/api/royaumes';

  private readonly batimentsUrl =
    'http://localhost:8080/api/batiments';

  private readonly chantiersUrl =
    'http://localhost:8080/api/chantiers';

  constructor(private readonly http: HttpClient) {}

 // ========================================
  // RÉCUPÉRATION DES ROYAUMES
  // ========================================

  getTousLesRoyaumes(): Observable<Royaume[]> {
    return this.http.get<Royaume[]>(this.apiUrl);
  }

  // ========================================
  // RÉCUPÉRATION D'UN ROYAUME
  // ========================================

  getMonRoyaume(royaumeId: number): Observable<Royaume> {
    return this.http.get<Royaume>(
      `${this.apiUrl}/${royaumeId}`
    );
  }

  // ========================================
  // RÉCUPÉRATION DES ROYAUMES D'UNE PARTIE
  // ========================================

  getRoyaumesParPartie(partieId: number): Observable<Royaume[]> {
    return this.http.get<Royaume[]>(
      `${this.apiUrl}/partie/${partieId}`
    );
  }

  // ========================================
  // SUPPRESSION D'UN ROYAUME
  // ========================================

  supprimerRoyaume(royaumeId: number): Observable<void> {
    return this.http.delete<void>(
      `${this.apiUrl}/${royaumeId}`
    );
  }

    // ========================================
  // RÉCUPÉRATION D'UN BÂTIMENT
  // ========================================

  getBatiment(batimentId: number): Observable<BatimentResponse> {
    return this.http.get<BatimentResponse>(
      `${this.batimentsUrl}/${batimentId}`
    );
  }

  // ========================================
  // CONSTRUCTION D'UNE MAISON
  // ========================================

  construireMaison(
    request: MaisonRequest
  ): Observable<BatimentResponse> {

    return this.http.post<BatimentResponse>(
      `${this.batimentsUrl}/maisons`,
      request
    );
  }

  // ========================================
  // CONSTRUCTION D'UN BÂTIMENT DE PRODUCTION
  // ========================================

  construireBatimentProduction(
    request: BatimentProductionRequest
  ): Observable<BatimentResponse> {

    return this.http.post<BatimentResponse>(
      `${this.batimentsUrl}/production`,
      request
    );
  }

  // ========================================
  // AMÉLIORATION D'UN BÂTIMENT
  // ========================================

  ameliorerBatiment(
    batimentId: number,
    ouvriersAssignes: number
  ): Observable<BatimentResponse> {

    const request: AmeliorationBatimentRequest = {
      ouvriersAssignes
    };

    return this.http.post<BatimentResponse>(
      `${this.batimentsUrl}/${batimentId}/ameliorer`,
      request
    );
  }

  // ========================================
  // RÉPARATION D'UN BÂTIMENT
  // ========================================

  reparerBatiment(
    batimentId: number,
    montant: number,
    ouvriersAssignes: number
  ): Observable<BatimentResponse> {

    const request: ReparationBatimentRequest = {
      montant,
      ouvriersAssignes
    };

    return this.http.post<BatimentResponse>(
      `${this.batimentsUrl}/${batimentId}/reparer`,
      request
    );
  }

  // ========================================
  // RÉCUPÉRATION DES CHANTIERS D'UN ROYAUME
  // ========================================

  getChantiersRoyaume(
    royaumeId: number
  ): Observable<ChantierConstructionResponse[]> {

    return this.http.get<ChantierConstructionResponse[]>(
      `${this.chantiersUrl}/royaume/${royaumeId}`
    );
  }

  // ========================================
  // RÉCUPÉRATION DU CHANTIER D'UN BÂTIMENT
  // ========================================

  getChantierParBatiment(
    batimentId: number
  ): Observable<ChantierConstructionResponse> {

    return this.http.get<ChantierConstructionResponse>(
      `${this.chantiersUrl}/batiment/${batimentId}`
    );
  }

  // ========================================
  // RÉCUPÉRATION D'UN CHANTIER
  // ========================================

  getChantier(chantierId: number): Observable<ChantierConstructionResponse> {

    return this.http.get<ChantierConstructionResponse>(
      `${this.chantiersUrl}/${chantierId}`
    );
  }

  // ========================================
  // AJOUT D'OUVRIERS SUR UN CHANTIER
  // ========================================

  ajouterOuvriers(
    chantierId: number,
    nombre: number
  ): Observable<ChantierConstructionResponse> {

    return this.http.patch<ChantierConstructionResponse>(
      `${this.chantiersUrl}/${chantierId}/ouvriers/ajouter`,
      null,
      {
        params: { nombre: nombre.toString() }
      }
    );
  }

  // ========================================
  // RETRAIT D'OUVRIERS D'UN CHANTIER
  // ========================================

  retirerOuvriers(
    chantierId: number,
    nombre: number
  ): Observable<ChantierConstructionResponse> {

    return this.http.patch<ChantierConstructionResponse>(
      `${this.chantiersUrl}/${chantierId}/ouvriers/retirer`,
      null,
      {
        params: { nombre: nombre.toString() }
      }
    );
  }

  // ========================================
  // CATALOGUE DES BÂTIMENTS
  // ========================================

  getCatalogueBatiments(): Observable<ModeleBatiment[]> {

    return this.http.get<ModeleBatiment[]>(
      `${this.batimentsUrl}/catalogue`
    );

  }

  // ========================================
  // RÉCUPÉRATION D'UNE AMÉLIORATION
  // ========================================

  getAmeliorationBatiment(
    batimentId: number
  ): Observable<AmeliorationBatiment> {

    return this.http.get<AmeliorationBatiment>(
      `${this.batimentsUrl}/${batimentId}/amelioration`
    );

  }

  // ========================================
  // RÉCUPÉRATION D'UNE RÉPARATION
  // ========================================

  getReparationBatiment(
    batimentId: number
  ): Observable<ReparationBatiment> {

    return this.http.get<ReparationBatiment>(
      `${this.batimentsUrl}/${batimentId}/reparation`
    );

  }


}
