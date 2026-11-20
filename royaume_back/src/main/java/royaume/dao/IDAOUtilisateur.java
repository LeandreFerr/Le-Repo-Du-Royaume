package royaume.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import royaume.model.Utilisateur;

import java.util.Optional;

public interface IDAOUtilisateur extends JpaRepository<Utilisateur, Integer> {

    Optional<Utilisateur> findByPseudo(String pseudo);

    Optional<Utilisateur> findByEmail(String email);
}
