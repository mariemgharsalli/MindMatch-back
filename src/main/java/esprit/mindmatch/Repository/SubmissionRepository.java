package esprit.mindmatch.Repository;

import esprit.mindmatch.Entities.Session;
import esprit.mindmatch.Entities.Submission;
import esprit.mindmatch.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findBySession(Session session);
    List<Submission> findByUser(User user);
    List<Submission> findByUser_Email(String email);

    @Query("SELECT COUNT(s) FROM Submission s WHERE size(s.documentPaths) > 0")
    long countDocumentsUploaded();


}