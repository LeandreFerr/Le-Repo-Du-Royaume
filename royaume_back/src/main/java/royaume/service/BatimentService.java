
package royaume.service;


import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.transaction.Transactional;

import royaume.dao.IDAOBatiment;
import royaume.dao.IDAOChantierConstruction;
import royaume.dao.IDAORoyaume;

import royaume.dto.QuantiteRessourceDTO;

import royaume.dto.response.AmeliorationBatimentResponse;
import royaume.dto.response.BatimentResponse;
import royaume.dto.response.ModeleBatimentResponse;
import royaume.dto.response.ReparationBatimentResponse;

import royaume.model.Ressource;
import royaume.model.Royaume;

import royaume.model.batiment.Batiment;
import royaume.model.batiment.BatimentFactory;
import royaume.model.batiment.BatimentProduction;
import royaume.model.batiment.ChantierConstruction;
import royaume.model.batiment.Chateau;
import royaume.model.batiment.Maison;

@Service
@Transactional
public class BatimentService {

    private final IDAORoyaume royaumeDAO;
    private final IDAOBatiment batimentDAO;
    private final IDAOChantierConstruction chantierDAO;

    public BatimentService(
            IDAORoyaume royaumeDAO,
            IDAOBatiment batimentDAO,
            IDAOChantierConstruction chantierDAO) {

        this.royaumeDAO = royaumeDAO;
        this.batimentDAO = batimentDAO;
        this.chantierDAO = chantierDAO;
    }

    // =========================================
    // RECUPERATION DU ROYAUME
    // =========================================

    private Royaume recupererRoyaume(Integer id) {

        return royaumeDAO.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Royaume introuvable : " + id
            ));
    }

    // =========================================
    // CHATEAU INITIAL
    // =========================================

    public Chateau creerChateauInitial(Integer royaumeId) {

        Royaume royaume = recupererRoyaume(royaumeId);

        return batimentDAO.findByRoyaume_Id(royaumeId)
            .orElseGet(() -> {

                Chateau chateau =
                    BatimentFactory.creerChateauInitial(royaume);

                return batimentDAO.save(chateau);
            });
    }

    // =========================================
    // CREATION MAISON
    // =========================================

public Maison creerMaison(
        Integer royaumeId,
        Integer id,
        String nom,
        int ouvriers) {

    Royaume royaume = recupererRoyaume(royaumeId);

    // Générer automatiquement le nom
    String nomMaison = genererNomMaison(royaumeId);

    Maison maison = BatimentFactory.creerMaison(
        royaume,
        id,
        nomMaison,
        ouvriers
    );

    // Sauvegarder la maison
    Maison maisonEnregistree = batimentDAO.save(maison);

    // Sauvegarder son chantier
    ChantierConstruction chantier =
        maisonEnregistree.getChantier();

    if (chantier != null) {
        chantierDAO.save(chantier);
    }

    return maisonEnregistree;
}

    // =========================================
    // CREATION BATIMENT PRODUCTION
    // =========================================

    public BatimentProduction creerBatimentProduction(
            Integer royaumeId,
            Integer id,
            String nom,
            int pointsDeVie,
            Ressource typeRessource,
            int productionBaseHeure,
            int ouvriers) {

        Royaume royaume = recupererRoyaume(royaumeId);

        return batimentDAO
            .findByRoyaume_IdAndTypeRessource(
                royaumeId, typeRessource
            )
            .orElseGet(() -> {

                BatimentProduction batiment =
                    BatimentFactory.creerBatimentProduction(
                        royaume,
                        id,
                        nom,
                        pointsDeVie,
                        typeRessource,
                        productionBaseHeure,
                        ouvriers
                    );

                BatimentProduction enregistre =
                    batimentDAO.save(batiment);

                ChantierConstruction chantier =
                    enregistre.getChantier();

                if (chantier != null) {
                    chantierDAO.save(chantier);
                }

                return enregistre;
            });
    }

private String genererNomMaison(Integer royaumeId) {

    List<Batiment> maisons =
        batimentDAO.findAllByRoyaume_Id(royaumeId);

    long nombreMaisons = maisons.stream()
        .filter(Maison.class::isInstance)
        .count();

    return "Maison " + convertirEnLettres(nombreMaisons);
}

private String convertirEnLettres(long nombre) {

    StringBuilder resultat = new StringBuilder();

    nombre++;

    while (nombre > 0) {
        nombre--;

        resultat.insert(
            0,
            (char) ('A' + nombre % 26)
        );

        nombre /= 26;
    }

    return resultat.toString();
}

    // =========================================
    // RECUPERATION BATIMENT
    // =========================================

    public Batiment trouverParId(Integer id) {

        return batimentDAO.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Bâtiment introuvable : " + id
            ));
    }

    private Batiment recupererBatiment(Integer id) {
        return trouverParId(id);
    }

    // =========================================
    // AMELIORATION
    // =========================================

    public Batiment ameliorerBatiment(
            Integer batimentId,
            int ouvriersAssignes) {

        Batiment batiment = trouverParId(batimentId);

        Royaume royaume = batiment.getRoyaume();

        if (royaume == null) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Royaume du bâtiment introuvable."
            );
        }

        batiment.ameliorer(ouvriersAssignes, royaume);

        return batiment;
    }

    public BatimentResponse ameliorer(
            Integer batimentId,
            int ouvriers) {

        Batiment batiment = recupererBatiment(batimentId);

        batiment.ameliorer(
            ouvriers,
            batiment.getRoyaume()
        );

        return convertirEnResponse(batiment);
    }

    // =========================================
    // REPARATION
    // =========================================

    public Batiment reparerBatiment(
            Integer batimentId,
            int montant,
            int ouvriersAssignes) {

        Batiment batiment = trouverParId(batimentId);

        Royaume royaume = batiment.getRoyaume();

        if (royaume == null) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Royaume du bâtiment introuvable."
            );
        }

        batiment.reparer(montant, ouvriersAssignes, royaume);

        return batiment;
    }

    public BatimentResponse reparer(
            Integer batimentId,
            int montant,
            int ouvriers) {

        Batiment batiment = recupererBatiment(batimentId);

        batiment.reparer(
            montant,
            ouvriers,
            batiment.getRoyaume()
        );

        return convertirEnResponse(batiment);
    }

    // =========================================
    // CREATION AVEC RETOUR DTO
    // =========================================

    public BatimentResponse creerMaisonResponse(
            Integer royaumeId,
            String nom,
            int ouvriers) {

        Maison maison = creerMaison(
            royaumeId, null, nom, ouvriers
        );

        return convertirEnResponse(maison);
    }

    public BatimentResponse creerProductionResponse(
            Integer royaumeId,
            String nom,
            int pointsDeVie,
            Ressource typeRessource,
            int productionBaseHeure,
            int ouvriers) {

        BatimentProduction batiment =
            creerBatimentProduction(
                royaumeId,
                null,
                nom,
                pointsDeVie,
                typeRessource,
                productionBaseHeure,
                ouvriers
            );

        return convertirEnResponse(batiment);
    }

    // =========================================
    // CATALOGUE DYNAMIQUE
    // =========================================

    public List<ModeleBatimentResponse> getCatalogueBatiments(
            Integer royaumeId) {

        recupererRoyaume(royaumeId);

        List<Batiment> existants =
            batimentDAO.findAllByRoyaume_Id(royaumeId);

        boolean possedeMine =
            possedeProduction(existants, Ressource.OR);

        boolean possedeTaverne =
            possedeProduction(existants, Ressource.NOURRITURE);

        boolean possedeScierie =
            possedeProduction(existants, Ressource.BOIS);

        boolean possedeCarriere =
            possedeProduction(existants, Ressource.PIERRE);

        List<ModeleBatimentResponse> catalogue =
            new java.util.ArrayList<>();

        // Maison toujours disponible, sans limite

        catalogue.add(creerModeleMaison());

        // Mine

        if (!possedeMine) {
            catalogue.add(creerModeleMine());
        }

        // Taverne

        if (!possedeTaverne) {
            catalogue.add(creerModeleTaverne());
        }

        // Scierie

        if (!possedeScierie) {
            catalogue.add(creerModeleScierie());
        }

        // Carrière

        if (!possedeCarriere) {
            catalogue.add(creerModeleCarriere());
        }

        return catalogue;
    }

    // =========================================
    // VERIFICATION PRODUCTION EXISTANTE
    // =========================================

    private boolean possedeProduction(
            List<Batiment> batiments,
            Ressource ressource) {

        return batiments.stream()
            .filter(BatimentProduction.class::isInstance)
            .map(BatimentProduction.class::cast)
            .anyMatch(b -> b.getTypeRessource() == ressource);
    }

    // =========================================
    // MODELE MAISON
    // =========================================

    private ModeleBatimentResponse creerModeleMaison() {

        return new ModeleBatimentResponse(
            "MAISON",
            "Maison",
            "Augmente la capacité d'accueil de votre royaume.",
            "/img/batiments/maison.svg",
            "MAISON",
            1,
            2,
            3600,
            200,
            null,
            0,
            List.of(
                new QuantiteRessourceDTO(Ressource.BOIS, 100),
                new QuantiteRessourceDTO(Ressource.PIERRE, 50)
            )
        );
    }

    // =========================================
    // MODELE MINE
    // =========================================

    private ModeleBatimentResponse creerModeleMine() {

        return new ModeleBatimentResponse(
            "MINE",
            "Mine",
            "Permet d'extraire de l'or.",
            "/img/batiments/mine.svg",
            "PRODUCTION",
            1,
            5,
            14400,
            500,
            Ressource.OR,
            10,
            List.of(
                new QuantiteRessourceDTO(Ressource.BOIS, 300),
                new QuantiteRessourceDTO(Ressource.PIERRE, 200)
            )
        );
    }

    // =========================================
    // MODELE TAVERNE
    // =========================================

    private ModeleBatimentResponse creerModeleTaverne() {

        return new ModeleBatimentResponse(
            "TAVERNE",
            "Taverne",
            "Permet de produire de la nourriture.",
            "/img/batiments/taverne.svg",
            "PRODUCTION",
            1,
            3,
            10800,
            350,
            Ressource.NOURRITURE,
            20,
            List.of(
                new QuantiteRessourceDTO(Ressource.BOIS, 200),
                new QuantiteRessourceDTO(Ressource.PIERRE, 100)
            )
        );
    }

    // =========================================
    // MODELE SCIERIE
    // =========================================

    private ModeleBatimentResponse creerModeleScierie() {

        return new ModeleBatimentResponse(
            "SCIERIE",
            "Scierie",
            "Permet de produire du bois.",
            "/img/batiments/scierie.svg",
            "PRODUCTION",
            1,
            3,
            10800,
            350,
            Ressource.BOIS,
            20,
            List.of(
                new QuantiteRessourceDTO(Ressource.PIERRE, 150),
                new QuantiteRessourceDTO(Ressource.OR, 50)
            )
        );
    }

    // =========================================
    // MODELE CARRIERE
    // =========================================

    private ModeleBatimentResponse creerModeleCarriere() {

        return new ModeleBatimentResponse(
            "CARRIERE",
            "Carrière",
            "Permet d'extraire de la pierre.",
            "/img/batiments/carriere.svg",
            "PRODUCTION",
            1,
            3,
            10800,
            350,
            Ressource.PIERRE,
            20,
            List.of(
                new QuantiteRessourceDTO(Ressource.BOIS, 200),
                new QuantiteRessourceDTO(Ressource.OR, 50)
            )
        );
    }

    // =========================================
    // PREVISUALISATION AMELIORATION
    // =========================================

    public AmeliorationBatimentResponse getAmeliorationBatiment(
            Integer id) {

        Batiment batiment = trouverParId(id);

        int niveauSuivant = batiment.getNiveau() + 1;

        Chateau chateau = batiment.getRoyaume().getChateau();

        if (chateau == null) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Château introuvable pour ce royaume."
            );
        }

        int niveauChateauRequis =
            chateau.getNiveauPalierDebloque();

        return new AmeliorationBatimentResponse(
            batiment.getId(),
            batiment.getNiveau(),
            niveauSuivant,
            niveauChateauRequis,
            batiment.getCoutAmelioration(),
            batiment instanceof Chateau
                ? 0
                : batiment.getTempsConstructionBase(),
            1
        );
    }

    // =========================================
    // PREVISUALISATION REPARATION
    // =========================================

    public ReparationBatimentResponse getReparationBatiment(
            Integer id) {

        Batiment batiment = trouverParId(id);

        return new ReparationBatimentResponse(
            batiment.getId(),
            batiment.getPointsDeVie(),
            batiment.getPointsDeVieMax(),
            batiment.getTempsConstructionBase(),
            1
        );
    }

    // =========================================
    // CONVERSION DTO
    // =========================================

    private BatimentResponse convertirEnResponse(Batiment b) {

        BatimentProduction prod =
            b instanceof BatimentProduction bp ? bp : null;

        Maison maison =
            b instanceof Maison m ? m : null;

        Chateau chateau =
            b instanceof Chateau c ? c : null;

        String type;

        if (chateau != null) {
            type = "CHATEAU";
        } else if (maison != null) {
            type = "MAISON";
        } else if (prod != null) {
            type = "PRODUCTION";
        } else {
            throw new IllegalStateException(
                "Type de bâtiment inconnu : " + b.getClass().getName()
            );
        }

        return new BatimentResponse(
            b.getId(),
            b.getNom(),
            type,
            b.getNiveau(),
            b.getPointsDeVie(),
            b.getPointsDeVieMax(),
            b.isEnConstruction(),
            b.getMontantReparationEnCours(),
            b.getRoyaume().getId(),

            prod != null ? prod.getTypeRessource() : null,
            prod != null ? prod.getProductionBaseHeure() : null,

            maison != null ? maison.getBonusPropre() : null,

            chateau != null ? chateau.getNiveauPalierDebloque() : null,
            chateau != null ? chateau.getBonusDefenseRoyaume() : null
        );
    }
}
