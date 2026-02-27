package pdl.backend;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

@DataJpaTest
public class ImageRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ImageRepository imageRepository;

    @Test
    void testFindByPublishedTrue() {
        // Créer quelques images de test
        Image publishedImage = new Image("published.jpg", "/images/published.jpg", new byte[]{1}, 1L, "jpeg");
        publishedImage.setPublished(true);
        
        Image unpublishedImage = new Image("unpublished.jpg", "/images/unpublished.jpg", new byte[]{2}, 1L, "jpeg");
        unpublishedImage.setPublished(false);

        // Sauvegarder les images
        entityManager.persist(publishedImage);
        entityManager.persist(unpublishedImage);
        entityManager.flush();

        // Rechercher les images publiées
        List<Image> publishedImages = imageRepository.findByPublishedTrue();

        // Vérifier les résultats
        assertEquals(1, publishedImages.size());
        assertEquals("published.jpg", publishedImages.get(0).getName());
    }

    @Test
    void testFindByUserIsNull() {
        // Créer une image sans utilisateur
        Image anonymousImage = new Image("anonymous.jpg", "/images/anonymous.jpg", new byte[]{1}, 1L, "jpeg");
        entityManager.persist(anonymousImage);
        entityManager.flush();

        // Rechercher les images sans utilisateur
        List<Image> anonymousImages = imageRepository.findByUserIsNull();

        // Vérifier les résultats
        assertEquals(1, anonymousImages.size());
        assertEquals("anonymous.jpg", anonymousImages.get(0).getName());
    }

    @Test
    void testFindByFavoriteTrue() {
        // Créer une image favorite
        Image favoriteImage = new Image("favorite.jpg", "/images/favorite.jpg", new byte[]{1}, 1L, "jpeg");
        favoriteImage.setFavorite(true);
        
        Image nonFavoriteImage = new Image("normal.jpg", "/images/normal.jpg", new byte[]{2}, 1L, "jpeg");
        
        // Sauvegarder les images
        entityManager.persist(favoriteImage);
        entityManager.persist(nonFavoriteImage);
        entityManager.flush();

        // Rechercher les images favorites
        List<Image> favoriteImages = imageRepository.findByFavoriteTrue();

        // Vérifier les résultats
        assertEquals(1, favoriteImages.size());
        assertEquals("favorite.jpg", favoriteImages.get(0).getName());
    }
} 