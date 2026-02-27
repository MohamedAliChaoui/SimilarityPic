package pdl.backend;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "images")
public class Image {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;

  @Column
  private boolean favorite = false;
  @Column
  private boolean published = false;
  @Column
  private boolean saved = false;

  @Column(unique = true) // Empêche les noms d'image en double
  private String name;

  @Column(nullable = false)
  private String path;

  @Basic(fetch = FetchType.LAZY)
  @Column(columnDefinition = "bytea")
  private byte[] data;

  @Column
  private Long size; // Taille de l'image en octets

  @Column(length = 10)
  private String format; // Format de l'image (jpeg, png, etc.)
  @OneToMany(mappedBy = "image", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Descriptor> descriptors = new ArrayList<>();

  @ManyToMany
  @JoinTable(
    name = "saved_images",
    joinColumns = @JoinColumn(name = "image_id"),
    inverseJoinColumns = @JoinColumn(name = "user_id")
  )
  private List<User> savedByUsers = new ArrayList<>();

  // Constructeur par défaut requis par JPA
  public Image() {
  }

  public Image(final String name, final String path, final byte[] data, final Long size, final String format) {
    this.name = name;
    this.path = path;
    this.data = data;
    this.size = size;
    this.format = format;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public String getPath() {
    return path;
  }

  public void setPath(final String path) {
    this.path = path;
  }

  public byte[] getData() {
    return data;
  }

  public Long getSize() {
    return size;
  }

  public void setSize(final Long size) {
    this.size = size;
  }

  public String getFormat() {
    return format;
  }

  public void setFormat(final String format) {
    this.format = format;
  }

  public List<Descriptor> getDescriptors() {
    return descriptors;
  }

  public void addDescriptor(Descriptor descriptor) {
    descriptors.add(descriptor);
    descriptor.setImage(this);
  }

  public void removeDescriptor(Descriptor descriptor) {
    descriptors.remove(descriptor);
    descriptor.setImage(null);
  }

  public boolean isFavorite() {
    return favorite;
  }

  public void setFavorite(boolean favorite) {
    this.favorite = favorite;
  }

  /**
   * Récupère le descripteur HS de l'image
   * 
   * @return le descripteur HS ou null s'il n'existe pas
   */
  public Descriptor getDescriptor() {
    return descriptors.stream()
        .filter(d -> d.getType().equals("HS_HISTOGRAM"))
        .findFirst()
        .orElse(null);
  }

  public boolean isPublished() {
    return published;
  }

  public void setPublished(boolean published) {
    this.published = published;
  }

  public boolean isSaved() {
    return saved;
  }

  public void setSaved(boolean saved) {
    this.saved = saved;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public List<User> getSavedByUsers() {
    return savedByUsers;
  }

  public void addSavedByUser(User user) {
    if (!savedByUsers.contains(user)) {
      savedByUsers.add(user);
    }
  }

  public void removeSavedByUser(User user) {
    savedByUsers.remove(user);
  }

  public boolean isSavedByUser(User user) {
    return savedByUsers.contains(user);
  }
}