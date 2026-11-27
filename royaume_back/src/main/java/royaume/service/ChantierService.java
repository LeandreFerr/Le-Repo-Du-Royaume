
package royaume.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import royaume.dao.IDAOChantierConstruction;
import royaume.dto.response.ChantierConstructionResponse;
import royaume.model.batiment.ChantierConstruction;

@Service
@Transactional
public class ChantierService {

    private final IDAOChantierConstruction chantierDAO;

    public ChantierService(IDAOChantierConstruction chantierDAO) {
        this.chantierDAO = chantierDAO;
    }

    // =========================================
    // RECUPERATION PAR ID
    // =========================================

    public ChantierConstructionResponse trouverParId(Integer id) {

        ChantierConstruction chantier = chantierDAO.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Chantier introuvable : " + id
            ));

        return convertirEnResponse(chantier);
    }

    // =========================================
    // RECUPERATION PAR BATIMENT
    // =========================================

    public ChantierConstructionResponse trouverParBatimentId(
            Integer batimentId) {

        ChantierConstruction chantier =
            chantierDAO.findByBatiment_Id(batimentId)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Aucun chantier actif pour ce bâtiment."
                ));

        return convertirEnResponse(chantier);
    }

    // =========================================
    // RECUPERATION DE TOUS LES CHANTIERS
    // =========================================

    public List<ChantierConstructionResponse> trouverTous() {

        return chantierDAO.findAll()
            .stream()
            .map(this::convertirEnResponse)
            .toList();
    }

    // =========================================
    // AJOUTER DES OUVRIERS
    // =========================================

    public ChantierConstructionResponse ajouterOuvriers(
            Integer id,
            int nombre) {

        ChantierConstruction chantier = recuperer(id);

        chantier.assignerOuvriers(nombre);

        return convertirEnResponse(chantier);
    }

    // =========================================
    // RETIRER DES OUVRIERS
    // =========================================

    public ChantierConstructionResponse retirerOuvriers(
            Integer id,
            int nombre) {

        ChantierConstruction chantier = recuperer(id);

        chantier.retirerOuvriers(nombre);

        return convertirEnResponse(chantier);
    }

    // =========================================
    // RECUPERATION INTERNE
    // =========================================

    private ChantierConstruction recuperer(Integer id) {

        return chantierDAO.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Chantier introuvable : " + id
            ));
    }

    // =========================================
    // CONVERSION DTO
    // =========================================

    private ChantierConstructionResponse convertirEnResponse(
            ChantierConstruction chantier) {

        return new ChantierConstructionResponse(
            chantier.getId(),
            chantier.getBatiment().getId(),
            chantier.getRoyaume().getId(),
            chantier.getDateDebut(),
            chantier.getDateFinPrevue(),
            chantier.getOuvriersMinimum(),
            chantier.getOuvriersAssignes(),
            chantier.getTempsBaseSecondes(),
            chantier.isAmelioration()
        );
    }

    // =========================================
// RECUPERATION PAR ROYAUME
// =========================================

public List<ChantierConstructionResponse> trouverParRoyaumeId(
        Integer royaumeId) {

    return chantierDAO.findByRoyaume_Id(royaumeId)
        .stream()
        .map(this::convertirEnResponse)
        .toList();
}
}
