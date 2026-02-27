package pdl.backend;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.core.io.ClassPathResource;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;

public class ImageProcessorTests {

    private ImageProcessor imageProcessor;
    private byte[] testImageData;
    private Image testImage;

    @BeforeEach
    public void setup() throws IOException {
        imageProcessor = new ImageProcessor();
        ClassPathResource imgFile = new ClassPathResource("test.jpg");
        testImageData = Files.readAllBytes(imgFile.getFile().toPath());
        testImage = new Image("test.jpg", "/images/test.jpg", testImageData, (long) testImageData.length, "jpeg");
    }

    @Test
    public void testGenerateHSHistogram() throws IOException {
        Descriptor descriptor = imageProcessor.generateHSHistogram(testImageData, testImage);

        assertNotNull(descriptor, "Le descripteur HS ne devrait pas être null");
        assertEquals("HS_HISTOGRAM", descriptor.getType(), "Le type du descripteur devrait être HS_HISTOGRAM");
        assertNotNull(descriptor.getHistogramData(), "Les données de l'histogramme ne devraient pas être null");
        assertTrue(descriptor.getHistogramData().length > 0, "L'histogramme devrait contenir des données");
    }

    @Test
    public void testGenerateRGB3DHistogram() {
        Descriptor descriptor = imageProcessor.generateRGB3DHistogram(testImageData, testImage);

        assertNotNull(descriptor, "Le descripteur RGB3D ne devrait pas être null");
        assertEquals("RGB3D", descriptor.getType(), "Le type du descripteur devrait être RGB3D");
        assertNotNull(descriptor.getHistogramData(), "Les données de l'histogramme ne devraient pas être null");
        assertEquals(512, descriptor.getHistogramData().length, "L'histogramme RGB3D devrait avoir 512 bins (8x8x8)");
    }

    @Test
    public void testComputeSimilarityWithSameImage() throws IOException {
        // Créer deux descripteurs identiques
        Descriptor desc1 = imageProcessor.generateHSHistogram(testImageData, testImage);
        Descriptor desc2 = imageProcessor.generateHSHistogram(testImageData, testImage);

        float similarity = imageProcessor.computeSimilarity(desc1, desc2);
        assertEquals(1.0f, similarity, 0.001f, "La similarité entre une image et elle-même devrait être 1.0");
    }

    @Test
    public void testComputeSimilarityWithDifferentTypes() throws IOException {
        Descriptor hsDesc = imageProcessor.generateHSHistogram(testImageData, testImage);
        Descriptor rgbDesc = imageProcessor.generateRGB3DHistogram(testImageData, testImage);

        assertThrows(IllegalArgumentException.class, () -> {
            imageProcessor.computeSimilarity(hsDesc, rgbDesc);
        }, "La comparaison de descripteurs de types différents devrait lever une exception");
    }

    @Test
    public void testComputeSimilarityWithNullDescriptors() {
        assertEquals(0.0f, imageProcessor.computeSimilarity(null, null),
                "La similarité avec des descripteurs null devrait être 0");
    }

    @Test
    public void testNormalization() throws IOException {
        Descriptor hsDesc = imageProcessor.generateHSHistogram(testImageData, testImage);
        float[] histogram = hsDesc.getHistogramData();

        float sum = 0;
        for (float value : histogram) {
            assertTrue(value >= 0 && value <= 1,
                    "Les valeurs de l'histogramme devraient être normalisées entre 0 et 1");
            sum += value;
        }
        assertTrue(Math.abs(sum - 1.0) < 0.01, "La somme des valeurs normalisées devrait être proche de 1");
    }
}