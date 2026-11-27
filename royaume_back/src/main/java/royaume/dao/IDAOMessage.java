package royaume.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import royaume.model.Message;

import java.util.List;

public interface IDAOMessage extends JpaRepository<Message, Integer> {

    List<Message> findByPartieIdOrderByDateEnvoiAsc(Integer partieId);
    List<Message> findByDestinataireIdOrderByDateEnvoiAsc(
    Integer destinataireId);
    List<Message> findByDestinataireIdOrderByDateEnvoiDesc(Integer destinataireId);

}
