package royaume.restcontroller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import royaume.dto.request.EmplacementCarteRequest;
import royaume.dto.response.EmplacementCarteResponse;
import royaume.service.EmplacementCarteService;

@RestController
@RequestMapping("/api/emplacements")
public class EmplacementCarteRestController {

    private final EmplacementCarteService emplacementCarteService;

    public EmplacementCarteRestController(EmplacementCarteService emplacementCarteService) {

        this.emplacementCarteService = emplacementCarteService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmplacementCarteResponse> trouverParId( @PathVariable @Positive  Integer id) {

        return ResponseEntity.ok(emplacementCarteService.trouverParId(id));
    }

    @PutMapping("/{id}/royaume")
    public ResponseEntity<EmplacementCarteResponse> assignerRoyaume( @PathVariable @Positive Integer id, @Valid @RequestBody EmplacementCarteRequest request) {

        return ResponseEntity.ok(emplacementCarteService.assignerRoyaume(id, request));
    }

    @DeleteMapping("/{id}/royaume")
    public ResponseEntity<EmplacementCarteResponse> liberer( @PathVariable @Positive Integer id) {

        return ResponseEntity.ok(emplacementCarteService.liberer(id));
    }
}
