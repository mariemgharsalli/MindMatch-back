package esprit.mindmatch.DTO;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Getter
@Setter
public class UserProfileDTO {
    private String firstname;
    private String lastname;
    private String email;
    private  byte[] profilePicture ;
    private List<byte[]> documents; // Liste des documents en bytes
}