package royaume.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import royaume.model.Marche;

public interface IDAOMarche extends JpaRepository<Marche, Integer> {

}
