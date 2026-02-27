<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { api } from '../http-api';

const router = useRouter();
const file = ref<File | null>(null);
const loading = ref(false);
const error = ref<string | null>(null);
const success = ref<string | null>(null); // Ajout de la variable success
const dragOver = ref(false);

const handleFileSelect = (event: Event) => {
  const input = event.target as HTMLInputElement;
  if (input.files && input.files.length > 0) {
    file.value = input.files[0];
    error.value = null;
    success.value = null; // Réinitialiser le message de succès
  }
};

const handleDrop = (event: DragEvent) => {
  event.preventDefault();
  dragOver.value = false;
  
  if (event.dataTransfer?.files.length) {
    file.value = event.dataTransfer.files[0];
    error.value = null;
    success.value = null; // Réinitialiser le message de succès
  }
};

const handleDragOver = (event: DragEvent) => {
  event.preventDefault();
  dragOver.value = true;
};

const handleDragLeave = () => {
  dragOver.value = false;
};

const calculateHistograms = async (file: File): Promise<{ [key: string]: number[] }> => {
  return new Promise((resolve, reject) => {
    const img = new Image();
    const canvas = document.createElement('canvas');
    const ctx = canvas.getContext('2d');
    
    img.onload = () => {
      if (!ctx) {
        reject(new Error("Impossible de créer le contexte 2D"));
        return;
      }

      // Redimensionner le canvas à la taille de l'image
      canvas.width = img.width;
      canvas.height = img.height;
      
      // Dessiner l'image sur le canvas
      ctx.drawImage(img, 0, 0);
      
      // Obtenir les données de l'image
      const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height);
      const data = imageData.data;
      
      // Initialiser les histogrammes
      const histograms: { [key: string]: number[] } = {
        red: new Array(256).fill(0),
        green: new Array(256).fill(0),
        blue: new Array(256).fill(0),
        gray: new Array(256).fill(0)
      };
      
      // Calculer les histogrammes
      for (let i = 0; i < data.length; i += 4) {
        const r = data[i];
        const g = data[i + 1];
        const b = data[i + 2];
        
        // RGB
        histograms.red[r]++;
        histograms.green[g]++;
        histograms.blue[b]++;
        
        // Niveaux de gris
        const gray = Math.round((r + g + b) / 3);
        histograms.gray[gray]++;
      }
      
      resolve(histograms);
    };
    
    img.onerror = () => {
      reject(new Error("Erreur lors du chargement de l'image"));
    };
    
    img.src = URL.createObjectURL(file);
  });
};

const uploadImage = async () => {
  if (!file.value) {
    error.value = "Veuillez sélectionner une image";
    return;
  }

  if (!file.value.type.startsWith('image/')) {
    error.value = "Le fichier sélectionné n'est pas une image";
    return;
  }

  try {
    loading.value = true;
    error.value = null;
    success.value = null; // Réinitialiser le message de succès

    // Calculer les histogrammes
    const histograms = await calculateHistograms(file.value);
    
    // Créer le FormData avec l'image et les histogrammes
    const formData = new FormData();
    formData.append('file', file.value);
    formData.append('histograms', JSON.stringify(histograms));
    
    // Envoyer l'image et les histogrammes au serveur
    await api.addImage(formData);
    success.value = "Image téléchargée avec succès !"; // Mettre à jour le message de succès

    // Attendre 2 secondes avant de rediriger
    setTimeout(() => {
      router.push('/');
    }, 1000);
  } catch (e) {
    error.value = e instanceof Error ? e.message : "Une erreur est survenue lors de l'upload";
    console.error(e);
  } finally {
    loading.value = false;
  }
};
</script>

<template>
  <div class="upload-container">
    <h2>Ajouter une image</h2>
    
    <div
      class="drop-zone"
      :class="{ 'drag-over': dragOver }"
      @drop="handleDrop"
      @dragover="handleDragOver"
      @dragleave="handleDragLeave"
    >
      <div class="drop-zone-content">
        <div class="upload-icon">📁</div>
        <p class="drop-text">Glissez et déposez votre image ici</p>
        <p class="or-text">ou</p>
        <label class="file-input-label">
          Parcourir
          <input
            type="file"
            accept="image/*"
            @change="handleFileSelect"
            class="file-input"
          >
        </label>
      </div>
    </div>

    <div v-if="file" class="selected-file">
      <div class="file-details">
        <p class="file-name">Fichier sélectionné : <span>{{ file.name }}</span></p>
        <p class="file-size">Taille : <span>{{ (file.size / 1024).toFixed(1) }} KB</span></p>
      </div>
    </div>

    <div v-if="error" class="error-message">
      <span class="message-icon">⚠️</span> {{ error }}
    </div>

    <div v-if="success" class="success-message">
      <span class="message-icon">✅</span> {{ success }}
    </div>

    <div class="actions">
      <button
        @click="uploadImage"
        :disabled="loading || !file"
        class="upload-button"
      >
        <span v-if="loading" class="button-icon loading-spinner"></span>
        <span v-else class="button-icon">⬆️</span>
        {{ loading ? 'Upload en cours...' : 'Uploader' }}
      </button>
      <button
        @click="router.push('/')"
        :disabled="loading"
        class="cancel-button"
      >
        <span class="button-icon">✖️</span>
        Annuler
      </button>
    </div>
  </div>
</template>

<style scoped>
.upload-container {
  max-width: 700px;
  margin: 0 auto;
  padding: 30px;
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.1);
}

h2 {
  color: #2c3e50;
  text-align: center;
  margin-bottom: 30px;
  font-size: 28px;
  font-weight: 600;
  position: relative;
}

h2:after {
  content: '';
  position: absolute;
  bottom: -10px;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 4px;
  background: linear-gradient(to right, #0062cc, #007bff);
  border-radius: 2px;
}

.drop-zone {
  border: 3px dashed #ccc;
  border-radius: 12px;
  padding: 50px 30px;
  text-align: center;
  background: #f8f9fa;
  transition: all 0.3s ease;
  margin-top: 30px;
}

.drag-over {
  border-color: #007bff;
  background: #e3f2fd;
  transform: scale(1.02);
  box-shadow: 0 8px 15px rgba(0, 123, 255, 0.15);
}

.drop-zone-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
}

.upload-icon {
  font-size: 62px;
  margin-bottom: 15px;
  color: #007bff;
}

.drop-text {
  font-size: 18px;
  color: #495057;
  margin: 0;
}

.or-text {
  color: #6c757d;
  font-size: 16px;
  margin: 0;
}

.file-input {
  display: none;
}

.file-input-label {
  background: linear-gradient(to right, #0062cc, #007bff);
  color: white;
  padding: 12px 24px;
  border-radius: 30px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 500;
  letter-spacing: 0.5px;
  font-size: 16px;
  box-shadow: 0 4px 8px rgba(0, 123, 255, 0.2);
}

.file-input-label:hover {
  background: linear-gradient(to right, #004fa3, #0069d9);
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(0, 123, 255, 0.3);
}

.selected-file {
  margin-top: 25px;
  padding: 20px;
  background: #e9ecef;
  border-radius: 8px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
  border-left: 4px solid #007bff;
}

.file-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.file-name, .file-size {
  margin: 0;
  color: #495057;
}

.file-name span, .file-size span {
  font-weight: 600;
  color: #212529;
}

.error-message {
  margin-top: 20px;
  padding: 15px;
  color: #dc3545;
  background: #f8d7da;
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 10px;
  box-shadow: 0 2px 6px rgba(220, 53, 69, 0.2);
  border-left: 4px solid #dc3545;
}

.success-message {
  margin-top: 20px;
  padding: 15px;
  color: #28a745;
  background: #d4edda;
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 10px;
  box-shadow: 0 2px 6px rgba(40, 167, 69, 0.2);
  border-left: 4px solid #28a745;
}

.message-icon {
  font-size: 20px;
}

.actions {
  margin-top: 30px;
  display: flex;
  gap: 15px;
  justify-content: center;
}

.upload-button, .cancel-button {
  padding: 14px 28px;
  border: none;
  border-radius: 30px;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  letter-spacing: 0.5px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.button-icon {
  font-size: 18px;
}

.upload-button {
  background: linear-gradient(to right, #1e9442, #28a745);
  color: white;
}

.upload-button:hover:not(:disabled) {
  background: linear-gradient(to right, #1a7f38, #218838);
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(40, 167, 69, 0.3);
}

.upload-button:disabled {
  background: #6c757d;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.cancel-button {
  background: linear-gradient(to right, #bd2130, #dc3545);
  color: white;
}

.cancel-button:hover:not(:disabled) {
  background: linear-gradient(to right, #a71d2a, #c82333);
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(220, 53, 69, 0.3);
}

.cancel-button:disabled {
  background: #6c757d;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.loading-spinner {
  display: inline-block;
  width: 18px;
  height: 18px;
  border: 3px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top-color: #fff;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 768px) {
  .upload-container {
    padding: 20px;
  }
  
  .drop-zone {
    padding: 30px 15px;
  }
  
  .actions {
    flex-direction: column;
  }
  
  .upload-button, .cancel-button {
    width: 100%;
  }
}
</style>