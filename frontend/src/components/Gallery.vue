<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { api } from '../http-api';
import type { ImageType } from '../image';
import Image from './Image.vue';

// État local
const imageList = ref<ImageType[]>([]);
const loading = ref(true);
const error = ref<string | null>(null);
const selectedImage = ref<ImageType | null>(null);
const showModal = ref(false);
const activeFolder = ref('all'); // 'all', 'favorites', 'saved'
const sortOrder = ref('name-asc'); // options: name-asc, name-desc, date-new, date-old
const searchQuery = ref('');

// Liste des formats disponibles pour le filtrage
const availableFormats = computed(() => {
  const formats = new Set<string>();
  imageList.value.forEach(img => formats.add(img.format));
  return Array.from(formats);
});

// État du filtre de format
const selectedFormat = ref('all');

// Images filtrées par la recherche et le format
const filteredImages = computed(() => {
  return imageList.value.filter(image => {
    // Filtre par recherche
    const matchesSearch = searchQuery.value === '' || 
      image.name.toLowerCase().includes(searchQuery.value.toLowerCase());
    
    // Filtre par format
    const matchesFormat = selectedFormat.value === 'all' || 
      image.format === selectedFormat.value;
    
    return matchesSearch && matchesFormat;
  });
});

// Images triées selon l'ordre sélectionné
const sortedImages = computed(() => {
  const images = [...filteredImages.value];
  
  switch (sortOrder.value) {
    case 'name-asc':
      return images.sort((a, b) => a.name.localeCompare(b.name));
    case 'name-desc':
      return images.sort((a, b) => b.name.localeCompare(a.name));
    case 'size-asc':
      return images.sort((a, b) => a.size - b.size);
    case 'size-desc':
      return images.sort((a, b) => b.size - a.size);
    default:
      return images;
  }
});

// Images favorites
const favoriteImages = computed(() => {
  return sortedImages.value.filter(image => image.favorite);
});

// Images sauvegardées
const savedImages = computed(() => {
  return sortedImages.value.filter(image => image.isSaved);
});

// Images à afficher en fonction du dossier actif
const displayedImages = computed(() => {
  switch (activeFolder.value) {
    case 'favorites':
      return favoriteImages.value;
    case 'saved':
      return savedImages.value;
    case 'all':
    default:
      return sortedImages.value;
  }
});

// Charger toutes les images
const loadImages = async () => {
  try {
    loading.value = true;
    error.value = null;
    if (activeFolder.value === 'saved') {
      imageList.value = await api.getSavedImages();
    } else {
      imageList.value = await api.getMyGallery();
    }
  } catch (e) {
    error.value = "Erreur lors du chargement des images";
    console.error(e);
  } finally {
    loading.value = false;
  }
};

// Ouvrir le modal d'une image
const openImageModal = (image: ImageType) => {
  selectedImage.value = image;
  showModal.value = true;
};

// Fermer le modal
const closeImageModal = () => {
  showModal.value = false;
  selectedImage.value = null;
};

// Gérer la suppression d'une image
const deleteImage = async (id: number) => {
  if (confirm('Êtes-vous sûr de vouloir supprimer cette image ?')) {
    try {
      await api.deleteImage(id);
      imageList.value = imageList.value.filter(img => img.id !== id);
    } catch (e) {
      console.error(e);
    }
  }
};

// Changer de dossier actif
const changeFolder = async (folder: string) => {
  activeFolder.value = folder;
  await loadImages();
};

// Basculer l'état favori d'une image
const toggleFavorite = async (id: number) => {
  try {
    await api.toggleFavorite(id);
    const image = imageList.value.find(img => img.id === id);
    if (image) {
      image.favorite = !image.favorite;
    }
  } catch (e) {
    console.error(e);
  }
};

// Basculer l'état sauvegardé d'une image
const toggleSave = async (id: number) => {
  try {
    await api.toggleSave(id);
    const image = imageList.value.find(img => img.id === id);
    if (image) {
      image.isSaved = !image.isSaved;
    }
  } catch (e) {
    console.error(e);
  }
};

onMounted(() => {
  loadImages();
});
</script>

<template>
  <div class="gallery-container">
    <h2>Galerie d'images</h2>
    
    <!-- Folder Navigation -->
    <div class="folder-navigation">
      <button 
        @click="changeFolder('all')"
        :class="['folder-btn', { active: activeFolder === 'all' }]">
        <div class="folder-icon">📁</div>
        <span>Toutes les images</span>
        <span class="count">{{ filteredImages.length }}</span>
      </button>
      
      <button 
        @click="changeFolder('favorites')"
        :class="['folder-btn', { active: activeFolder === 'favorites' }]">
        <div class="folder-icon">❤️</div>
        <span>Favoris</span>
        <span class="count">{{ favoriteImages.length }}</span>
      </button>
      
      <button 
        @click="changeFolder('saved')"
        :class="['folder-btn', { active: activeFolder === 'saved' }]">
        <div class="folder-icon">📥</div>
        <span>Sauvegardées</span>
        <span class="count">{{ savedImages.length }}</span>
      </button>
    </div>
    
    <!-- Filters and search bar -->
    <div class="gallery-controls">
      <div class="search-bar">
        <input 
          v-model="searchQuery"
          type="text"
          placeholder="Rechercher par nom..."
          class="search-input"
        />
        <button class="search-button">🔍</button>
      </div>
      
      <div class="filters">
        <select v-model="sortOrder" class="filter-select">
          <option value="name-asc">Nom (A-Z)</option>
          <option value="name-desc">Nom (Z-A)</option>
          <option value="size-asc">Taille (plus petite)</option>
          <option value="size-desc">Taille (plus grande)</option>
        </select>
        
        <select v-model="selectedFormat" class="filter-select">
          <option value="all">Tous les formats</option>
          <option v-for="format in availableFormats" :key="format" :value="format">
            {{ format.toUpperCase() }}
          </option>
        </select>
      </div>
    </div>
    
    <!-- Current folder header -->
    <div class="current-folder-header">
      <h3>
        <span v-if="activeFolder === 'all'">📁 Toutes les images</span>
        <span v-else-if="activeFolder === 'favorites'">❤️ Favoris</span>
        <span v-else-if="activeFolder === 'saved'">📥 Sauvegardées</span>
      </h3>
    </div>
    
    <!-- Loading, Error and Empty states -->
    <div v-if="loading" class="loading">
      <div class="spinner"></div>
      <p>Chargement des images...</p>
    </div>
    
    <div v-else-if="error" class="error">
      {{ error }}
      <button @click="loadImages" class="retry-button">Réessayer</button>
    </div>
    
    <div v-else-if="displayedImages.length === 0" class="empty-gallery">
      <p v-if="activeFolder === 'all'">Aucune image dans la galerie.</p>
      <p v-else-if="activeFolder === 'favorites'">Aucune image favorite.</p>
      <p v-else-if="activeFolder === 'saved'">Aucune image sauvegardée.</p>
      <router-link to="/upload" class="upload-link">Ajouter une image</router-link>
    </div>
    
    <!-- Image Grid -->
    <div v-else class="image-grid">
      <div 
        v-for="image in displayedImages" 
        :key="image.id" 
        class="image-card"
        @click="openImageModal(image)">
        <Image 
          :id="image.id" 
          :name="image.name"
          :size="image.size"
          :format="image.format"
          :preview-mode="true"
          @deleted="deleteImage"
        />
        <button 
          :class="['favorite-button', { favorited: image.favorite }]"
          @click.stop="toggleFavorite(image.id)">
          {{ image.favorite ? '❤️' : '🤍' }}
        </button>
        <button 
          :class="['save-button', { saved: image.isSaved }]"
          @click.stop="toggleSave(image.id)">
          {{ image.isSaved ? '📥' : '📄' }}
        </button>
      </div>
    </div>

    <!-- Modal pour l'image sélectionnée -->
    <div v-if="showModal" class="modal-overlay" @click="closeImageModal">
      <div class="modal-content" @click.stop>
        <button class="modal-close" @click="closeImageModal">&times;</button>
        <Image 
          v-if="selectedImage"
          :id="selectedImage.id"
          :name="selectedImage.name"
          :size="selectedImage.size"
          :format="selectedImage.format"
          :preview-mode="false"
          @deleted="deleteImage"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.gallery-container {
  padding: 30px;
  max-width: 1400px;
  margin: 0 auto;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.05);
}

h2 {
  color: #2c3e50;
  text-align: center;
  margin-bottom: 30px;
  font-size: 32px;
  font-weight: 700;
  letter-spacing: -0.5px;
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
  width: 60px;
  height: 4px;
  background: linear-gradient(to right, #0062cc, #007bff);
  border-radius: 2px;
}

/* Folder Navigation Styles */
.folder-navigation {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
  gap: 15px;
}

.folder-btn {
  display: flex;
  align-items: center;
  padding: 12px 18px;
  background: #f8f9fa;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 2px 5px rgba(0,0,0,0.05);
  color: #495057;
}

.folder-btn.active {
  background: #e7f1ff;
  color: #007bff;
  box-shadow: 0 4px 8px rgba(0,123,255,0.15);
}

.folder-icon {
  font-size: 20px;
  margin-right: 10px;
}

.count {
  background: rgba(0,0,0,0.1);
  border-radius: 12px;
  padding: 3px 8px;
  font-size: 12px;
  margin-left: 10px;
}

.folder-btn.active .count {
  background: rgba(0,123,255,0.2);
}

/* Gallery Controls Styles */
.gallery-controls {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
  gap: 20px;
  flex-wrap: wrap;
}

.search-bar {
  display: flex;
  flex: 1;
  min-width: 250px;
}

.search-input {
  flex: 1;
  padding: 10px 15px;
  border: 1px solid #ced4da;
  border-right: none;
  border-radius: 6px 0 0 6px;
  font-size: 14px;
}

.search-button {
  background: #007bff;
  color: white;
  border: none;
  padding: 0 15px;
  border-radius: 0 6px 6px 0;
  cursor: pointer;
}

.filters {
  display: flex;
  gap: 10px;
}

.filter-select {
  padding: 10px;
  border: 1px solid #ced4da;
  border-radius: 6px;
  background-color: white;
  min-width: 130px;
}

/* Current Folder Header */
.current-folder-header {
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #dee2e6;
}

.current-folder-header h3 {
  color: #2c3e50;
  font-size: 22px;
  font-weight: 600;
}

/* Loading, Error & Empty States */
.loading, .error, .empty-gallery {
  text-align: center;
  padding: 40px;
  background: #f8f9fa;
  border-radius: 8px;
  margin: 20px 0;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
}

.loading p {
  margin-top: 15px;
  color: #666;
  font-weight: 500;
}

.error {
  color: #dc3545;
  background: #fff3f3;
  border-left: 4px solid #dc3545;
}

.retry-button {
  margin-top: 15px;
  padding: 10px 24px;
  background: linear-gradient(to right, #c82333, #dc3545);
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 4px 6px rgba(220, 53, 69, 0.2);
}

.retry-button:hover {
  background: linear-gradient(to right, #bd2130, #c82333);
  transform: translateY(-2px);
  box-shadow: 0 6px 8px rgba(220, 53, 69, 0.3);
}

/* Image Grid Styles */
.image-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 25px;
  padding: 20px 0;
}

.image-card {
  position: relative;
  aspect-ratio: 1;
  background: white;
  border-radius: 10px;
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  overflow: visible;
  border: 1px solid #f0f0f0;
}

.image-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 20px rgba(0, 0, 0, 0.15);
  border-color: rgba(0, 123, 255, 0.3);
}

.empty-gallery {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  background: linear-gradient(to bottom right, #f8f9fa, #e9ecef);
}

.empty-gallery p {
  font-size: 18px;
  color: #6c757d;
  margin-bottom: 20px;
}

.upload-link {
  display: inline-block;
  padding: 12px 28px;
  background: linear-gradient(to right, #0062cc, #007bff);
  color: white;
  text-decoration: none;
  border-radius: 6px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 4px 8px rgba(0, 123, 255, 0.2);
}

.upload-link:hover {
  background: linear-gradient(to right, #0056b3, #0062cc);
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(0, 123, 255, 0.3);
}

.spinner {
  width: 50px;
  height: 50px;
  border: 4px solid rgba(0, 123, 255, 0.1);
  border-top: 4px solid #007bff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  backdrop-filter: blur(5px);
  transition: all 0.3s ease;
}

.modal-content {
  background: white;
  padding: 25px;
  border-radius: 12px;
  max-width: 90%;
  max-height: 90vh;
  overflow-y: auto;
  position: relative;
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.2);
  animation: modalFadeIn 0.3s ease forwards;
}

.modal-close {
  position: absolute;
  top: 15px;
  right: 15px;
  background: rgba(255, 255, 255, 0.8);
  border: none;
  font-size: 28px;
  height: 40px;
  width: 40px;
  line-height: 40px;
  text-align: center;
  border-radius: 50%;
  cursor: pointer;
  color: #333;
  z-index: 1;
  transition: all 0.2s ease;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.modal-close:hover {
  background: white;
  color: #dc3545;
  transform: rotate(90deg);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

@keyframes modalFadeIn {
  from { opacity: 0; transform: scale(0.9); }
  to { opacity: 1; transform: scale(1); }
}

/* Favorite and Save Button Styles */
.favorite-button, .save-button {
  position: absolute;
  background: rgba(255, 255, 255, 0.9);
  border: none;
  border-radius: 50%;
  width: 36px;
  height: 36px;
  font-size: 18px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  z-index: 10;
}

.favorite-button {
  top: 10px;
  right: 10px;
}

.save-button {
  top: 10px;
  right: 55px;
}

.favorite-button.favorited {
  background: rgba(255, 215, 0, 0.9);
  color: #c00;
}

.save-button.saved {
  background: rgba(40, 167, 69, 0.9);
  color: white;
}

.favorite-button:hover, .save-button:hover {
  transform: scale(1.2);
}

/* Responsive Styles */
@media (max-width: 768px) {
  .gallery-container {
    padding: 20px 15px;
  }
  
  .folder-navigation {
    flex-direction: column;
  }
  
  .gallery-controls {
    flex-direction: column;
  }
  
  .search-bar {
    width: 100%;
  }
  
  .filters {
    width: 100%;
    justify-content: space-between;
  }
  
  .image-grid {
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 15px;
  }
  
  h2 {
    font-size: 28px;
  }
  
  .modal-content {
    padding: 15px;
    width: 95%;
  }
}
</style>