package royaume.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.transaction.annotation.Transactional;
import royaume.dao.IDAOPartie;
import royaume.dto.response.TickerResponse;
import royaume.model.Partie;
import royaume.model.StatutPartie;
import royaume.model.ticker.HorlogeJeu;
import royaume.model.ticker.MoteurJeu;

@Service 
public class TickerService {

    private final IDAOPartie daoPartie;

    private final Map<Integer, MoteurJeu> moteurs = new ConcurrentHashMap<>();

    public TickerService(IDAOPartie daoPartie) {
        this.daoPartie = daoPartie;
    }

    @Transactional 
    public TickerResponse executerTick(Integer partieId, long secondes) {
        Partie partie = daoPartie.findById(partieId)
             .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La partie id=" + partieId + " n'existe pas."));

        if (partie.getStatut() != StatutPartie.EN_COURS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La partie id=" + partieId + " n'est pas EN_COURS");
        }

        MoteurJeu moteur = moteurs.computeIfAbsent(partieId, id -> new MoteurJeu(partie, new HorlogeJeu(LocalDateTime.now(), partie.getVitesseJeu())));
        moteur.setPartie(partie);
        moteur.executerTick(Duration.ofSeconds(secondes));
        daoPartie.save(partie);
        return new TickerResponse(partieId, moteur.getHorloge().getTempsCourant(), moteur.getHorloge().getVitesseSimulation());
    }

    @Transactional 
    @Scheduled (fixedRate = 60000)
    public void tickAutomatique() {
        List<Partie> partiesEnCours = daoPartie.findByStatut(StatutPartie.EN_COURS);
        for (Partie partie : partiesEnCours) {
            executerTick(partie.getId(), Math.round(60 * partie.getVitesseJeu()));
        }
    }

    public TickerResponse getTempsCourant(Integer partieId) {
        Partie partie = daoPartie.findById(partieId)
             .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La partie id=" + partieId + " n'existe pas."));
        MoteurJeu moteur = moteurs.get(partieId);
        if (partie.getStatut() != StatutPartie.EN_COURS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La partie id=" + partieId + " n'est pas EN_COURS");
        }
        if (moteur == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Le moteur de jeu n'a pas encore exécuté son premier tick.");
        }
        return new TickerResponse(partieId, moteur.getHorloge().getTempsCourant(), moteur.getHorloge().getVitesseSimulation());
    }
}
