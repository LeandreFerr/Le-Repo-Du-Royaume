package royaume.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import royaume.model.PalierNiveau;

@Repository
public interface PalierNiveauIDAO
        extends JpaRepository<PalierNiveau, Integer> {

    Optional<PalierNiveau> findByNiveau(int niveau);

    List<PalierNiveau> findAllByOrderByNiveauAsc();
}