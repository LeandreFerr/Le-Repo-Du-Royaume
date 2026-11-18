package royaume.model;

import java.time.Duration;

public class MoteurJeu {

    // Attributs

    private Partie partie;
    private HorlogeJeu horloge;


    // Constructeurs

    public MoteurJeu() {
    }

    public MoteurJeu(Partie partie, HorlogeJeu horloge) {
        this.partie = partie;
        this.horloge = horloge;
    }

    // Méthodes métier

    public void executerTick(Duration duree) {
        horloge.avancerTemps(duree);
    }
    
    // Getters/Setters

    public Partie getPartie() {
        return partie;
    }

    public void setPartie(Partie partie) {
        this.partie = partie;
    }

    public HorlogeJeu getHorloge() {
        return horloge;
    }

    public void setHorloge(HorlogeJeu horloge) {
        this.horloge = horloge;
    }


    // ToString

     @Override
    public String toString() {
        return "MoteurJeu [partie=" + partie + ", horloge=" + horloge + "]";
    }
}
