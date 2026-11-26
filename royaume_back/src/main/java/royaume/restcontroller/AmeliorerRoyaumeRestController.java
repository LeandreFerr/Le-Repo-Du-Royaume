
package royaume.restcontroller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import royaume.dto.response.AmeliorerRoyaumeResponse;
import royaume.service.AmeliorerRoyaumeService;

@RestController
@RequestMapping("/api/royaumes")
@CrossOrigin(origins = "http://localhost:4200")
public class AmeliorerRoyaumeRestController {

    private final AmeliorerRoyaumeService service;

    public AmeliorerRoyaumeRestController(
        AmeliorerRoyaumeService service
    ) {
        this.service = service;
    }

    // Informations nécessaires à l'amélioration
    @GetMapping("/{id}/prochain-palier")
    public ResponseEntity<AmeliorerRoyaumeResponse> prochainPalier(
        @PathVariable Integer id
    ) {
        return ResponseEntity.ok(
            service.obtenirInformationsAmelioration(id)
        );
    }

    // Amélioration effective du royaume
    @PostMapping("/{id}/ameliorer")
    public ResponseEntity<?> ameliorer(
        @PathVariable Integer id
    ) {

        boolean succes = service.ameliorerRoyaume(id);

        if (!succes) {
            return ResponseEntity.badRequest()
                .body("Population ou ressources insuffisantes pour améliorer le royaume.");
        }

        return ResponseEntity.ok(
            "Félicitations ! Votre royaume a gagné un niveau."
        );
    }
}
