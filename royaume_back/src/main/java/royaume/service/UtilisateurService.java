package royaume.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.transaction.Transactional;
import royaume.dao.IDAOUtilisateur;
import royaume.dto.request.InscriptionRequest;
import royaume.dto.request.UtilisateurRequest;
import royaume.dto.response.UtilisateurResponse;
import royaume.model.Royaume;
import royaume.model.Utilisateur;

@Service
public class UtilisateurService {

    private final IDAOUtilisateur utilisateurDAO;

    public UtilisateurService(IDAOUtilisateur utilisateurDAO) {
        this.utilisateurDAO = utilisateurDAO;
    }

    @Transactional
    public UtilisateurResponse login(UtilisateurRequest request) {

        if (request.pseudo() == null || request.pseudo().isBlank()
                || request.password() == null || request.password().isBlank()) {
            throw new IllegalArgumentException("Identifiants obligatoires");
        }

        // Recherche par pseudo, puis par email
        Utilisateur utilisateur = utilisateurDAO
                .findByPseudo(request.pseudo())
                .or(() -> utilisateurDAO.findByEmail(request.pseudo()))
                .orElseThrow(() -> new RuntimeException("Identifiants incorrects"));

        // Vérification du mot de passe
        if (!utilisateur.seConnecter(request.password())) {
            throw new RuntimeException("Identifiants incorrects");
        }

        // Mise à jour du statut online
        utilisateurDAO.save(utilisateur);

        Royaume royaume = utilisateur.getRoyaume();

        return new UtilisateurResponse(
                utilisateur.getId(),
                utilisateur.getPseudo(),
                utilisateur.getEmail(),
                utilisateur.isOnline(),
                utilisateur.isAdmin(),
                royaume != null ? royaume.getId() : null,
                royaume != null ? royaume.getNom() : null);
    }

    @Transactional
    public UtilisateurResponse inscrire(InscriptionRequest request) {

        if (utilisateurDAO.existsByPseudo(request.pseudo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ce pseudo est déjà utilisé.");
        }
        if (utilisateurDAO.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cet email est déjà utilisé.");
        }

        Utilisateur utilisateur = new Utilisateur(
                null, request.pseudo(), request.email(), request.password(), false, false, null);

        Utilisateur sauvegarde = utilisateurDAO.save(utilisateur);

        return new UtilisateurResponse(
                sauvegarde.getId(), sauvegarde.getPseudo(), sauvegarde.getEmail(),
                sauvegarde.isOnline(), sauvegarde.isAdmin(), null, null);
    }

    @Transactional
    public void logout(Integer id) {
        Utilisateur utilisateur = utilisateurDAO.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "L'utilisateur " + id + " n'existe pas."));
        utilisateur.seDeconnecter();
        utilisateurDAO.save(utilisateur);
    }

    @Transactional
    public UtilisateurResponse getUtilisateurById(Integer id) {

        Utilisateur utilisateur = utilisateurDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : " + id));

        Royaume royaume = utilisateur.getRoyaume();

        return new UtilisateurResponse(
                utilisateur.getId(),
                utilisateur.getPseudo(),
                utilisateur.getEmail(),
                utilisateur.isOnline(),
                utilisateur.isAdmin(),
                royaume != null ? royaume.getId() : null,
                royaume != null ? royaume.getNom() : null);
    }
    /*
     * getUtilisateurById(id: number): Observable<UtilisateurResponse> {
     * return this.http.get<UtilisateurResponse>(
     * `${this.apiUrl}/${id}`
     * );
     * }
     */

}