package pdl.backend;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DescriptorTests {

    @Test
    public void testDescriptorCreation() {
        Image image = new Image("test.jpg", "/images/test.jpg", new byte[] {}, 0L, "jpeg");
        float[] histogramData = new float[] { 0.1f, 0.2f, 0.3f };
        Descriptor descriptor = new Descriptor("TEST_TYPE", histogramData, image);

        assertNotNull(descriptor, "Le descripteur ne devrait pas être null");
        assertEquals("TEST_TYPE", descriptor.getType(), "Le type du descripteur devrait correspondre");
        assertArrayEquals(histogramData, descriptor.getHistogramData(), 0.001f,
                "Les données de l'histogramme devraient correspondre");
        assertEquals(image, descriptor.getImage(), "L'image associée devrait correspondre");
    }

    @Test
    public void testDescriptorWithNullHistogram() {
        Image image = new Image("test.jpg", "/images/test.jpg", new byte[] {}, 0L, "jpeg");

        assertThrows(IllegalArgumentException.class, () -> {
            new Descriptor("TEST_TYPE", null, image);
        }, "La création d'un descripteur avec un histogramme null devrait lever une exception");
    }

    @Test
    public void testDescriptorWithNullType() {
        Image image = new Image("test.jpg", "/images/test.jpg", new byte[] {}, 0L, "jpeg");
        float[] histogramData = new float[] { 0.1f, 0.2f, 0.3f };

        assertThrows(IllegalArgumentException.class, () -> {
            new Descriptor(null, histogramData, image);
        }, "La création d'un descripteur avec un type null devrait lever une exception");
    }

    @Test
    public void testDescriptorWithNullImage() {
        float[] histogramData = new float[] { 0.1f, 0.2f, 0.3f };

        assertThrows(IllegalArgumentException.class, () -> {
            new Descriptor("TEST_TYPE", histogramData, null);
        }, "La création d'un descripteur avec une image null devrait lever une exception");
    }

    @Test
    public void testDescriptorEquality() {
        Image image = new Image("test.jpg", "/images/test.jpg", new byte[] {}, 0L, "jpeg");
        float[] histogramData = new float[] { 0.1f, 0.2f, 0.3f };

        Descriptor descriptor1 = new Descriptor("TEST_TYPE", histogramData, image);
        Descriptor descriptor2 = new Descriptor("TEST_TYPE", histogramData.clone(), image);
        Descriptor descriptor3 = new Descriptor("OTHER_TYPE", histogramData.clone(), image);

        assertEquals(descriptor1, descriptor2, "Des descripteurs avec les mêmes données devraient être égaux");
        assertNotEquals(descriptor1, descriptor3,
                "Des descripteurs avec des types différents ne devraient pas être égaux");
    }

    @Test
    public void testDescriptorHashCode() {
        Image image = new Image("test.jpg", "/images/test.jpg", new byte[] {}, 0L, "jpeg");
        float[] histogramData = new float[] { 0.1f, 0.2f, 0.3f };

        Descriptor descriptor1 = new Descriptor("TEST_TYPE", histogramData, image);
        Descriptor descriptor2 = new Descriptor("TEST_TYPE", histogramData.clone(), image);

        assertEquals(descriptor1.hashCode(), descriptor2.hashCode(),
                "Des descripteurs égaux devraient avoir le même hashCode");
    }
}