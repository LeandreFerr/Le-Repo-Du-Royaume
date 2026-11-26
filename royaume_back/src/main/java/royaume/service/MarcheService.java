package royaume.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import royaume.dao.IDAOMarche;
import royaume.dao.IDAOOffreMarche;
import royaume.dao.IDAORoyaume;
import royaume.dto.QuantiteRessourceDTO;
import royaume.dto.response.OffreMarcheResponse;
import royaume.model.Marche;
import royaume.model.OffreMarche;
import royaume.model.QuantiteRessource;
import royaume.model.Royaume;
import royaume.model.StatutOffre;

@Service
@Transactional
public class MarcheService {
	
	private final IDAOOffreMarche offreMarcheDAO;
	private final IDAORoyaume royaumeDAO;
	private final IDAOMarche marcheDAO;
	
	public MarcheService(IDAOOffreMarche offreMarcheDAO,IDAORoyaume royaumeDAO, IDAOMarche marcheDAO) {
		this.royaumeDAO = royaumeDAO;
		this.offreMarcheDAO = offreMarcheDAO;
		this.marcheDAO = marcheDAO;
	}
	
	 // =========================================
    // RECUPERATION DU ROYAUME ET DU MARCHE PAR ID
    // =========================================

    private Royaume recupererRoyaume(Integer id) {
        return royaumeDAO.findById(id)
            .orElseThrow(() -> new RuntimeException(
                "Royaume introuvable : " + id
            ));
    }
    
    private Marche recupererOuCreerMarche() {
        return marcheDAO.findAll().stream()
            .findFirst()
            .orElseGet(() -> marcheDAO.save(new Marche(null, "Marche")));
    }
    
    // =========================================
    // OFFRE MARCHE
    // =========================================
    
    public OffreMarche publierOffre(Integer royaumeIdVendeur, QuantiteRessource aVendre, QuantiteRessource aRecevoir) {
        Royaume vendeur = recupererRoyaume(royaumeIdVendeur);
        Marche marche = recupererOuCreerMarche();
        
        OffreMarche offre = marche.publierOffre(vendeur, aVendre, aRecevoir);

        
        return offreMarcheDAO.save(offre);
    }


    @Transactional(readOnly = true)
public List<OffreMarcheResponse> recupererOffresResponse() {

    	
    return offreMarcheDAO.findAll()
        .stream()
        .map(offre -> new OffreMarcheResponse(
            offre.getId(),
            offre.getDatePublication(),
            offre.getStatut(),
            offre.getVendeur().getId(),
            offre.getVendeur().getNom(),
            convertirQuantite(offre.getPropose()),
            convertirQuantite(offre.getDemande())
        ))
        .toList();
    
    
}
    private QuantiteRessourceDTO convertirQuantite(QuantiteRessource q) {
	    if (q == null) return null;
	    return new QuantiteRessourceDTO(q.getRessource(), q.getQuantite());
	}

  public OffreMarche recupererOffre(Integer id) {
    return offreMarcheDAO.findById(id)
        .orElseThrow(() ->
            new RuntimeException("Offre introuvable : " + id)
        );
}

    
@Transactional
public void annulerOffre(Integer id) {

    // 1. Récupérer l'offre
    OffreMarche offre = offreMarcheDAO.findById(id)
        .orElseThrow(() ->
            new RuntimeException("Offre introuvable : " + id)
        );

    // 2. Vérifier que l'offre est encore disponible
    if (offre.getStatut() != StatutOffre.DISPONIBLE) {
        throw new RuntimeException(
            "Impossible d'annuler cette offre : elle n'est plus disponible."
        );
    }

    // 3. Récupérer le vendeur
    Royaume vendeur = offre.getVendeur();

    if (vendeur == null) {
        throw new RuntimeException("Vendeur introuvable.");
    }

    // 4. Récupérer les ressources proposées
    QuantiteRessource ressourcesProposees = offre.getPropose();

    if (ressourcesProposees == null) {
        throw new RuntimeException("Les ressources proposées sont introuvables.");
    }

    // 5. Restituer les ressources au vendeur
    vendeur.crediterRessources(
        ressourcesProposees.getRessource(),
        ressourcesProposees.getQuantite()
    );

    // 6. Passer l'offre au statut ANNULEE
    offre.setStatut(StatutOffre.ANNULEE);

    // 7. Sauvegarder les modifications
    royaumeDAO.save(vendeur);
    offreMarcheDAO.save(offre);
}


    
@Transactional
public void accepterOffre(Integer id, Integer acheteurId) {

    // 1. Récupérer l'offre
    OffreMarche offre = offreMarcheDAO.findById(id)
        .orElseThrow(() ->
            new RuntimeException("Offre introuvable : " + id)
        );

    // 2. Vérifier que l'offre est disponible
    if (offre.getStatut() != StatutOffre.DISPONIBLE) {
        throw new RuntimeException(
            "Cette offre n'est plus disponible."
        );
    }

    // 3. Récupérer le vendeur
    Royaume vendeur = offre.getVendeur();

    if (vendeur == null) {
        throw new RuntimeException("Vendeur introuvable.");
    }

    // 4. Récupérer l'acheteur
    Royaume acheteur = recupererRoyaume(acheteurId);

    // 5. Empêcher un royaume d'accepter sa propre offre
    if (vendeur.getId().equals(acheteur.getId())) {
        throw new RuntimeException(
            "Un royaume ne peut pas accepter sa propre offre."
        );
    }

    // 6. Récupérer les ressources demandées
    QuantiteRessource demande = offre.getDemande();

    // 7. Récupérer les ressources proposées
    QuantiteRessource propose = offre.getPropose();

    if (demande == null || propose == null) {
        throw new RuntimeException(
            "Les ressources de l'offre sont invalides."
        );
    }

    // 8. Vérifier que l'acheteur possède suffisamment
    // de ressources pour effectuer l'échange
    if (!acheteur.aAssezDeRessources(
            demande.getRessource(),
            demande.getQuantite())) {

        throw new RuntimeException(
            "L'acheteur ne possède pas suffisamment de ressources."
        );
    }

    // 9. Débiter les ressources demandées chez l'acheteur
    boolean debitReussi = acheteur.debiterRessources(
        demande.getRessource(),
        demande.getQuantite()
    );

    if (!debitReussi) {
        throw new RuntimeException(
            "Impossible de débiter les ressources de l'acheteur."
        );
    }

    // 10. Créditer les ressources demandées chez le vendeur
    vendeur.crediterRessources(
        demande.getRessource(),
        demande.getQuantite()
    );

    // 11. Créditer les ressources proposées chez l'acheteur
    acheteur.crediterRessources(
        propose.getRessource(),
        propose.getQuantite()
    );

    // 12. Passer l'offre au statut CONCLUE
    offre.setStatut(StatutOffre.CONCLUE);

    // 13. Sauvegarder les royaumes et l'offre
    royaumeDAO.save(vendeur);
    royaumeDAO.save(acheteur);
    offreMarcheDAO.save(offre);
}
}