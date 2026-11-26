
package royaume.restcontroller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Positive;
import royaume.dto.response.ChantierConstructionResponse;
import royaume.service.ChantierService;

@RestController
@Validated
@RequestMapping("/api/chantiers")
public class ChantierRestController {

    private final ChantierService chantierService;

    public ChantierRestController(ChantierService chantierService) {
        this.chantierService = chantierService;
    }

    @GetMapping
    public ResponseEntity<List<ChantierConstructionResponse>>
            trouverTous() {

        return ResponseEntity.ok(
            chantierService.trouverTous()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChantierConstructionResponse>
            trouverParId(
                @PathVariable @Positive Integer id) {

        return ResponseEntity.ok(
            chantierService.trouverParId(id)
        );
    }

    @GetMapping("/batiment/{batimentId}")
    public ResponseEntity<ChantierConstructionResponse>
            trouverParBatiment(
                @PathVariable @Positive Integer batimentId) {

        return ResponseEntity.ok(
            chantierService.trouverParBatimentId(batimentId)
        );
    }

    @PatchMapping("/{id}/ouvriers/ajouter")
    public ResponseEntity<ChantierConstructionResponse>
            ajouterOuvriers(
                @PathVariable @Positive Integer id,
                @RequestParam @Positive int nombre) {

        return ResponseEntity.ok(
            chantierService.ajouterOuvriers(id, nombre)
        );
    }

    @PatchMapping("/{id}/ouvriers/retirer")
    public ResponseEntity<ChantierConstructionResponse>
            retirerOuvriers(
                @PathVariable @Positive Integer id,
                @RequestParam @Positive int nombre) {

        return ResponseEntity.ok(
            chantierService.retirerOuvriers(id, nombre)
        );
    }
}
