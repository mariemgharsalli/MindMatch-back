package esprit.mindmatch.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Date;

@Data
public class UserRegistrationDTO {
    private String firstName;
    private String lastName;
    private String address;
    private String password;
    private String email;
    private String phone;
    @JsonFormat(pattern = "dd-MM-yyyy")
    private Date dateOfBirth;
    private String sexe;
    private String role ;

}