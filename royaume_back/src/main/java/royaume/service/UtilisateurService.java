package royaume.service;

import org.springframework.stereotype.Service;

import royaume.dao.IDAOUtilisateur;
import royaume.dto.request.UtilisateurRequest;
import royaume.dto.response.UtilisateurResponse;
import royaume.model.Utilisateur;
import royaume.model.Royaume;

@Service
public class UtilisateurService {

    private final IDAOUtilisateur utilisateurDAO;

    public UtilisateurService(IDAOUtilisateur utilisateurDAO) {
        this.utilisateurDAO = utilisateurDAO;
    }

    public UtilisateurResponse login(UtilisateurRequest request) {

        if (request.pseudo() == null || request.pseudo().isBlank()
                || request.password() == null || request.password().isBlank()) {
            throw new IllegalArgumentException("Identifiants obligatoires");
        }

        // Recherche par pseudo, puis par email
        Utilisateur utilisateur = utilisateurDAO
                .findByPseudo(request.pseudo())
                .or(() -> utilisateurDAO.findByEmail(request.pseudo()))
                .orElseThrow(() ->
                        new RuntimeException("Identifiants incorrects"));

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
                royaume != null ? royaume.getNom() : null
        );
    }
    public UtilisateurResponse getUtilisateurById(Integer id) {

    Utilisateur utilisateur = utilisateurDAO.findById(id)
            .orElseThrow(() ->
                    new RuntimeException("Utilisateur introuvable : " + id));

    Royaume royaume = utilisateur.getRoyaume();

    return new UtilisateurResponse(
            utilisateur.getId(),
            utilisateur.getPseudo(),
            utilisateur.getEmail(),
            utilisateur.isOnline(),
            utilisateur.isAdmin(),
            royaume != null ? royaume.getId() : null,
            royaume != null ? royaume.getNom() : null
    );
}
/* 
getUtilisateurById(id: number): Observable<UtilisateurResponse> {
  return this.http.get<UtilisateurResponse>(
    `${this.apiUrl}/${id}`
  );
}*/
}