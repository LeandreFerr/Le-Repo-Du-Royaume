
package royaume.restcontroller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import royaume.dto.request.MessageRequest;
import royaume.dto.response.MessageResponse;
import royaume.service.MessageService;

@RestController
@RequestMapping("/api/messages")
public class MessageRestController {

    private final MessageService messageService;

    public MessageRestController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/recus/{destinataireId}")
public ResponseEntity<?> getMessagesRecus(
        @PathVariable Integer destinataireId) {

    return ResponseEntity.ok(
        messageService.getMessagesRecus(destinataireId)
    );
}


    @PostMapping("/envoyer")
    public ResponseEntity<?> envoyerMessage(
            @RequestBody MessageRequest request) {

        try {
            MessageResponse response =
                    messageService.envoyerMessage(request);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PatchMapping("/{messageId}/lu")
public ResponseEntity<?> marquerCommeLu(
        @PathVariable Integer messageId) {

    try {
        messageService.marquerCommeLu(messageId);
        return ResponseEntity.noContent().build();

    } catch (RuntimeException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
}
