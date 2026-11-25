package royaume.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import royaume.dao.IDAOEmplacementCarte;
import royaume.dao.IDAORoyaume;
import royaume.dto.request.EmplacementCarteRequest;
import royaume.dto.response.EmplacementCarteResponse;
import royaume.model.EmplacementCarte;
import royaume.model.Royaume;

@Service
@Transactional(readOnly = true)
public class EmplacementCarteService {

    private final IDAOEmplacementCarte daoEmplacementCarte;
    private final IDAORoyaume daoRoyaume;

    public EmplacementCarteService(
            IDAOEmplacementCarte daoEmplacementCarte,
            IDAORoyaume daoRoyaume) {

        this.daoEmplacementCarte = daoEmplacementCarte;
        this.daoRoyaume = daoRoyaume;
    }

    public EmplacementCarteResponse trouverParId(Integer id) {
        return convertirEnResponse(trouverEntiteParId(id));
    }

    @Transactional
    public EmplacementCarteResponse assignerRoyaume(
            Integer emplacementId,
            EmplacementCarteRequest request) {

        EmplacementCarte emplacement =
                trouverEntiteParId(emplacementId);

        Royaume royaume = daoRoyaume.findById(request.royaumeId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Le royaume " + request.royaumeId() + " n'existe pas."
                ));

        try {
            emplacement.assignerRoyaume(royaume);
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    exception.getMessage()
            );
        }

        EmplacementCarte emplacementSauvegarde =
                daoEmplacementCarte.save(emplacement);

        return convertirEnResponse(emplacementSauvegarde);
    }

    @Transactional
    public EmplacementCarteResponse liberer(Integer emplacementId) {

        EmplacementCarte emplacement =
                trouverEntiteParId(emplacementId);

        emplacement.liberer();

        EmplacementCarte emplacementSauvegarde =
                daoEmplacementCarte.save(emplacement);

        return convertirEnResponse(emplacementSauvegarde);
    }

    private EmplacementCarte trouverEntiteParId(Integer id) {
        return daoEmplacementCarte.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "L'emplacement " + id + " n'existe pas."
                ));
    }

    public static EmplacementCarteResponse convertirEnResponse(
            EmplacementCarte emplacement) {

        if (emplacement == null) {
            return null;
        }

        Royaume royaume = emplacement.getRoyaume();

        return new EmplacementCarteResponse(
                emplacement.getId(),
                emplacement.getNumero(),
                emplacement.getCoordX(),
                emplacement.getCoordY(),
                emplacement.isOccupe(),
                emplacement.getCarte() != null ? emplacement.getCarte().getId() : null,
                royaume != null ? royaume.getId() : null,
                royaume != null ? royaume.getNom() : null
        );
    }
}