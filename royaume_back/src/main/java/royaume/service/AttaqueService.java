package royaume.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import royaume.dao.IDAOAttaque;
import royaume.dao.IDAOEmplacementCarte;
import royaume.dao.IDAORoyaume;
import royaume.dto.QuantiteRessourceDTO;
import royaume.dto.request.LancerAttaqueRequest;
import royaume.dto.response.AttaqueResponse;
import royaume.dto.response.ContingentTroupeResponse;
import royaume.model.Royaume;
import royaume.model.militaire.Attaque;
import royaume.model.militaire.ContingentTroupe;
import royaume.model.militaire.StatutAttaque;

@Service
@Transactional(readOnly = true)
public class AttaqueService {

    private final IDAOAttaque daoAttaque;
    private final IDAORoyaume daoRoyaume;
    private final IDAOEmplacementCarte daoEmplacementCarte;

    public AttaqueService(IDAOAttaque daoAttaque, IDAORoyaume daoRoyaume, IDAOEmplacementCarte daoEmplacementCarte) {
        this.daoAttaque = daoAttaque;
        this.daoRoyaume = daoRoyaume;
        this.daoEmplacementCarte = daoEmplacementCarte;   }

    @Transactional
    public AttaqueResponse lancer(LancerAttaqueRequest request) {

        Royaume attaquant = trouverRoyaume(request.attaquantId());
        Royaume defenseur = trouverRoyaume(request.defenseurId());

        rattacherEmplacementSiAbsent(attaquant);
        rattacherEmplacementSiAbsent(defenseur);
        
        if (attaquant.estEnRuine()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Un royaume en ruine ne peut pas attaquer.");
        }

        if (attaquant.getEmplacement() == null || defenseur.getEmplacement() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Les deux royaumes doivent être placés sur la carte.");
        }

        try {
            double distance = attaquant.getEmplacement().getCarte()
                    .calculerDistanceEntre(attaquant, defenseur);

            Attaque attaque = attaquant.lancerAttaque(defenseur, request.troupes(), distance, LocalDateTime.now());
            return convertirEnResponse(daoAttaque.save(attaque));
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage());
        }
    }
    
     
    
    private void rattacherEmplacementSiAbsent(Royaume royaume) {
        if (royaume.getEmplacement() == null && royaume.getId() != null) {
            daoEmplacementCarte.findByRoyaumeId(royaume.getId())
                    .ifPresent(royaume::setEmplacement);
        }
    }
    
    @Transactional
    public AttaqueResponse resoudreCombat(Integer attaqueId) {
        Attaque attaque = trouverEntiteParId(attaqueId);

        rattacherEmplacementSiAbsent(attaque.getAttaquant());
        rattacherEmplacementSiAbsent(attaque.getDefenseur());

        try {
            attaque.resoudreCombat();
            daoRoyaume.save(attaque.getAttaquant());
            daoRoyaume.save(attaque.getDefenseur());
            return convertirEnResponse(daoAttaque.save(attaque));
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage());
        }
    }

    @Transactional
    public AttaqueResponse receptionnerRetour(Integer attaqueId) {
        Attaque attaque = trouverEntiteParId(attaqueId);

        rattacherEmplacementSiAbsent(attaque.getAttaquant());

        try {
            attaque.receptionnerRetour();
            daoRoyaume.save(attaque.getAttaquant());
            return convertirEnResponse(daoAttaque.save(attaque));
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage());
        }
    }

    public AttaqueResponse trouverParId(Integer id) {
        return convertirEnResponse(trouverEntiteParId(id));
    }

    public List<AttaqueResponse> rechercher(StatutAttaque statut, Integer attaquantId, Integer defenseurId) {

        List<Attaque> attaques = (statut != null) ? daoAttaque.findByStatut(statut) : daoAttaque.findAll();

        return attaques.stream()
                .filter(a -> attaquantId == null || attaquantId.equals(a.getAttaquant().getId()))
                .filter(a -> defenseurId == null || defenseurId.equals(a.getDefenseur().getId()))
                .map(this::convertirEnResponse)
                .toList();
    }

    private Attaque trouverEntiteParId(Integer id) {
        return daoAttaque.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "L'attaque " + id + " n'existe pas."));
    }

    private Royaume trouverRoyaume(Integer id) {
        return daoRoyaume.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Le royaume " + id + " n'existe pas."));
    }


    private AttaqueResponse convertirEnResponse(Attaque attaque) {

        List<ContingentTroupeResponse> troupes = attaque.getTroupesEnvoyees().stream()
                .map(this::convertirContingentEnResponse)
                .toList();

        List<QuantiteRessourceDTO> butin = attaque.getButin().stream()
                .map(qr -> new QuantiteRessourceDTO(qr.getRessource(), qr.getQuantite()))
                .toList();

        return new AttaqueResponse(
            attaque.getId(),
            attaque.getAttaquant().getId(),
            attaque.getAttaquant().getNom(),
            attaque.getDefenseur().getId(),
            attaque.getDefenseur().getNom(),
            troupes,
            butin,
            attaque.getDateDepart(),
            attaque.getDateImpact(),
            attaque.getDateRetourPrevue(),
            attaque.getTempsTrajet(),
            attaque.isVictoireAttaquant(),
            attaque.getStatut()
        );
    }

    private ContingentTroupeResponse convertirContingentEnResponse(ContingentTroupe c) {
        return new ContingentTroupeResponse(c.getType(), c.getQuantite());
    }
    
    public List<ContingentTroupeResponse> trouverTroupesDisponibles(Integer royaumeId) {
        Royaume royaume = trouverRoyaume(royaumeId);

        return royaume.getGarnison().stream()
                .map(this::convertirContingentEnResponse)
                .toList();
    }
    
    
    
}