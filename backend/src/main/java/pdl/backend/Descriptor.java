package pdl.backend;

import jakarta.persistence.*;
import java.util.Arrays;
import java.util.Objects;

@Entity
@Table(name = "descriptors")
public class Descriptor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type; // "HS_HISTOGRAM" or "RGB_HISTOGRAM"

    @Column(name = "histogram_data")
    private float[] histogramData;

    @ManyToOne
    @JoinColumn(name = "image_id")
    private Image image;

    // Default constructor required by JPA
    public Descriptor() {
    }

    public Descriptor(String type, float[] histogramData, Image image) {
        if (type == null) {
            throw new IllegalArgumentException("Le type ne peut pas être null");
        }
        if (histogramData == null) {
            throw new IllegalArgumentException("L'histogramme ne peut pas être null");
        }
        if (image == null) {
            throw new IllegalArgumentException("L'image ne peut pas être null");
        }
        this.type = type;
        this.histogramData = histogramData.clone();
        this.image = image;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        if (type == null) {
            throw new IllegalArgumentException("Le type ne peut pas être null");
        }
        this.type = type;
    }

    public float[] getHistogramData() {
        return histogramData != null ? histogramData.clone() : null;
    }

    public void setHistogramData(float[] histogramData) {
        if (histogramData == null) {
            throw new IllegalArgumentException("L'histogramme ne peut pas être null");
        }
        this.histogramData = histogramData.clone();
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        if (image == null) {
            throw new IllegalArgumentException("L'image ne peut pas être null");
        }
        this.image = image;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Descriptor that = (Descriptor) o;
        return Objects.equals(type, that.type) &&
                Arrays.equals(histogramData, that.histogramData);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(type);
        result = 31 * result + Arrays.hashCode(histogramData);
        return result;
    }

    @Override
    public String toString() {
        return "Descriptor{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", histogramData=" + Arrays.toString(histogramData) +
                '}';
    }
}