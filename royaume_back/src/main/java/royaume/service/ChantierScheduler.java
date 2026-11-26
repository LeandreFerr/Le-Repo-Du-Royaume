
package royaume.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import royaume.dao.IDAOChantierConstruction;
import royaume.model.batiment.ChantierConstruction;

@Component
public class ChantierScheduler {

    private final IDAOChantierConstruction chantierDAO;

    public ChantierScheduler(
            IDAOChantierConstruction chantierDAO) {
        this.chantierDAO = chantierDAO;
    }

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void finaliserChantiersTermines() {

        List<ChantierConstruction> chantiers =
            chantierDAO.findByDateFinPrevueLessThanEqual(
                LocalDateTime.now()
            );

        for (ChantierConstruction chantier : chantiers) {
            if (chantier.estPretAAchever()) {
                chantier.finaliser();
            }
        }
    }
}
