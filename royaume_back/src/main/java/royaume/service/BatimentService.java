
package royaume.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.transaction.Transactional;
import royaume.dao.IDAOBatiment;
import royaume.dao.IDAOChantierConstruction;
import royaume.dao.IDAORoyaume;
import royaume.dto.response.AmeliorationBatimentResponse;
import royaume.dto.response.BatimentResponse;
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
    // RECUPERATION DU ROYAUME PAR ID
    // =========================================

    private Royaume recupererRoyaume(Integer id) {
        return royaumeDAO.findById(id)
            .orElseThrow(() -> new RuntimeException(
                "Royaume introuvable : " + id
            ));
    }

    // =========================================
    // CHATEAU INITIAL
    // Un seul château par royaume
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
    // MAISON
    // Même nom + même royaume = pas de doublon
    // =========================================
public Maison creerMaison(
        Integer royaumeId,
        Integer id,
        String nom,
        int ouvriers) {

    Royaume royaume = recupererRoyaume(royaumeId);

    return batimentDAO
        .findByRoyaume_IdAndNom(royaumeId, nom)
        .orElseGet(() -> {

            Maison maison = BatimentFactory.creerMaison(
                royaume, id, nom, ouvriers
            );

            // 1. Sauvegarder le bâtiment
            Maison maisonEnregistree = batimentDAO.save(maison);

            // 2. Récupérer le chantier créé par la Factory
            ChantierConstruction chantier = maisonEnregistree.getChantier();

            // 3. Sauvegarder le chantier
            if (chantier != null) {
                chantierDAO.save(chantier);
            }

            return maisonEnregistree;
        });
}

    // =========================================
    // BATIMENT DE PRODUCTION
    // Même ressource + même royaume = pas de doublon
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

            // 1. Sauvegarder le bâtiment
            BatimentProduction batimentEnregistre =
                batimentDAO.save(batiment);

            // 2. Récupérer le chantier
            ChantierConstruction chantier =
                batimentEnregistre.getChantier();

            // 3. Sauvegarder le chantier
            if (chantier != null) {
                chantierDAO.save(chantier);
            }

            return batimentEnregistre;
        });
}
// =========================================
// RECUPERATION D'UN BATIMENT
// =========================================

public Batiment trouverParId(Integer id) {

    return batimentDAO.findById(id)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Bâtiment introuvable : " + id
        ));
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

        BatimentProduction batiment = creerBatimentProduction(
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
    // AMELIORATION
    // =========================================

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
    // RECUPERATION BATIMENT
    // =========================================

    private Batiment recupererBatiment(Integer id) {

        return batimentDAO.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Bâtiment introuvable : " + id
            ));
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

    return new BatimentResponse(
        b.getId(),
        b.getNom(),
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

    // =========================================
    // CATALOGUE DES BATIMENTS
    // =========================================

    public List<BatimentResponse> getCatalogueBatiments() {

        return batimentDAO.findAll()
            .stream()
            .map(this::convertirEnResponse)
            .toList();
    }

    // =========================================
    // PREVISUALISATION AMELIORATION
    // =========================================

    public AmeliorationBatimentResponse getAmeliorationBatiment(
            Integer id) {

        Batiment batiment = trouverParId(id);

        int niveauSuivant = batiment.getNiveau() + 1;

        int niveauChateauRequis =
            batiment.getRoyaume()
                .getChateau()
                .getNiveauPalierDebloque();

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

}