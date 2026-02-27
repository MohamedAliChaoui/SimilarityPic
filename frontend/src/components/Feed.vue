<script setup lang="ts">
import { ref, onMounted } from 'vue';
import type { ImageType } from '../image';
import { api } from '../http-api';

const feed = ref<ImageType[]>([]);
const loading = ref(true);
const error = ref<string | null>(null);
const imageUrls = ref<{ [key: number]: string }>({});

const loadImageData = async (imageId: number) => {
  try {
    console.log('Début du chargement de l\'image:', imageId);
    const response = await api.getImage(imageId);
    console.log('Réponse reçue pour l\'image:', imageId, 'Type:', response.type);
    const url = URL.createObjectURL(response);
    console.log('URL créée pour l\'image:', imageId, ':', url);
    imageUrls.value[imageId] = url;
  } catch (e) {
    console.error('Erreur lors du chargement de l\'image:', imageId, e);
    error.value = `Erreur lors du chargement de l'image ${imageId}`;
  }
};

const handleLike = async (image: ImageType) => {
  try {
    await api.toggleFavorite(image.id);
    image.likes = (image.likes || 0) + (image.isLiked ? -1 : 1);
    image.isLiked = !image.isLiked;
  } catch (e) {
    console.error('Erreur lors du like:', e);
  }
};

const handleSave = async (image: ImageType) => {
  try {
    await api.toggleSave(image.id);
    image.saves = (image.saves || 0) + (image.isSaved ? -1 : 1);
    image.isSaved = !image.isSaved;
  } catch (e) {
    console.error('Erreur lors de la sauvegarde:', e);
  }
};

onMounted(async () => {
  try {
    console.log('Chargement du feed...');
    feed.value = await api.getFeed();
    console.log('Feed chargé avec les données:', feed.value);
    console.log('Détails des utilisateurs:');
    feed.value.forEach(image => {
      console.log(`Image ${image.id} - Username: ${image.username}, Name: ${image.name}`);
    });
    // Charger les images une par une
    await Promise.all(feed.value.map(image => loadImageData(image.id)));
    console.log('Toutes les images sont chargées');
  } catch (e) {
    error.value = 'Erreur lors du chargement du fil d\'actualité';
    console.error('Erreur feed:', e);
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <div class="feed-container">
    <h1>📰 Fil d'actualité</h1>
    
    <div v-if="loading" class="loading">
      Chargement du fil d'actualité...
    </div>
    
    <div v-else-if="error" class="error">
      {{ error }}
    </div>
    
    <div v-else-if="feed.length === 0" class="empty-feed">
      Aucune publication pour le moment.
    </div>
    
    <div v-else class="feed-grid">
      <div v-for="image in feed" :key="image.id" class="feed-item">
        <div class="image-container">
          <img 
            v-if="imageUrls[image.id]"
            :src="imageUrls[image.id]"
            :alt="image.name"
          />
          <div v-else class="loading-image">
            Chargement de l'image...
          </div>
        </div>
        <div class="image-info">
          <div class="user-info">
            <span class="username">👤 {{ image.username || 'Anonyme' }}</span>
          </div>
          <span class="image-name">{{ image.name }}</span>
          <div class="actions">
            <button 
              @click="handleLike(image)" 
              :class="{ active: image.isLiked }"
              class="action-button like"
            >
              ❤️ {{ image.likes || 0 }}
            </button>
            <button 
              @click="handleSave(image)" 
              :class="{ active: image.isSaved }"
              class="action-button save"
            >
              📥 {{ image.saves || 0 }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.feed-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

h1 {
  text-align: center;
  margin-bottom: 30px;
  color: #2c3e50;
}

.loading, .error, .empty-feed {
  text-align: center;
  padding: 20px;
  font-size: 1.2em;
  color: #666;
}

.feed-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  padding: 20px 0;
}

.feed-item {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.feed-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
}

.image-container {
  width: 100%;
  height: 300px;
  overflow: hidden;
}

.image-container img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.feed-item:hover .image-container img {
  transform: scale(1.05);
}

.image-info {
  padding: 15px;
}

.user-info {
  margin-bottom: 8px;
}

.username {
  font-weight: 600;
  color: #2c3e50;
  font-size: 0.9em;
}

.image-name {
  font-weight: 500;
  color: #2c3e50;
  display: block;
  margin-bottom: 12px;
  font-size: 0.9em;
}

.actions {
  display: flex;
  gap: 10px;
}

.action-button {
  flex: 1;
  padding: 8px 12px;
  border: none;
  border-radius: 6px;
  background-color: #f8f9fa;
  color: #6c757d;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
}

.action-button:hover {
  background-color: #e9ecef;
}

.action-button.active {
  background-color: #007bff;
  color: white;
}

.action-button.like.active {
  background-color: #dc3545;
}

.action-button.save.active {
  background-color: #28a745;
}

.loading-image {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 300px;
  background-color: #f8f9fa;
  color: #6c757d;
  font-size: 0.9em;
}

@media (max-width: 768px) {
  .feed-grid {
    grid-template-columns: 1fr;
  }
}
</style> 