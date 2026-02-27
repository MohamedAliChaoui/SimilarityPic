<script setup lang="ts">
import { ref, onMounted } from 'vue';

import { api } from '../http-api';
import type { ImageType } from '../image';
import SimilarImages from './SimilarImages.vue';
// Add this to your <script setup> section in Image.vue, along with your other methods
import { useRouter } from 'vue-router';

const router = useRouter();

const editImage = () => {
  router.push(`/edit/${props.id}`);  // Changez "/editor/" par "/edit/"
};
const props = defineProps<{
  id: number;
  name: string;
  size: number;
  format: string;
  previewMode?: boolean;
}>();

const emit = defineEmits<{
  (e: 'deleted', id: number): void;
}>();


const image = ref<ImageType | null>(null);
const loading = ref(true);
const error = ref<string | null>(null);
const imageUrl = ref('');
const showActions = ref(false);
const showSimilarImages = ref(false); // Variable to control the display of similar images

const loadImage = async () => {
  try {
    const response = await api.getImage(props.id);
    imageUrl.value = URL.createObjectURL(response);
    const images = await api.getImageList();
    image.value = images.find(img => img.id === props.id) || null;
  } catch (e) {
    error.value = "Erreur lors du chargement de l'image";
    console.error(e);
  } finally {
    loading.value = false;
  }
};

const deleteImage = async () => {
  if (confirm('Êtes-vous sûr de vouloir supprimer cette image ?')) {
    try {
      await api.deleteImage(props.id);
      alert('Image supprimée avec succès'); // Afficher un message de confirmation
      emit('deleted', props.id);
    } catch (e) {
      console.error(e);
    }
  }
};

const downloadImage = async () => {
  try {
    const response = await api.getImage(props.id);
    const url = window.URL.createObjectURL(response);
    const a = document.createElement('a');
    a.href = url;
    a.download = props.name;
    document.body.appendChild(a);
    a.click();
    window.URL.revokeObjectURL(url);
    document.body.removeChild(a);
  } catch (e) {
    console.error(e);
  }
};

const togglePublish = async () => {
  try {
    await api.togglePublish(props.id);
    if (image.value) {
      image.value.isPublished = !image.value.isPublished;
      alert(image.value.isPublished ? 'Image publiée avec succès' : 'Image retirée du fil d\'actualité');
    }
  } catch (e) {
    console.error('Erreur lors de la publication:', e);
  }
};

const toggleSimilarImages = () => {
  showSimilarImages.value = !showSimilarImages.value;
};

const formatSize = (size: number): string => {
  const units = ['B', 'KB', 'MB', 'GB'];
  let i = 0;
  let formattedSize = size;
  while (formattedSize >= 1024 && i < units.length - 1) {
    formattedSize /= 1024;
    i++;
  }
  return `${formattedSize.toFixed(1)} ${units[i]}`;
};

onMounted(() => {
  loadImage();
});
</script>

<template>
  <div class="image-container" :class="{ 'preview-mode': previewMode }">
    <div v-if="loading" class="loading">
      <div class="spinner"></div>
      <p>Chargement de l'image...</p>
    </div>
    <div v-else-if="error" class="error">
      {{ error }}
    </div>
    <div v-else class="image-view">
      <img 
        :src="imageUrl" 
        :alt="name" 
        class="main-image" 
        @mouseenter="showActions = true"
        @mouseleave="showActions = false"
      />
      
      <div v-if="!previewMode" class="image-details">
        <h3>{{ name }}</h3>
        <p>Format : <span class="detail-value">{{ format }}</span></p>
        <p>Taille : <span class="detail-value">{{ formatSize(size) }}</span></p>
      </div>

      <div 
  v-if="!previewMode || showActions" 
  class="image-actions"
  :class="{ 'preview-actions': previewMode }"
>
  <button @click="editImage" class="action-button edit">
    <span class="icon">✏️</span>
    <span v-if="!previewMode">Éditer</span>
  </button>
  <button @click="downloadImage" class="action-button download">
    <span class="icon">💾</span>
    <span v-if="!previewMode">Télécharger</span>
  </button>
  <button @click="togglePublish" class="action-button publish" :class="{ active: image?.isPublished }">
    <span class="icon">📢</span>
    <span v-if="!previewMode">{{ image?.isPublished ? 'Retirer du fil' : 'Publier' }}</span>
  </button>
  <button @click="toggleSimilarImages" class="action-button similar">
    <span class="icon">🔍</span>
    <span v-if="!previewMode">{{ showSimilarImages ? 'Masquer similaires' : 'Images similaires' }}</span>
  </button>
  <button @click="deleteImage" class="action-button delete">
    <span class="icon">🗑️</span>
    <span v-if="!previewMode">Supprimer</span>
  </button>
</div>
      <div v-if="!previewMode && showSimilarImages" class="similar-images-container">
        <SimilarImages :imageId="id" :maxResults="1" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.image-container {
  width: 100%;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  margin-bottom: 20px;
  transition: transform 0.3s, box-shadow 0.3s;
}

.image-container:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 20px rgba(0, 0, 0, 0.1);
}

.preview-mode {
  height: 100%;
  position: relative;
}

.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 30px;
  color: #666;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #007bff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 15px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error {
  color: #dc3545;
  text-align: center;
  padding: 20px;
  background-color: rgba(220, 53, 69, 0.1);
  border-radius: 6px;
  border-left: 4px solid #dc3545;
  margin: 15px;
}

.image-view {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 20px;
  align-items: center;
  padding-bottom: 20px;
}

.preview-mode .image-view {
  height: 100%;
  padding-bottom: 0;
}

.main-image {
  width: 100%;
  max-height: 500px;
  object-fit: contain;
  border-radius: 8px 8px 0 0;
  transition: transform 0.3s ease;
}

.main-image:hover {
  transform: scale(1.02);
}

.preview-mode .main-image {
  border-radius: 8px;
  object-fit: cover;
  height: 100%;
}

.image-details {
  padding: 15px 25px;
  text-align: center;
  width: 100%;
  box-sizing: border-box;
  background-color: #f8f9fa;
  border-radius: 8px;
  margin: 0 20px;
}

h3 {
  margin: 0 0 15px 0;
  color: #2c3e50;
  font-size: 1.3em;
  position: relative;
  display: inline-block;
  padding-bottom: 8px;
}

h3:after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 40px;
  height: 3px;
  background: linear-gradient(to right, #0062cc, #007bff);
  border-radius: 2px;
}

p {
  margin: 8px 0;
  color: #666;
  font-size: 0.95em;
  display: flex;
  justify-content: space-between;
  max-width: 250px;
  margin-left: auto;
  margin-right: auto;
}

.detail-value {
  font-weight: 500;
  color: #2c3e50;
}

.image-actions {
  display: flex;
  justify-content: center;
  gap: 15px;
  padding: 5px 15px;
  flex-wrap: wrap;
}

.preview-actions {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0, 0, 0, 0.7);
  padding: 15px;
  display: flex;
  justify-content: center;
  gap: 15px;
  opacity: 0;
  transition: opacity 0.3s ease;
  backdrop-filter: blur(4px);
}

.preview-mode:hover .preview-actions {
  opacity: 1;
}

.action-button {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 18px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.95em;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 3px 6px rgba(0, 0, 0, 0.1);
}
.action-button.edit {
  background-color: #ffc107;
  color: #212529;
}
.preview-mode .action-button {
  padding: 10px;
  background: rgba(255, 255, 255, 0.9);
}

.action-button.delete {
  background-color: #ff4444;
  color: white;
}

.action-button.download {
  background-color: #28a745;
  color: white;
}

.action-button.publish {
  background-color: #007bff;
  color: white;
}

.action-button.publish.active {
  background-color: #28a745;
}

.action-button.similar {
  background-color: #007bff;
  color: white;
}

.action-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 10px rgba(0, 0, 0, 0.2);
}

.action-button:active {
  transform: translateY(0);
}

.icon {
  font-size: 1.2em;
}

.preview-mode .action-button {
  border-radius: 50%;
  width: 45px;
  height: 45px;
  padding: 0;
  display: flex;
  justify-content: center;
  align-items: center;
}

.preview-mode .action-button span:not(.icon) {
  display: none;
}

.similar-images-container {
  width: 100%;
  padding: 0 20px 10px;
  margin-top: -10px;
}

@media (max-width: 768px) {
  .image-actions {
    flex-direction: column;
    align-items: center;
  }
  
  .action-button {
    width: 100%;
    justify-content: center;
  }
  
  .preview-mode .action-button {
    width: 40px;
    height: 40px;
  }
}
</style>