package royaume.model;

import java.time.Duration;
import java.time.LocalDateTime;

public class HorlogeJeu {

    // Attributs

    private LocalDateTime tempsCourant;
    private Double vitesseSimulation = 1.0;

    // Constructeurs
    
    public HorlogeJeu() {
        this(LocalDateTime.now(), 1.0);
    }

    public HorlogeJeu(LocalDateTime tempsCourant) {
        this(tempsCourant, 1.0);
    }

    public HorlogeJeu(LocalDateTime tempsCourant, Double vitesseSimulation) {
        initialiser(tempsCourant);
        setVitesseSimulation(vitesseSimulation);
    }

    // Méthodes métier


    public void initialiser(LocalDateTime tempsDepart) {
        if (tempsDepart == null) {
            throw new IllegalArgumentException("Le temps de départ ne peut pas être null");
        }

        tempsCourant = tempsDepart;
    }

    public void avancerTemps(Duration duree) {
        if (duree == null) {
            throw new IllegalArgumentException("La durée ne peut pas être null.");
        }
        if (tempsCourant == null) {
            throw new IllegalStateException("Le temps courant est null, impossible de lui ajouter une durée.");
        }
        if (duree.isNegative()) {
            throw new IllegalArgumentException("La durée ne peut pas être négative.");
        }

        tempsCourant =  tempsCourant.plus(duree);
    }

    public void avancerMinutes(long minutes) {
        if (minutes < 0) {
            throw new IllegalArgumentException("On ne peut pas avancer de " + minutes + " minutes.");
        }
        avancerTemps(Duration.ofMinutes(minutes));
    }

    public void avancerHeures(long heures) {
        if (heures < 0) {
            throw new IllegalArgumentException("On ne peut pas avancer de " + heures + " heures.");
        }
        avancerTemps(Duration.ofHours(heures));
    }

    
    // Getters/Setters

    public LocalDateTime getTempsCourant() {
        return tempsCourant;
    }

    public void setTempsCourant(LocalDateTime tempsCourant) {
        this.tempsCourant = tempsCourant;
    }

    public Double getVitesseSimulation() {
        return vitesseSimulation;
    }

    public void setVitesseSimulation(Double vitesseSimulation) {
        this.vitesseSimulation = vitesseSimulation;
    }

    
    @Override
    public String toString() {
        return "HorlogeJeu [tempsCourant=" + tempsCourant + ", vitesseSimulation=" + vitesseSimulation + "]";
    }

}
