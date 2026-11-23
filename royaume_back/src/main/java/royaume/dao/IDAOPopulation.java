package royaume.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import royaume.model.peuple.Population;

public interface IDAOPopulation extends JpaRepository<Population, Integer> {

Optional<Population> findByNom(String nom);
}