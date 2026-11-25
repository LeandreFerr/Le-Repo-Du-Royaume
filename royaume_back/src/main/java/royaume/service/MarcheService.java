package royaume.service;

import java.util.List;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import royaume.dao.IDAOMarche;
import royaume.dao.IDAOOffreMarche;
import royaume.dao.IDAORoyaume;
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
            offre.getPropose(),
            offre.getDemande()
        ))
        .toList();
}

  public OffreMarche recupererOffre(Integer id) {
    return offreMarcheDAO.findById(id)
        .orElseThrow(() ->
            new RuntimeException("Offre introuvable : " + id)
        );
}

    @Transactional
    public void annulerOffre(Integer id) {

        OffreMarche offre = offreMarcheDAO.findById(id)
            .orElseThrow(() ->
                new RuntimeException("Offre introuvable : " + id)
            );

        offre.setStatut(StatutOffre.ANNULEE);

        offreMarcheDAO.save(offre);
    }
}
