package royaume.service;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import royaume.dao.IDAOMarche;
import royaume.dao.IDAOOffreMarche;
import royaume.dao.IDAORoyaume;
import royaume.model.Marche;
import royaume.model.OffreMarche;
import royaume.model.QuantiteRessource;
import royaume.model.Royaume;

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
}
