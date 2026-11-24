package royaume.dao;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import jakarta.persistence.EntityManager;
import royaume.model.Royaume;
import royaume.model.militaire.SessionEntrainement;
import royaume.model.militaire.TypeTroupe;
import royaume.model.peuple.Canards;
import royaume.model.peuple.Population;

@DataJpaTest
class IDAOSessionEntrainementTest {

    @Autowired private IDAOSessionEntrainement daoSession;
    @Autowired private IDAORoyaume daoRoyaume;
    @Autowired private EntityManager entityManager;

    private Population peuple;

    private Royaume creerRoyaume(String nom) {
        if (peuple == null) {
            peuple = new Canards(null, "Canards", "Archiduc", 0.05);
            entityManager.persist(peuple);
            entityManager.flush();
        }
        return new Royaume(null, nom, peuple);
    }

    @Test
    void testFindByRoyaumeAndFinaliseeFalse() {
        // Given
        Royaume royaume = creerRoyaume("Avalon");
        Royaume autre = creerRoyaume("Autre");
        LocalDateTime t = LocalDateTime.of(2026, 1, 1, 12, 0);

        royaume.setPopulationEnEntrainement(5);   // évite un négatif lors de la finalisation
        SessionEntrainement enCours = new SessionEntrainement(TypeTroupe.ARCHER, 3, t, t.plusMinutes(15));
        SessionEntrainement finie = new SessionEntrainement(TypeTroupe.LANCIER, 2, t, t.plusMinutes(10));
        royaume.ajouterSession(enCours);
        royaume.ajouterSession(finie);
        finie.finaliserFormation(royaume);

        autre.ajouterSession(new SessionEntrainement(TypeTroupe.CAVALIER, 1, t, t.plusMinutes(5)));

        daoRoyaume.saveAndFlush(royaume);   // cascade vers les sessions
        daoRoyaume.saveAndFlush(autre);
        Integer idRoyaume = royaume.getId();
        entityManager.clear();

        // When
        List<SessionEntrainement> resultat =
                daoSession.findByRoyaumeAndFinaliseeFalse(daoRoyaume.getReferenceById(idRoyaume));

        // Then
        assertEquals(1, resultat.size());
        assertEquals(TypeTroupe.ARCHER, resultat.get(0).getTypeTroupe());
        assertFalse(resultat.get(0).isFinalisee());
    }
}