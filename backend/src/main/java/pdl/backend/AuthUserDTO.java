package pdl.backend;

import java.time.LocalDate;

/**
 * DTO (Data Transfer Object) pour l'authentification des utilisateurs
 * Cette classe permet d'éviter les problèmes d'accès aux champs BLOB
 * en ne contenant que les champs nécessaires pour l'authentification
 */
public class AuthUserDTO {
    private Long id;
    private String username;
    private String password;
    private String email;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private String phoneNumber;
    private String bio;
    private boolean hasProfilePicture;
    
    // Constructeur par défaut
    public AuthUserDTO() {
    }
    
    // Constructeur pour la requête JPQL
    public AuthUserDTO(Long id, String username, String password, String email, 
                     String firstName, String lastName, LocalDate birthDate, 
                     String phoneNumber, String bio, boolean hasProfilePicture) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.phoneNumber = phoneNumber;
        this.bio = bio;
        this.hasProfilePicture = hasProfilePicture;
    }
    
    // Getters et setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public LocalDate getBirthDate() {
        return birthDate;
    }
    
    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getBio() {
        return bio;
    }
    
    public void setBio(String bio) {
        this.bio = bio;
    }
    
    public boolean isHasProfilePicture() {
        return hasProfilePicture;
    }
    
    public void setHasProfilePicture(boolean hasProfilePicture) {
        this.hasProfilePicture = hasProfilePicture;
    }
}
