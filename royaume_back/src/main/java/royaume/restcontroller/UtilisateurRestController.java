package royaume.restcontroller;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import royaume.dto.request.UtilisateurRequest;
import royaume.dto.response.UtilisateurResponse;
import royaume.service.UtilisateurService;

@RestController
@RequestMapping("/api/utilisateurs")
public class UtilisateurRestController {

    private final UtilisateurService utilisateurService;

    public UtilisateurRestController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UtilisateurRequest request) {
        try {
            UtilisateurResponse response = utilisateurService.login(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }
}