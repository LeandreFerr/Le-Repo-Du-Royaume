
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';

// ========================================
// MODÈLES
// ========================================

import { Royaume } from '../../models/royaume.model';
import { PalierNiveau } from '../../models/palier-niveau.model';
import { ModeleBatiment } from '../../models/modele-batiment.model';
import { AmeliorationBatiment } from '../../models/amelioration-batiment.model';
import { ReparationBatiment } from '../../models/reparation-batiment.model';
import { ChantierConstructionResponse } from '../../models/chantier.model';
// ========================================
// SERVICE
// ========================================

@Injectable({
  providedIn: 'root'
})
export class RoyaumeService {

  private nonConnecte<T>(fonction: string): Observable<T> {
    return throwError(() =>
      new Error(`${fonction} : endpoint backend non configuré.`)
    );
  }

  // ========================================
  // RÉCUPÉRATION DU ROYAUME
  // ========================================

  getMonRoyaume(): Observable<Royaume> {
    return this.nonConnecte<Royaume>('Récupération du royaume');
  }

  // ========================================
  // PALIERS DE DÉVELOPPEMENT
  // ========================================

  getPaliers(): Observable<PalierNiveau[]> {
    return this.nonConnecte<PalierNiveau[]>('Récupération des paliers');
  }

  // ========================================
  // CATALOGUE DE CONSTRUCTION
  // ========================================

  getCatalogueBatiments(): Observable<ModeleBatiment[]> {
    return this.nonConnecte<ModeleBatiment[]>('Catalogue des bâtiments');
  }

  // ========================================
  // DEMANDE DE CONSTRUCTION
  // ========================================

  construireBatiment(
    royaume: Royaume,
    modele: ModeleBatiment,
    ouvriersAssignes: number
  ): Observable<Royaume> {
    return this.nonConnecte<Royaume>('Construction du bâtiment');
  }

  // ========================================
  // RÉCUPÉRATION D'UNE AMÉLIORATION
  // ========================================

  getAmeliorationBatiment(
    batimentId: number
  ): Observable<AmeliorationBatiment> {
    return this.nonConnecte<AmeliorationBatiment>(
      'Récupération de l’amélioration'
    );
  }

  // ========================================
  // DEMANDE D'AMÉLIORATION
  // ========================================

  ameliorerBatiment(
    batimentId: number,
    ouvriersAssignes: number
  ): Observable<Royaume> {
    return this.nonConnecte<Royaume>('Amélioration du bâtiment');
  }

  // ========================================
  // RÉCUPÉRATION D'UNE RÉPARATION
  // ========================================

  getReparationBatiment(
    batimentId: number
  ): Observable<ReparationBatiment> {
    return this.nonConnecte<ReparationBatiment>(
      'Récupération de la réparation'
    );
  }

  // ========================================
  // DEMANDE DE RÉPARATION
  // ========================================

  reparerBatiment(
    batimentId: number,
    montant: number,
    ouvriersAssignes: number
  ): Observable<Royaume> {
    return this.nonConnecte<Royaume>('Réparation du bâtiment');
  }

    getChantiersRoyaume(royaumeId: number) : Observable<ChantierConstructionResponse[]>{
        return this.nonConnecte<ChantierConstructionResponse[]>('Récupération des chantiers du royaume ');}
    }


