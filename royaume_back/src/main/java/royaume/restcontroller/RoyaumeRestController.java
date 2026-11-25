package royaume.restcontroller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import royaume.dto.request.RoyaumeRequest;
import royaume.dto.response.RoyaumeResponse;
import royaume.service.RoyaumeService;

@RestController
@Validated
@RequestMapping("/api/royaumes")
public class RoyaumeRestController {

    private final RoyaumeService royaumeService;

    public RoyaumeRestController(RoyaumeService royaumeService) {
        this.royaumeService = royaumeService;
    }

    @GetMapping
    public ResponseEntity<List<RoyaumeResponse>> trouverTous() {
        return ResponseEntity.ok(royaumeService.trouverTous());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoyaumeResponse> trouverParId(@PathVariable @Positive Integer id) {
        return ResponseEntity.ok(royaumeService.trouverParId(id));
    }

    @GetMapping("/partie/{partieId}")
    public ResponseEntity<List<RoyaumeResponse>> trouverParPartie(@PathVariable @Positive Integer partieId) {
        return ResponseEntity.ok(royaumeService.trouverParPartie(partieId));
    }

    @PostMapping
    public ResponseEntity<RoyaumeResponse> creer(@Valid @RequestBody RoyaumeRequest royaumeRequest) {
        RoyaumeResponse response = royaumeService.creer(royaumeRequest);
        return ResponseEntity.created(URI.create("/api/royaumes/" + response.id())).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable @Positive Integer id) {
        royaumeService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
