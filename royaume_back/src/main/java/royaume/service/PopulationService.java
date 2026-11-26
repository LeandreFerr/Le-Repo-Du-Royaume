package royaume.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import royaume.dao.IDAOPopulation;
import royaume.dto.response.PopulationResponse;
import royaume.model.peuple.Population;

@Service
@Transactional(readOnly = true)
public class PopulationService {

    private final IDAOPopulation daoPopulation;

    public PopulationService(IDAOPopulation daoPopulation) {
        this.daoPopulation = daoPopulation;
    }

    public List<PopulationResponse> trouverTous() {
        return daoPopulation.findAll().stream()
                .map(this::convertirEnResponse)
                .toList();
    }

    public PopulationResponse trouverParId(Integer id) {
        return convertirEnResponse(daoPopulation.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Le peuple " + id + " n'existe pas.")));
    }

    private PopulationResponse convertirEnResponse(Population p) {
        return new PopulationResponse(
                p.getId(),
                p.getNom(),
                p.getTitreSouverain(),
                p.getAttraction(),
                p.getCapaciteSpeciale(),
                p.getDescriptionCapacite()
        );
    }
}