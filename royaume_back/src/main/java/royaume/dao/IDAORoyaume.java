package royaume.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import royaume.model.Royaume;

public interface IDAORoyaume extends JpaRepository<Royaume, Integer> {

    boolean existsByNom(String nom);
    
    @Query("SELECT r FROM Royaume r LEFT JOIN FETCH r.population WHERE r.nom = :nom")
    List<Royaume> findByNom(@Param("nom") String nom);

}