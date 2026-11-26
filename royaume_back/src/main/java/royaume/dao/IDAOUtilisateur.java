package royaume.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import royaume.model.Utilisateur;

import java.util.Optional;

public interface IDAOUtilisateur extends JpaRepository<Utilisateur, Integer> {

    boolean existsByPseudo(String pseudo);
    Optional<Utilisateur> findByPseudo(String pseudo);
    
    boolean existsByEmail(String email);
    Optional<Utilisateur> findByEmail(String email);

    
   

}
