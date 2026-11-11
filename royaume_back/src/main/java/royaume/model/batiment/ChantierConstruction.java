package royaume.model.batiment;

import java.time.Duration;
import java.time.LocalDateTime;

import royaume.model.Royaume;

public class ChantierConstruction {

    private Integer id;

    private Batiment batiment;
    private Royaume royaume;
    private int ouvriersMinimum = 1;
    private int ouvriersAssignes = 0;
    private int ouvriersPrecedents;
    private int tempsBaseSecondes;
    private boolean amelioration = false;

    private LocalDateTime dateDebut;				 // Ajout regle dateFin > dateDebut 
    private LocalDateTime dateFinPrevue;  
    											//  getTempsConstruction()/ ouvriersAssigne = dateFinPrevu
  

    //private boolean termine = false;

    // ---------------- CONSTRUCTEUR ----------------

    public ChantierConstruction(
            Integer id,
            Batiment batiment,
            Royaume royaume,
            int ouvriersMinimum,
            int ouvriersAssignes,
            int tempsBaseSecondes,
            boolean amelioration) {

        if (batiment == null || royaume == null) {
            throw new IllegalArgumentException(
                "Le bâtiment et le royaume sont obligatoires."
            );
        }

        if (ouvriersMinimum < 1 ||
        		ouvriersAssignes < ouvriersMinimum) {

            throw new IllegalArgumentException(
                "Paramètres du chantier invalides."
            );
        }

        this.id = id;
        this.batiment = batiment;
        this.royaume = royaume;

        this.ouvriersMinimum = ouvriersMinimum;
        this.tempsBaseSecondes = tempsBaseSecondes;
        this.amelioration = amelioration;

        // Affectation initiale des ouvriers : 
        
        royaume.affecterOuvriers(ouvriersAssignes);

        this.ouvriersAssignes = ouvriersAssignes;
        this.ouvriersPrecedents = ouvriersAssignes;

        this.dateDebut = LocalDateTime.now();

        // Liaison entre le bâtiment et son chantier
        batiment.setEnConstruction(true);
        batiment.setChantier(this);

        initialiserDateFin()  ;
        
    }

    // ---------------- MÉTHODES MÉTIER ----------------

    public void assignerOuvriers(int nombre) {

        assignerOuvriers(nombre, LocalDateTime.now());
    }

    // Surcharge pour les tests unitaires
    public void assignerOuvriers(int nombre, LocalDateTime maintenant) {

        if (!batiment.isEnConstruction()) {
            throw new IllegalStateException(
                "Le chantier est terminé."
            );
        }

        if (nombre <= 0) {
            throw new IllegalArgumentException(
                "Le nombre d'ouvriers doit être positif."
            );
        }

        // Mémoriser le temps restant et l'ancien effectif
        long ancienTempsRestant = calculerTempsRestant(maintenant);
        int anciensOuvriers = ouvriersAssignes;

        royaume.affecterOuvriers(nombre);

        ouvriersAssignes += nombre;

        recalculerDateFin(
            ancienTempsRestant,
            anciensOuvriers,
            maintenant
        );
    }

    public void retirerOuvriers(int nombre) {

        retirerOuvriers(nombre, LocalDateTime.now());
    }

    public void retirerOuvriers(int nombre, LocalDateTime maintenant) {

        if (!batiment.isEnConstruction()) {
            throw new IllegalStateException(
                "Le chantier est terminé."
            );
        }

        if (nombre <= 0) {
            throw new IllegalArgumentException(
                "Le nombre doit être positif."
            );
        }

        if (ouvriersAssignes - nombre < ouvriersMinimum) {
            throw new IllegalArgumentException(
                "Impossible de descendre sous le minimum d'ouvriers."
            );
        }

        long ancienTempsRestant = calculerTempsRestant(maintenant);
        int anciensOuvriers = ouvriersAssignes;

        royaume.libererOuvriers(nombre);

        ouvriersAssignes -= nombre;

        recalculerDateFin(
            ancienTempsRestant,
            anciensOuvriers,
            maintenant
        );
    }
   
  //________________________________________


    public long calculerTempsRestant() {
        return calculerTempsRestant(LocalDateTime.now());
    }

    // ( Surcharge pratique pour les tests unitaires )
    
    public long calculerTempsRestant(LocalDateTime maintenant) {

        long secondes = Duration.between(
            maintenant,
            dateFinPrevue
        ).getSeconds();

        return Math.max(0, secondes);
    }
//________________________________________ 
    
    
    private void initialiserDateFin() {

        long duree = (long) Math.ceil(
            (double) tempsBaseSecondes / ouvriersAssignes
        );

        dateFinPrevue = dateDebut.plusSeconds(duree);
    }
    
    
    // --> Mises à jour MAJ successives de DateFinPrevue 
   //(appel dans le constructeur pour init)
   
    
    public void recalculerDateFin(
            long ancienTempsRestant,
            int ouvriersPrecedents,
            LocalDateTime maintenant) {

        if (ouvriersAssignes <= 0) {
            throw new IllegalStateException(
                "Un chantier doit avoir au moins un ouvrier."
            );
        }

        // Temps restant -> Avant le changement d'effectif
        // (transmis en paramètre)

        // Recalcul proportionnel au nouvel effectif
        long nouveauTempsRestant = (long) Math.ceil(
            (double) ancienTempsRestant * ouvriersPrecedents
            / ouvriersAssignes
        );

        // Nouvelle date de fin
        dateFinPrevue = maintenant.plusSeconds(nouveauTempsRestant);

    }
   
 
    //________________________________________

    public boolean estPretAAchever() {
        return estPretAAchever(LocalDateTime.now());
    }

    // Surcharge pour faciliter les tests
    
    public boolean estPretAAchever(LocalDateTime maintenant) {

        return batiment.isEnConstruction() &&
               !maintenant.isBefore(dateFinPrevue); // date de peremption (datedefin est dépassé)
    }

    // ---------------- FINALISATION ----------------
    public void finaliser() {
        finaliser(LocalDateTime.now());
    }
    
    
    public void finaliser(LocalDateTime maintenant) {

        // Vérifier si le chantier est déjà terminé
    	
        if (!batiment.isEnConstruction()) {
            return;
        }

        //  la date de fin est atteinte : 
        
        if (!estPretAAchever(maintenant)) {
            throw new IllegalStateException(
                "Le chantier n'est pas encore terminé."
            );
        }

        // Si c'est une amélioration, augmenter le niveau : 
        
        if (amelioration) {
            batiment.ameliorer();
        }
        
        else {
        	Batiment b = this.batiment;
            b.setPointsDeVie(Math.min(b.getPointsDeVieMax(), b.getPointsDeVie() + b.getMontantReparationEnCours()));
            b.setMontantReparationEnCours(0);
        }
        
        // Restituer les ouvriers au royaume : 
        
        royaume.libererOuvriers(ouvriersAssignes);

        ouvriersAssignes = 0;


        // Le chantier devient null (car c'est un attribut semi-optionnel si null : 
        
       //pas de chantier pour le batiment actuellement) 
        
        batiment.setChantier(null);

        // Marquer le chantier comme terminé +  Rendre le bâtiment opérationnel
      
        
        batiment.setEnConstruction(false);    // --> Simili :  termine = true;
    }
    
    // ---------------- GETTERS / SETTERS ----------------

    public Integer getId() {
        return id;
    }

    public int getOuvriersAssignes() {
        return ouvriersAssignes;
    }

    public int getOuvriersMinimum() {
        return ouvriersMinimum;
    }

    public LocalDateTime getDateDebut() {
        return dateDebut;
    }

    public LocalDateTime getDateFinPrevue() {
        return dateFinPrevue;
    }

    public boolean isAmelioration() {
        return amelioration;
    }

    public Batiment getBatiment() {
        return batiment;
    }

    public Royaume getRoyaume() {
        return royaume;
    }

   
}
