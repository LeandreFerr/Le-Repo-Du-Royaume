package royaume.restcontroller;

import java.net.URI;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import royaume.dto.request.EntrainerTroupeRequest;
import royaume.dto.response.SessionEntrainementResponse;
import royaume.service.SessionEntrainementService;

@RestController
@RequestMapping("/api/sessions-entrainement")
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
}