package royaume.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import royaume.model.Partie;
import royaume.model.StatutPartie;

import java.util.List;

public interface IDAOPartie extends JpaRepository<Partie, Integer> {

    List<Partie> findByStatut(StatutPartie statut);
}
