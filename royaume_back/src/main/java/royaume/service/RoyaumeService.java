package royaume.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import royaume.dao.IDAOPartie;
import royaume.dao.IDAOPopulation;
import royaume.dao.IDAORoyaume;
import royaume.dao.IDAOUtilisateur;
import royaume.dto.QuantiteRessourceDTO;
import royaume.dto.request.RoyaumeRequest;
import royaume.dto.response.BatimentResponse;
import royaume.dto.response.ContingentTroupeResponse;
import royaume.dto.response.PopulationResponse;
import royaume.dto.response.RoyaumeResponse;
import royaume.dto.response.UtilisateurResponse;
import royaume.model.Partie;
import royaume.model.Royaume;
import royaume.model.Utilisateur;
import royaume.model.batiment.Batiment;
import royaume.model.batiment.BatimentProduction;
import royaume.model.batiment.Chateau;
import royaume.model.batiment.Maison;
import royaume.model.peuple.Population;

@Service 
@Transactional (readOnly = true)
public class RoyaumeService {

    private final IDAORoyaume daoRoyaume;
    private final IDAOPartie daoPartie;
    private final IDAOUtilisateur daoUtilisateur;
    private final IDAOPopulation daoPopulation;

    public RoyaumeService(
        IDAORoyaume daoRoyaume,
        IDAOPartie daoPartie,
        IDAOUtilisateur daoUtilisateur,
        IDAOPopulation daoPopulation
    ) {
        this.daoRoyaume = daoRoyaume;
        this.daoPartie = daoPartie;
        this.daoUtilisateur = daoUtilisateur;
        this.daoPopulation = daoPopulation;
    }


    @Transactional 
    public RoyaumeResponse creer(RoyaumeRequest royaumeRequest) {
        if (daoRoyaume.existsByNom(royaumeRequest.nom())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Un royaume avec le nom '" + royaumeRequest.nom() + "' existe déjà.");
        }
        Partie partie = daoPartie.findById(royaumeRequest.partieId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La partie id=" + royaumeRequest.partieId() + " n'existe pas."));
        Utilisateur utilisateur = daoUtilisateur.findById(royaumeRequest.utilisateurId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "L'utilisateur id=" + royaumeRequest.utilisateurId() + " n'existe pas."));
        Population peuple = daoPopulation.findById(royaumeRequest.peupleId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Le peuple id=" + royaumeRequest.peupleId() + " n'existe pas."));

        Royaume royaume = daoRoyaume.save(new Royaume(null, royaumeRequest.nom(), peuple));
        partie.inscrireRoyaume(royaume);
        utilisateur.setRoyaume(royaume);

        return convertirEnResponse(royaume);
    }

    public RoyaumeResponse trouverParId(Integer id) {
        return convertirEnResponse(
            daoRoyaume.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Le royaume id=" + id + " n'existe pas.")
                )
        );
    }

    public List<RoyaumeResponse> trouverTous() {
        return daoRoyaume.findAll().stream().map(r -> convertirEnResponse(r)).toList();
    }

    public List<RoyaumeResponse> trouverParPartie(Integer partieId) {
        Partie partie = daoPartie.findById(partieId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La partie id=" + partieId + " n'existe pas."));
        return partie.getRoyaumes().stream().map(this::convertirEnResponse).toList();
    }

    @Transactional 
    public void supprimer(Integer id) {
        Royaume royaume = daoRoyaume.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Le royaume id=" + id + " n'existe pas.")
            );
        daoRoyaume.delete(royaume);
    }


    public RoyaumeResponse convertirEnResponse(Royaume royaume) {
        if (royaume == null) {
            return null;
        }

        List<QuantiteRessourceDTO> ressources = royaume.getRessources() == null
            ? List.of()
            : royaume.getRessources().stream()
                .map(r -> new QuantiteRessourceDTO(r.getRessource(), r.getQuantite()))
                .toList();

        List<ContingentTroupeResponse> garnison = royaume.getGarnison() == null
            ? List.of()
            : royaume.getGarnison().stream()
                .map(c -> new ContingentTroupeResponse(c.getType(), c.getQuantite()))
                .toList();

        List<BatimentResponse> batiments = royaume.getBatiments() == null
            ? List.of()
            : royaume.getBatiments().stream()
                .map(b -> convertirBatimentEnResponse(b, royaume.getId()))
                .toList();

        Integer x = royaume.getEmplacement() == null ? null : royaume.getEmplacement().getCoordX();
        Integer y = royaume.getEmplacement() == null ? null : royaume.getEmplacement().getCoordY();
        String pseudoJoueur = daoUtilisateur.findAll().stream().filter(u -> u.getRoyaume() != null && u.getRoyaume().getId().equals(royaume.getId())).map(Utilisateur::getPseudo).findFirst().orElse("Inconnu");

       return new RoyaumeResponse(
    royaume.getId(),
    royaume.getNom(),
    royaume.getPeuple() == null ? null :
        new PopulationResponse(
            royaume.getPeuple().getId(),
            royaume.getPeuple().getNom(),
            royaume.getPeuple().getTitreSouverain(),
            royaume.getPeuple().getAttraction(),
            royaume.getPeuple().getCapaciteSpeciale()
        ),
    pseudoJoueur,
    royaume.getNiveau(),
    royaume.getPopulationDisponible(),
    royaume.getPopulationOuvriere(),
    royaume.getPopulationEnEntrainement(),
    royaume.getPopulationMilitaire(),
    royaume.isEnRuine(),
    royaume.getBonusCroissance(),
    ressources,
    garnison,
    batiments,
    x,
    y
);}

private static BatimentResponse convertirBatimentEnResponse(Batiment b, Integer royaumeId) {

    BatimentProduction prod =
        b instanceof BatimentProduction bp ? bp : null;

    Maison maison =
        b instanceof Maison m ? m : null;

    Chateau chateau =
        b instanceof Chateau c ? c : null;

    // Détermination du type réel du bâtiment
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
        royaumeId,
        prod == null ? null : prod.getTypeRessource(),
        prod == null ? null : prod.getProductionBaseHeure(),
        maison == null ? null : maison.getBonusPropre(),
        chateau == null ? null : chateau.getNiveauPalierDebloque(),
        chateau == null ? null : chateau.getBonusDefenseRoyaume()
    );
}


}
