
<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { api } from '../http-api';
import { Cropper } from 'vue-advanced-cropper';
import 'vue-advanced-cropper/dist/style.css';
const isSaving = ref(false);
const route = useRoute();
const router = useRouter();
const showResetOriginal = ref(false);
const imageId = computed(() => Number(route.params.id));
const imageUrl = ref('');
const loading = ref(true);
const error = ref<string | null>(null);
const originalImageUrl = ref('');
const isCropping = ref(false);
//const croppedImageUrl = ref('');
const saveMessage = ref(''); // Message de confirmation

// Editor state
const filters = ref({
  brightness: 100,
  contrast: 100,
  saturation: 100,
  blur: 0,
  tint: 0,
  xProcess: 0,
  vignette: 0,
  warmth: 0,
  clarity: 0
});
// Reset the crop
// Reset the crop to show the entire image
// Reset the crop to show the entire image
const resetCrop = () => {
  isCropping.value = false;

  // Small delay to ensure the component is fully unmounted
  setTimeout(() => {
    // Use the current edited image
    const img = new Image();
    img.onload = () => {
      isCropping.value = true;
    };
    img.src = imageUrl.value; // Ensure we're using the current state
  }, 150);
};
const activeTab = ref('adjust'); // 'adjust', 'crop', 'flip', 'text'
const resetToOriginalImage = async () => {
  try {
    loading.value = true;
    const response = await api.getImage(imageId.value);
    originalImageUrl.value = URL.createObjectURL(response);
    imageUrl.value = originalImageUrl.value;
    showResetOriginal.value = false;
    // Reset also the crop state if we're in crop mode
    if (activeTab.value === 'crop') {
      isCropping.value = false;
    }
  } catch (e) {
    error.value = "Erreur lors du rechargement de l'image originale";
    console.error(e);
  } finally {
    loading.value = false;
  }
};
// Text overlay state
const textOverlays = ref<Array<{
  id: number;
  text: string;
  x: number;
  y: number;
  fontSize: number;
  fontFamily: string;
  color: string;
  isDragging: boolean;
  rotation: number;
}>>([]);

const newText = ref('');
const selectedTextId = ref<number | null>(null);
const textColor = ref('#ffffff');
const fontSize = ref(24);
const fontFamily = ref('Arial');
const textRotation = ref(0);
const isAddingText = ref(false);

// Image style with applied filters
const imageStyle = computed(() => {
  return {
    filter: `
      brightness(${filters.value.brightness}%) 
      contrast(${filters.value.contrast}%) 
      saturate(${filters.value.saturation}%)
      blur(${filters.value.blur}px)
      sepia(${filters.value.warmth}%)
      hue-rotate(${filters.value.tint}deg)
    `,
    transform: `scale(${1 + filters.value.clarity / 200})`,
  };
});

// Flip state
const flipState = ref({
  horizontal: false,
  vertical: false
});

// Load the image
const loadImage = async () => {
  try {
    loading.value = true;
    const response = await api.getImage(imageId.value);
    originalImageUrl.value = URL.createObjectURL(response);
    imageUrl.value = originalImageUrl.value;
  } catch (e) {
    error.value = "Erreur lors du chargement de l'image";
    console.error(e);
  } finally {
    loading.value = false;
  }
};

// Reset filters to default
const resetFilters = () => {
  filters.value = {
    brightness: 100,
    contrast: 100,
    saturation: 100,
    blur: 0,
    tint: 0,
    xProcess: 0,
    vignette: 0,
    warmth: 0,
    clarity: 0
  };
};

// Flip image horizontally
const flipHorizontal = () => {
  flipState.value.horizontal = !flipState.value.horizontal;
  applyFlip();
};

// Flip image vertically
const flipVertical = () => {
  flipState.value.vertical = !flipState.value.vertical;
  applyFlip();
};

// Apply flip to the image
const applyFlip = () => {
  const canvas = document.createElement('canvas');
  const ctx = canvas.getContext('2d');
  const img = new Image();
  
  img.onload = () => {
    canvas.width = img.width;
    canvas.height = img.height;
    
    if (ctx) {
      ctx.save();
      ctx.translate(
        flipState.value.horizontal ? canvas.width : 0,
        flipState.value.vertical ? canvas.height : 0
      );
      ctx.scale(
        flipState.value.horizontal ? -1 : 1,
        flipState.value.vertical ? -1 : 1
      );
      ctx.drawImage(img, 0, 0);
      ctx.restore();
      
      imageUrl.value = canvas.toDataURL('image/jpeg');
    }
  };
  
  img.src = originalImageUrl.value;
};

// Start cropping process

const startCropping = () => {
  const canvas = document.createElement('canvas');
  const ctx = canvas.getContext('2d');
  const img = new Image();

  img.onload = () => {
    canvas.width = img.width;
    canvas.height = img.height;

    if (ctx) {
      // Apply current filters
      // ctx.filter = `
      //   brightness(${filters.value.brightness}%) 
      //   contrast(${filters.value.contrast}%) 
      //   saturate(${filters.value.saturation}%)
      //   blur(${filters.value.blur}px)
      //   sepia(${filters.value.warmth}%)
      //   hue-rotate(${filters.value.tint}deg)
      // `;
      ctx.drawImage(img, 0, 0);

      // Also apply flip transformations if needed
      if (flipState.value.horizontal || flipState.value.vertical) {
        ctx.setTransform(
          flipState.value.horizontal ? -1 : 1,
          0,
          0,
          flipState.value.vertical ? -1 : 1,
          flipState.value.horizontal ? canvas.width : 0,
          flipState.value.vertical ? canvas.height : 0
        );
        ctx.drawImage(img, 0, 0);
      }

      // Update imageUrl with filtered image
      imageUrl.value = canvas.toDataURL('image/jpeg');

      // Enable cropping mode
      setTimeout(() => {
        isCropping.value = true;
      }, 100);
    }
  };

  img.src = originalImageUrl.value;
};

// Define a custom type for the Cropper instance
interface CropperInstance {
  getResult: () => { coordinates: any; canvas: HTMLCanvasElement | null };
}

// Update the type of cropperRef
const cropperRef = ref<CropperInstance | null>(null);


// Complete the crop
const completeCrop = () => {
  if (cropperRef.value) {
    const { canvas } = cropperRef.value.getResult();

    if (canvas) {
      // Convert the cropped canvas directly to an image URL
      imageUrl.value = canvas.toDataURL('image/jpeg');
      
      // Disable cropping mode
      isCropping.value = false;
      showResetOriginal.value = true;
    }
  }
};

// Cancel cropping
const cancelCrop = () => {
  isCropping.value = false;
  // No need to reapply filters - the imageUrl already has them
};
// Add text overlay
const addTextOverlay = () => {
  if (!newText.value.trim()) return;
  
  // Get canvas container dimensions to place text in the center
  const container = document.querySelector('.canvas-container');
  let centerX = 200;
  let centerY = 200;
  
  if (container) {
    const rect = container.getBoundingClientRect();
    centerX = rect.width / 2;
    centerY = rect.height / 2;
  }
  
  const newId = Date.now();
  textOverlays.value.push({
    id: newId,
    text: newText.value,
    x: centerX - 50,  // Approximate position
    y: centerY,
    fontSize: fontSize.value,
    fontFamily: fontFamily.value,
    color: textColor.value,
    isDragging: false,
    rotation: textRotation.value
  });
  
  // Select the newly added text
  selectedTextId.value = newId;
  newText.value = '';
  isAddingText.value = false;
};

// Start adding text
const startAddingText = () => {
  isAddingText.value = true;
  newText.value = 'Texte';
};

// Select text overlay
const selectTextOverlay = (id: number) => {
  selectedTextId.value = id;
  
  // Update the controls to match selected text
  const selected = textOverlays.value.find(t => t.id === id);
  if (selected) {
    textColor.value = selected.color;
    fontSize.value = selected.fontSize;
    fontFamily.value = selected.fontFamily;
    textRotation.value = selected.rotation;
  }
};

// Update selected text
const updateSelectedText = () => {
  if (selectedTextId.value === null) return;
  
  const index = textOverlays.value.findIndex(t => t.id === selectedTextId.value);
  if (index !== -1) {
    textOverlays.value[index].color = textColor.value;
    textOverlays.value[index].fontSize = fontSize.value;
    textOverlays.value[index].fontFamily = fontFamily.value;
    textOverlays.value[index].rotation = textRotation.value;
  }
};

// Delete selected text
const deleteSelectedText = () => {
  if (selectedTextId.value === null) return;
  
  textOverlays.value = textOverlays.value.filter(t => t.id !== selectedTextId.value);
  selectedTextId.value = null;
};




const dragOffset = ref({ x: 0, y: 0 });

const startDragText = (id: number, event: MouseEvent) => {
  event.preventDefault();
  selectTextOverlay(id);

  const index = textOverlays.value.findIndex(t => t.id === id);
  if (index !== -1) {
    const text = textOverlays.value[index];
    text.isDragging = true;

    const canvasContainer = document.querySelector('.canvas-container');
    if (canvasContainer) {
      //const containerRect = canvasContainer.getBoundingClientRect();

      // Calculate the offset from where the user clicked on the text
      dragOffset.value.x = event.clientX - text.x;
      dragOffset.value.y = event.clientY - text.y;

      console.log("Drag offset:", dragOffset.value);
    }
  }

  document.addEventListener("mousemove", onDragText);
  document.addEventListener("mouseup", stopDragText);
};

const onDragText = (event: MouseEvent) => {
  if (selectedTextId.value === null) return;

  const index = textOverlays.value.findIndex(t => t.id === selectedTextId.value);
  if (index !== -1 && textOverlays.value[index].isDragging) {
    const canvasContainer = document.querySelector('.canvas-container');
    if (canvasContainer) {
      // Keep the text aligned with the mouse without a large offset
      const newX = event.clientX - dragOffset.value.x;
      const newY = event.clientY - dragOffset.value.y;

      textOverlays.value[index].x = newX;
      textOverlays.value[index].y = newY;
    }
  }
};

const stopDragText = () => {
  if (selectedTextId.value === null) return;

  const index = textOverlays.value.findIndex(t => t.id === selectedTextId.value);
  if (index !== -1) {
    textOverlays.value[index].isDragging = false;
  }

  // Remove event listeners to prevent memory leaks
  document.removeEventListener("mousemove", onDragText);
  document.removeEventListener("mouseup", stopDragText);
};
const selectedText = computed({
  get: () => {
    if (selectedTextId.value === null) return null;
    return textOverlays.value.find(t => t.id === selectedTextId.value);
  },
  set: (value) => {
    if (!value || selectedTextId.value === null) return;
    const index = textOverlays.value.findIndex(t => t.id === selectedTextId.value);
    if (index !== -1) {
      textOverlays.value[index] = { ...textOverlays.value[index], ...value };
    }
  }
});
const updateTextContent = (event: Event) => {
  const target = event.target as HTMLInputElement;
  if (!selectedTextId.value || !target) return;
  
  const index = textOverlays.value.findIndex(t => t.id === selectedTextId.value);
  if (index !== -1) {
    textOverlays.value[index].text = target.value;
  }
};

// Save the edited image
const saveImage = async () => {
  try {
    isSaving.value = true; // Activer l'indicateur de chargement
    saveMessage.value = ''; // Réinitialiser le message

    // Création du canvas avec les modifications
    const canvas = document.createElement('canvas');
    const ctx = canvas.getContext('2d');
    const img = new Image();

    await new Promise((resolve) => {
      img.onload = resolve;
      img.src = imageUrl.value;
    });

    canvas.width = img.width;
    canvas.height = img.height;

    if (ctx) {
      // Application des filtres
      ctx.filter = `
        brightness(${filters.value.brightness}%) 
        contrast(${filters.value.contrast}%) 
        saturate(${filters.value.saturation}%)
        blur(${filters.value.blur}px)
        sepia(${filters.value.warmth}%)
        hue-rotate(${filters.value.tint}deg)
      `;
      ctx.drawImage(img, 0, 0);

      // Dessin des textes
      ctx.filter = 'none';
      const displayedImg = document.querySelector('.edited-image') as HTMLImageElement;
      if (displayedImg) {
        const scaleX = img.width / displayedImg.offsetWidth;
        const scaleY = img.height / displayedImg.offsetHeight;

        textOverlays.value.forEach(text => {
          ctx.save();
          const scaledX = text.x * scaleX;
          const scaledY = text.y * scaleY;
          ctx.fillStyle = text.color;
          ctx.font = `${text.fontSize * scaleY}px ${text.fontFamily}`;

          if (text.rotation !== 0) {
            ctx.translate(scaledX, scaledY);
            ctx.rotate((text.rotation * Math.PI) / 180);
            ctx.fillText(text.text, 0, 0);
          } else {
            ctx.fillText(text.text, scaledX, scaledY);
          }
          ctx.restore();
        });
      }

      // Conversion en Blob
      const blob = await new Promise<Blob>((resolve, reject) => {
        canvas.toBlob((b) => {
          if (!b) {
            reject(new Error("Échec de la conversion du canvas en image"));
            return;
          }
          resolve(b);
        }, 'image/jpeg', 0.95);
      });
      // Récupération du nom original
      const originalImages = await api.getImageList();
      const originalImage = originalImages.find(img => img.id === imageId.value);

      // Construction du nouveau nom
      let newName: string;
      if (originalImage) {
        const baseName = originalImage.name.replace(/\.[^/.]+$/, ''); // Retirer l'extension
        const ext = originalImage.format.toLowerCase() === 'png' ? 'png' : 'jpg'; // Déterminer l'extension
        newName = `${baseName}_edited.${ext}`;
      } else {
        newName = `edited_${Date.now()}.jpg`; // Nom par défaut si l'image originale n'est pas trouvée
      }

      // Création du FormData
      const formData = new FormData();
      formData.append('file', new File([blob], newName, { type: 'image/jpeg' }));

      
      

      // Envoi à l'API
    

      const newImageId = await api.addImage(formData);

      // Afficher un message de confirmation après l'enregistrement
      saveMessage.value = `Image enregistrée avec succès (ID: ${newImageId})`;
      alert(saveMessage.value); // Afficher une alerte
      router.push('/'); // Rediriger vers la page d'accueil
    }
  } catch (error) {
    console.error('Erreur lors de la sauvegarde de l\'image:', error);
    saveMessage.value = "Une erreur s'est produite lors de la sauvegarde de l'image.";
    alert(saveMessage.value); // Afficher une alerte en cas d'erreur
  } finally {
    isSaving.value = false; // Désactiver l'indicateur de chargement
  }
};

// Cancel editing and go back
const cancelEditing = () => {
  if (confirm('Êtes-vous sûr de vouloir annuler les modifications?')) {
    router.back();
  }
};

// Change active tab
const setActiveTab = (tab: string) => {
  activeTab.value = tab;
  // Reset states when changing tabs
  if (tab !== 'crop') {
    isCropping.value = false;
  }
  if (tab !== 'text') {
    isAddingText.value = false;
    selectedTextId.value = null;
  }
};

// Watch for tab changes to reset states
watch(activeTab, (newTab) => {
  if (newTab === 'crop') {
    // Prepare for cropping
  } else if (newTab === 'flip') {
    // Reset flip state when entering flip tab
    flipState.value = { horizontal: false, vertical: false };
  }
});

// Watch for text property changes to update the selected text
watch([textColor, fontSize, fontFamily, textRotation], () => {
  updateSelectedText();
});

onMounted(() => {
  loadImage();
  
  // Add event listeners for text dragging
  window.addEventListener('mousemove', onDragText);
  window.addEventListener('mouseup', stopDragText);
});
</script>

<template>
  <div v-if="isSaving" class="saving-overlay">
  <div class="spinner"></div>
  <p>Enregistrement en cours...</p>
</div>
  <div class="editor-container">
    <!-- Header with logo and title -->
    <header class="editor-header">
      <div class="logo">Mon Éditeur d'Images</div>
      <div class="editor-actions">
        <button class="btn-cancel" @click="cancelEditing">Annuler</button>
        <button class="btn-save" @click="saveImage">Sauvegarder</button>
      </div>
    </header>
    
    <!-- Main editing area -->
    <div class="editor-content">
      <!-- Sidebar with editing tools -->
      <div class="editor-sidebar">
        <div class="sidebar-tabs">
          <button 
            @click="setActiveTab('adjust')" 
            :class="{ active: activeTab === 'adjust' }"
            class="tab-button"
          >
            <span class="tab-icon">⚙️</span>
            <span>Ajuster</span>
          </button>
          <button 
            @click="setActiveTab('crop')" 
            :class="{ active: activeTab === 'crop' }"
            class="tab-button"
          >
            <span class="tab-icon">✂️</span>
            <span>Recadrer</span>
          </button>
          <button 
            @click="setActiveTab('flip')" 
            :class="{ active: activeTab === 'flip' }"
            class="tab-button"
          >
            <span class="tab-icon">🔄</span>
            <span>Retourner</span>
          </button>
          <button 
            @click="setActiveTab('text')" 
            :class="{ active: activeTab === 'text' }"
            class="tab-button"
          >
            <span class="tab-icon">🔤</span>
            <span>Texte</span>
          </button>
        </div>
        
        <!-- Adjustment controls -->
        <div v-if="activeTab === 'adjust'" class="sidebar-content">
          <h3>Ajustements</h3>
          
          <div class="adjustment-control">
            <div class="control-header">
              <label for="brightness">Luminosité</label>
              <input type="number" v-model="filters.brightness" class="value-input" />
            </div>
            <input 
              type="range" 
              id="brightness" 
              v-model="filters.brightness" 
              min="0" 
              max="200" 
              class="slider"
            >
            <div class="slider-track">
              <div class="track-fill" :style="{ width: `${filters.brightness / 2}%` }"></div>
            </div>
          </div>
          
          <div class="adjustment-control">
            <div class="control-header">
              <label for="contrast">Contraste</label>
              <input type="number" v-model="filters.contrast" class="value-input" />
            </div>
            <input 
              type="range" 
              id="contrast" 
              v-model="filters.contrast" 
              min="0" 
              max="200" 
              class="slider"
            >
            <div class="slider-track">
              <div class="track-fill" :style="{ width: `${filters.contrast / 2}%` }"></div>
            </div>
          </div>
          
          <div class="adjustment-control">
            <div class="control-header">
              <label for="saturation">Saturation</label>
              <input type="number" v-model="filters.saturation" class="value-input" />
            </div>
            <input 
              type="range" 
              id="saturation" 
              v-model="filters.saturation" 
              min="0" 
              max="200" 
              class="slider"
            >
            <div class="slider-track">
              <div class="track-fill" :style="{ width: `${filters.saturation / 2}%` }"></div>
            </div>
          </div>
          
          <div class="adjustment-control">
            <div class="control-header">
              <label for="blur">Flou</label>
              <input type="number" v-model="filters.blur" class="value-input" />
            </div>
            <input 
              type="range" 
              id="blur" 
              v-model="filters.blur" 
              min="0" 
              max="20" 
              class="slider"
            >
            <div class="slider-track">
              <div class="track-fill" :style="{ width: `${(filters.blur / 20) * 100}%` }"></div>
            </div>
          </div>
          
          <div class="adjustment-control">
            <div class="control-header">
              <label for="tint">Teinte</label>
              <input type="number" v-model="filters.tint" class="value-input" />
            </div>
            <input 
              type="range" 
              id="tint" 
              v-model="filters.tint" 
              min="0" 
              max="360" 
              class="slider tint-slider"
            >
            <div class="slider-track tint-track">
              <div class="track-fill" :style="{ width: `${(filters.tint / 360) * 100}%` }"></div>
            </div>
          </div>
          
          <div class="adjustment-control">
            <div class="control-header">
              <label for="warmth">Chaleur</label>
              <input type="number" v-model="filters.warmth" class="value-input" />
            </div>
            <input 
              type="range" 
              id="warmth" 
              v-model="filters.warmth" 
              min="0" 
              max="100" 
              class="slider"
            >
            <div class="slider-track">
              <div class="track-fill" :style="{ width: `${filters.warmth}%` }"></div>
            </div>
          </div>
          
          <button class="reset-button" @click="resetFilters">Réinitialiser</button>
        </div>
        
        <!-- Crop controls -->
        <div v-else-if="activeTab === 'crop'" class="sidebar-content">
          <h3>Recadrer</h3>
          
          <div v-if="!isCropping" class="crop-buttons">
            <button class="tool-button" @click="startCropping">
              Commencer le recadrage
            </button>
          </div>
          
          <div v-else class="crop-controls">
            <p class="crop-instructions">Ajustez le cadre pour recadrer l'image</p>
            <div class="crop-actions">
              <button class="tool-button" @click="completeCrop">Appliquer</button>
              <button class="tool-button" @click="resetCrop">Réinitialiser</button>
              <button class="tool-button secondary" @click="cancelCrop">Annuler</button>
            </div>
            <div v-if="showResetOriginal" class="reset-original">
              <button class="tool-button" @click="resetToOriginalImage">
                Réinitialiser l'image originale
              </button>
            </div>
          </div>
        </div>
        
        <!-- Flip controls -->
        <div v-else-if="activeTab === 'flip'" class="sidebar-content">
          <h3>Retourner</h3>
          <div class="flip-buttons">
            <button 
              class="tool-button" 
              @click="flipHorizontal"
              :class="{ 'active-flip': flipState.horizontal }"
            >
              Retourner horizontalement
            </button>
            <button 
              class="tool-button" 
              @click="flipVertical"
              :class="{ 'active-flip': flipState.vertical }"
            >
              Retourner verticalement
            </button>
          </div>
        </div>
        
        <!-- Text controls -->
        <div v-else-if="activeTab === 'text'" class="sidebar-content">
          <h3>Ajouter du texte</h3>
          
          <div v-if="!isAddingText && selectedTextId === null" class="text-intro">
            <button class="tool-button" @click="startAddingText">
              Ajouter un nouveau texte
            </button>
            <p class="text-instructions" v-if="textOverlays.length > 0">
              Cliquez sur un texte existant pour le modifier.
            </p>
          </div>
          
          <div v-if="isAddingText" class="text-form">
            <div class="text-input-group">
              <label for="newTextInput">Texte:</label>
              <input 
                id="newTextInput"
                type="text" 
                v-model="newText" 
                class="text-input"
                placeholder="Entrez votre texte"
              />
            </div>
            
            <div class="text-properties">
              <div class="property-group">
                <label for="textColor">Couleur:</label>
                <input type="color" id="textColor" v-model="textColor" />
              </div>
              
              <div class="property-group">
                <label for="fontSize">Taille:</label>
                <input 
                  type="range" 
                  id="fontSize" 
                  v-model="fontSize" 
                  min="10" 
                  max="72" 
                  class="slider"
                />
                <span class="value-display">{{ fontSize }}px</span>
              </div>
              
              <div class="property-group">
                <label for="fontFamily">Police:</label>
                <select id="fontFamily" v-model="fontFamily" class="font-select">
                  <option value="Arial">Arial</option>
                  <option value="Verdana">Verdana</option>
                  <option value="Georgia">Georgia</option>
                  <option value="Times New Roman">Times New Roman</option>
                  <option value="Courier New">Courier New</option>
                  <option value="Impact">Impact</option>
                </select>
              </div>
              
              <div class="property-group">
                <label for="textRotation">Rotation:</label>
                <input 
                  type="range" 
                  id="textRotation" 
                  v-model="textRotation" 
                  min="0" 
                  max="360" 
                  class="slider"
                />
                <span class="value-display">{{ textRotation }}°</span>
              </div>
            </div>
            
            <div class="text-actions">
              <button class="tool-button" @click="addTextOverlay">Ajouter</button>
              <button class="tool-button secondary" @click="isAddingText = false">Annuler</button>
            </div>
          </div>
          
          <div v-if="selectedTextId !== null && !isAddingText" class="text-edit">
            <div class="text-input-group">
              <label for="editTextContent">Texte:</label>
              <input 
                id="editTextContent"
                type="text" 
                :value="selectedText?.text || ''"
                @input="updateTextContent($event as InputEvent)"
                class="text-input"
                placeholder="Modifiez votre texte"
                @keydown.stop
              />
            </div>
            <div class="text-properties">
              <div class="property-group">
                <label for="editTextColor">Couleur:</label>
                <input type="color" id="editTextColor" v-model="textColor" />
              </div>
              
              <div class="property-group">
                <label for="editFontSize">Taille:</label>
                <input 
                  type="range" 
                  id="editFontSize" 
                  v-model="fontSize" 
                  min="10" 
                  max="72" 
                  class="slider"
                />
                <span class="value-display">{{ fontSize }}px</span>
              </div>
              
              <div class="property-group">
                <label for="editFontFamily">Police:</label>
                <select id="editFontFamily" v-model="fontFamily" class="font-select">
                  <option value="Arial">Arial</option>
                  <option value="Verdana">Verdana</option>
                  <option value="Georgia">Georgia</option>
                  <option value="Times New Roman">Times New Roman</option>
                  <option value="Courier New">Courier New</option>
                  <option value="Impact">Impact</option>
                </select>
              </div>
              
              <div class="property-group">
                <label for="editTextRotation">Rotation:</label>
                <input 
                  type="range" 
                  id="editTextRotation" 
                  v-model="textRotation" 
                  min="0" 
                  max="360" 
                  class="slider"
                />
                <span class="value-display">{{ textRotation }}°</span>
              </div>
            </div>
            
            <div class="text-hint">
              <p>Faites glisser le texte pour le repositionner.</p>
            </div>
            
            <div class="text-actions">
              <button class="tool-button delete-button" @click="deleteSelectedText">Supprimer</button>
              <button class="tool-button secondary" @click="selectedTextId = null">Terminer</button>
            </div>
          </div>
        </div>
      </div>
      
      <!-- Main canvas area -->
      <div class="editor-canvas">
        <div v-if="loading" class="loading-overlay">
          <div class="spinner"></div>
          <p>Chargement de l'image...</p>
        </div>
        <div v-else-if="error" class="error-message">
          {{ error }}
        </div>
        <div v-else class="canvas-container">
          <!-- Show cropper when in crop mode -->
          <div v-if="activeTab === 'crop' && isCropping" class="cropper-container">
            <Cropper
              ref="cropperRef"
              :src="imageUrl"
              :stencil-props="{
                aspectRatio: null
              }"
              class="cropper"
            />
          </div>
          
          <!-- Show normal image when not in crop mode -->
          <div v-else class="image-container" :style="{ position: 'relative' }">
            <img 
              :src="imageUrl" 
              class="edited-image" 
              :style="imageStyle" 
              alt="Image en cours d'édition"
            />
            
            <!-- Text overlays -->
            <div 
              v-for="text in textOverlays" 
              :key="text.id"
              :id="`text-overlay-${text.id}`"
              class="text-overlay"
              :style="{
                position: 'absolute',
                left: `${text.x}px`,
                top: `${text.y}px`,
                color: text.color,
                fontSize: `${text.fontSize}px`,
                fontFamily: text.fontFamily,
                cursor: 'move',
                transform: `rotate(${text.rotation}deg)`,
                userSelect: 'none',
                textShadow: '1px 1px 2px rgba(0,0,0,0.5)'
              }"
              :class="{ 'selected-text': selectedTextId === text.id }"
              @mousedown="startDragText(text.id, $event)"
            >
              {{ text.text }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.editor-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  width: 100%;
  background-color: #f5f5f5;
}
.saving-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(255, 255, 255, 0.8);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.saving-overlay .spinner {
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

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem 1.5rem;
  background-color: #2c3e50;
  color: white;
  height: 60px;
}

.logo {
  font-size: 1.5rem;
  font-weight: bold;
}

.editor-actions {
  display: flex;
  gap: 10px;
}

.btn-cancel, .btn-save {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s;
}

.btn-cancel {
  background-color: #6c757d;
  color: white;
}

.btn-save {
  background-color: #28a745;
  color: white;
}

.btn-cancel:hover, .btn-save:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.2);
}

.editor-content {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.editor-sidebar {
  width: 280px;
  background-color: #ffffff;
  border-right: 1px solid #e0e0e0;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}

.sidebar-tabs {
  display: flex;
  border-bottom: 1px solid #e0e0e0;
  background-color: #f8f9fa;
}

.tab-button {
  flex: 1;
  padding: 12px 8px;
  border: none;
  background: none;
  cursor: pointer;
  font-size: 0.85rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
  color: #495057;
}

.tab-button.active {
  background-color: #fff;
  color: #007bff;
  font-weight: 500;
  border-bottom: 2px solid #007bff;
}

.tab-icon {
  font-size: 1.2rem;
}

.sidebar-content {
  padding: 1.5rem;
}

h3 {
  margin-top: 0;
  margin-bottom: 1.5rem;
  font-size: 1.1rem;
  color: #343a40;
  border-bottom: 1px solid #eee;
  padding-bottom: 0.5rem;
}

.adjustment-control {
  margin-bottom: 1.5rem;
}

.control-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

label {
  font-size: 0.9rem;
  color: #495057;
}

.value-input {
  width: 50px;
  text-align: center;
  border: 1px solid #ced4da;
  border-radius: 4px;
  padding: 2px 4px;
}

.slider {
  -webkit-appearance: none; 
  appearance: none; 
  width: 100%;
  height: 8px;
  border-radius: 4px;
  background: linear-gradient(to right, #007bff, #e9ecef); 
  outline: none;
  margin-bottom: 10px;
  transition: background 0.3s ease;
}

.slider:hover {
  background: linear-gradient(to right, #0056b3, #e9ecef); 
}

.slider::-webkit-slider-thumb {
  -webkit-appearance: none; 
  appearance: none;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #007bff;
  cursor: pointer;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  transition: transform 0.2s ease, background 0.3s ease;
}

.slider::-webkit-slider-thumb:hover {
  transform: scale(1.2); /* Agrandit le curseur au survol */
  background: #0056b3;
}

.slider::-moz-range-thumb {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #007bff;
  cursor: pointer;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  transition: transform 0.2s ease, background 0.3s ease;
}

.slider::-moz-range-thumb:hover {
  transform: scale(1.2);
  background: #0056b3;
}

.slider::-ms-thumb {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #007bff;
  cursor: pointer;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  transition: transform 0.2s ease, background 0.3s ease;
}

.slider::-ms-track {
  background: transparent; 
  border-color: transparent;
  color: transparent;
}



.reset-button {
  width: 100%;
  padding: 10px;
  background-color: #f8f9fa;
  border: 1px solid #ced4da;
  border-radius: 4px;
  cursor: pointer;
  color: #495057;
  margin-top: 1rem;
  transition: all 0.2s;
}

.reset-button:hover {
  background-color: #e9ecef;
}

.tool-button {
  width: 100%;
  padding: 10px;
  background-color: #007bff;
  border: 1px solid #0069d9;
  border-radius: 4px;
  cursor: pointer;
  color: white;
  margin-bottom: 10px;
  transition: all 0.2s;
  font-weight: 500;
}

.tool-button:hover {
  background-color: #0069d9;
}

.tool-button.secondary {
  background-color: #6c757d;
  border-color: #5a6268;
}

.tool-button.secondary:hover {
  background-color: #5a6268;
}

.tool-button.active-flip {
  background-color: #28a745;
  border-color: #1e7e34;
}

.flip-buttons, .crop-buttons, .crop-controls {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.crop-actions {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}

.crop-instructions {
  text-align: center;
  color: #495057;
  margin-bottom: 15px;
  font-size: 0.9rem;
}

.editor-canvas {
  flex: 1;
  background-color: #e9ecef;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: auto;
  position: relative;
}

.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(255, 255, 255, 0.8);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  z-index: 10;
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
.text-input-group {
  margin-bottom: 15px;
}

.text-input {
  width: 100%;
  padding: 8px;
  border: 1px solid #ced4da;
  border-radius: 4px;
   margin-top: 5px; 
}

.font-select {
  width: 100%;
  padding: 8px;
  border: 1px solid #ced4da;
  border-radius: 4px;
  margin-top: 5px;
}

.property-group {
  margin-bottom: 15px;
}

.value-display {
  display: inline-block;
  width: 50px;
  text-align: center;
  margin-left: 10px;
  font-size: 0.9rem;
}

.delete-button {
  background-color: #dc3545;
  border-color: #c82333;
}

.delete-button:hover {
  background-color: #c82333;
}
.error-message {
  color: #dc3545;
  text-align: center;
  padding: 20px;
  background-color: rgba(220, 53, 69, 0.1);
  border-radius: 6px;
  border-left: 4px solid #dc3545;
}

.canvas-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  width: 100%;
  padding: 20px;
}

.edited-image {
  max-width: 90%;
  max-height: 90%;
  object-fit: contain;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  filter: none;
}

.cropper-container {
  width: 90%;
  height: 90%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.cropper {
  width: 100%;
  height: 100%;
  background: #f8f9fa;
}

@media (max-width: 768px) {
  .editor-content {
    flex-direction: column;
  }
  
  .editor-sidebar {
    width: 100%;
    height: 40%;
    border-right: none;
    border-bottom: 1px solid #e0e0e0;
  }
  
  .editor-canvas {
    height: 60%;
  }
}
</style>
