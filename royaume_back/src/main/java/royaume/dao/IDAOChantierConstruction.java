package royaume.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import royaume.model.batiment.ChantierConstruction;

public interface IDAOChantierConstruction extends JpaRepository<ChantierConstruction, Integer> {
List<ChantierConstruction> findByDateFinPrevueLessThanEqual(
        LocalDateTime date
    );

    Optional<ChantierConstruction> findByBatiment_Id(Integer batimentId);

    
}

   //save(ChantierConstruction chantier);

   // findById(Integer id);

   // findAll();

 // deleteById(Integer id);

// delete(ChantierConstruction chantier);

//  existsById(Integer id);
