package royaume.dto.request;

import java.time.LocalDateTime;

import royaume.model.QuantiteRessource;
import royaume.model.Royaume;
import royaume.model.StatutEchange;

public record PropositionEchangeRequest(LocalDateTime dateCreation, StatutEchange statut, Royaume expediteur,
		Royaume destinataire, QuantiteRessource offerte, QuantiteRessource demandee) {

}
