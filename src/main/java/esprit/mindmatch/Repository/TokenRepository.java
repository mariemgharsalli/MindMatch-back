package esprit.mindmatch.Repository;

import esprit.mindmatch.Entities.Token;
import esprit.mindmatch.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenRepository  extends JpaRepository<Token, Integer> {

    Optional<Token> findByToken(String token);
    @Query("SELECT t.user FROM Token t WHERE t.token =:token")
    User findUserByToken(@Param("token") String token);

}
