package royaume.restcontroller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;


import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import royaume.dto.request.PartieRequest;
import royaume.dto.response.PartieResponse;
import royaume.model.StatutPartie;
import royaume.service.PartieService;

@RestController 
@Validated 
@RequestMapping ("/api/parties")
public class PartieRestController {

    private final PartieService partieService;

    PartieRestController(PartieService partieService) {
        this.partieService = partieService;
    }

    @GetMapping 
    public ResponseEntity<List<PartieResponse>> trouverToutes() {
        return ResponseEntity.ok(partieService.trouverToutes());
    }

    @GetMapping ("/{id}")
    public ResponseEntity<PartieResponse> trouverParId(@PathVariable @Positive Integer id) {
        return ResponseEntity.ok(partieService.trouverParId(id));
    }

    @GetMapping ("/statut/{statut}")
    public ResponseEntity<List<PartieResponse>> trouverParStatut(@PathVariable StatutPartie statut) {
        return ResponseEntity.ok(partieService.trouverParStatut(statut));
    }

    @GetMapping ("/royaume/{royaumeId}")
    public ResponseEntity<PartieResponse> trouverParRoyaume(@PathVariable @Positive Integer royaumeId) {
        return ResponseEntity.ok(partieService.trouverParRoyaumeId(royaumeId));
    }

    @PostMapping 
    public ResponseEntity<PartieResponse> creer(@Valid @RequestBody PartieRequest partieRequest) {
        PartieResponse response = partieService.creer(partieRequest);
        return ResponseEntity.created(URI.create("/api/parties/" + response.id())).body(response);
    }

    @DeleteMapping ("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable @Positive Integer id) {
        partieService.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping ("/{id}/demarrer")
    public ResponseEntity<PartieResponse> demarrer(@PathVariable @Positive Integer id) {
        return ResponseEntity.ok(partieService.demarrer(id));
    }

    @PatchMapping ("/{id}/terminer")
    public ResponseEntity<PartieResponse> terminer(@PathVariable @Positive Integer id) {
        return ResponseEntity.ok(partieService.terminer(id));
    }
}
