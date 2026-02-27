package pdl.backend;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class ImageTest {

    @Test
    void testImageCreation() {
        // Créer une nouvelle image
        Image image = new Image(
            "test.jpg",
            "/images/test.jpg",
            new byte[]{1, 2, 3},
            3L,
            "jpeg"
        );

        // Vérifier les valeurs
        assertEquals("test.jpg", image.getName());
        assertEquals("/images/test.jpg", image.getPath());
        assertArrayEquals(new byte[]{1, 2, 3}, image.getData());
        assertEquals(3L, image.getSize());
        assertEquals("jpeg", image.getFormat());
        assertFalse(image.isPublished());
        assertFalse(image.isFavorite());
    }

    @Test
    void testTogglePublished() {
        // Créer une nouvelle image
        Image image = new Image(
            "test.jpg",
            "/images/test.jpg",
            new byte[]{1, 2, 3},
            3L,
            "jpeg"
        );

        // Vérifier l'état initial
        assertFalse(image.isPublished());

        // Changer l'état
        image.setPublished(true);
        assertTrue(image.isPublished());

        // Changer à nouveau
        image.setPublished(false);
        assertFalse(image.isPublished());
    }

    @Test
    void testToggleFavorite() {
        // Créer une nouvelle image
        Image image = new Image(
            "test.jpg",
            "/images/test.jpg",
            new byte[]{1, 2, 3},
            3L,
            "jpeg"
        );

        // Vérifier l'état initial
        assertFalse(image.isFavorite());

        // Changer l'état
        image.setFavorite(true);
        assertTrue(image.isFavorite());

        // Changer à nouveau
        image.setFavorite(false);
        assertFalse(image.isFavorite());
    }
} 