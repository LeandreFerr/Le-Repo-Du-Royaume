
package royaume.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import royaume.dao.IDAOMessage;
import royaume.dao.IDAOUtilisateur;
import royaume.dao.IDAOPartie;
import royaume.dto.request.MessageRequest;
import royaume.dto.response.MessageResponse;
import royaume.model.Message;
import royaume.model.Utilisateur;
import royaume.model.Partie;

@Service
public class MessageService {

    private final IDAOMessage messageDAO;
    private final IDAOUtilisateur utilisateurDAO;
    private final IDAOPartie partieDAO;

    public MessageService(
            IDAOMessage messageDAO,
            IDAOUtilisateur utilisateurDAO,
            IDAOPartie partieDAO) {

        this.messageDAO = messageDAO;
        this.utilisateurDAO = utilisateurDAO;
        this.partieDAO = partieDAO;
    }

    @Transactional
    public MessageResponse envoyerMessage(MessageRequest request) {

        if (request.contenu() == null ||
                request.contenu().isBlank()) {
            throw new IllegalArgumentException(
                "Le message ne peut pas être vide."
            );
        }

        if (request.contenu().length() > 1000) {
            throw new IllegalArgumentException(
                "Le message ne peut pas dépasser 1000 caractères."
            );
        }

        Utilisateur expediteur = utilisateurDAO
                .findById(request.expediteurId())
                .orElseThrow(() ->
                    new RuntimeException("Expéditeur introuvable.")
                );

        Utilisateur destinataire = utilisateurDAO
                .findById(request.destinataireId())
                .orElseThrow(() ->
                    new RuntimeException("Destinataire introuvable.")
                );

        Partie partie = partieDAO
                .findById(request.partieId())
                .orElseThrow(() ->
                    new RuntimeException("Partie introuvable.")
                );

        Message message = new Message();

        message.setContenu(request.contenu().trim());
        message.setDateEnvoi(LocalDateTime.now());
        message.setLu(false);
        message.setPartie(partie);
        message.setExpediteur(expediteur);
        message.setDestinataire(destinataire);

        messageDAO.save(message);

        return new MessageResponse(
                message.getId(),
                message.getContenu(),
                message.getDateEnvoi(),
                message.isLu(),
                partie.getId(),
                expediteur.getId(),
                expediteur.getPseudo(),
                destinataire.getId(),
                destinataire.getPseudo()
        );
    }
}
