package royaume.restcontroller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.Positive;
import royaume.dto.response.PopulationResponse;
import royaume.service.PopulationService;

@RestController
@Validated
@RequestMapping("/api/populations")
public class PopulationRestController {

    private final PopulationService populationService;

    public PopulationRestController(PopulationService populationService) {
        this.populationService = populationService;
    }

    @GetMapping
    public ResponseEntity<List<PopulationResponse>> trouverTous() {
        return ResponseEntity.ok(populationService.trouverTous());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PopulationResponse> trouverParId(@PathVariable @Positive Integer id) {
        return ResponseEntity.ok(populationService.trouverParId(id));
    }
}