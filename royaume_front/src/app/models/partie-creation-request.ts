import { ArchetypeCarte } from './carte.model';

export interface PartieCreationRequest {
  nom: string;
  vitesseJeu: number;
  maxJoueurs: number;
  archetype?: ArchetypeCarte;
  seed?: string;
}
