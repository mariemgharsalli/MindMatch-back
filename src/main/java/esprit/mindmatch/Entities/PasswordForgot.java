package esprit.mindmatch.Entities;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class PasswordForgot {
    @NotEmpty
    @Email
    private String email;
}
