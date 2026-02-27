package pdl.backend;

import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    
    // Méthode pour rechercher des utilisateurs par nom d'utilisateur, prénom ou nom
    List<User> findByUsernameContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
        String username, String firstName, String lastName);
        
    // Méthode optimisée qui évite de charger les champs LOB pour la recherche
    @Query("SELECT new User(u.id, u.username, u.firstName, u.lastName) FROM User u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> findUsersBySearchTerm(@Param("searchTerm") String searchTerm);
    
    // Méthode spécifique pour l'authentification sans charger les données binaires
    @Query("SELECT new pdl.backend.AuthUserDTO(u.id, u.username, u.password, u.email, u.firstName, " +
           "u.lastName, u.birthDate, u.phoneNumber, u.bio, " +
           "CASE WHEN u.profilePictureFormat IS NOT NULL AND LENGTH(u.profilePictureFormat) > 0 THEN true ELSE false END) " +
           "FROM User u WHERE u.username = :username")
    Optional<AuthUserDTO> findUserForAuthentication(@Param("username") String username);
}
