package royaume.model.ticker;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import royaume.model.Partie;
import royaume.model.Ressource;
import royaume.model.Royaume;
import royaume.model.batiment.Batiment;
import royaume.model.batiment.BatimentProduction;
import royaume.model.militaire.Attaque;
import royaume.model.militaire.StatutAttaque;

public class MoteurJeu {

    private static final double EPSILON = 1e-9;

    // Attributs

    private Partie partie;
    private HorlogeJeu horloge;
    private List<Attaque> attaques = new ArrayList<>();
    private final Map<Royaume, Map<Ressource, Double>> restesRessources = new HashMap<>();
    private final Map<Royaume, Double> restesPopulation = new HashMap<>();


    // Constructeurs

    public MoteurJeu(Partie partie, HorlogeJeu horloge) {
        if (partie == null) {
            throw new IllegalArgumentException("La partie ne peut pas être null.");
        }
        if (horloge == null) {
            throw new IllegalArgumentException("L'horloge ne peut pas être null.");
        }
        this.partie = partie;
        this.horloge = horloge;
    }

    // Méthodes métier

    public void executerTick(Duration duree) {
        if (duree == null) {
            throw new IllegalArgumentException("La durée ne peut pas être null.");
        }
        horloge.avancerTemps(duree);
        LocalDateTime tempsCourantHorloge = horloge.getTempsCourant();

        partie.getRoyaumes().stream()
            .filter(r -> !r.isEnRuine())
            .forEach(r -> {
                traiterProductions(r, duree);
                traiterCroissancePopulation(r, duree);
                traiterChantiers(r, tempsCourantHorloge);
                r.traiterFormations(tempsCourantHorloge);
            });
        traiterAttaques(tempsCourantHorloge);
    }

    public void addAttaque(Attaque attaque) {
        if (attaque == null) {
            throw new IllegalArgumentException("L'attaque ne peut pas être null.");
        }
        attaques.add(attaque);
    }

    // Méthode helper

    private void traiterProductions(Royaume royaume, Duration duree) {
        Map<Ressource, Double> restesDuRoyaume = restesRessources.computeIfAbsent(
            royaume, r -> new EnumMap<>(Ressource.class)
        );

        royaume.getBatiments().stream()
            .filter(b -> b instanceof BatimentProduction && b.estOperationnel())
            .map(BatimentProduction.class::cast)
            .forEach(b -> {
                double productionExacte = b.calculerProduction() * (duree.toSeconds() / 3600.0);
                double cumul = restesDuRoyaume.getOrDefault(b.getTypeRessource(), 0.0) + productionExacte;
                int quantiteACrediter = (int) Math.floor(cumul + EPSILON);
                restesDuRoyaume.put(b.getTypeRessource(), Math.max(0.0, cumul - quantiteACrediter));

                if (quantiteACrediter > 0) {
                    royaume.crediterRessources(b.getTypeRessource(), quantiteACrediter);
                }
            });
    }

    private void traiterChantiers(Royaume royaume, LocalDateTime tempsCourantHorloge) {
        royaume.getBatiments().stream()
            .map(Batiment::getChantier)
            .filter(b -> b != null && b.estPretAAchever(tempsCourantHorloge))
            .forEach(b -> b.finaliser(tempsCourantHorloge));
    }


    private void traiterAttaques(LocalDateTime tempsCourantHorloge) {
        attaques.forEach(a -> {
            if (a.getStatut() == StatutAttaque.ALLER && !tempsCourantHorloge.isBefore(a.getDateImpact())) {
                a.resoudreCombat();
            }
            if (a.getStatut() == StatutAttaque.RETOUR && !tempsCourantHorloge.isBefore(a.getDateRetourPrevue())) {
                a.receptionnerRetour();
            }
        });
    }

    private void traiterCroissancePopulation(Royaume royaume, Duration duree) {
        if (royaume.getBonusCroissance() <= 0 || royaume.getPopulationDisponible() <= 0) {
            return;
        }
        double gainExact = royaume.getPopulationDisponible() * royaume.getBonusCroissance() * (duree.toSeconds() / 3600.0);
        double cumul = restesPopulation.getOrDefault(royaume, 0.0) + gainExact;
        int gainEntier = (int) Math.floor(cumul + EPSILON);
        restesPopulation.put(royaume, Math.max(0.0, cumul - gainEntier));

        if (gainEntier > 0) {
            royaume.setPopulationDisponible(royaume.getPopulationDisponible() + gainEntier);
        }
    }

    
    // Getters/Setters

    public Partie getPartie() {
        return partie;
    }

    public void setPartie(Partie partie) {
        if (partie == null) {
            throw new IllegalArgumentException("La partie ne peut pas être null.");
        }
        this.partie = partie;
    }

    public HorlogeJeu getHorloge() {
        return horloge;
    }

    public void setHorloge(HorlogeJeu horloge) {
        if (horloge == null) {
            throw new IllegalArgumentException("L'horloge ne peut pas être null.");
        }
        this.horloge = horloge;
    }

    public List<Attaque> getAttaques() {
         return attaques;
     }

     public void setAttaques(List<Attaque> attaques) {
        if (attaques == null) {
            throw new IllegalArgumentException("La liste attaques ne peut pas être null.");
        }
        this.attaques = attaques;
     }


    // ToString

     @Override
    public String toString() {
        return "MoteurJeu [partie=" + partie + ", horloge=" + horloge + "]";
    }

     
}
