package royaume.restcontroller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import royaume.model.PalierNiveau;
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

    @GetMapping("/{id}/prochain-palier")
    public ResponseEntity<PalierNiveau> prochainPalier(
        @PathVariable Integer id
    ) {
        return ResponseEntity.ok(
            service.obtenirProchainPalier(id)
        );
    }

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