package royaume.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import royaume.model.PalierNiveau;

public interface PalierNiveauIDAO
        extends JpaRepository<PalierNiveau, Integer> {

    Optional<PalierNiveau> findByNiveau(int niveau);

    List<PalierNiveau> findAllByOrderByNiveauAsc();
}