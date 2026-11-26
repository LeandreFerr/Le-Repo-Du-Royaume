package royaume.dto.response;

import royaume.model.Ressource;

public record RessourcesAmeliorerRoyaumeResponse(

    Ressource ressource,
    int quantitePossedee,
    int quantiteRequise,
    int quantiteManquante,
    boolean suffisante

 ){}