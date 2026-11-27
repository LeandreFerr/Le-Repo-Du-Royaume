
package royaume.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import royaume.dao.IDAOPropositionEchange;
import royaume.dao.IDAORoyaume;
import royaume.dto.QuantiteRessourceDTO;
import royaume.dto.response.PropositionEchangeResponse;
import royaume.model.PropositionEchange;
import royaume.model.QuantiteRessource;
import royaume.model.Royaume;
import royaume.model.StatutEchange;

@Service
@Transactional
public class PropositionEchangeService {


    private static final Integer ID_ROYAUME_ADVERSE = 2;

    private final IDAORoyaume royaumeDAO;
    private final IDAOPropositionEchange propositionEchangeDAO;

    public PropositionEchangeService(
            IDAORoyaume royaumeDAO,
            IDAOPropositionEchange propositionEchangeDAO) {

        this.royaumeDAO = royaumeDAO;
        this.propositionEchangeDAO = propositionEchangeDAO;
    }

    private Royaume recupererRoyaume(Integer id) {
        return royaumeDAO.findById(id)
            .orElseThrow(() ->
                new RuntimeException("Royaume introuvable : " + id)
            );
    }

    // =========================================
    // CREER UNE PROPOSITION
    // =========================================
    public PropositionEchange creerProposition(
            Integer expediteurId,
            QuantiteRessource offerte,
            QuantiteRessource demandee) {
        return creerProposition(expediteurId, ID_ROYAUME_ADVERSE, offerte, demandee);
    }

    public PropositionEchange creerProposition(
            Integer expediteurId,
            Integer destinataireId,
            QuantiteRessource offerte,
            QuantiteRessource demandee) {

        Integer cibleId = destinataireId != null ? destinataireId : ID_ROYAUME_ADVERSE;
        Royaume expediteur = recupererRoyaume(expediteurId);
        Royaume destinataire = recupererRoyaume(cibleId);

        if (expediteur.getId().equals(destinataire.getId())) {
            throw new RuntimeException(
                "Un royaume ne peut pas proposer un échange à lui-même."
            );
        }

        if (expediteur.isEnRuine() || destinataire.isEnRuine()) {
            throw new RuntimeException(
                "Un royaume en ruine ne peut pas effectuer d'échange."
            );
        }

        verifierQuantite(offerte, "offerte");
        verifierQuantite(demandee, "demandée");

        if (!expediteur.aAssezDeRessources(
                offerte.getRessource(),
                offerte.getQuantite())) {
            throw new RuntimeException(
                "Tu ne possèdes pas suffisamment de ressources pour cette proposition."
            );
        }

        PropositionEchange echange = new PropositionEchange(
            null,
            LocalDateTime.now(),
            StatutEchange.EN_ATTENTE,
            expediteur,
            destinataire,
            offerte,
            demandee
        );

        return propositionEchangeDAO.save(echange);
    }
    private void verifierQuantite(
            QuantiteRessource quantite,
            String nom) {

        if (quantite == null
                || quantite.getRessource() == null
                || quantite.getQuantite() <= 0) {

            throw new IllegalArgumentException(
                "La quantité " + nom + " est invalide."
            );
        }
    }

    // =========================================
    // RECUPERER LES PROPOSITIONS
    // =========================================

    @Transactional(readOnly = true)
    public List<PropositionEchangeResponse> recupererPropositions() {

        return propositionEchangeDAO.findAll()
            .stream()
            .map(this::convertirEnResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<PropositionEchangeResponse>
            recupererPropositionsDestinataire(Integer destinataireId) {

        return propositionEchangeDAO
            .findByDestinataireId(destinataireId)
            .stream()
            .map(this::convertirEnResponse)
            .toList();
    }

    public PropositionEchangeResponse convertirEnResponse(
            PropositionEchange echange) {

        return new PropositionEchangeResponse(
            echange.getId(),
            echange.getDateCreation(),
            echange.getStatut(),
            echange.getExpediteur().getId(),
            echange.getExpediteur().getNom(),
            echange.getDestinataire().getId(),
            echange.getDestinataire().getNom(),
            convertirQuantite(echange.getOfferte()),
            convertirQuantite(echange.getDemandee())
        );
    }

    private QuantiteRessourceDTO convertirQuantite(
            QuantiteRessource quantite) {

        if (quantite == null) {
            return null;
        }

        return new QuantiteRessourceDTO(
            quantite.getRessource(),
            quantite.getQuantite()
        );
    }
// =========================================
// ACCEPTER UNE PROPOSITION
// =========================================

public PropositionEchange accepterProposition(Integer id) {

    PropositionEchange proposition = propositionEchangeDAO.findById(id)
        .orElseThrow(() ->
            new RuntimeException("Proposition introuvable : " + id)
        );

    if (proposition.getStatut() != StatutEchange.EN_ATTENTE) {
        throw new RuntimeException(
            "Cette proposition n'est plus en attente."
        );
    }

    proposition.accepter();

    if (proposition.getStatut() != StatutEchange.ACCEPTE) {
        throw new RuntimeException(
            "Impossible d'effectuer cet échange. Vérifiez les ressources disponibles."
        );
    }

    return propositionEchangeDAO.save(proposition);
}


// =========================================
// REFUSER UNE PROPOSITION
// =========================================

public PropositionEchange refuserProposition(Integer id) {

    PropositionEchange proposition = propositionEchangeDAO.findById(id)
        .orElseThrow(() ->
            new RuntimeException("Proposition introuvable : " + id)
        );

    if (proposition.getStatut() != StatutEchange.EN_ATTENTE) {
        throw new RuntimeException(
            "Cette proposition n'est plus en attente."
        );
    }

    proposition.refuser();

    return propositionEchangeDAO.save(proposition);
}

}

