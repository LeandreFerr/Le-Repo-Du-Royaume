package royaume.service;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import royaume.dao.IDAOBatiment;
import royaume.dao.IDAORoyaume;
import royaume.model.Ressource;
import royaume.model.Royaume;
import royaume.model.batiment.BatimentFactory;
import royaume.model.batiment.BatimentProduction;
import royaume.model.batiment.Chateau;
import royaume.model.batiment.Maison;

@Service
@Transactional
public class BatimentService {

    private final IDAORoyaume royaumeDAO;
    private final IDAOBatiment batimentDAO;

    public BatimentService(IDAORoyaume royaumeDAO,IDAOBatiment batimentDAO) {
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
    // =========================================

    public Chateau creerChateauInitial(Integer royaumeId) {

        Royaume royaume = recupererRoyaume(royaumeId);

        return BatimentFactory.creerChateauInitial(royaume);
    }
    
 // =========================================
    // MAISON
    // =========================================

    public Maison creerMaison(
            Integer royaumeId,
            Integer id,
            String nom,
            int ouvriers) {

        Royaume royaume = recupererRoyaume(royaumeId);

        Maison maison =  BatimentFactory.creerMaison(
            royaume,
            id,
            nom,
            ouvriers
        );
        
        return batimentDAO.save(maison);
    }

    // =========================================
    // BATIMENT DE PRODUCTION
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

        BatimentProduction batiment = BatimentFactory.creerBatimentProduction(
            royaume,
            id,
            nom,
            pointsDeVie,
            typeRessource,
            productionBaseHeure,
            ouvriers
        );
        
        return batimentDAO.save(batiment);
    }
}