package pdl.backend;

import org.springframework.stereotype.Service;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

@Service
public class ImageProcessor {
    private static final Logger logger = Logger.getLogger(ImageProcessor.class.getName());
    private static final int HS_BINS = 30; // Nombre de bins pour la teinte et la saturation
    private static final String HS_DESCRIPTOR = "HS_HISTOGRAM";
    private static final int BINS_PER_COLOR = 8; // Nombre de bins pour chaque composante de couleur
    private static final int TOTAL_BINS = BINS_PER_COLOR * BINS_PER_COLOR * BINS_PER_COLOR; // Total pour RGB 3D
    private static final int RGB_BINS = 256; // Pour l'histogramme RGB simple
    private static final int HSV_BINS = 30; // Pour l'histogramme HSV

    /**
     * Génère le descripteur Teinte/Saturation pour une image
     * Implémente le Besoin 3 : Indexation d'une image avec descripteur HS
     */
    public Descriptor generateHSHistogram(byte[] imageData, Image image) throws IOException {
        // Lecture de l'image
        BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imageData));
        if (bufferedImage == null) {
            logger.severe("Erreur: Impossible de lire l'image " + image.getName());
            throw new IOException("Erreur lors de la lecture de l'image " + image.getName());
        }

        // Initialisation de l'histogramme HS
        float[] histogram = new float[HS_BINS * HS_BINS];
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        int totalPixels = width * height;
        int processedPixels = 0;

        // Parcours de chaque pixel
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = bufferedImage.getRGB(x, y);

                // Extraction des composantes RGB
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                // Conversion en HSV
                float[] hsv = rgbToHsv(r, g, b);
                float h = hsv[0]; // Teinte
                float s = hsv[1]; // Saturation

                // Ignorer les pixels avec une saturation trop faible
                if (s < 0.1f) {
                    continue;
                }

                // Calcul des indices des bins
                int hBin = Math.min((int) (h * HS_BINS), HS_BINS - 1);
                int sBin = Math.min((int) (s * HS_BINS), HS_BINS - 1);

                // Mise à jour de l'histogramme
                histogram[hBin + sBin * HS_BINS]++;
                processedPixels++;
            }
        }

        // Normalisation de l'histogramme
        if (processedPixels > 0) {
            for (int i = 0; i < histogram.length; i++) {
                histogram[i] /= processedPixels;
            }
        }

        return new Descriptor(HS_DESCRIPTOR, histogram, image);
    }

    /**
     * Calcule la similarité entre deux descripteurs
     * Supporte les types : HS_HISTOGRAM, RGB, HSV, RGB3D
     */
    public float computeSimilarity(Descriptor desc1, Descriptor desc2) {
        if (desc1 == null || desc2 == null) {
            return 0;
        }

        float[] hist1 = desc1.getHistogramData();
        float[] hist2 = desc2.getHistogramData();

        if (hist1 == null || hist2 == null) {
            return 0;
        }

        String type = desc1.getType();
        if (!type.equals(desc2.getType())) {
            throw new IllegalArgumentException("Les descripteurs doivent être du même type");
        }

        switch (type) {
            case "HS_HISTOGRAM":
                // Calcul de la distance euclidienne pour HS
                double sum = 0.0;
                for (int i = 0; i < hist1.length; i++) {
                    double diff = hist1[i] - hist2[i];
                    sum += diff * diff;
                }
                double distance = Math.sqrt(sum);
                return (float) (1.0 / (1.0 + distance));

            case "RGB":
                return computeRGBSimilarity(hist1, hist2);
            case "HSV":
                return computeHSVSimilarity(hist1, hist2);
            case "RGB3D":
                return computeRGB3DSimilarity(hist1, hist2);
            default:
                throw new IllegalArgumentException("Type de descripteur non supporté : " + type);
        }
    }

    /**
     * Trouve les N images les plus similaires à une image donnée
     * Implémente le Besoin 4 : Recherche d'images similaires
     */
    public List<ImageSimilarity> findSimilarImages(Image queryImage, List<Image> allImages, int n) {
        List<ImageSimilarity> similarities = new ArrayList<>();
        Descriptor queryDesc = queryImage.getDescriptor();

        for (Image image : allImages) {
            if (image.getId() != queryImage.getId()) {
                double similarity = computeSimilarity(queryDesc, image.getDescriptor());
                similarities.add(new ImageSimilarity(image, similarity));
            }
        }

        // Tri par similarité décroissante
        similarities.sort(Comparator.comparingDouble(ImageSimilarity::getSimilarity).reversed());

        // Retourne les N premiers résultats
        return similarities.subList(0, Math.min(n, similarities.size()));
    }

    private float[] rgbToHsv(int r, int g, int b) {
        float[] hsv = new float[3];
        float rf = r / 255f;
        float gf = g / 255f;
        float bf = b / 255f;

        float max = Math.max(rf, Math.max(gf, bf));
        float min = Math.min(rf, Math.min(gf, bf));
        float delta = max - min;

        // Calcul de la teinte (H)
        if (delta == 0) {
            hsv[0] = 0;
        } else if (max == rf) {
            hsv[0] = ((gf - bf) / delta) % 6;
        } else if (max == gf) {
            hsv[0] = ((bf - rf) / delta) + 2;
        } else {
            hsv[0] = ((rf - gf) / delta) + 4;
        }

        hsv[0] /= 6f; // Normalisation à [0,1]
        if (hsv[0] < 0)
            hsv[0] += 1;

        // Calcul de la saturation (S)
        hsv[1] = max == 0 ? 0 : delta / max;

        // Calcul de la valeur (V)
        hsv[2] = max;

        return hsv;
    }

    public Descriptor generateRGB3DHistogram(byte[] imageData, Image image) {
        try {
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imageData));
            if (bufferedImage == null) {
                throw new IOException("Impossible de lire l'image");
            }

            float[] histogram = new float[TOTAL_BINS];
            int totalPixels = bufferedImage.getWidth() * bufferedImage.getHeight();

            // Parcourir chaque pixel de l'image
            for (int y = 0; y < bufferedImage.getHeight(); y++) {
                for (int x = 0; x < bufferedImage.getWidth(); x++) {
                    int rgb = bufferedImage.getRGB(x, y);
                    int r = (rgb >> 16) & 0xFF;
                    int g = (rgb >> 8) & 0xFF;
                    int b = rgb & 0xFF;

                    // Quantifier les valeurs RGB en bins
                    int rBin = r * BINS_PER_COLOR / 256;
                    int gBin = g * BINS_PER_COLOR / 256;
                    int bBin = b * BINS_PER_COLOR / 256;

                    // Calculer l'index dans l'histogramme 3D
                    int index = (rBin * BINS_PER_COLOR * BINS_PER_COLOR) + (gBin * BINS_PER_COLOR) + bBin;
                    histogram[index]++;
                }
            }

            // Normaliser l'histogramme
            for (int i = 0; i < histogram.length; i++) {
                histogram[i] /= totalPixels;
            }

            return new Descriptor("RGB3D", histogram, image);

        } catch (IOException e) {
            logger.warning("Erreur lors de la génération de l'histogramme RGB 3D : " + e.getMessage());
            return null;
        }
    }

    private float computeRGB3DSimilarity(float[] hist1, float[] hist2) {
        if (hist1.length != TOTAL_BINS || hist2.length != TOTAL_BINS) {
            throw new IllegalArgumentException("Les histogrammes doivent avoir " + TOTAL_BINS + " bins");
        }

        float intersection = 0;
        float union = 0;

        for (int i = 0; i < TOTAL_BINS; i++) {
            intersection += Math.min(hist1[i], hist2[i]);
            union += Math.max(hist1[i], hist2[i]);
        }

        return union == 0 ? 0 : intersection / union;
    }

    private float computeRGBSimilarity(float[] hist1, float[] hist2) {
        if (hist1.length != RGB_BINS || hist2.length != RGB_BINS) {
            throw new IllegalArgumentException("Les histogrammes RGB doivent avoir " + RGB_BINS + " bins");
        }

        float intersection = 0;
        float union = 0;

        for (int i = 0; i < RGB_BINS; i++) {
            intersection += Math.min(hist1[i], hist2[i]);
            union += Math.max(hist1[i], hist2[i]);
        }

        return union == 0 ? 0 : intersection / union;
    }

    private float computeHSVSimilarity(float[] hist1, float[] hist2) {
        if (hist1.length != HSV_BINS * HSV_BINS || hist2.length != HSV_BINS * HSV_BINS) {
            throw new IllegalArgumentException("Les histogrammes HSV doivent avoir " + (HSV_BINS * HSV_BINS) + " bins");
        }

        float intersection = 0;
        float union = 0;

        for (int i = 0; i < HSV_BINS * HSV_BINS; i++) {
            intersection += Math.min(hist1[i], hist2[i]);
            union += Math.max(hist1[i], hist2[i]);
        }

        return union == 0 ? 0 : intersection / union;
    }
}
