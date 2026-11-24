package royaume.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import royaume.model.OffreMarche;
import royaume.model.Ressource;
import royaume.model.StatutOffre;

public interface IDAOOffreMarche extends JpaRepository<OffreMarche, Integer> {

	List<OffreMarche> findByStatut(StatutOffre statut);
	List<OffreMarche> findByStatutAndProposeRessource (StatutOffre statut, Ressource ressource);
}
