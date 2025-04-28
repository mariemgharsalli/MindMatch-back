package esprit.mindmatch.Entities;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordResetRequest {
    private String email ;
    private String newPassword ;
    private String confirmPassword ;
}