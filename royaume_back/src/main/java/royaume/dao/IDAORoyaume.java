package royaume.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import royaume.model.Royaume;

public interface IDAORoyaume extends JpaRepository<Royaume, Integer> {

    boolean existsByNom(String nom);

}