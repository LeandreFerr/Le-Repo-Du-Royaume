
package royaume.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import royaume.dao.IDAORoyaume;
import royaume.dao.PalierNiveauIDAO;
import royaume.dto.QuantiteRessourceDTO;
import royaume.dto.response.AmeliorerRoyaumeResponse;
import royaume.dto.response.PalierNiveauResponse;
import royaume.dto.response.RessourcesAmeliorerRoyaumeResponse;
import royaume.model.PalierNiveau;
import royaume.model.QuantiteRessource;
import royaume.model.Ressource;
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

    // =========================================================
    // RECUPERER LES INFORMATIONS DU PROCHAIN PALIER
    // =========================================================

    @Transactional(readOnly = true)
    public AmeliorerRoyaumeResponse obtenirInformationsAmelioration(
        Integer royaumeId
    ) {

        Royaume royaume = royaumeDAO.findById(royaumeId)
            .orElseThrow(() ->
                new IllegalArgumentException("Royaume introuvable.")
            );

        List<PalierNiveau> paliers =
            palierDAO.findAllByOrderByNiveauAsc();

        int niveauActuel = royaume.getNiveau();

        // Aucun niveau supérieur possible
        if (niveauActuel >= 10) {

            return new AmeliorerRoyaumeResponse(
                niveauActuel,
                null,
                populationTotale(royaume),
                0,
                0,
                true,
                List.of(),
                false
            );
        }

        // Recherche du prochain palier
        PalierNiveau palierSuivant = paliers.stream()
            .filter(p -> p.getNiveau() == niveauActuel + 1)
            .findFirst()
            .orElse(null);

        // Aucun palier configuré
        if (palierSuivant == null) {

            return new AmeliorerRoyaumeResponse(
                niveauActuel,
                null,
                populationTotale(royaume),
                0,
                0,
                true,
                List.of(),
                false
            );
        }

        // =====================================================
        // POPULATION
        // =====================================================

        int populationDisponible = populationTotale(royaume);

        int populationRequise =
            palierSuivant.getPopulationRequise();

        int populationManquante = Math.max(
            0,
            populationRequise - populationDisponible
        );

        boolean populationSuffisante =
            populationDisponible >= populationRequise;

        // =====================================================
        // RESSOURCES
        // =====================================================

        List<RessourcesAmeliorerRoyaumeResponse> ressources =
            new ArrayList<>();

        for (QuantiteRessource prerequis :
                palierSuivant.getPrerequis()) {

            Ressource ressource = prerequis.getRessource();

            int quantiteRequise =
                PalierNiveau.getCoutRessource(
                    ressource,
                    royaume,
                    paliers
                );

            int quantiteManquante =
                PalierNiveau.getRessourceManquante(
                    ressource,
                    royaume,
                    paliers
                );

            int quantitePossedee = royaume.getRessources()
                .stream()
                .filter(r -> r.getRessource() == ressource)
                .mapToInt(QuantiteRessource::getQuantite)
                .sum();

            boolean suffisante = quantiteManquante == 0;

            ressources.add(
                new RessourcesAmeliorerRoyaumeResponse(
                    ressource,
                    quantitePossedee,
                    quantiteRequise,
                    quantiteManquante,
                    suffisante
                )
            );
        }

        // =====================================================
        // VERIFICATION METIER GLOBALE
        // =====================================================

        boolean ameliorationPossible =
            royaume.peutMonterDeNiveau(paliers);

        // =====================================================
        // CONSTRUCTION DU DTO
        // =====================================================

        PalierNiveauResponse prochainPalierResponse =
    new PalierNiveauResponse(
        palierSuivant.getId(),
        palierSuivant.getNiveau(),
        palierSuivant.getPopulationRequise(),
        palierSuivant.getPrerequis().stream()
            .map(q -> new QuantiteRessourceDTO(
                q.getRessource(),
                q.getQuantite()
            ))
            .toList()
    );

        return new AmeliorerRoyaumeResponse(
            niveauActuel,
            prochainPalierResponse,
            populationDisponible,
            populationRequise,
            populationManquante,
            populationSuffisante,
            ressources,
            ameliorationPossible
        );
    }

    // =========================================================
    // POPULATION TOTALE MOBILISABLE
    // =========================================================

    private int populationTotale(Royaume royaume) {

        return royaume.getPopulationDisponible()
            + royaume.getPopulationOuvriere()
            + royaume.getPopulationMilitaire()
            + royaume.getPopulationEnEntrainement();
    }

    // =========================================================
    // AMELIORER LE ROYAUME
    // =========================================================

    @Transactional
    public boolean ameliorerRoyaume(Integer royaumeId) {

        Royaume royaume = royaumeDAO.findById(royaumeId)
            .orElseThrow(() ->
                new IllegalArgumentException("Royaume introuvable.")
            );

        List<PalierNiveau> paliers =
            palierDAO.findAllByOrderByNiveauAsc();

        if (!royaume.peutMonterDeNiveau(paliers)) {
            return false;
        }

        return royaume.ameliorerNiveau(paliers);
    }
}
