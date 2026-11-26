package royaume.dao;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import jakarta.persistence.EntityManager;
import royaume.model.Ressource;
import royaume.model.Royaume;
import royaume.model.militaire.Attaque;
import royaume.model.militaire.ContingentTroupe;
import royaume.model.militaire.StatutAttaque;
import royaume.model.militaire.TypeTroupe;
import royaume.model.peuple.Canards;
import royaume.model.peuple.Population;

@DataJpaTest
class IDAOAttaqueTest {

    @Autowired private IDAOAttaque daoAttaque;
    @Autowired private IDAORoyaume daoRoyaume;
    @Autowired private EntityManager entityManager;

    private Population peuple;

    private Royaume creerRoyaume(String nom) {
        if (peuple == null) {
            peuple = new Canards(null, "Canards", "Archiduc", 0.05);
            entityManager.persist(peuple);
            entityManager.flush();
        }
        Royaume r = new Royaume(null, nom, peuple);
        // Stocks remis à zéro (défauts 200/200/500/200) pour des montants prévisibles
        r.setRessources(List.of(new royaume.model.QuantiteRessource(0, Ressource.PIERRE), new royaume.model.QuantiteRessource(0, Ressource.BOIS), new royaume.model.QuantiteRessource(0, Ressource.OR), new royaume.model.QuantiteRessource(0, Ressource.NOURRITURE)));
        return daoRoyaume.saveAndFlush(r);
    }

    private Attaque creerAttaque(Royaume att, Royaume def) {
        List<ContingentTroupe> convoi = List.of(
                new ContingentTroupe(TypeTroupe.ARCHER, 3),
                new ContingentTroupe(TypeTroupe.CAVALIER, 2));
        return new Attaque(att, def, convoi, LocalDateTime.of(2026, 1, 1, 12, 0), 20);
    }

    @Test
    void testRelectureCompleteAvecTroupesEtButinJson() {
        // Given : défenseur sans garnison mais avec de l'or -> victoire + butin
        Royaume att = creerRoyaume("Attaquant");
        Royaume def = creerRoyaume("Defenseur");
        def.crediterRessources(Ressource.OR, 100);
        daoRoyaume.saveAndFlush(def);

        Attaque attaque = creerAttaque(att, def);
        attaque.resoudreCombat();
        Integer id = daoAttaque.saveAndFlush(attaque).getId();
        entityManager.clear();

        // When
        Attaque relue = daoAttaque.findById(id).orElseThrow();

        // Then
        assertAll(
            () -> assertEquals(StatutAttaque.RETOUR, relue.getStatut()),
            () -> assertTrue(relue.isVictoireAttaquant()),
            () -> assertNotNull(relue.getDateRetourPrevue()),
            () -> assertEquals(2, relue.getTroupesEnvoyees().size()),
            () -> assertEquals(TypeTroupe.ARCHER, relue.getTroupesEnvoyees().get(0).getType()),
            () -> assertEquals(3, relue.getTroupesEnvoyees().get(0).getQuantite()),
            () -> assertEquals(1, relue.getButin().size()),
            () -> assertEquals(Ressource.OR, relue.getButin().get(0).getRessource()),
            () -> assertEquals(100, relue.getButin().get(0).getQuantite()),
            () -> assertEquals(att.getId(), relue.getAttaquant().getId()),
            () -> assertEquals(def.getId(), relue.getDefenseur().getId()),
            () -> assertEquals(attaque.getTempsTrajet(), relue.getTempsTrajet())
        );
    }

    @Test
    void testDateRetourPrevueNullableAvantResolution() {
        Royaume att = creerRoyaume("Attaquant");
        Royaume def = creerRoyaume("Defenseur");

        Integer id = daoAttaque.saveAndFlush(creerAttaque(att, def)).getId();
        entityManager.clear();

        Attaque relue = daoAttaque.findById(id).orElseThrow();
        assertNull(relue.getDateRetourPrevue());
    }

    @Test
    void testFinders() {
        Royaume att = creerRoyaume("Attaquant");
        Royaume def = creerRoyaume("Defenseur");
        def.crediterRessources(Ressource.OR, 1000);
        daoRoyaume.saveAndFlush(def);

        Attaque aller = creerAttaque(att, def);                       // ALLER

        Attaque retour = creerAttaque(att, def);
        retour.resoudreCombat();                                      // RETOUR

        Attaque terminee = creerAttaque(att, def);
        terminee.resoudreCombat();
        terminee.receptionnerRetour();                                // TERMINEE

        daoAttaque.saveAll(List.of(aller, retour, terminee));
        daoAttaque.flush();
        entityManager.clear();

        Royaume attRef = daoRoyaume.getReferenceById(att.getId());
        Royaume defRef = daoRoyaume.getReferenceById(def.getId());

        assertAll(
            () -> assertEquals(1, daoAttaque.findByStatut(StatutAttaque.ALLER).size()),
            () -> assertEquals(2, daoAttaque.findByStatutIn(
                    List.of(StatutAttaque.ALLER, StatutAttaque.RETOUR)).size()),
            () -> assertEquals(3, daoAttaque.findByAttaquant(attRef).size()),
            () -> assertEquals(3, daoAttaque.findByDefenseur(defRef).size()),
            () -> assertTrue(daoAttaque.findByAttaquant(defRef).isEmpty())
        );
    }
}