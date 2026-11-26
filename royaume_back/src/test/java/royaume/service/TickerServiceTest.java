package royaume.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import royaume.dao.IDAOPartie;
import royaume.dto.response.TickerResponse;
import royaume.model.Partie;
import royaume.model.StatutPartie;

@ExtendWith(MockitoExtension.class)
class TickerServiceTest {

    @Mock
    private IDAOPartie daoPartie;

    @InjectMocks
    private TickerService tickerService;

    @Test
    void executerTick_devraitAvancerHorlogeEtSauvegarder() {
        Partie partie = new Partie("Partie 1", 2.0, 4);
        partie.setId(1);
        partie.setStatut(StatutPartie.EN_COURS);

        when(daoPartie.findById(1)).thenReturn(Optional.of(partie));
        when(daoPartie.save(partie)).thenReturn(partie);

        TickerResponse first = tickerService.executerTick(1, 60);
        LocalDateTime t1 = first.tempsCourant();

        TickerResponse second = tickerService.executerTick(1, 120);
        LocalDateTime t2 = second.tempsCourant();

        assertNotNull(t1);
        assertEquals(2.0, first.vitesseSimulation());
        assertTrue(t2.isAfter(t1));
        assertEquals(t1.plusSeconds(120), t2);
        verify(daoPartie, times(2)).save(partie);
    }

    @Test
    void executerTick_devraitLever404_quandPartieAbsente() {
        when(daoPartie.findById(99)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> tickerService.executerTick(99, 60));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(daoPartie, never()).save(any());
    }

    @Test
    void executerTick_devraitLever409_quandPartieNonEnCours() {
        Partie partie = new Partie("Partie Attente", 1.0, 4);
        partie.setId(2);
        partie.setStatut(StatutPartie.EN_ATTENTE);
        when(daoPartie.findById(2)).thenReturn(Optional.of(partie));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> tickerService.executerTick(2, 60));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(daoPartie, never()).save(any());
    }

    @Test
    void getTempsCourant_devraitLever409_quandAucunTickExecute() {
        Partie partie = new Partie("Partie 1", 1.0, 4);
        partie.setId(3);
        partie.setStatut(StatutPartie.EN_COURS);
        when(daoPartie.findById(3)).thenReturn(Optional.of(partie));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> tickerService.getTempsCourant(3));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void getTempsCourant_devraitRetournerHorlogeApresPremierTick() {
        Partie partie = new Partie("Partie 1", 1.0, 4);
        partie.setId(4);
        partie.setStatut(StatutPartie.EN_COURS);
        when(daoPartie.findById(4)).thenReturn(Optional.of(partie));
        when(daoPartie.save(partie)).thenReturn(partie);

        TickerResponse tick = tickerService.executerTick(4, 60);
        TickerResponse current = tickerService.getTempsCourant(4);

        assertEquals(tick.tempsCourant(), current.tempsCourant());
    }

    @Test
    void tickAutomatique_devraitExecuterTickSurToutesPartiesEnCours() {
        Partie p1 = new Partie("P1", 1.0, 2);
        p1.setId(10);
        p1.setStatut(StatutPartie.EN_COURS);

        when(daoPartie.findByStatut(StatutPartie.EN_COURS)).thenReturn(List.of(p1));
        when(daoPartie.findById(10)).thenReturn(Optional.of(p1));
        when(daoPartie.save(p1)).thenReturn(p1);

        tickerService.tickAutomatique();

        verify(daoPartie).save(p1);
    }
}
