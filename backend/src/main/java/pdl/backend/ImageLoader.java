package pdl.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

@Component
public class ImageLoader implements CommandLineRunner {

    private final ImageRepository imageRepository;
    private final ImageProcessor imageProcessor;
    private static final String IMAGES_FOLDER = "images";
    private static final List<String> SUPPORTED_FORMATS = Arrays.asList(
            "image/jpeg", "image/png");
    private static final Logger logger = Logger.getLogger(ImageLoader.class.getName());

    @Autowired
    public ImageLoader(ImageRepository imageRepository, ImageProcessor imageProcessor) {
        this.imageRepository = imageRepository;
        this.imageProcessor = imageProcessor;
    }

    @Override
    public void run(String... args) throws Exception {
        loadImagesFromDisk();
    }

    public void loadImagesFromDisk() throws IOException {
        Path imagesPath = Paths.get(IMAGES_FOLDER);

        // Check if folder exists
        if (!Files.exists(imagesPath) || !Files.isDirectory(imagesPath)) {
            System.out.println(
                    "Avertissement : Le dossier 'images' n'existe pas ou n'est pas un répertoire à l'emplacement "
                            + imagesPath.toAbsolutePath() + ". Aucune image initiale ne sera chargée.");
            return;
        }

        File imagesDir = imagesPath.toFile();
        File[] files = imagesDir.listFiles();

        if (files == null) {
            throw new IOException("Impossible de lister les fichiers du dossier 'images'.");
        }

        int imageCount = 0;
        System.out.println("Chargement des images du dossier: " + imagesPath.toAbsolutePath());

        for (File file : files) {
            if (file.isFile()) {
                String contentType = Files.probeContentType(file.toPath());

                if (contentType != null && SUPPORTED_FORMATS.contains(contentType)) {
                    try {
                        // Vérifier si l'image existe déjà dans la base de données par son nom
                        if (imageRepository.existsByName(file.getName())) {
                            System.out.println(
                                    "L'image " + file.getName() + " existe déjà dans la base de données. Ignorée.");
                            continue; // Passer au fichier suivant
                        }

                        byte[] imageData = Files.readAllBytes(file.toPath());
                        Long size = file.length();
                        String format = contentType.split("/")[1];
                        String imagePath = file.getAbsolutePath();

                        Image image = new Image(file.getName(), imagePath, imageData, size, format);

                        // Vérifier si l'image existe déjà
                        if (imageRepository.findByName(file.getName()).isPresent()) {
                            System.out.println("L'image " + file.getName() + " existe déjà dans la base de données.");
                            continue;
                        }

                        // Generate and add descriptors
                        generateAndAddDescriptors(imageData, image);

                        imageRepository.save(image);
                        imageCount++;
                        System.out
                                .println("Image chargée: " + file.getName() + " (" + format + ", " + size + " octets)");
                    } catch (IOException e) {
                        System.err.println(
                                "Erreur lors du chargement de l'image " + file.getName() + ": " + e.getMessage());
                    }
                } else {
                    System.out.println(
                            "Format non supporté pour le fichier: " + file.getName() + " (Type: " + contentType + ")");
                }
            }
        }

        System.out.println("Chargement terminé: " + imageCount + " images ont été indexées.");
    }

    private void generateAndAddDescriptors(byte[] imageData, Image image) {
        try {
            // Génération du descripteur HS
            Descriptor hsDescriptor = imageProcessor.generateHSHistogram(imageData, image);
            image.addDescriptor(hsDescriptor);

            // Génération du descripteur RGB 3D
            Descriptor rgb3dDescriptor = imageProcessor.generateRGB3DHistogram(imageData, image);
            if (rgb3dDescriptor != null) {
                image.addDescriptor(rgb3dDescriptor);
            }

        } catch (Exception e) {
            logger.warning("Erreur lors de la génération des descripteurs pour l'image " + image.getName() + ": "
                    + e.getMessage());
        }
    }
}