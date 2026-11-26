package royaume.restcontroller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import royaume.dto.request.LancerAttaqueRequest;
import royaume.dto.response.AttaqueResponse;
import royaume.dto.response.ContingentTroupeResponse;
import royaume.model.militaire.StatutAttaque;
import royaume.service.AttaqueService;

@RestController
@RequestMapping("/api/attaques")
public class AttaqueRestController {

    private final AttaqueService attaqueService;

    public AttaqueRestController(AttaqueService attaqueService) {
        this.attaqueService = attaqueService;
    }

    @PostMapping
    public ResponseEntity<AttaqueResponse> lancer(
            @Valid @RequestBody LancerAttaqueRequest request) {

        AttaqueResponse response = attaqueService.lancer(request);

        return ResponseEntity.created(URI.create("/api/attaques/" + response.id())).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AttaqueResponse> trouverParId(@PathVariable @Positive Integer id) {

        return ResponseEntity.ok(attaqueService.trouverParId(id));
    }

    @GetMapping
    public ResponseEntity<List<AttaqueResponse>> rechercher(
            @RequestParam(required = false) StatutAttaque statut,
            @RequestParam(required = false) Integer attaquantId,
            @RequestParam(required = false) Integer defenseurId) {

        return ResponseEntity.ok(attaqueService.rechercher(statut, attaquantId, defenseurId));
    }
    
    @GetMapping("/royaume/{royaumeId}/troupes-disponibles")
    public List<ContingentTroupeResponse> trouverTroupesDisponibles(
            @PathVariable Integer royaumeId) {
        return attaqueService.trouverTroupesDisponibles(royaumeId);
    }
}