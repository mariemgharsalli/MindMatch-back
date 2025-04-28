package esprit.mindmatch.Entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "user_profiles")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Image en base64 ou URL, selon ton choix
    @Column(columnDefinition = "TEXT")
    private String profilePicture;

    // Document unique encodé en base64 ou chemin de fichier. Si tu veux plusieurs : utilise une @ElementCollection
    @Column(columnDefinition = "TEXT")
    private String documents;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;
}
