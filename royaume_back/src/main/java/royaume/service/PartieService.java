package royaume.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import royaume.dao.IDAOPartie;
import royaume.dto.request.PartieRequest;
import royaume.dto.response.PartieResponse;
import royaume.model.Partie;
import royaume.model.StatutPartie;

import royaume.dao.IDAOCarte;
import royaume.model.Carte;

@Service
@Transactional(readOnly = true)
public class PartieService {

    private final IDAOPartie daoPartie;
    private final IDAOCarte daoCarte;
    private final RoyaumeService royaumeService;

    public PartieService(IDAOPartie daoPartie, IDAOCarte daoCarte, RoyaumeService royaumeService) {
        this.daoPartie = daoPartie;
        this.daoCarte = daoCarte;
        this.royaumeService = royaumeService;
    }

    @Transactional
    public PartieResponse creer(PartieRequest partieRequest) {

        Partie partie = new Partie(
                partieRequest.nom(),
                partieRequest.vitesseJeu(),
                partieRequest.maxJoueurs());

        partie = daoPartie.save(partie);

        Carte carte = new Carte();
        carte.setPartie(partie);
        carte.genererEmplacements(
                partieRequest.maxJoueurs());

        daoCarte.save(carte);

        return convertirEnResponse(partie);
    }

    public PartieResponse trouverParId(Integer id) {
        return convertirEnResponse(
                daoPartie.findById(id)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "La partie id=" + id + " n'existe pas.")));
    }

    public List<PartieResponse> trouverToutes() {
        return daoPartie.findAll().stream().map(p -> convertirEnResponse(p)).toList();
    }

    public List<PartieResponse> trouverParStatut(StatutPartie statut) {
        return daoPartie.findByStatut(statut).stream().map(p -> convertirEnResponse(p)).toList();
    }

    @Transactional
    public PartieResponse demarrer(Integer id) {
        Partie partie = daoPartie.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "La partie id=" + id + " n'existe pas."));
        partie.setStatut(StatutPartie.EN_COURS);
        partie = daoPartie.save(partie);
        return convertirEnResponse(partie);
    }

    @Transactional
    public PartieResponse terminer(Integer id) {
        Partie partie = daoPartie.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "La partie id=" + id + " n'existe pas."));
        partie.setStatut(StatutPartie.TERMINEE);
        partie = daoPartie.save(partie);
        return convertirEnResponse(partie);
    }

    @Transactional
    public void supprimer(Integer id) {
        Partie partie = daoPartie.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "La partie id=" + id + " n'existe pas."));
        daoPartie.delete(partie);
    }

    private PartieResponse convertirEnResponse(Partie partie) {

        if (partie == null) {
            return null;
        }
        return new PartieResponse(
            partie.getId(),
            partie.getNom(),
            partie.getDateCreation(),
            partie.getVitesseJeu(),
            partie.getMaxJoueurs(),
            partie.getVainqueur() == null ? null : partie.getVainqueur().getId(),
            partie.getVainqueur() == null ? null : partie.getVainqueur().getPseudo(),
            partie.getTypeVictoire(),
            partie.getRoyaumes() == null ? List.of() : partie.getRoyaumes().stream().map(royaumeService::convertirEnResponse).toList(),
            partie.getStatut()
        );
    }

}
