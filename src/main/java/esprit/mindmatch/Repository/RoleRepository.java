package esprit.mindmatch.Repository;

import esprit.mindmatch.Entities.ERole;
import esprit.mindmatch.Entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    Optional<Role> findByRole(ERole role);
    boolean existsByRole(ERole role);

}