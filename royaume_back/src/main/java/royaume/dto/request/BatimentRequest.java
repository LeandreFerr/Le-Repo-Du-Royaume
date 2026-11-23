package royaume.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import royaume.model.Ressource;

public record BatimentRequest(

    @NotBlank
    @Size(min = 2, max = 50)
    String nom,

    @NotNull
    Integer royaumeId,

    // Uniquement pour BatimentProduction
    
    Ressource typeRessource

) {}
