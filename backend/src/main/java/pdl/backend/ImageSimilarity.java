package pdl.backend;

/**
 * Classe représentant la similarité entre deux images
 * Utilisée pour le besoin 9 : Recherche d'images similaires
 */
public class ImageSimilarity {
    private final Image image;
    private final double similarity;

    public ImageSimilarity(Image image, double similarity) {
        this.image = image;
        this.similarity = similarity;
    }

    public Image getImage() {
        return image;
    }

    public double getSimilarity() {
        return similarity;
    }
} 