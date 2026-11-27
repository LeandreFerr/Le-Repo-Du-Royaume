
package royaume.restcontroller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import royaume.dto.request.PropositionEchangeRequest;
import royaume.dto.response.PropositionEchangeResponse;
import royaume.model.PropositionEchange;
import royaume.model.QuantiteRessource;
import royaume.service.PropositionEchangeService;

@RestController
@RequestMapping("/api/propositions-echange")
public class PropositionEchangeRestController {

    private final PropositionEchangeService propositionService;

    public PropositionEchangeRestController(
            PropositionEchangeService propositionService) {

        this.propositionService = propositionService;
    }

    // =========================================
    // PROPOSER UN ECHANGE AU ROYAUME ID 2
    // =========================================

    @PostMapping
public ResponseEntity<PropositionEchangeResponse> creerProposition(
        @RequestBody PropositionEchangeRequest request) {

        QuantiteRessource offerte = new QuantiteRessource(
            request.offerte().quantite(),
            request.offerte().ressource()
        );

        QuantiteRessource demandee = new QuantiteRessource(
            request.demandee().quantite(),
            request.demandee().ressource()
        );
    PropositionEchange echange =
        propositionService.creerProposition(
            request.expediteurId(),
            offerte,
            demandee
        );

    PropositionEchangeResponse response =
        propositionService.convertirEnResponse(echange);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(response);
}

    // =========================================
    // RECUPERER LES PROPOSITIONS DU ROYAUME
    // =========================================

    @GetMapping("/destinataire/{id}")
    public ResponseEntity<List<PropositionEchangeResponse>>
            recupererPropositionsDestinataire(
                    @PathVariable Integer id) {

        return ResponseEntity.ok(
            propositionService.recupererPropositionsDestinataire(id)
        );
    }
}
