
package royaume.restcontroller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import royaume.dto.QuantiteRessourceDTO;
import royaume.dto.request.OffreMarcheRequest;
import royaume.dto.response.OffreMarcheResponse;
import royaume.model.OffreMarche;
import royaume.model.QuantiteRessource;
import royaume.service.MarcheService;

@RestController
@RequestMapping("/api/marche")
public class OffreMarcheRestController {

    private final MarcheService marcheService;

    public OffreMarcheRestController(MarcheService marcheService) {
        this.marcheService = marcheService;
    }

    // =========================================
    // 1. PUBLIER UNE OFFRE
    // =========================================

    @PostMapping("/offres")
    public ResponseEntity<OffreMarcheResponse> publierOffre(
            @RequestBody OffreMarcheRequest request) {

        OffreMarche offre = marcheService.publierOffre(
            request.vendeurId(),
            request.propose(),
            request.demande()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(convertirEnResponse(offre));
    }

    // =========================================
    // 2. RECUPERER TOUTES LES OFFRES
    // =========================================

    @GetMapping("/offres")
    public ResponseEntity<List<OffreMarcheResponse>> recupererOffres(
            @RequestParam(required = false) Integer partieId,
            @RequestParam(required = false) Integer royaumeId) {

        List<OffreMarcheResponse> offres =
            marcheService.recupererOffresResponse(partieId, royaumeId);

        return ResponseEntity.ok(offres);
    }

    // =========================================
    // 3. RECUPERER UNE OFFRE
    // =========================================

    @GetMapping("/offres/{id}")
    public ResponseEntity<OffreMarcheResponse> recupererOffre(
            @PathVariable Integer id) {

        OffreMarche offre = marcheService.recupererOffre(id);

        return ResponseEntity.ok(convertirEnResponse(offre));
    }

    // =========================================
    // 4.bis SUPPRIMER UNE OFFRE
    // =========================================
    @DeleteMapping("/offres/{id}")
    public ResponseEntity<Void> annulerOffre(
            @PathVariable Integer id) {

        marcheService.annulerOffre(id);

        return ResponseEntity.noContent().build();
    }
 // =========================================
    // 4.bis ACCEPTER UNE OFFRE
    // =========================================
    @PostMapping("/offres/{id}/accepter")
public ResponseEntity<Void> accepterOffre(
        @PathVariable Integer id,
        @RequestParam Integer acheteurId) {

    marcheService.accepterOffre(id, acheteurId);

    return ResponseEntity.ok().build();
}
    // =========================================
    // 4. CONVERSION ENTITE -> DTO
    // =========================================

    private OffreMarcheResponse convertirEnResponse(OffreMarche offre) {
        return new OffreMarcheResponse(
            offre.getId(),
            offre.getDatePublication(),
            offre.getStatut(),
            offre.getVendeur().getId(),
            offre.getVendeur().getNom(),
            convertirQuantite(offre.getPropose()),
            convertirQuantite(offre.getDemande())
        );
    }
    private QuantiteRessourceDTO convertirQuantite(QuantiteRessource q) {
        if (q == null) return null;
        return new QuantiteRessourceDTO(q.getRessource(), q.getQuantite());
    }
}
