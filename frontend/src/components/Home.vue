<script setup lang="ts">
import { ref, reactive } from 'vue';
import { api } from '../http-api';
import type { ImageType } from '../image';
import type { UserSearchResult, ImageSearchParams } from '../http-api';
import { useRouter } from 'vue-router';

const router = useRouter();

// Paramètres pour les deux types de recherche
const searchType = ref<'users' | 'images'>('images'); // Type de recherche actif

// Recherche d'utilisateurs
const userQuery = ref('');
const userResults = ref<UserSearchResult[]>([]);
const searchingUsers = ref(false);
const userError = ref<string | null>(null);

// Recherche d'images
const imageParams = reactive<ImageSearchParams>({
  name: '',
  minSize: undefined,
  maxSize: undefined,
  format: ''
});
const imageResults = ref<ImageType[]>([]);
const searchingImages = ref(false);
const imageError = ref<string | null>(null);
const imageUrls = ref<{[key: number]: string}>({});

// État pour la modal d'image
const showImageModal = ref(false);
const selectedImage = ref<any>(null);

// Pru00e9charger les images pour amu00e9liorer la vitesse d'affichage
const preloadImages = (images: ImageType[]) => {
  images.forEach(async (image) => {
    try {
      if (!imageUrls.value[image.id]) {
        const blob = await api.getImage(image.id);
        imageUrls.value[image.id] = URL.createObjectURL(blob);
      }
    } catch (error) {
      console.error(`Erreur de pru00e9chargement pour l'image ${image.id}:`, error);
    }
  });
};

// Fonction pour rechercher des utilisateurs
async function searchUsers() {
  const trimmedQuery = userQuery.value.trim();
  
  if (!trimmedQuery) {
    userError.value = "Veuillez entrer un terme de recherche";
    return;
  }
  
  try {
    searchingUsers.value = true;
    userError.value = null;
    console.log("Recherche d'utilisateurs avec le terme:", trimmedQuery);
    userResults.value = await api.searchUsers(trimmedQuery);
    console.log("Résultats obtenus:", userResults.value);
    
    // Afficher un message si aucun résultat n'est trouvé
    if (userResults.value.length === 0) {
      userError.value = "Aucun utilisateur trouvé avec ce terme de recherche";
    }
  } catch (e: any) {
    console.error("Erreur lors de la recherche d'utilisateurs:", e);
    // Afficher le message d'erreur du serveur si disponible
    userError.value = e.response?.data || "Erreur lors de la recherche d'utilisateurs";
  } finally {
    searchingUsers.value = false;
  }
}

// Fonction pour rechercher des images
async function searchImages() {
  try {
    searchingImages.value = true;
    imageError.value = null;
    
    // Nettoyer les paramètres vides pour la recherche
    const params: ImageSearchParams = {};
    if (imageParams.name) params.name = imageParams.name;
    if (imageParams.format) params.format = imageParams.format;
    
    // Convertir KB en octets pour l'API
    if (imageParams.minSize) params.minSize = imageParams.minSize * 1024;
    if (imageParams.maxSize) params.maxSize = imageParams.maxSize * 1024;
    
    imageResults.value = await api.searchImages(params);
    // Pru00e9charger les images du ru00e9sultat
    preloadImages(imageResults.value);
  } catch (e) {
    console.error(e);
    imageError.value = "Erreur lors de la recherche d'images";
  } finally {
    searchingImages.value = false;
  }
}

// Fonction pour voir le profil d'un utilisateur
function viewUserProfile(userId: number) {
  router.push(`/profile/${userId}`);
}

// Fonction pour voir les détails d'une image
const viewImageDetails = (image: any) => {
  // Afficher l'image dans la modal
  selectedImage.value = image;
  showImageModal.value = true;
};

const closeImageModal = () => {
  showImageModal.value = false;
  selectedImage.value = null;
};

// Fonction pour réinitialiser les formulaires
function resetForm() {
  if (searchType.value === 'users') {
    userQuery.value = '';
    userResults.value = [];
  } else {
    imageParams.name = '';
    imageParams.format = '';
    imageParams.minSize = undefined;
    imageParams.maxSize = undefined;
    imageResults.value = [];
  }
}
</script>

<template>
  <div class="search-container">
    <h2>Moteur de recherche</h2>
    
    <!-- Sélection du type de recherche -->
    <div class="search-type-selector">
      <button 
        @click="searchType = 'users'" 
        :class="['type-button', { active: searchType === 'users' }]">
        <span class="icon">👤</span> Rechercher des utilisateurs
      </button>
      <button 
        @click="searchType = 'images'" 
        :class="['type-button', { active: searchType === 'images' }]">
        <span class="icon">🖼️</span> Rechercher des images
      </button>
    </div>
    
    <!-- Formulaire de recherche d'utilisateurs -->
    <div v-if="searchType === 'users'" class="search-form users-search">
      <h3>Moteur de recherche d'utilisateurs</h3>
      <div class="input-group">
        <input 
          v-model="userQuery" 
          type="text" 
          placeholder="Nom d'utilisateur, prénom ou nom"  
          class="search-input"
        />
      </div>
      
      <div class="buttons-group">
        <button 
          @click="searchUsers" 
          class="search-button" 
          :disabled="searchingUsers || !userQuery.trim()"
        >
          <span v-if="searchingUsers">Recherche...</span>
          <span v-else>Rechercher</span>
        </button>
        <button @click="resetForm" class="reset-button">Réinitialiser</button>
      </div>
      
      <!-- Message d'erreur -->
      <div v-if="userError" class="error">
        {{ userError }}
      </div>
      
      <!-- Résultats de recherche -->
      <div v-if="userResults.length > 0" class="search-results">
        <div v-for="user in userResults" :key="user.id" class="user-result">
          <div class="user-info">
            <h4>{{ user.username }}</h4>
            <div v-if="user.firstName || user.lastName" class="user-fullname">{{ user.firstName }} {{ user.lastName }}</div>
          </div>
          <button @click="viewUserProfile(user.id)" class="view-button">
            Voir profil
          </button>
        </div>
      </div>
    </div>
    
    <!-- Formulaire de recherche d'images -->
    <div v-else class="search-form images-search">
      <h3>Moteur de recherche d'images</h3>
      <div class="input-group">
        <input 
          v-model="imageParams.name" 
          type="text" 
          placeholder="Nom de l'image" 
          class="search-input"
        />
      </div>
      
      <div class="input-group">
        <select v-model="imageParams.format" class="search-input format-select">
          <option value="">Tout format</option>
          <option value="jpeg">JPEG</option>
          <option value="png">PNG</option>
          <option value="bmp">BMP</option>
        </select>
      </div>
      
      <div class="input-group size-inputs">
        <input 
          v-model="imageParams.minSize" 
          type="number" 
          placeholder="Taille min. (KB)" 
          class="search-input size-input"
        />
        <input 
          v-model="imageParams.maxSize" 
          type="number" 
          placeholder="Taille max. (KB)" 
          class="search-input size-input"
        />
      </div>
      
      <div class="buttons-group">
        <button @click="searchImages" class="search-button" :disabled="searchingImages">
          <span v-if="searchingImages">Recherche...</span>
          <span v-else>Rechercher</span>
        </button>
        <button @click="resetForm" class="reset-button">Réinitialiser</button>
      </div>
      
      <div v-if="imageError" class="error">
        {{ imageError }}
      </div>
      
      <!-- Résultats de recherche images -->
      <div v-if="imageResults.length > 0" class="search-results image-grid">
        <div v-for="image in imageResults" :key="image.id" class="image-result">
          <div class="image-info">
            <h4>{{ image.name }}</h4>
            <div class="image-details">
              Format: {{ image.format }} | Taille: {{ (image.size / 1024).toFixed(1) }} KB
            </div>
          </div>
          <button @click="viewImageDetails(image)" class="view-button">
            Voir
          </button>
        </div>
      </div>
      <div v-else-if="(imageParams.name || imageParams.format || imageParams.minSize || imageParams.maxSize) && !searchingImages && !imageError" class="no-results">
        Aucune image trouvée
      </div>
    </div>
    
    <!-- Modal pour afficher l'image -->
    <div v-if="showImageModal" class="image-modal-overlay" @click="closeImageModal">
      <div class="image-modal-content" @click.stop>
        <button class="close-modal-btn" @click="closeImageModal">&times;</button>
        <div v-if="selectedImage" class="modal-image-container">
          <img :src="imageUrls[selectedImage.id] || `/api/images/${selectedImage.id}`" :alt="selectedImage.name" class="modal-image" />
          <div class="modal-image-info">
            <h3>{{ selectedImage.name }}</h3>
            <p>Format: {{ selectedImage.format }} | Taille: {{ (selectedImage.size / 1024).toFixed(1) }} KB</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.search-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  background: #f0f4f8;
  border-radius: 8px;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.05);
}

h2 {
  color: #333;
  text-align: center;
  margin-bottom: 30px;
  font-size: 28px;
  font-weight: 600;
  position: relative;
  display: inline-block;
  left: 50%;
  transform: translateX(-50%);
}

h2:after {
  content: '';
  position: absolute;
  bottom: -8px;
  left: 50%;
  transform: translateX(-50%);
  width: 50px;
  height: 3px;
  background: linear-gradient(to right, #0062cc, #007bff);
  border-radius: 2px;
}

h3 {
  color: #555;
  margin-bottom: 20px;
  font-size: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ddd;
}

/* Sélecteur de type de recherche */
.search-type-selector {
  display: flex;
  justify-content: center;
  margin-bottom: 30px;
  gap: 15px;
}

.type-button {
  padding: 12px 20px;
  font-size: 16px;
  background: #e9ecef;
  border: 1px solid #ddd;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.type-button.active {
  background: #007bff;
  color: white;
  border-color: #0062cc;
}

.type-button:hover {
  background: #d8e0e8;
}

.type-button.active:hover {
  background: #0069d9;
}

/* Formulaires de recherche */
.search-form {
  margin-top: 20px;
}

.input-group {
  margin-bottom: 15px;
  display: flex;
  gap: 10px;
}

.search-input {
  padding: 12px 15px;
  font-size: 16px;
  border: 1px solid #ddd;
  border-radius: 6px;
  flex: 1;
  min-width: 0;
  transition: all 0.3s ease;
}

.search-input:focus {
  border-color: #007bff;
  box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.25);
  outline: none;
}

.size-inputs {
  display: flex;
  gap: 10px;
}

.size-input {
  flex: 1;
}

.search-button {
  padding: 12px 20px;
  background: #28a745;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.2s;
  white-space: nowrap;
}

.search-button:hover {
  background: #218838;
}

.search-button:disabled {
  background: #6c757d;
  cursor: not-allowed;
}

.reset-button {
  padding: 12px 20px;
  background: #6c757d;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.reset-button:hover {
  background: #5a6268;
}

.buttons-group {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

/* Résultats de recherche */
.search-results {
  margin-top: 30px;
  border-top: 1px solid #ddd;
  padding-top: 20px;
}

.user-result, .image-result {
  background: white;
  border-radius: 6px;
  padding: 15px;
  margin-bottom: 15px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-result h4, .image-result h4 {
  margin: 0 0 5px 0;
  color: #343a40;
}

.user-fullname, .user-email, .image-details {
  color: #6c757d;
  font-size: 14px;
  margin-bottom: 3px;
}

.view-button {
  padding: 8px 15px;
  background: #17a2b8;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.view-button:hover {
  background: #138496;
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.error {
  color: #dc3545;
  margin: 15px 0;
  padding: 10px;
  background: #f8d7da;
  border-radius: 4px;
}

.no-results {
  text-align: center;
  color: #6c757d;
  margin: 30px 0;
  font-style: italic;
}

.icon {
  font-size: 18px;
}

@media (max-width: 768px) {
  .search-type-selector {
    flex-direction: column;
  }
  
  .input-group {
    flex-direction: column;
  }
  
  .image-grid {
    grid-template-columns: 1fr;
  }
}

.loading, .error, .no-image {
  text-align: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  margin-top: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

/* Styles pour la modal d'image */
.image-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  backdrop-filter: blur(3px);
}

.image-modal-content {
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 5px 25px rgba(0, 0, 0, 0.25);
  max-width: 90%;
  max-height: 90vh;
  overflow: auto;
  position: relative;
  animation: modalFadeIn 0.3s ease-out;
}

@keyframes modalFadeIn {
  from { opacity: 0; transform: scale(0.95); }
  to { opacity: 1; transform: scale(1); }
}

.close-modal-btn {
  position: absolute;
  top: 10px;
  right: 10px;
  font-size: 24px;
  background: transparent;
  border: none;
  cursor: pointer;
  color: #333;
  z-index: 10;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.close-modal-btn:hover {
  background-color: rgba(0, 0, 0, 0.1);
  transform: scale(1.1);
}

.modal-image-container {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.modal-image {
  max-width: 100%;
  max-height: 60vh;
  border-radius: 4px;
  margin-bottom: 15px;
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.1);
}

.modal-image-info {
  text-align: center;
  width: 100%;
}

.modal-image-info h3 {
  margin: 0 0 10px 0;
  color: #333;
}

.modal-image-info p {
  margin: 0 0 15px 0;
  color: #666;
}

.details-link {
  display: inline-block;
  color: white;
  background-color: #007bff;
  padding: 8px 16px;
  border-radius: 4px;
  text-decoration: none;
  transition: all 0.2s ease;
  margin-top: 10px;
}

.details-link:hover {
  background-color: #0056b3;
  transform: translateY(-2px);
  box-shadow: 0 3px 8px rgba(0, 0, 0, 0.2);
}
</style>
