package pdl.backend;

import java.util.Optional;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.util.logging.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;
    
    // Map simple pour stocker les sessions actives (en production, utilisez Redis ou une solution plus robuste)
    private static final Map<String, Long> activeSessions = new HashMap<>();
    
    // Logger pour débogage
    private static final Logger logger = Logger.getLogger(AuthController.class.getName());

    // Méthode statique pour vérifier la validité du token
    public static Long getUserIdFromToken(String token) {
        return activeSessions.get(token);
    }

    // Méthode statique pour vérifier si un token est valide
    public static boolean isValidToken(String token) {
        return token != null && activeSessions.containsKey(token);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest request) {
        // Vérifier si l'utilisateur existe déjà
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("Ce nom d'utilisateur est déjà pris");
        }
        
        // Vérifier si l'email existe déjà
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Cet email est déjà utilisé");
        }

        // Créer un nouvel utilisateur avec tous les champs
        // Note: Dans une application réelle, vous devriez hacher le mot de passe
        User user = new User(request.getUsername(), request.getPassword());
        // Ajouter les informations supplémentaires
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setBirthDate(request.getBirthDate());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setBio(request.getBio());
        
        User savedUser = userRepository.save(user);

        // Retourner un message de succès
        return ResponseEntity.ok("Votre compte a été créé avec succès. Vous pouvez maintenant vous connecter.");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            String username = request.getUsername();
            String password = request.getPassword();
            
            System.out.println("Tentative de connexion pour l'utilisateur: " + username);
            
            // Utiliser la nouvelle méthode optimisée qui évite complètement les champs BLOB
            Optional<AuthUserDTO> userOpt = userRepository.findUserForAuthentication(username);
            
            if (userOpt.isEmpty() || !userOpt.get().getPassword().equals(password)) {
                return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Nom d'utilisateur ou mot de passe incorrect");
            }

            AuthUserDTO user = userOpt.get();
            
            // Générer un token pour la session
            String token = UUID.randomUUID().toString();
            activeSessions.put(token, user.getId());
            
            // Construire la réponse avec les informations utilisateur
            AuthResponse response = new AuthResponse(
                user.getId(),
                user.getUsername(), 
                token
            );
            
            // Ajouter les informations de profil
            response.setEmail(user.getEmail());
            response.setFirstName(user.getFirstName());
            response.setLastName(user.getLastName());
            response.setBirthDate(user.getBirthDate());
            response.setPhoneNumber(user.getPhoneNumber());
            response.setBio(user.getBio());
            response.setHasProfilePicture(user.isHasProfilePicture());
            
            System.out.println("Connexion réussie pour l'utilisateur: " + username);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la connexion: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Une erreur est survenue lors de la connexion: " + e.getMessage());
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        
        if (token != null && activeSessions.containsKey(token)) {
            activeSessions.remove(token);
            return ResponseEntity.ok().body("Déconnexion réussie");
        }
        
        return ResponseEntity.badRequest().body("Session invalide");
    }
    
    // Recherche d'utilisateurs
    @GetMapping("/users/search")
    public ResponseEntity<?> searchUsers(@RequestParam String query) {
        System.out.println("AuthController: Recherche d'utilisateurs avec query: '" + query + "'");
        
        try {
            if (query == null || query.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Le terme de recherche ne peut pas être vide");
            }
            
            // Vérifier si la requête est trop courte (moins de 2 caractères)
            String trimmedQuery = query.trim();
            
            // Utiliser la méthode optimisée qui évite les problèmes de LOB
            System.out.println("AuthController: Exécution de la recherche optimisée avec: '" + trimmedQuery + "'");
            List<User> users = userRepository.findUsersBySearchTerm(trimmedQuery);
            
            System.out.println("AuthController: Nombre d'utilisateurs trouvés: " + users.size());
            
            return buildUserListResponse(users);
        } catch (Exception e) {
            System.err.println("AuthController: Erreur pendant la recherche d'utilisateurs: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Une erreur est survenue pendant la recherche: " + e.getMessage());
        }
    }

    // Méthode utilitaire pour construire la réponse avec la liste d'utilisateurs
    private ResponseEntity<List<Map<String, Object>>> buildUserListResponse(List<User> users) {
        List<Map<String, Object>> results = new java.util.ArrayList<>();
        
        for (User user : users) {
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("firstName", user.getFirstName());
            userInfo.put("lastName", user.getLastName());
            userInfo.put("hasProfilePicture", user.getProfilePicture() != null && user.getProfilePicture().length > 0);
            results.add(userInfo);
        }
        
        return ResponseEntity.ok(results);
    }
    
    @PostMapping("/update-profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, Object> request) {
        // Vérifier l'authentification
        String token = (String) request.get("token");
        if (token == null || !activeSessions.containsKey(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Non autorisé");
        }
        
        // Récupérer l'utilisateur
        Long userId = activeSessions.get(token);
        Optional<User> userOpt = userRepository.findById(userId);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Utilisateur non trouvé");
        }
        
        User user = userOpt.get();
        
        // Mettre à jour les champs fournis
        if (request.containsKey("firstName")) {
            user.setFirstName((String) request.get("firstName"));
        }
        if (request.containsKey("lastName")) {
            user.setLastName((String) request.get("lastName"));
        }
        if (request.containsKey("email")) {
            user.setEmail((String) request.get("email"));
        }
        if (request.containsKey("birthDate")) {
            user.setBirthDate(LocalDate.parse((String) request.get("birthDate")));
        }
        if (request.containsKey("phoneNumber")) {
            user.setPhoneNumber((String) request.get("phoneNumber"));
        }
        if (request.containsKey("bio")) {
            user.setBio((String) request.get("bio"));
        }
        
        userRepository.save(user);
        
        // Retourner les informations de l'utilisateur mis à jour
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("username", user.getUsername());
        response.put("email", user.getEmail());
        response.put("firstName", user.getFirstName());
        response.put("lastName", user.getLastName());
        response.put("birthDate", user.getBirthDate());
        response.put("phoneNumber", user.getPhoneNumber());
        response.put("bio", user.getBio());
        response.put("hasProfilePicture", user.getProfilePicture() != null && user.getProfilePicture().length > 0);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/upload-profile-picture")
    public ResponseEntity<?> uploadProfilePicture(
            @RequestParam("file") MultipartFile file, 
            @RequestParam("token") String token,
            @RequestParam(value = "userId", required = false) Long userIdParam) {
        
        System.out.println("Upload de photo de profil - Token reçu: " + token);
        System.out.println("Sessions actives: " + activeSessions.keySet());
        System.out.println("UserId param: " + userIdParam);
        
        Long userId = null;
        
        // Essayer d'abord d'authentifier avec le token
        if (token != null && activeSessions.containsKey(token)) {
            userId = activeSessions.get(token);
            System.out.println("Authentification réussie avec token, userId: " + userId);
        } 
        // Si le token ne fonctionne pas, utiliser directement l'userId fourni (pour développement/débogage)
        else if (userIdParam != null) {
            userId = userIdParam;
            System.out.println("Authentification alternative avec userIdParam: " + userId);
        }
        // Aucune méthode d'authentification n'a fonctionné
        else {
            System.out.println("Impossible d'authentifier l'utilisateur");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Non autorisé - Session expirée ou invalide");
        }
        
        // Vérifier que le fichier n'est pas vide
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Fichier vide");
        }
        
        // Vérifier le type de fichier (doit être une image)
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest().body("Le fichier doit être une image");
        }
        
        // Récupérer l'utilisateur avec l'ID déjà obtenu, sans redéclarer userId
        Optional<User> userOpt = userRepository.findById(userId);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Utilisateur non trouvé");
        }
        
        User user = userOpt.get();
        
        try {
            // Enregistrer l'image
            user.setProfilePicture(file.getBytes());
            user.setProfilePictureFormat(contentType);
            userRepository.save(user);
            
            return ResponseEntity.ok("Photo de profil mise à jour avec succès");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de l'enregistrement de l'image: " + e.getMessage());
        }
    }
    
    // Endpoint pour récupérer l'image de profil d'un utilisateur
    @GetMapping("/api/users/{userId}/profile-picture")
    public ResponseEntity<?> getProfilePicture(@PathVariable Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        User user = userOpt.get();
        
        // Vérifier si l'utilisateur a une photo de profil
        if (user.getProfilePicture() == null || user.getProfilePictureFormat() == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Retourner l'image avec le bon Content-Type
        return ResponseEntity
                .ok()
                .contentType(MediaType.parseMediaType(user.getProfilePictureFormat()))
                .body(user.getProfilePicture());
    }
}
