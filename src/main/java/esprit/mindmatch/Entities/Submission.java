package esprit.mindmatch.Entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ElementCollection
    private List<String> documentPaths; // liste des chemins vers les fichiers


    @Temporal(TemporalType.TIMESTAMP)
    private Date submissionDate;

    @ManyToOne
    @JsonIgnoreProperties({"submissions", "profile", "password", "phone", "dateOfBirth", "sexe", "role"})
    private User user;

    @ManyToOne
    @JoinColumn(name = "session_id")
    private Session session;

    @Enumerated(EnumType.STRING)
    private submissionStatus status = submissionStatus.PENDING;
}
