package esprit.mindmatch.Entities;

import esprit.mindmatch.Validator.PasswordConfirmation;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
@PasswordConfirmation(
        password = "password",
        confirmPassword = "confirmPassword",
        message = "Password and confirmation password must be the same!"
)
public class PasswordReset {
    @NotEmpty
    private String password;
    @NotEmpty
    private String confirmPassword;
    @NotEmpty
    private String token;
}
