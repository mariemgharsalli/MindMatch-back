package esprit.mindmatch.DTO;

import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Builder
@Data
public class SessionDTO {

    private Long id;
    private String nom;
    private String description;
    private Date date;
    private Date expirationDate;
    private String salle;
    private Integer prix ;
    private String niveau ;
    private String location;
    private Boolean archived = false;
    private String speakerName;
    private String speakerEmail;
    private  byte[] profilePicture ;
}
