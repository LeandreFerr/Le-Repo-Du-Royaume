
package royaume.restcontroller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import royaume.dto.request.AmeliorationBatimentRequest;
import royaume.dto.request.BatimentProductionRequest;
import royaume.dto.request.MaisonRequest;
import royaume.dto.request.ReparationBatimentRequest;
import royaume.dto.response.BatimentResponse;
import royaume.model.batiment.Batiment;
import royaume.model.batiment.BatimentProduction;
import royaume.model.batiment.Chateau;
import royaume.model.batiment.Maison;
import royaume.service.BatimentService;

@RestController
@Validated
@RequestMapping("/api/batiments")
public class BatimentRestController {

    private final BatimentService batimentService;

    public BatimentRestController(BatimentService batimentService) {
        this.batimentService = batimentService;
    }

    // =========================================
    // RECUPERATION
    // =========================================

    @GetMapping("/{id}")
    public ResponseEntity<BatimentResponse> trouverParId(
            @PathVariable @Positive Integer id) {

        return ResponseEntity.ok(
            convertirEnResponse(batimentService.trouverParId(id))
        );
    }

    // =========================================
    // CONSTRUCTION MAISON
    // =========================================

    @PostMapping("/maisons")
    public ResponseEntity<BatimentResponse> construireMaison(
            @Valid @RequestBody MaisonRequest request) {

        Maison maison = batimentService.creerMaison(
            request.royaumeId(),
            null,
            request.nom(),
            request.ouvriersAssignes()
        );

        return ResponseEntity.ok(convertirEnResponse(maison));
    }

    // =========================================
    // CONSTRUCTION PRODUCTION
    // =========================================

    @PostMapping("/production")
    public ResponseEntity<BatimentResponse> construireProduction(
            @Valid @RequestBody BatimentProductionRequest request) {

        BatimentProduction batiment =
            batimentService.creerBatimentProduction(
                request.royaumeId(),
                null,
                request.nom(),
                request.pointsDeVie(),
                request.typeRessource(),
                request.productionBaseHeure(),
                request.ouvriersAssignes()
            );

        return ResponseEntity.ok(convertirEnResponse(batiment));
    }

    // =========================================
    // AMELIORATION
    // =========================================

    @PostMapping("/{id}/ameliorer")
    public ResponseEntity<BatimentResponse> ameliorer(
            @PathVariable @Positive Integer id,
            @Valid @RequestBody AmeliorationBatimentRequest request) {

        Batiment batiment = batimentService.ameliorerBatiment(
            id,
            request.ouvriersAssignes()
        );

        return ResponseEntity.ok(convertirEnResponse(batiment));
    }

    // =========================================
    // REPARATION
    // =========================================

    @PostMapping("/{id}/reparer")
    public ResponseEntity<BatimentResponse> reparer(
            @PathVariable @Positive Integer id,
            @Valid @RequestBody ReparationBatimentRequest request) {

        Batiment batiment = batimentService.reparerBatiment(
            id,
            request.montant(),
            request.ouvriersAssignes()
        );

        return ResponseEntity.ok(convertirEnResponse(batiment));
    }

    // =========================================
    // CONVERSION DTO
    // =========================================

private BatimentResponse convertirEnResponse(Batiment b) {

    BatimentProduction prod =
        b instanceof BatimentProduction bp ? bp : null;

    Maison maison =
        b instanceof Maison m ? m : null;

    Chateau chateau =
        b instanceof Chateau c ? c : null;

    // Détermination du type réel
    String type;

    if (chateau != null) {
        type = "CHATEAU";
    } else if (maison != null) {
        type = "MAISON";
    } else if (prod != null) {
        type = "PRODUCTION";
    } else {
        throw new IllegalStateException(
            "Type de bâtiment inconnu : " + b.getClass().getName()
        );
    }

    return new BatimentResponse(
        b.getId(),
        b.getNom(),
        b.getNiveau(),
        b.getPointsDeVie(),
        b.getPointsDeVieMax(),
        b.isEnConstruction(),
        b.getMontantReparationEnCours(),
        b.getRoyaume().getId(),
        prod == null ? null : prod.getTypeRessource(),
        prod == null ? null : prod.getProductionBaseHeure(),
        maison == null ? null : maison.getBonusPropre(),
        chateau == null ? null : chateau.getNiveauPalierDebloque(),
        chateau == null ? null : chateau.getBonusDefenseRoyaume()
    );
}


    
}
