package royaume.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InscriptionRequest(

    @NotBlank
    @Size(min = 3, max = 30)
    String pseudo,

    @NotBlank
    @Email
    @Size(max = 100)
    String email,

    @NotBlank
    @Size(min = 6)
    String password

) {}