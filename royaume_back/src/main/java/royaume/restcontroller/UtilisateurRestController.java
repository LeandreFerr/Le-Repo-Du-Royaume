package royaume.restcontroller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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