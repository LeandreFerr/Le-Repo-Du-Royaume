package royaume.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import royaume.dao.IDAOCarte;
import royaume.dao.IDAOEmplacementCarte;
import royaume.dao.IDAOPartie;
import royaume.dto.request.CarteRequest;
import royaume.dto.response.CarteResponse;
import royaume.dto.response.EmplacementCarteResponse;
import royaume.model.Carte;
import royaume.model.Partie;

@Service
@Transactional(readOnly = true)
public class CarteService {

    private final IDAOCarte daoCarte;
    private final IDAOEmplacementCarte daoEmplacementCarte;
    private final IDAOPartie daoPartie;

    public CarteService( IDAOCarte daoCarte, IDAOEmplacementCarte daoEmplacementCarte, IDAOPartie daoPartie) {

        this.daoCarte = daoCarte;
        this.daoEmplacementCarte = daoEmplacementCarte;
        this.daoPartie = daoPartie;

    }

    @Transactional
    public CarteResponse creer(CarteRequest request) {

        Partie partie = daoPartie.findById(request.partieId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La partie " + request.partieId() + " n'existe pas."));

        if (daoCarte.findByPartieId(request.partieId()).isPresent()) {

            throw new ResponseStatusException(HttpStatus.CONFLICT, "La partie possède déjà une carte.");

        }

        Carte carte = new Carte();
        carte.setPartie(partie);
        if (request.archetype() != null && !request.archetype().isBlank()) {
            carte.setArchetype(request.archetype());
        }
        if (request.seed() != null && !request.seed().isBlank()) {
            carte.setSeed(request.seed());
        } else {
            carte.setSeed("partie-" + partie.getId());
        }
        carte.genererEmplacements(request.nombreEmplacements());

        Carte carteSauvegardee = daoCarte.save(carte);

        return convertirEnResponse(carteSauvegardee);
    }

    public CarteResponse trouverParId(Integer id) {
        return convertirEnResponse(trouverEntiteParId(id));
    }

    public CarteResponse trouverParPartieId(Integer partieId) {

        Carte carte = daoCarte.findByPartieId(partieId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aucune carte trouvée pour la partie " + partieId + "."
                ));

        return convertirEnResponse(carte);
    }

    public List<EmplacementCarteResponse> trouverEmplacements(Integer carteId) {
        verifierExistenceCarte(carteId);

        return daoEmplacementCarte.findByCarteId(carteId)
                .stream()
                .map(EmplacementCarteService::convertirEnResponse)
                .toList();
    }

    public List<EmplacementCarteResponse> trouverEmplacementsLibres(
            Integer carteId) {

        verifierExistenceCarte(carteId);

        return daoEmplacementCarte
                .findByCarteIdAndOccupeFalse(carteId).stream()
                .map(EmplacementCarteService::convertirEnResponse).toList();
    }

    @Transactional
    public void supprimer(Integer id) {
        Carte carte = trouverEntiteParId(id);
        daoCarte.delete(carte);
    }

    private Carte trouverEntiteParId(Integer id) {
        return daoCarte.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "La carte " + id + " n'existe pas."));
    }

    private void verifierExistenceCarte(Integer carteId) {
        if (!daoCarte.existsById(carteId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "La carte " + carteId + " n'existe pas.");
        }
    }

    private CarteResponse convertirEnResponse(Carte carte) {

        List<EmplacementCarteResponse> emplacements = carte.getEmplacements().stream()
                .map(EmplacementCarteService::convertirEnResponse).toList();

        EmplacementCarteResponse emplacementMarche = EmplacementCarteService.convertirEnResponse(carte.getEmplacementMarche());

        return new CarteResponse(
                carte.getId(),
                carte.getLargeur(),
                carte.getHauteur(),
                carte.getCentreX(),
                carte.getCentreY(),
                carte.getPartie() != null ? carte.getPartie().getId() : null,
                emplacements,
                emplacementMarche,
                carte.getArchetype(),
                carte.getSeed()
        );
    }
}
