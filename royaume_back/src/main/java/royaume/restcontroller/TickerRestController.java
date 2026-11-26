package royaume.restcontroller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.Positive;
import royaume.dto.response.TickerResponse;
import royaume.service.TickerService;

@RestController
@Validated
@RequestMapping ("/api/ticker/parties/{partieId}")
public class TickerRestController {

    private final TickerService tickerService;

    public TickerRestController (TickerService tickerService) {
        this.tickerService = tickerService;
    }

    @PostMapping ("/tick")
    public ResponseEntity<TickerResponse> executerTick(@Positive @PathVariable Integer partieId, @Positive @RequestParam(defaultValue = "60") long secondes) {
        return ResponseEntity.ok(tickerService.executerTick(partieId, secondes));
    }

    @GetMapping
    public ResponseEntity<TickerResponse> trouverTempsCourant(@Positive @PathVariable Integer partieId) {
        return ResponseEntity.ok(tickerService.getTempsCourant(partieId));
    }

}
