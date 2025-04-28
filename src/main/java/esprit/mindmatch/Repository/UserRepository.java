package esprit.mindmatch.Repository;

import esprit.mindmatch.Entities.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    User getUserByEmail(String email);


    @EntityGraph(attributePaths = {"profile"})
    Optional<User> findWithProfileByEmail(String email);


}