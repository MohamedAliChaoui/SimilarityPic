<template>
  <div class="similar-images">
    <h3>Images similaires</h3>
    <div class="controls">
      <div class="control-group">
        <label for="descriptor">Descripteur :</label>
        <select v-model="selectedDescriptor" id="descriptor" @change="loadSimilarImages">
          <option value="RGB3D">RGB 3D Histogram</option>
          <option value="HS">HS Histogram</option>
        </select>
      </div>
      <div class="control-group">
        <label for="maxResults">Nombre d'images :</label>
        <input 
          type="number" 
          id="maxResults" 
          v-model="numberOfImages" 
          min="1" 
          max="20" 
          @change="loadSimilarImages"
        >
      </div>
    </div>
    <div v-if="loading" class="loading">
      Recherche d'images similaires...
    </div>
    <div v-else-if="error" class="error">
      {{ error }}
    </div>
    <div v-else class="similar-grid">
      <div v-for="image in similarImages" :key="image.id" class="similar-item">
        <img :src="getImageUrl(image.id)" :alt="image.name" />
        <div class="similarity">
          Similarité : {{ (image.similarity * 100).toFixed(1) }}%
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { api, type SimilarImageType } from '../http-api';

const props = defineProps<{
  imageId: number;
  maxResults?: number;
}>();

const similarImages = ref<SimilarImageType[]>([]);
const loading = ref(false);
const error = ref<string | null>(null);
const selectedDescriptor = ref('RGB3D');
const numberOfImages = ref(props.maxResults || 5);

const loadSimilarImages = async () => {
  loading.value = true;
  error.value = null;
  try {
    similarImages.value = await api.getSimilarImages(
      props.imageId, 
      numberOfImages.value,
      selectedDescriptor.value as "RGB3D" | "HS"
    );
  } catch (e) {
    error.value = "Erreur lors de la recherche d'images similaires";
    console.error(e);
  } finally {
    loading.value = false;
  }
};

const getImageUrl = (id: number) => {
  return `/images/${id}`;
};

watch(() => props.imageId, () => {
  loadSimilarImages();
});

onMounted(() => {
  loadSimilarImages();
});
</script>

<style scoped>
.similar-images {
  margin: 20px 0;
}

.controls {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f5f5;
  border-radius: 8px;
}

.control-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.control-group label {
  font-weight: 500;
}

.control-group select,
.control-group input {
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background-color: white;
}

.control-group input[type="number"] {
  width: 80px;
}

.loading, .error {
  text-align: center;
  padding: 20px;
  color: #666;
}

.error {
  color: #ff4444;
}

.similar-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
  padding: 20px;
}

.similar-item {
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s;
}

.similar-item:hover {
  transform: scale(1.02);
}

.similar-item img {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.similarity {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 8px;
  text-align: center;
  font-size: 0.9em;
}
</style>