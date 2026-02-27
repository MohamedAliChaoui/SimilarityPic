<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { auth, authState } from '../auth';

// État pour les données du formulaire
const firstName = ref('');
const lastName = ref('');
const email = ref('');
const phoneNumber = ref('');
const birthDate = ref('');
const bio = ref('');
const newPassword = ref('');
const confirmPassword = ref('');

// État pour la gestion des messages
const message = ref('');
const errorMessage = ref('');
const isSuccess = ref(false);
const isLoading = ref(false);

// Référence pour le champ d'upload de photo
const fileInput = ref<HTMLInputElement | null>(null);
const selectedFile = ref<File | null>(null);
const previewUrl = ref('');

// Charger les données utilisateur actuelles au chargement du composant
onMounted(() => {
  if (authState.user.value) {
    firstName.value = authState.user.value.firstName || '';
    lastName.value = authState.user.value.lastName || '';
    email.value = authState.user.value.email || '';
    phoneNumber.value = authState.user.value.phoneNumber || '';
    birthDate.value = authState.user.value.birthDate || '';
    bio.value = authState.user.value.bio || '';
    
    // Si l'utilisateur a une photo de profil, afficher l'aperçu
    if (authState.user.value.hasProfilePicture) {
      previewUrl.value = auth.getProfilePictureUrl();
    }
  }
});

// Gérer la sélection d'un fichier
const handleFileChange = (event: Event) => {
  const target = event.target as HTMLInputElement;
  if (target.files && target.files.length > 0) {
    selectedFile.value = target.files[0];
    
    // Créer un aperçu de l'image
    const reader = new FileReader();
    reader.onload = (e) => {
      previewUrl.value = e.target?.result as string;
    };
    reader.readAsDataURL(selectedFile.value);
  }
};

// Gérer l'upload de la photo de profil
const uploadPicture = async () => {
  if (!selectedFile.value) return;
  
  try {
    isLoading.value = true;
    const formData = new FormData();
    formData.append('file', selectedFile.value);
    
    await auth.uploadProfilePicture(formData);
    
    message.value = 'Photo de profil mise à jour avec succès!';
    isSuccess.value = true;
    errorMessage.value = '';
  } catch (error: any) {
    errorMessage.value = error.response?.data || 'Erreur lors de la mise à jour de la photo de profil';
    isSuccess.value = false;
  } finally {
    isLoading.value = false;
  }
};

// Mettre à jour le profil
const updateProfile = async () => {
  try {
    isLoading.value = true;
    
    // Valider les données avant de soumettre
    if (newPassword.value && newPassword.value !== confirmPassword.value) {
      errorMessage.value = 'Les mots de passe ne correspondent pas';
      isSuccess.value = false;
      return;
    }
    
    // Préparer les données à envoyer
    const updateData: any = {
      firstName: firstName.value,
      lastName: lastName.value,
      email: email.value,
      phoneNumber: phoneNumber.value,
      birthDate: birthDate.value,
      bio: bio.value
    };
    
    // Ajouter le mot de passe seulement s'il est renseigné
    if (newPassword.value) {
      updateData.password = newPassword.value;
    }
    
    await auth.updateProfile(updateData);
    
    message.value = 'Profil mis à jour avec succès!';
    isSuccess.value = true;
    errorMessage.value = '';
    
    // Réinitialiser les champs de mot de passe
    newPassword.value = '';
    confirmPassword.value = '';
  } catch (error: any) {
    errorMessage.value = error.response?.data || 'Erreur lors de la mise à jour du profil';
    isSuccess.value = false;
  } finally {
    isLoading.value = false;
  }
};
</script>

<template>
  <div class="profile-container">
    <h1 class="profile-title">Mon Profil</h1>
    
    <!-- Affichage des messages de succès ou d'erreur -->
    <div v-if="message" class="alert alert-success" role="alert">
      {{ message }}
    </div>
    <div v-if="errorMessage" class="alert alert-danger" role="alert">
      {{ errorMessage }}
    </div>
    
    <div class="profile-content">
      <!-- Section photo de profil -->
      <div class="profile-picture-section">
        <div class="picture-container">
          <img 
            v-if="previewUrl" 
            :src="previewUrl" 
            alt="Photo de profil" 
            class="profile-picture"
          />
          <div v-else class="profile-picture-placeholder">
            <span>{{ firstName.charAt(0) }}{{ lastName.charAt(0) }}</span>
          </div>
        </div>
        
        <div class="picture-actions">
          <label for="profilePicture" class="btn-upload-photo">
            <i class="upload-icon">📷</i>
            Changer la photo
          </label>
          <input 
            type="file" 
            id="profilePicture" 
            ref="fileInput"
            accept="image/*"
            @change="handleFileChange"
            class="file-input"
          />
          <button 
            v-if="selectedFile" 
            @click="uploadPicture" 
            class="btn-save-photo"
            :disabled="isLoading"
          >
            <span v-if="isLoading">Chargement...</span>
            <span v-else>Enregistrer la photo</span>
          </button>
        </div>
      </div>
      
      <!-- Formulaire d'information du profil -->
      <form @submit.prevent="updateProfile" class="profile-form">
        <div class="form-row">
          <div class="form-group">
            <label for="firstName">Prénom</label>
            <input 
              type="text" 
              id="firstName" 
              v-model="firstName" 
              class="form-control" 
              required
            />
          </div>
          
          <div class="form-group">
            <label for="lastName">Nom</label>
            <input 
              type="text" 
              id="lastName" 
              v-model="lastName" 
              class="form-control" 
              required
            />
          </div>
        </div>
        
        <div class="form-group">
          <label for="email">Email</label>
          <input 
            type="email" 
            id="email" 
            v-model="email" 
            class="form-control" 
            required
          />
        </div>
        
        <div class="form-row">
          <div class="form-group">
            <label for="phoneNumber">Téléphone</label>
            <input 
              type="tel" 
              id="phoneNumber" 
              v-model="phoneNumber" 
              class="form-control"
            />
          </div>
          
          <div class="form-group">
            <label for="birthDate">Date de naissance</label>
            <input 
              type="date" 
              id="birthDate" 
              v-model="birthDate" 
              class="form-control"
            />
          </div>
        </div>
        
        <div class="form-group">
          <label for="bio">Biographie</label>
          <textarea 
            id="bio" 
            v-model="bio" 
            class="form-control" 
            rows="3"
          ></textarea>
        </div>
        
        <h3 class="password-section-title">Changer le mot de passe</h3>
        
        <div class="form-group">
          <label for="newPassword">Nouveau mot de passe</label>
          <input 
            type="password" 
            id="newPassword" 
            v-model="newPassword" 
            class="form-control"
          />
        </div>
        
        <div class="form-group">
          <label for="confirmPassword">Confirmer le mot de passe</label>
          <input 
            type="password" 
            id="confirmPassword" 
            v-model="confirmPassword" 
            class="form-control"
          />
        </div>
        
        <button 
          type="submit" 
          class="btn btn-primary save-button"
          :disabled="isLoading"
        >
          <span v-if="isLoading">Chargement...</span>
          <span v-else>Enregistrer les modifications</span>
        </button>
      </form>
    </div>
  </div>
</template>

<style scoped>
.profile-container {
  max-width: 800px;
  margin: 2rem auto;
  padding: 2rem;
  background-color: white;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.profile-title {
  text-align: center;
  margin-bottom: 2rem;
  color: #2c3e50;
  font-weight: 600;
}

.profile-content {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.profile-picture-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}

.picture-container {
  width: 150px;
  height: 150px;
  border-radius: 50%;
  overflow: hidden;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.profile-picture {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.profile-picture-placeholder {
  width: 100%;
  height: 100%;
  background-color: #007bff;
  color: white;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 3rem;
  font-weight: 500;
}

.picture-actions {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.file-input {
  display: none;
}

.btn-upload-photo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 16px;
  background-color: #3498db;
  color: white;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: background-color 0.3s ease, transform 0.2s ease;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
  border: none;
  margin-bottom: 10px;
}

.btn-upload-photo:hover {
  background-color: #2980b9;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.upload-icon {
  font-size: 1.2rem;
}

.btn-save-photo {
  padding: 10px 16px;
  background-color: #2ecc71;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
  width: 100%;
  max-width: 200px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.btn-save-photo:hover {
  background-color: #27ae60;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.btn-save-photo:disabled {
  background-color: #95a5a6;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.profile-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.form-row {
  display: flex;
  gap: 1rem;
}

.form-group {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-control {
  padding: 0.75rem;
  border: 1px solid #ced4da;
  border-radius: 4px;
  font-size: 1rem;
}

.password-section-title {
  margin-top: 1rem;
  font-size: 1.25rem;
  color: #2c3e50;
  font-weight: 600;
}

.save-button {
  margin-top: 1.5rem;
  padding: 0.75rem 1.5rem;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
  transition: background-color 0.2s;
}

.save-button:hover {
  background-color: #0069d9;
}

.save-button:disabled {
  background-color: #6c757d;
  cursor: not-allowed;
}

.alert {
  padding: 1rem;
  border-radius: 4px;
  margin-bottom: 1.5rem;
}

.alert-success {
  background-color: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.alert-danger {
  background-color: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

@media (max-width: 576px) {
  .form-row {
    flex-direction: column;
  }
}
</style>
