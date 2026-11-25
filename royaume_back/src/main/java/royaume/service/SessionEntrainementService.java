package royaume.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import royaume.dao.IDAORoyaume;
import royaume.dao.IDAOSessionEntrainement;
import royaume.dto.request.EntrainerTroupeRequest;
import royaume.dto.response.ContingentTroupeResponse;
import royaume.dto.response.SessionEntrainementResponse;
import royaume.model.Royaume;
import royaume.model.militaire.ContingentTroupe;
import royaume.model.militaire.SessionEntrainement;

@Service
@Transactional(readOnly = true)
public class SessionEntrainementService {

    private final IDAOSessionEntrainement daoSession;
    private final IDAORoyaume daoRoyaume;

    public SessionEntrainementService(IDAOSessionEntrainement daoSession, IDAORoyaume daoRoyaume) {
        this.daoSession = daoSession;
        this.daoRoyaume = daoRoyaume;
    }

    @Transactional
    public SessionEntrainementResponse entrainer(EntrainerTroupeRequest request) {

        Royaume royaume = trouverRoyaume(request.royaumeId());

        boolean accepte = royaume.entrainerTroupe(request.type(), request.quantite(), LocalDateTime.now());

        if (!accepte) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Entraînement impossible : royaume en ruine, population ou ressources insuffisantes.");
        }

        // flush : la cascade persiste la session et lui attribue son id
        daoRoyaume.saveAndFlush(royaume);

        List<SessionEntrainement> sessions = royaume.getSessions();
        return convertirEnResponse(sessions.get(sessions.size() - 1));
    }

    public List<SessionEntrainementResponse> sessions(Integer royaumeId, boolean finalisee) {

        Royaume royaume = trouverRoyaume(royaumeId);

        return daoSession.findByRoyaumeAndFinalisee(royaume, finalisee).stream()
                .map(this::convertirEnResponse)
                .toList();
    }

    public List<ContingentTroupeResponse> garnison(Integer royaumeId) {

        return trouverRoyaume(royaumeId).getGarnison().stream()
                .map(this::convertirContingentEnResponse)
                .toList();
    }

    private Royaume trouverRoyaume(Integer id) {
        return daoRoyaume.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Le royaume " + id + " n'existe pas."));
    }

    private SessionEntrainementResponse convertirEnResponse(SessionEntrainement s) {
        return new SessionEntrainementResponse(
                s.getId(),
                s.getTypeTroupe(),
                s.getQuantite(),
                s.getDateDebut(),
                s.getDateFinPrevue(),
                s.isFinalisee(),
                s.getRoyaume().getId()
        );
    }

    private ContingentTroupeResponse convertirContingentEnResponse(ContingentTroupe c) {
        return new ContingentTroupeResponse(c.getType(), c.getQuantite());
    }
}