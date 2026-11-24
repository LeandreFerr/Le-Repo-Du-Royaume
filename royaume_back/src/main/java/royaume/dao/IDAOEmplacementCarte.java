package royaume.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import royaume.model.EmplacementCarte;

public interface IDAOEmplacementCarte extends JpaRepository<EmplacementCarte, Integer>{

    List<EmplacementCarte> findByCarteId(Integer carteId);

    List<EmplacementCarte> findByCarteIdAndOccupeFalse(Integer carteId);

}
