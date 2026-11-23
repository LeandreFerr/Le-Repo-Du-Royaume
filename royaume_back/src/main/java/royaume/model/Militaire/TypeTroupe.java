package royaume.model.Militaire;

import royaume.model.Ressource;
import java.util.Map;


        // temps pour former une troupe
        public enum TypeTroupe {
        ARCHER(1, "Archer", CategorieUnite.ARCHER, 20, 5, 10, 15,
                Map.of(Ressource.OR, 10, Ressource.BOIS, 10, Ressource.NOURRITURE, 10)),
        CAVALIER(2, "Cavalier", CategorieUnite.CAVALIER, 15, 10, 15, 50,
                Map.of(Ressource.OR, 10, Ressource.PIERRE, 10, Ressource.NOURRITURE, 20)),
        LANCIER(3, "Lancier", CategorieUnite.LANCIER, 15, 15, 10, 25,
                Map.of(Ressource.OR, 10, Ressource.BOIS, 5, Ressource.NOURRITURE, 10)),
        SPECIAL(4, "Special", CategorieUnite.SPECIAL, 20, 20, 20, 30,
                Map.of(Ressource.OR, 30, Ressource.PIERRE, 10, Ressource.BOIS, 10));

        private final int id;
        private final String nom;
        private final CategorieUnite categorie;
        private final int attaque;
        private final int defense;
        private final int vitesse;            // cases par heure
        private final int capaciteTransport;
        private final Map<Ressource, Integer> cout;

        TypeTroupe(int id, String nom, CategorieUnite categorie, int attaque, int defense,
                int vitesse, int capaciteTransport, Map<Ressource, Integer> cout) {
                this.id = id;
                this.nom = nom;
                this.categorie = categorie;
                this.attaque = attaque;
                this.defense = defense;
                this.vitesse = vitesse;
                this.capaciteTransport = capaciteTransport;
                this.cout = cout;
        }

        public int getId() { return id; }
        public String getNom() { return nom; }
        public CategorieUnite getCategorie() { return categorie; }
        public int getAttaque() { return attaque; }
        public int getDefense() { return defense; }
        public int getVitesse() { return vitesse; }
        public int getCapaciteTransport() { return capaciteTransport; }
        public Map<Ressource, Integer> getCout() { return cout; }


        /** Le type que cette troupe domine (null = aucun, pour SPECIAL). */
        public TypeTroupe getProie() {
                return switch (this) {
                        case ARCHER -> LANCIER;
                        case LANCIER -> CAVALIER;
                        case CAVALIER -> ARCHER;
                        case SPECIAL -> null;
                };
        }

        public double multiplicateurContre(TypeTroupe adversaire) {
                if (adversaire == null) return 1.0;
                if (adversaire == getProie()) return 1.2;
                if (adversaire.getProie() == this) return 0.8;
                return 1.0;
        }


}