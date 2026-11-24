package royaume.dao;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import royaume.model.Royaume;
import royaume.model.militaire.Attaque;
import royaume.model.militaire.StatutAttaque;

public interface IDAOAttaque extends JpaRepository<Attaque, Integer> {

    List<Attaque> findByStatut(StatutAttaque statut);

    /** Ex : attaques en cours = ALLER + RETOUR. */
    List<Attaque> findByStatutIn(Collection<StatutAttaque> statuts);

    List<Attaque> findByAttaquant(Royaume attaquant);

    List<Attaque> findByDefenseur(Royaume defenseur);
}