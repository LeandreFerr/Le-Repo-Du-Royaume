package royaume.restcontroller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import royaume.dto.request.EntrainerTroupeRequest;
import royaume.dto.response.ContingentTroupeResponse;
import royaume.dto.response.SessionEntrainementResponse;
import royaume.service.SessionEntrainementService;

@RestController
@RequestMapping("/api/sessions-entrainement")
@CrossOrigin(origins = "http://localhost:4200")
public class SessionEntrainementRestController {

    private final SessionEntrainementService sessionService;

    public SessionEntrainementRestController(SessionEntrainementService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    public ResponseEntity<SessionEntrainementResponse> entrainer(
            @Valid @RequestBody EntrainerTroupeRequest request) {

        SessionEntrainementResponse response = sessionService.entrainer(request);

        return ResponseEntity.created(URI.create("/api/sessions-entrainement/" + response.id())).body(response);
    }
    
    @GetMapping("/royaume/{royaumeId}/sessions")
    public List<SessionEntrainementResponse> getSessions(
            @PathVariable Integer royaumeId,
            @RequestParam(defaultValue = "false") boolean finalisee) {
        return sessionService.sessions(royaumeId, finalisee);
    }

    @GetMapping("/royaume/{royaumeId}/garnison")
    public List<ContingentTroupeResponse> getGarnison(@PathVariable Integer royaumeId) {
        return sessionService.garnison(royaumeId);
    }
}
