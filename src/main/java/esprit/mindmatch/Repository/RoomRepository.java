package esprit.mindmatch.Repository;

import esprit.mindmatch.Entities.Room;
import esprit.mindmatch.Entities.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    Optional<Session> findBySession(Session competition);

}
