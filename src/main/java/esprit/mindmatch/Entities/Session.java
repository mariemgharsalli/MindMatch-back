package esprit.mindmatch.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sessions")
public class Session {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)

    private Long id;
    private String nom;
    private String description;
    @Temporal(TemporalType.DATE)
    private Date date;
    @Temporal(TemporalType.DATE)
    private Date expirationDate;
    private String salle;
    private Integer prix ;
    private String niveau ;
    private String profilePicture ;
    private String location;
    @Column(nullable = false)
    private Boolean archived = false;
    private String speakerName;
    private String speakerEmail;
    @Temporal(TemporalType.TIMESTAMP)
    private Date archiveDate;

    @JsonIgnore
    @OneToOne
    private Room room;

    @ManyToMany
    @JsonIgnore
    private List<User> userCompetition;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Submission> submissions;


    public Boolean isArchived() {
        return archived;
    }

    public void setArchived(Boolean archived) {
        this.archived = archived;
    }


}
