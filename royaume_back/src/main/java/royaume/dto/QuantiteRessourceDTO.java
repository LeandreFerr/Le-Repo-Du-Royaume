package royaume.dto;

import royaume.model.Ressource;

public record QuantiteRessourceDTO(
    Ressource ressource,
    int quantite
) {}