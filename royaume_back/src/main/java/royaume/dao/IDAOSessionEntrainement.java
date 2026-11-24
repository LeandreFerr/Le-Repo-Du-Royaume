package royaume.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import royaume.model.Royaume;
import royaume.model.militaire.SessionEntrainement;

public interface IDAOSessionEntrainement extends JpaRepository<SessionEntrainement, Integer> {

    /** Sessions non finalisées (finalisee = false) d'un royaume. */
    List<SessionEntrainement> findByRoyaumeAndFinaliseeFalse(Royaume royaume);
}