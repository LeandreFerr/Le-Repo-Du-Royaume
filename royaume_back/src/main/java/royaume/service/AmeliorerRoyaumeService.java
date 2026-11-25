package royaume.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import royaume.dao.IDAORoyaume;
import royaume.dao.PalierNiveauIDAO;
import royaume.model.PalierNiveau;
import royaume.model.Royaume;

@Service
public class AmeliorerRoyaumeService {

    private final IDAORoyaume royaumeDAO;
    private final PalierNiveauIDAO palierDAO;

    public AmeliorerRoyaumeService(
        IDAORoyaume royaumeDAO,
        PalierNiveauIDAO palierDAO
    ) {
        this.royaumeDAO = royaumeDAO;
        this.palierDAO = palierDAO;
    }

    @Transactional(readOnly = true)
    public PalierNiveau obtenirProchainPalier(Integer royaumeId) {

        Royaume royaume = royaumeDAO.findById(royaumeId)
            .orElseThrow(() ->
                new IllegalArgumentException("Royaume introuvable.")
            );

        int niveauSuivant = royaume.getNiveau() + 1;

        return palierDAO.findByNiveau(niveauSuivant)
            .orElseThrow(() ->
                new IllegalStateException(
                    "Aucun palier suivant disponible."
                )
            );
    }

    @Transactional
    public boolean ameliorerRoyaume(Integer royaumeId) {

        Royaume royaume = royaumeDAO.findById(royaumeId)
            .orElseThrow(() ->
                new IllegalArgumentException("Royaume introuvable.")
            );

        List<PalierNiveau> paliers = palierDAO.findAll();

        if (!royaume.peutMonterDeNiveau(paliers)) {
            return false;
        }

        return royaume.ameliorerNiveau(paliers);
    }
}