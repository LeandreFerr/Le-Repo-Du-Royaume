package royaume.restcontroller;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import royaume.dto.request.CarteRequest;
import royaume.dto.response.CarteResponse;
import royaume.dto.response.EmplacementCarteResponse;
import royaume.service.CarteService;

@RestController
@Validated
@RequestMapping("/api/cartes")
public class CarteRestController {

    private final CarteService carteService;

    public CarteRestController(CarteService carteService) {
        this.carteService = carteService;
    }

    @PostMapping
    public ResponseEntity<CarteResponse> creer(
            @Valid @RequestBody CarteRequest request) {

        CarteResponse response = carteService.creer(request);

        return ResponseEntity.created(URI.create("/api/cartes/" + response.id())).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarteResponse> trouverParId( @PathVariable @Positive Integer id) {

        return ResponseEntity.ok(carteService.trouverParId(id));
    }

    @GetMapping("/partie/{partieId}")
    public ResponseEntity<CarteResponse> trouverParPartie( @PathVariable @Positive Integer partieId) {

        return ResponseEntity.ok(carteService.trouverParPartieId(partieId));
    }

    @GetMapping("/{carteId}/emplacements")
    public ResponseEntity<List<EmplacementCarteResponse>> trouverEmplacements( @PathVariable @Positive Integer carteId) {

        return ResponseEntity.ok(carteService.trouverEmplacements(carteId));
    }

    @GetMapping("/{carteId}/emplacements/libres")
    public ResponseEntity<List<EmplacementCarteResponse>> trouverEmplacementsLibres( @PathVariable @Positive Integer carteId) {

        return ResponseEntity.ok(carteService.trouverEmplacementsLibres(carteId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer( @PathVariable @Positive Integer id) {

        carteService.supprimer(id);

        return ResponseEntity.noContent().build();
    }
}
