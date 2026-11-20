package royaume.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import royaume.model.batiment.ChantierConstruction;

public interface IDAOChantierConstruction extends JpaRepository<ChantierConstruction, Integer> {

}

   //save(ChantierConstruction chantier);

   // findById(Integer id);

   // findAll();

 // deleteById(Integer id);

// delete(ChantierConstruction chantier);

//  existsById(Integer id);
