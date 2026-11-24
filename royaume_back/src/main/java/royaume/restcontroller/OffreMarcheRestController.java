package royaume.restcontroller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import royaume.dto.request.OffreMarcheRequest;
import royaume.dto.response.OffreMarcheResponse;
import royaume.model.OffreMarche;
import royaume.service.MarcheService;

@RestController
@RequestMapping("/api/marche")
@CrossOrigin(origins = "http://localhost:4200")
public class OffreMarcheRestController {

    private final MarcheService marcheService;

    public OffreMarcheRestController(MarcheService marcheService) {
        this.marcheService = marcheService;
    }
/* 
    @PostMapping("/offres")
    public ResponseEntity<Void> publierOffre(
            @RequestBody OffreMarcheRequest request) {

        marcheService.publierOffre(
            request.vendeurId(),
            request.propose(),
            request.demande()
        );

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
*/

@PostMapping("/offres")
public ResponseEntity<OffreMarcheResponse> publierOffre(
        @RequestBody OffreMarcheRequest request) {

    OffreMarche offre = marcheService.publierOffre(
        request.vendeurId(),
        request.propose(),
        request.demande()
    );

    OffreMarcheResponse response = new OffreMarcheResponse(
        offre.getId(),
        offre.getDatePublication(),
        offre.getStatut(),
        offre.getVendeur().getId(),
        offre.getVendeur().getNom(),
        offre.getPropose(),
        offre.getDemande()
    );

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}

}
