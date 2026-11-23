package royaume.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import royaume.model.Carte;
import java.util.Optional;

public interface IDAOCarte extends JpaRepository<Carte, Integer> {

    Optional<Carte> findByPartieId(Integer partieId);

}
