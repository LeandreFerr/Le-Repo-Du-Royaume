
package royaume.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.transaction.Transactional;
import royaume.dao.IDAOBatiment;
import royaume.dao.IDAORoyaume;
import royaume.dto.response.BatimentResponse;
import royaume.model.Ressource;
import royaume.model.Royaume;
import royaume.model.batiment.Batiment;
import royaume.model.batiment.BatimentFactory;
import royaume.model.batiment.BatimentProduction;
import royaume.model.batiment.Chateau;
import royaume.model.batiment.Maison;

@Service
@Transactional
public class BatimentService {

    private final IDAORoyaume royaumeDAO;
    private final IDAOBatiment batimentDAO;

    public BatimentService(
            IDAORoyaume royaumeDAO,
            IDAOBatiment batimentDAO) {

        this.royaumeDAO = royaumeDAO;
        this.batimentDAO = batimentDAO;
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

                return batimentDAO.save(maison);
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

                return batimentDAO.save(batiment);
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
            prod == null ? null : prod.getTypeRessource(),
            prod == null ? null : prod.getProductionBaseHeure(),
            maison == null ? null : maison.getBonusPropre(),
            chateau == null ? null : chateau.getNiveauPalierDebloque(),
            chateau == null ? null : chateau.getBonusDefenseRoyaume()
        );
    }

}

