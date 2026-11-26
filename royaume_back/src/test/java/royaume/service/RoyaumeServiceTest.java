package royaume.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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

import royaume.dao.IDAOPartie;
import royaume.dao.IDAOPopulation;
import royaume.dao.IDAORoyaume;
import royaume.dao.IDAOUtilisateur;
import royaume.dto.request.RoyaumeRequest;
import royaume.dto.response.RoyaumeResponse;
import royaume.model.Partie;
import royaume.model.Royaume;
import royaume.model.Utilisateur;
import royaume.model.peuple.Canards;
import royaume.model.peuple.Population;

@ExtendWith(MockitoExtension.class)
class RoyaumeServiceTest {

    @Mock
    private IDAORoyaume daoRoyaume;
    @Mock
    private IDAOPartie daoPartie;
    @Mock
    private IDAOUtilisateur daoUtilisateur;
    @Mock
    private IDAOPopulation daoPopulation;

    @InjectMocks
    private RoyaumeService royaumeService;

    private Population creerPeupleValide() {
        Canards c = new Canards();
        c.setNom("Canards");
        return c;
    }

    @Test
    void creer_devraitCreerEtInscrireRoyaume() {
        RoyaumeRequest request = new RoyaumeRequest("Valoria", 1, 10, 20);
        Partie partie = new Partie("Partie 1", 1.0, 4);
        partie.setId(10);
        Utilisateur utilisateur = new Utilisateur();
        Population peuple = creerPeupleValide();

        when(daoRoyaume.existsByNom("Valoria")).thenReturn(false);
        when(daoPartie.findById(10)).thenReturn(Optional.of(partie));
        when(daoUtilisateur.findById(20)).thenReturn(Optional.of(utilisateur));
        when(daoPopulation.findById(1)).thenReturn(Optional.of(peuple));
        when(daoRoyaume.save(any(Royaume.class))).thenAnswer(inv -> {
            Royaume r = inv.getArgument(0);
            r.setId(100);
            return r;
        });

        RoyaumeResponse response = royaumeService.creer(request);

        assertNotNull(response);
        assertEquals(100, response.id());
        assertEquals("Valoria", response.nom());
        assertEquals("Canards", response.peuple());
        assertEquals(1, partie.getRoyaumes().size());
        assertNotNull(utilisateur.getRoyaume());
    }

    @Test
    void creer_devraitLever409_quandNomDejaPris() {
        RoyaumeRequest request = new RoyaumeRequest("Valoria", 1, 10, 20);
        when(daoRoyaume.existsByNom("Valoria")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> royaumeService.creer(request));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(daoRoyaume, never()).save(any());
    }

    @Test
    void creer_devraitLever404_quandPartieAbsente() {
        RoyaumeRequest request = new RoyaumeRequest("Valoria", 1, 99, 20);
        when(daoRoyaume.existsByNom("Valoria")).thenReturn(false);
        when(daoPartie.findById(99)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> royaumeService.creer(request));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void creer_devraitLever404_quandUtilisateurAbsent() {
        RoyaumeRequest request = new RoyaumeRequest("Valoria", 1, 10, 99);
        Partie partie = new Partie("P", 1.0, 4);
        when(daoRoyaume.existsByNom("Valoria")).thenReturn(false);
        when(daoPartie.findById(10)).thenReturn(Optional.of(partie));
        when(daoUtilisateur.findById(99)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> royaumeService.creer(request));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void creer_devraitLever404_quandPeupleAbsent() {
        RoyaumeRequest request = new RoyaumeRequest("Valoria", 99, 10, 20);
        Partie partie = new Partie("P", 1.0, 4);
        Utilisateur utilisateur = new Utilisateur();
        when(daoRoyaume.existsByNom("Valoria")).thenReturn(false);
        when(daoPartie.findById(10)).thenReturn(Optional.of(partie));
        when(daoUtilisateur.findById(20)).thenReturn(Optional.of(utilisateur));
        when(daoPopulation.findById(99)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> royaumeService.creer(request));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void trouverParId_devraitLever404_quandAbsent() {
        when(daoRoyaume.findById(77)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> royaumeService.trouverParId(77));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void trouverParPartie_devraitRetournerRoyaumesDeLaPartie() {
        Partie partie = new Partie("P", 1.0, 4);
        partie.setId(10);
        Royaume r = new Royaume(5, "Eldoria", creerPeupleValide());
        partie.inscrireRoyaume(r);

        when(daoPartie.findById(10)).thenReturn(Optional.of(partie));

        List<RoyaumeResponse> result = royaumeService.trouverParPartie(10);

        assertEquals(1, result.size());
        assertEquals("Eldoria", result.get(0).nom());
    }

    @Test
    void trouverParPartie_devraitLever404_quandPartieAbsente() {
        when(daoPartie.findById(10)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> royaumeService.trouverParPartie(10));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void supprimer_devraitSupprimer_quandExiste() {
        Royaume r = new Royaume(5, "Eldoria", creerPeupleValide());
        when(daoRoyaume.findById(5)).thenReturn(Optional.of(r));

        royaumeService.supprimer(5);

        verify(daoRoyaume).delete(r);
    }

    @Test
    void supprimer_devraitLever404_quandAbsent() {
        when(daoRoyaume.findById(5)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> royaumeService.supprimer(5));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(daoRoyaume, never()).delete(any());
    }
}
