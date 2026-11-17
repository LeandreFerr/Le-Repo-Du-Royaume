package royaume.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import royaume.model.OffreMarche;
import royaume.model.Ressource;
import royaume.model.StatutOffre;

public interface IDAOOffreMarche extends JpaRepository<OffreMarche, Integer> {

	@Query("Find offre by statut")
	List<OffreMarche> findByStatut(StatutOffre statut);
	
	@Query("Find offre by statut et type de ressource proposee")
	List<OffreMarche> findbyStatutAndProposeRessource (StatutOffre statut, Ressource ressource);
}
