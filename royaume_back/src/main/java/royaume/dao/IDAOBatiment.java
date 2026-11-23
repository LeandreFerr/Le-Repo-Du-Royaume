package royaume.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import royaume.model.batiment.Batiment;

// Fonctionne aussi pour les classes filles : 

public interface IDAOBatiment extends JpaRepository<Batiment, Integer> {

}
 //save(Batiment batiment); findById(Integer id); findAll(); 
	//deleteById(Integer id); delete(Batiment batiment); 
	//existsById(Integer id);

