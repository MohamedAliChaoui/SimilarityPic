package pdl.backend;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.io.File;
import java.nio.file.Files;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.beans.factory.annotation.Value;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
public class ImageController {
  // Add these new endpoints for favorite functionality
  @PutMapping("/images/{id}/favorite")
  public ResponseEntity<?> toggleFavorite(@PathVariable("id") long id) {
      Optional<Image> image = imageRepository.findById(id);
      if (image.isEmpty()) {
          return new ResponseEntity<>("Image id=" + id + " not found.", HttpStatus.NOT_FOUND);
      }
      
      Image img = image.get();
      img.setFavorite(!img.isFavorite());
      imageRepository.save(img);
      
      return ResponseEntity.ok().build();
  }

  @PutMapping("/images/{id}/save")
  public ResponseEntity<?> toggleSave(@PathVariable("id") long id, @RequestHeader("Authorization") String authHeader) {
      // Extraire le token du header Authorization
      String token = authHeader.replace("Bearer ", "");
      
      // Vérifier l'authentification
      Long userId = AuthController.getUserIdFromToken(token);
      if (userId == null) {
          return new ResponseEntity<>("Non autorisé", HttpStatus.UNAUTHORIZED);
      }

      Optional<Image> imageOpt = imageRepository.findById(id);
      if (imageOpt.isEmpty()) {
          return new ResponseEntity<>("Image id=" + id + " not found.", HttpStatus.NOT_FOUND);
      }
      
      Optional<User> userOpt = userRepository.findById(userId);
      if (userOpt.isEmpty()) {
          return new ResponseEntity<>("Utilisateur non trouvé", HttpStatus.NOT_FOUND);
      }

      Image image = imageOpt.get();
      User user = userOpt.get();
      
      if (image.isSavedByUser(user)) {
          image.removeSavedByUser(user);
      } else {
          image.addSavedByUser(user);
      }
      
      imageRepository.save(image);
      
      return ResponseEntity.ok().build();
  }

  @GetMapping("/images/favorites")
  public ResponseEntity<List<Image>> getFavoriteImages() {
      List<Image> images = imageRepository.findByFavoriteTrue();
      return ResponseEntity.ok(images);
  }

  @GetMapping("/images/saved")
  public ResponseEntity<ArrayNode> getSavedImages(@RequestHeader("Authorization") String authHeader) {
      // Extraire le token du header Authorization
      String token = authHeader.replace("Bearer ", "");
      
      // Vérifier l'authentification
      Long userId = AuthController.getUserIdFromToken(token);
      if (userId == null) {
          return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
      }

      // Récupérer les images sauvegardées par l'utilisateur
      List<Image> images = imageRepository.findBySavedByUsersId(userId);
      ArrayNode nodes = mapper.createArrayNode();
      
      for (Image image : images) {
          ObjectNode objectNode = mapper.createObjectNode();
          objectNode.put("id", image.getId());
          objectNode.put("name", image.getName());
          objectNode.put("size", image.getSize());
          objectNode.put("format", image.getFormat());
          objectNode.put("favorite", image.isFavorite());
          objectNode.put("isSaved", true); // Toujours true car ce sont des images sauvegardées
          objectNode.put("isPublished", image.isPublished());
          objectNode.put("hasDescriptors", !image.getDescriptors().isEmpty());
          
          // Ajouter le nom d'utilisateur
          if (image.getUser() != null) {
              objectNode.put("username", image.getUser().getUsername());
          }
          
          nodes.add(objectNode);
      }
      
      return ResponseEntity.ok(nodes);
  }
  
  @GetMapping("/images/feed")
  public ResponseEntity<ArrayNode> getFeed() {
    try {
      // Récupérer uniquement les images publiées et non-null
      List<Image> publishedImages = imageRepository.findByPublishedTrue();
      ArrayNode nodes = mapper.createArrayNode();
      
      // Ajouter les images publiées
      for (Image image : publishedImages) {
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("id", image.getId());
        objectNode.put("name", image.getName());
        objectNode.put("size", image.getSize());
        objectNode.put("format", image.getFormat());
        objectNode.put("favorite", image.isFavorite());
        objectNode.put("isPublished", true);
        objectNode.put("hasDescriptors", !image.getDescriptors().isEmpty());
        
        // Si l'image a un utilisateur, utiliser son nom d'utilisateur
        // Sinon, utiliser le nom aléatoire fixe qui a été attribué au démarrage
        if (image.getUser() != null) {
          objectNode.put("username", image.getUser().getUsername());
        } else {
          objectNode.put("username", image.getName()); // Le nom aléatoire a déjà été attribué au démarrage
        }
        
        nodes.add(objectNode);
      }
      
      return ResponseEntity.ok(nodes);
    } catch (Exception e) {
      System.err.println("Erreur lors du chargement du feed: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @PutMapping("/images/{id}/publish")
  public ResponseEntity<?> togglePublish(@PathVariable("id") long id) {
    Optional<Image> image = imageRepository.findById(id);
    if (image.isEmpty()) {
      return new ResponseEntity<>("Image id=" + id + " not found.", HttpStatus.NOT_FOUND);
    }
    
    Image img = image.get();
    boolean newPublishState = !img.isPublished();
    img.setPublished(newPublishState);
    imageRepository.save(img);
    
    System.out.println("Image ID: " + id + " - État de publication changé à : " + newPublishState);
    
    return ResponseEntity.ok().build();
  }
  
  @Autowired
  private ObjectMapper mapper;

  private final ImageRepository imageRepository;
  private final UserRepository userRepository;
  private final Random random = new Random();
  private final String[] randomNames = {
    "Emma", "Lucas", "Léa", "Hugo", "Chloé", "Nathan", "Manon", "Enzo",
    "Camille", "Thomas", "Sarah", "Léo", "Clara", "Louis", "Inès", "Jules",
    "Lola", "Gabriel", "Zoé", "Raphaël", "Lilou", "Arthur", "Louise", "Adam",
    "Alice", "Noah", "Juliette", "Liam", "Rose", "Ethan", "Ambre", "Paul"
  };

  @Value("${image.upload.dir}")
  private String uploadDir;

  @PostConstruct
  public void init() {
    try {
      // Créer le dossier uploads s'il n'existe pas
      File uploadDirectory = new File(uploadDir);
      if (!uploadDirectory.exists()) {
        boolean created = uploadDirectory.mkdirs();
        if (!created) {
          throw new RuntimeException("Impossible de créer le dossier uploads");
        }
      }

      // Vérifier les permissions en écriture
      if (!uploadDirectory.canWrite()) {
        throw new RuntimeException("Pas de permissions en écriture sur le dossier uploads");
      }

      // Vérifier les permissions en lecture
      if (!uploadDirectory.canRead()) {
        throw new RuntimeException("Pas de permissions en lecture sur le dossier uploads");
      }

      System.out.println("Dossier uploads configuré avec succès : " + uploadDirectory.getAbsolutePath());
      
      // Vérifier s'il y a des images dans la base de données
      if (imageRepository.count() == 0) {
        // Charger les images par défaut si la base de données est vide
        loadDefaultImages();
      }
      
      // Attribuer des noms aléatoires fixes aux images sans utilisateur
      assignRandomNamesToAnonymousImages();
      
    } catch (Exception e) {
      System.err.println("Erreur lors de l'initialisation du dossier uploads : " + e.getMessage());
      throw new RuntimeException("Erreur critique : " + e.getMessage());
    }
  }

  private void loadDefaultImages() {
    File imagesDir = new File("backend/images");
    if (imagesDir.exists() && imagesDir.isDirectory()) {
        for (File file : imagesDir.listFiles()) {
            try {
                String randomName = getRandomName(); // Utiliser un nom aléatoire
                byte[] content = Files.readAllBytes(file.toPath());
                String format = file.getName().substring(file.getName().lastIndexOf(".") + 1).toLowerCase();
                
                Image image = new Image(
                    randomName + "." + format, // Nom aléatoire + extension
                    "backend/images/" + file.getName(),
                    content,
                    (long) content.length,
                    format
                );
                image.setPublished(true);  // Marquer l'image comme publiée
                imageRepository.save(image);
                System.out.println("Image chargée avec succès : " + randomName + "." + format);
            } catch (IOException e) {
                System.err.println("Erreur lors du chargement de l'image : " + file.getName());
                e.printStackTrace();
            }
        }
    } else {
        System.err.println("Le répertoire backend/images n'existe pas ou n'est pas un répertoire");
    }
  }

  private void assignRandomNamesToAnonymousImages() {
    try {
      List<Image> anonymousImages = imageRepository.findByUserIsNull();
      for (Image image : anonymousImages) {
        if (image.getName() == null || image.getName().isEmpty()) {
          image.setName(getRandomName());
        }
        // Définir l'image comme publiée pour qu'elle apparaisse dans le fil d'actualité
        image.setPublished(true);
        imageRepository.save(image);
      }
      System.out.println("Noms aléatoires attribués et images publiées avec succès");
    } catch (Exception e) {
      System.err.println("Erreur lors de l'attribution des noms aléatoires : " + e.getMessage());
    }
  }

  private String getRandomName() {
    return randomNames[random.nextInt(randomNames.length)];
  }

  @Autowired
  private DescriptorRepository descriptorRepository;

  @Autowired
  private ImageProcessor imageProcessor;

  @Autowired
  public ImageController(ImageRepository imageRepository, UserRepository userRepository) {
    this.imageRepository = imageRepository;
    this.userRepository = userRepository;
  }

  public enum DescriptorType {
    RGB3D, HS
  }

  @RequestMapping(value = "/images/{id}", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
  public ResponseEntity<?> getImage(@PathVariable("id") long id) {
    Optional<Image> image = imageRepository.findById(id);

    if (image.isPresent()) {
      return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(image.get().getData());
    }
    return new ResponseEntity<>("Image id=" + id + " not found.", HttpStatus.NOT_FOUND);
  }

  @RequestMapping(value = "/images/{id}", method = RequestMethod.DELETE)
  public ResponseEntity<?> deleteImage(@PathVariable("id") long id) {
    Optional<Image> image = imageRepository.findById(id);

    if (image.isPresent()) {
      imageRepository.delete(image.get());
      return new ResponseEntity<>("Image id=" + id + " deleted.", HttpStatus.OK);
    }
    return new ResponseEntity<>("Image id=" + id + " not found.", HttpStatus.NOT_FOUND);
  }

  @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<?> addImage(@RequestParam("file") MultipartFile file,
          @RequestHeader(value = "Authorization", required = false) String authHeader,
          RedirectAttributes redirectAttributes) {
      if (authHeader == null || authHeader.isEmpty()) {
          return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                  .body("Non autorisé - En-tête d'autorisation manquant");
      }
      
      // Extraire le token du header Authorization
      String token = authHeader.replace("Bearer ", "");
      System.out.println("Tentative d'upload d'image avec token: " + token);
      
      // Vérifier l'authentification
      Long userId = AuthController.getUserIdFromToken(token);
      if (userId == null) {
        System.err.println("Échec de l'authentification - Token invalide ou expiré");
        return new ResponseEntity<>("Non autorisé - Session expirée ou invalide", HttpStatus.UNAUTHORIZED);
      }
      System.out.println("Utilisateur authentifié avec ID: " + userId);

      String contentType = file.getContentType();

      // Vérification plus précise des types MIME
      if (contentType == null || !(contentType.equals(MediaType.IMAGE_JPEG_VALUE) ||
          contentType.equals(MediaType.IMAGE_PNG_VALUE) ||
          contentType.equals("image/jpg") ||
          contentType.equals("image/jpeg") ||
          contentType.equals("image/png"))) {
        System.err.println("Format d'image non supporté: " + contentType);
        return new ResponseEntity<>(
            "Format non supporté. Seuls les formats JPEG et PNG sont acceptés.",
            HttpStatus.UNSUPPORTED_MEDIA_TYPE);
      }

      try {
        String format = contentType.split("/")[1].toLowerCase();
        if (format.equals("jpg") || format.equals("jpeg")) {
          format = "jpeg";
        }

        Long size = file.getSize();
        byte[] imageData = file.getBytes();
        System.out.println("Image reçue - Nom: " + file.getOriginalFilename() + ", Taille: " + size + " octets, Format: " + format);

        // Création et sauvegarde de l'image
        Image image = new Image(file.getOriginalFilename(), "/images/" + file.getOriginalFilename(), imageData, size, format);
        
        // Associer l'image à l'utilisateur
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
          User user = userOpt.get();
          image.setUser(user);
          System.out.println("Image associée à l'utilisateur: " + user.getUsername());
        } else {
          System.err.println("Utilisateur non trouvé avec l'ID: " + userId);
          return new ResponseEntity<>("Utilisateur non trouvé", HttpStatus.NOT_FOUND);
        }

        // Génération des descripteurs
        try {
          // Génération du descripteur HS
          Descriptor hsDescriptor = imageProcessor.generateHSHistogram(imageData, image);
          image.addDescriptor(hsDescriptor);

          // Génération du descripteur RGB3D
          Descriptor rgb3dDescriptor = imageProcessor.generateRGB3DHistogram(imageData, image);
          if (rgb3dDescriptor != null) {
            image.addDescriptor(rgb3dDescriptor);
          }
          System.out.println("Descripteurs générés avec succès");
        } catch (Exception e) {
          System.err.println("Erreur lors de la génération des descripteurs: " + e.getMessage());
          e.printStackTrace();
        }

        // Sauvegarde de l'image avec ses descripteurs
        Image savedImage = imageRepository.save(image);
        System.out.println("Image sauvegardée avec succès, ID: " + savedImage.getId() + ", Propriétaire: " + savedImage.getUser().getUsername());

        return ResponseEntity.ok().body(String.valueOf(savedImage.getId()));
      } catch (IOException e) {
        System.err.println("Erreur lors de l'upload: " + e.getMessage());
        return new ResponseEntity<>("Erreur lors de la lecture du fichier", HttpStatus.INTERNAL_SERVER_ERROR);
      }
  }

  @RequestMapping(value = "/images/search", method = RequestMethod.GET)
  public ResponseEntity<ArrayNode> searchImages(
      @RequestParam(required = false) String name,
      @RequestParam(required = false) Long minSize,
      @RequestParam(required = false) Long maxSize,
      @RequestParam(required = false) String format) {

    List<Image> images = imageRepository.findAll();
    ArrayNode nodes = mapper.createArrayNode();

    // Filtrer les images selon les critères de recherche
    for (Image image : images) {
      // Vérifier si l'image correspond aux critères de recherche
      boolean matchesName = (name == null || name.isEmpty() || image.getName().toLowerCase().contains(name.toLowerCase()));
      boolean matchesFormat = (format == null || format.isEmpty() || image.getFormat().equalsIgnoreCase(format));
      boolean matchesSize = (minSize == null || image.getSize() >= minSize) && (maxSize == null || image.getSize() <= maxSize);

      if (matchesName && matchesFormat && matchesSize) {
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("id", image.getId());
        objectNode.put("name", image.getName());
        objectNode.put("size", image.getSize());
        objectNode.put("format", image.getFormat());
        objectNode.put("favorite", image.isFavorite());
        objectNode.put("saved", image.isSaved());
        objectNode.put("hasDescriptors", !image.getDescriptors().isEmpty());
        nodes.add(objectNode);
      }
    }

    return ResponseEntity.ok(nodes);
  }

  @RequestMapping(value = "/images", method = RequestMethod.GET, produces = "application/json")
  @ResponseBody
  public ArrayNode getImageList() {
    List<Image> images = imageRepository.findAll();
    ArrayNode nodes = mapper.createArrayNode();
    for (Image image : images) {
      ObjectNode objectNode = mapper.createObjectNode();
      objectNode.put("id", image.getId());
      objectNode.put("name", image.getName());
      objectNode.put("size", image.getSize());
      objectNode.put("format", image.getFormat());
      objectNode.put("favorite", image.isFavorite());
      objectNode.put("saved", image.isSaved());
      objectNode.put("hasDescriptors", !image.getDescriptors().isEmpty());
      nodes.add(objectNode);
    }
    return nodes;
  }

  // Add endpoints to access descriptors
  @RequestMapping(value = "/images/{id}/descriptors", method = RequestMethod.GET, produces = "application/json")
  @ResponseBody
  public ResponseEntity<?> getImageDescriptors(@PathVariable("id") long id) {
    Optional<Image> image = imageRepository.findById(id);

    if (image.isPresent()) {
      List<Descriptor> descriptors = descriptorRepository.findByImageId(id);

      ArrayNode nodes = mapper.createArrayNode();
      for (Descriptor descriptor : descriptors) {
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("id", descriptor.getId());
        objectNode.put("type", descriptor.getType());
        // Don't include the full histogram data unless specifically requested
        objectNode.put("size", descriptor.getHistogramData().length);
        nodes.add(objectNode);
      }

      return ResponseEntity.ok(nodes);
    }

    return new ResponseEntity<>("Image id=" + id + " not found.", HttpStatus.NOT_FOUND);
  }

  @RequestMapping(value = "/images/{id}/descriptors/{descriptorId}", method = RequestMethod.GET, produces = "application/json")
  @ResponseBody
  public ResponseEntity<?> getDescriptor(@PathVariable("id") long imageId,
      @PathVariable("descriptorId") long descriptorId) {
    Optional<Descriptor> descriptor = descriptorRepository.findById(descriptorId);

    if (descriptor.isPresent() && descriptor.get().getImage().getId() == imageId) {
      ObjectNode responseNode = mapper.createObjectNode();
      responseNode.put("id", descriptor.get().getId());
      responseNode.put("type", descriptor.get().getType());

      // Convert float array to JSON array
      ArrayNode histogramNode = mapper.createArrayNode();
      for (float value : descriptor.get().getHistogramData()) {
        histogramNode.add(value);
      }
      responseNode.set("histogram", histogramNode);

      return ResponseEntity.ok(responseNode);
    }

    return new ResponseEntity<>("Descriptor not found.", HttpStatus.NOT_FOUND);
  }

  @RequestMapping(value = "/images/{id}/similar", method = RequestMethod.GET, produces = "application/json")
  public ResponseEntity<?> findSimilarImages(
      @PathVariable("id") long id,
      @RequestParam(value = "number", defaultValue = "5") int number,
      @RequestParam(value = "descriptor", defaultValue = "RGB3D") DescriptorType descriptorType) {

    // Valider le nombre d'images similaires demandé
    if (number <= 0) {
      return new ResponseEntity<>("Number of similar images must be positive.", HttpStatus.BAD_REQUEST);
    }

    Optional<Image> queryImage = imageRepository.findById(id);
    if (queryImage.isEmpty()) {
      return new ResponseEntity<>("Image id=" + id + " not found.", HttpStatus.NOT_FOUND);
    }

    try {
      // Récupérer toutes les images pour la comparaison
      List<Image> allImages = imageRepository.findAll();

      // Utiliser ImageProcessor pour trouver les images similaires
      List<ImageSimilarity> similarImages = imageProcessor.findSimilarImages(
          queryImage.get(),
          allImages,
          number);

      // Convertir les résultats en JSON
      ArrayNode nodes = mapper.createArrayNode();
      for (ImageSimilarity sim : similarImages) {
        Image image = sim.getImage();
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("id", image.getId());
        objectNode.put("name", image.getName());
        objectNode.put("size", image.getSize());
        objectNode.put("format", image.getFormat());
        objectNode.put("similarity", sim.getSimilarity());
        nodes.add(objectNode);
      }

      return ResponseEntity.ok(nodes);
    } catch (Exception e) {
      return new ResponseEntity<>("Error processing similarity: " + e.getMessage(),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/images/personal")
  public ResponseEntity<ArrayNode> getPersonalImages(@RequestParam("token") String token) {
    // Vérifier le token
    if (!AuthController.isValidToken(token)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    
    // Récupérer les images sans utilisateur associé
    List<Image> images = imageRepository.findByUserIsNull();
    ArrayNode nodes = mapper.createArrayNode();
    
    for (Image image : images) {
      ObjectNode objectNode = mapper.createObjectNode();
      objectNode.put("id", image.getId());
      objectNode.put("name", image.getName());
      objectNode.put("size", image.getSize());
      objectNode.put("format", image.getFormat());
      objectNode.put("favorite", image.isFavorite());
      objectNode.put("saved", image.isSaved());
      objectNode.put("isPublished", image.isPublished());
      objectNode.put("hasDescriptors", !image.getDescriptors().isEmpty());
      
      nodes.add(objectNode);
    }
    
    return ResponseEntity.ok(nodes);
  }

  @GetMapping("/images/my-gallery")
  public ResponseEntity<ArrayNode> getMyGallery(@RequestHeader("Authorization") String authHeader) {
      // Extraire le token du header Authorization
      String token = authHeader.replace("Bearer ", "");
      
      // Vérifier l'authentification
      Long userId = AuthController.getUserIdFromToken(token);
      if (userId == null) {
          return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
      }

      // Récupérer l'utilisateur
      Optional<User> userOpt = userRepository.findById(userId);
      if (userOpt.isEmpty()) {
          return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }
      User user = userOpt.get();

      // Récupérer les images uploadées par l'utilisateur
      List<Image> uploadedImages = imageRepository.findByUserId(userId);
      // Récupérer les images sauvegardées par l'utilisateur
      List<Image> savedImages = imageRepository.findBySavedByUsersId(userId);
      
      // Combiner les deux listes en évitant les doublons
      ArrayNode nodes = mapper.createArrayNode();
      
      // Ajouter les images uploadées
      for (Image image : uploadedImages) {
          ObjectNode objectNode = mapper.createObjectNode();
          objectNode.put("id", image.getId());
          objectNode.put("name", image.getName());
          objectNode.put("size", image.getSize());
          objectNode.put("format", image.getFormat());
          objectNode.put("favorite", image.isFavorite());
          objectNode.put("isSaved", image.isSavedByUser(user));
          objectNode.put("isPublished", image.isPublished());
          objectNode.put("hasDescriptors", !image.getDescriptors().isEmpty());
          
          // Ajouter le nom d'utilisateur
          if (image.getUser() != null) {
              objectNode.put("username", image.getUser().getUsername());
          }
          
          nodes.add(objectNode);
      }
      
      // Ajouter les images sauvegardées qui ne sont pas déjà dans la liste
      for (Image image : savedImages) {
          // Vérifier si l'image n'est pas déjà dans la liste
          boolean alreadyExists = false;
          for (int i = 0; i < nodes.size(); i++) {
              if (nodes.get(i).get("id").asLong() == image.getId()) {
                  alreadyExists = true;
                  break;
              }
          }
          
          if (!alreadyExists) {
              ObjectNode objectNode = mapper.createObjectNode();
              objectNode.put("id", image.getId());
              objectNode.put("name", image.getName());
              objectNode.put("size", image.getSize());
              objectNode.put("format", image.getFormat());
              objectNode.put("favorite", image.isFavorite());
              objectNode.put("isSaved", true); // Toujours true car c'est une image sauvegardée
              objectNode.put("isPublished", image.isPublished());
              objectNode.put("hasDescriptors", !image.getDescriptors().isEmpty());
              
              // Ajouter le nom d'utilisateur
              if (image.getUser() != null) {
                  objectNode.put("username", image.getUser().getUsername());
              }
              
              nodes.add(objectNode);
          }
      }
      
      return ResponseEntity.ok(nodes);
  }

  private String generateRandomName() {
    String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    Random random = new Random();
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 10; i++) {
      sb.append(chars.charAt(random.nextInt(chars.length())));
    }
    return sb.toString();
  }
}
