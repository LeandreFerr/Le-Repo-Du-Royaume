package royaume.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import royaume.dao.IDAOCarte;
import royaume.dao.IDAOPartie;
import royaume.dto.request.PartieRequest;
import royaume.dto.response.PartieResponse;
import royaume.model.Partie;
import royaume.model.StatutPartie;

@ExtendWith(MockitoExtension.class)
class PartieServiceTest {

    @Mock
    private IDAOPartie daoPartie;
    @Mock
    private IDAOCarte daoCarte;
    @Mock
    private RoyaumeService royaumeService;

    @InjectMocks
    private PartieService partieService;

    @Test
    void creer_devraitSauvegarderEtRetournerPartieResponse() {
        PartieRequest request = new PartieRequest("Partie Test", 1.5, 4);
        Partie saved = new Partie("Partie Test", 1.5, 4);
        saved.setId(1);

        when(daoPartie.save(any(Partie.class))).thenReturn(saved);

        PartieResponse response = partieService.creer(request);

        assertNotNull(response);
        assertEquals(1, response.id());
        assertEquals("Partie Test", response.nom());
        assertEquals(1.5, response.vitesseJeu());
        assertEquals(4, response.maxJoueurs());
        assertEquals(StatutPartie.EN_ATTENTE, response.statut());
        verify(daoPartie).save(any(Partie.class));
    }

    @Test
    void trouverParId_devraitRetournerPartie_quandExiste() {
        Partie partie = new Partie("Alpha", 1.0, 2);
        partie.setId(10);
        when(daoPartie.findById(10)).thenReturn(Optional.of(partie));

        PartieResponse response = partieService.trouverParId(10);

        assertEquals(10, response.id());
        assertEquals("Alpha", response.nom());
    }

    @Test
    void trouverParId_devraitLever404_quandAbsente() {
        when(daoPartie.findById(99)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> partieService.trouverParId(99));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void trouverToutes_devraitRetournerListe() {
        Partie p1 = new Partie("P1", 1.0, 2);
        p1.setId(1);
        Partie p2 = new Partie("P2", 2.0, 4);
        p2.setId(2);
        when(daoPartie.findAll()).thenReturn(List.of(p1, p2));

        List<PartieResponse> result = partieService.trouverToutes();

        assertEquals(2, result.size());
        assertEquals("P1", result.get(0).nom());
        assertEquals("P2", result.get(1).nom());
    }

    @Test
    void trouverParStatut_devraitFiltrerParStatut() {
        Partie p = new Partie("EnCours", 1.0, 2);
        p.setId(3);
        p.setStatut(StatutPartie.EN_COURS);
        when(daoPartie.findByStatut(StatutPartie.EN_COURS)).thenReturn(List.of(p));

        List<PartieResponse> result = partieService.trouverParStatut(StatutPartie.EN_COURS);

        assertEquals(1, result.size());
        assertEquals(StatutPartie.EN_COURS, result.get(0).statut());
    }

    @Test
    void demarrer_devraitPasserStatutEnCours() {
        Partie partie = new Partie("P1", 1.0, 2);
        partie.setId(1);
        when(daoPartie.findById(1)).thenReturn(Optional.of(partie));
        when(daoPartie.save(partie)).thenReturn(partie);

        PartieResponse response = partieService.demarrer(1);

        assertEquals(StatutPartie.EN_COURS, response.statut());
        verify(daoPartie).save(partie);
    }

    @Test
    void demarrer_devraitLever404_quandAbsente() {
        when(daoPartie.findById(42)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> partieService.demarrer(42));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(daoPartie, never()).save(any());
    }

    @Test
    void terminer_devraitPasserStatutTerminee() {
        Partie partie = new Partie("P1", 1.0, 2);
        partie.setId(1);
        when(daoPartie.findById(1)).thenReturn(Optional.of(partie));
        when(daoPartie.save(partie)).thenReturn(partie);

        PartieResponse response = partieService.terminer(1);

        assertEquals(StatutPartie.TERMINEE, response.statut());
        verify(daoPartie).save(partie);
    }

    @Test
    void supprimer_devraitSupprimer_quandExiste() {
        Partie partie = new Partie("P1", 1.0, 2);
        partie.setId(5);
        when(daoPartie.findById(5)).thenReturn(Optional.of(partie));

        partieService.supprimer(5);

        verify(daoPartie).delete(partie);
    }

    @Test
    void supprimer_devraitLever404_quandAbsente() {
        when(daoPartie.findById(5)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> partieService.supprimer(5));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(daoPartie, never()).delete(any());
    }
}
