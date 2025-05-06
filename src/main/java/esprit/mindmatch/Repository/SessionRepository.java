package esprit.mindmatch.Repository;

import esprit.mindmatch.Entities.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    Session getSessionByRoom_RoomId(Long id);
    List<Session> findByArchived(boolean archived);
    List<Session> findByArchivedAndArchiveDateBefore(boolean archived, Date date);

    List<Session> findByScheduledForDeletion(boolean scheduledForDeletion);
}
