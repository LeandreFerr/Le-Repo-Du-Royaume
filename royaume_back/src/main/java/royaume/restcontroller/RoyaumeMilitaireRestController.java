package royaume.restcontroller;

import java.util.List;

import jakarta.validation.constraints.Positive;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import royaume.dto.response.ContingentTroupeResponse;
import royaume.dto.response.SessionEntrainementResponse;
import royaume.service.SessionEntrainementService;


// On a pas encore de royaume controller ?
// Je crois pas, alors je met ca la
// Faudra ptet le renommer
// (ptet pas le meme chemin pour pas la meme fonctionnalité en fait)


@RestController
@RequestMapping("/api/royaumes/{royaumeId}")
public class RoyaumeMilitaireRestController {

    private final SessionEntrainementService sessionService;

    public RoyaumeMilitaireRestController(SessionEntrainementService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping("/garnison")
    public ResponseEntity<List<ContingentTroupeResponse>> garnison(
            @PathVariable @Positive Integer royaumeId) {

        return ResponseEntity.ok(sessionService.garnison(royaumeId));
    }

    @GetMapping("/sessions-entrainement")
    public ResponseEntity<List<SessionEntrainementResponse>> sessions(
            @PathVariable @Positive Integer royaumeId,
            @RequestParam(defaultValue = "false") boolean finalisee) {

        return ResponseEntity.ok(sessionService.sessions(royaumeId, finalisee));
    }
}