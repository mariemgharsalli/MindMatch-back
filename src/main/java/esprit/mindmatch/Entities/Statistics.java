package esprit.mindmatch.Entities;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Statistics {

    private long totalUsers;
    private long totalSessions;
    private long totalSubmissions;
    private long totalDocumentsUploaded;
}
