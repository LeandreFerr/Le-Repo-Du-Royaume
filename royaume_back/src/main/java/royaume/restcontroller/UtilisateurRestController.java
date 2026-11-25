package royaume.restcontroller;

import org.springframework.http.ResponseEntity;
<<<<<<< HEAD
=======
import org.springframework.web.bind.annotation.CrossOrigin;
>>>>>>> Modul_6_pikachu
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

<<<<<<< HEAD
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
=======
import royaume.dao.IDAOUtilisateur;
import royaume.dto.response.UtilisateurResponse;

@RestController
@RequestMapping("/api/utilisateurs")
@CrossOrigin(origins = "http://localhost:4200")
public class UtilisateurRestController {

    private final IDAOUtilisateur utilisateurDAO;

    public UtilisateurRestController(IDAOUtilisateur utilisateurDAO) {
        this.utilisateurDAO = utilisateurDAO;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        return utilisateurDAO.findByPseudo(request.pseudo())
            .filter(utilisateur ->
                utilisateur.seConnecter(request.password())
            )
            .map(utilisateur -> {

                UtilisateurResponse response = new UtilisateurResponse(
                    utilisateur.getId(),
                    utilisateur.getPseudo(),
                    utilisateur.getEmail(),
                    utilisateur.isOnline(),
                    utilisateur.isAdmin(),
                    utilisateur.getRoyaume() != null
                        ? utilisateur.getRoyaume().getId()
                        : null,
                    utilisateur.getRoyaume() != null
                        ? utilisateur.getRoyaume().getNom()
                        : null
                );

                return ResponseEntity.ok(response);
            })
            .orElse(ResponseEntity.status(401).build());
    }

    public record LoginRequest(String pseudo, String password) {}
}
>>>>>>> Modul_6_pikachu
