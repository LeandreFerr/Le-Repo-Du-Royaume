package royaume.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import royaume.model.Ressource;
import royaume.model.batiment.Batiment;
import royaume.model.batiment.BatimentProduction;
import royaume.model.batiment.Chateau;
import royaume.model.batiment.Maison;

// Fonctionne aussi pour les classes filles : 

public interface IDAOBatiment extends JpaRepository<Batiment, Integer> {
	
  // Recherche d'une maison par royaume et nom
    Optional<Maison> findByRoyaume_IdAndNom(
        Integer royaumeId,
        String nom
    );

    // Recherche d'un bâtiment de production par royaume et ressource
    Optional<BatimentProduction> findByRoyaume_IdAndTypeRessource(
        Integer royaumeId,
        Ressource typeRessource
    );

    // Recherche du château d'un royaume
    Optional<Chateau> findByRoyaume_Id(Integer royaumeId);

}
 //save(Batiment batiment); findById(Integer id); findAll(); 
	//deleteById(Integer id); delete(Batiment batiment); 
	//existsById(Integer id);

