package pdl.backend;
import java.util.List;  
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findByFavoriteTrue();
    
    // Méthode pour vérifier si une image existe déjà par son nom
    boolean existsByName(String name);
    Optional<Image> findByName(String name);
    List<Image> findBySavedTrue();
    List<Image> findByPublishedTrue();
    List<Image> findByUserIsNull();
    List<Image> findByUserId(Long userId);
    List<Image> findBySavedByUsersId(Long userId);
}
