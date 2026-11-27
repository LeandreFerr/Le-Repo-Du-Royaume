package royaume.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import royaume.model.OffreMarche;
import royaume.model.Ressource;
import royaume.model.StatutOffre;

public interface IDAOOffreMarche extends JpaRepository<OffreMarche, Integer> {

	List<OffreMarche> findByStatut(StatutOffre statut);
	List<OffreMarche> findByStatutAndProposeRessource (StatutOffre statut, Ressource ressource);

	@Query("SELECT o FROM OffreMarche o WHERE o.vendeur IN (SELECT r FROM Partie p JOIN p.royaumes r WHERE p.id = :partieId)")
	List<OffreMarche> findByPartieId(@Param("partieId") Integer partieId);

	@Query("SELECT o FROM OffreMarche o WHERE o.vendeur IN (SELECT r2 FROM Partie p JOIN p.royaumes r1 JOIN p.royaumes r2 WHERE r1.id = :royaumeId)")
	List<OffreMarche> findByRoyaumePartie(@Param("royaumeId") Integer royaumeId);
}
