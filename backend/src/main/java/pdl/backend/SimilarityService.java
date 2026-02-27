package pdl.backend;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
@Service
public class SimilarityService {
    private static final Logger logger = Logger.getLogger(SimilarityService.class.getName());
    
    @Autowired
    private ImageRepository imageRepository;
    
    @Autowired
    private DescriptorRepository descriptorRepository;
    
    /**
     * Trouve les N images les plus similaires à une image donnée en utilisant un type de descripteur spécifique
     */
    public List<SimilarityResult> findSimilarImages(Long sourceImageId, String descriptorType, int limit) {
        try {
            logger.info("Recherche des images similaires à l'image " + sourceImageId + " avec le descripteur " + descriptorType);
            
            // Vérifier que les paramètres sont valides
            if (sourceImageId == null || descriptorType == null) {
                logger.warning("Paramètres invalides: sourceImageId=" + sourceImageId + ", descriptorType=" + descriptorType);
                return Collections.emptyList();
            }
            
            // Récupérer l'image source
            Optional<Image> sourceImageOpt = imageRepository.findById(sourceImageId);
            if (!sourceImageOpt.isPresent()) {
                logger.warning("Image source non trouvée: id=" + sourceImageId);
                return Collections.emptyList();
            }
            
            Image sourceImage = sourceImageOpt.get();
            logger.info("Image source trouvée: " + sourceImage.getName());
            
            // Récupérer le descripteur source
            List<Descriptor> sourceDescriptors = descriptorRepository.findByImageId(sourceImageId);
            logger.info("Nombre de descripteurs trouvés pour l'image source: " + sourceDescriptors.size());
            
            if (sourceDescriptors.isEmpty()) {
                logger.warning("Aucun descripteur trouvé pour l'image id=" + sourceImageId);
                return Collections.emptyList();
            }
            
            Optional<Descriptor> sourceDescriptorOpt = sourceDescriptors.stream()
                    .filter(d -> descriptorType.equals(d.getType()))
                    .findFirst();
            
            if (!sourceDescriptorOpt.isPresent()) {
                logger.warning("Descripteur " + descriptorType + " non trouvé pour l'image source id=" + sourceImageId);
                return Collections.emptyList();
            }
            
            Descriptor sourceDescriptor = sourceDescriptorOpt.get();
            float[] sourceHistogram = sourceDescriptor.getHistogramData();
            
            if (sourceHistogram == null) {
                logger.warning("L'histogramme source est null");
                return Collections.emptyList();
            }
            
            if (sourceHistogram.length == 0) {
                logger.warning("L'histogramme source est vide");
                return Collections.emptyList();
            }
            
            logger.info("Descripteur source trouvé, longueur de l'histogramme: " + sourceHistogram.length);
            
            // Récupérer tous les descripteurs du même type
            List<Descriptor> allDescriptors = descriptorRepository.findByType(descriptorType);
            logger.info("Nombre total de descripteurs du type " + descriptorType + ": " + allDescriptors.size());
            
            if (allDescriptors.isEmpty()) {
                logger.warning("Aucun descripteur du type " + descriptorType + " trouvé dans la base de données");
                return Collections.emptyList();
            }
            
            // Calculer les distances et créer les résultats de similarité
            List<SimilarityResult> results = new ArrayList<>();
            
            for (Descriptor targetDescriptor : allDescriptors) {
                try {
                    // Ne pas comparer l'image avec elle-même
                    if (targetDescriptor.getImage() == null) {
                        logger.warning("Image associée au descripteur id=" + targetDescriptor.getId() + " est null");
                        continue;
                    }
                    
                    if (targetDescriptor.getImage().getId().equals(sourceImageId)) {
                        continue;
                    }
                    
                    float[] targetHistogram = targetDescriptor.getHistogramData();
                    if (targetHistogram == null) {
                        logger.warning("L'histogramme cible est null pour le descripteur id=" + targetDescriptor.getId());
                        continue;
                    }
                    
                    if (targetHistogram.length == 0) {
                        logger.warning("L'histogramme cible est vide pour le descripteur id=" + targetDescriptor.getId());
                        continue;
                    }
                    
                    // Vérifier que les histogrammes ont la même taille
                    if (sourceHistogram.length != targetHistogram.length) {
                        logger.warning("Tailles d'histogramme incompatibles: source=" + sourceHistogram.length + 
                                   ", cible=" + targetHistogram.length);
                        continue;
                    }
                    
                    float distance = calculateDistance(sourceHistogram, targetHistogram);
                    results.add(new SimilarityResult(
                        targetDescriptor.getImage().getId(), 
                        targetDescriptor.getImage().getName(),
                        distance
                    ));
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Erreur lors du traitement du descripteur id=" + 
                              targetDescriptor.getId(), e);
                }
            }
            
            logger.info("Nombre de résultats de similarité calculés: " + results.size());
            
            if (results.isEmpty()) {
                logger.warning("Aucun résultat de similarité n'a pu être calculé");
                return Collections.emptyList();
            }
            
            // Trier les résultats par distance croissante (plus petite distance = plus similaire)
            results.sort(Comparator.comparing(SimilarityResult::getDistance));
            
            // Limiter le nombre de résultats
            List<SimilarityResult> limitedResults = results.stream()
                    .limit(limit)
                    .collect(Collectors.toList());
            
            logger.info("Retour de " + limitedResults.size() + " résultats");
            return limitedResults;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception non gérée dans findSimilarImages", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Calcule la distance euclidienne entre deux histogrammes.
     * Plus la distance est petite, plus les images sont similaires.
     */
    private float calculateDistance(float[] histogram1, float[] histogram2) {
        try {
            if (histogram1 == null || histogram2 == null) {
                throw new IllegalArgumentException("Les histogrammes ne peuvent pas être null");
            }
            
            if (histogram1.length != histogram2.length) {
                throw new IllegalArgumentException("Les histogrammes doivent avoir la même taille: " + 
                                                  histogram1.length + " vs " + histogram2.length);
            }
            
            float sum = 0;
            for (int i = 0; i < histogram1.length; i++) {
                // Vérifier que les valeurs ne sont pas NaN ou Infinity
                if (Float.isNaN(histogram1[i]) || Float.isNaN(histogram2[i]) || 
                    Float.isInfinite(histogram1[i]) || Float.isInfinite(histogram2[i])) {
                    continue;
                }
                float diff = histogram1[i] - histogram2[i];
                sum += diff * diff;
            }
            
            return (float) Math.sqrt(sum);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Erreur dans calculateDistance", e);
            throw e;
        }
    }
    
    /**
     * Classe interne représentant le résultat d'une comparaison de similarité
     */
    public static class SimilarityResult {
        private Long imageId;
        private String imageName;
        private float distance;
        
        public SimilarityResult(Long imageId, String imageName, float distance) {
            this.imageId = imageId;
            this.imageName = imageName;
            this.distance = distance;
        }
        
        public Long getImageId() {
            return imageId;
        }
        
        public String getImageName() {
            return imageName;
        }
        
        public float getDistance() {
            return distance;
        }
        
        @Override
        public String toString() {
            return "SimilarityResult{" +
                "imageId=" + imageId +
                ", imageName='" + imageName + '\'' +
                ", distance=" + distance +
                '}';
        }
    }
}