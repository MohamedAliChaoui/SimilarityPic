<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { auth } from '../auth';

const router = useRouter();
const isRegisterMode = ref(false);
const registrationSuccess = ref(false);

// Champs de base
const username = ref('');
const password = ref('');

// Champs supplémentaires pour l'inscription
const email = ref('');
const firstName = ref('');
const lastName = ref('');
const birthDate = ref('');
const phoneNumber = ref('');

const errorMessage = ref('');
const successMessage = ref('');
const loading = ref(false);

// Vérifier si l'utilisateur est déjà connecté
onMounted(() => {
  if (auth.isLoggedIn()) {
    router.push('/');
  }
});

// Basculer entre connexion et inscription
const toggleMode = () => {
  isRegisterMode.value = !isRegisterMode.value;
  registrationSuccess.value = false;
  successMessage.value = '';
  errorMessage.value = '';
};

// Réinitialiser les champs du formulaire
const resetForm = () => {
  username.value = '';
  password.value = '';
  email.value = '';
  firstName.value = '';
  lastName.value = '';
  birthDate.value = '';
  phoneNumber.value = '';
};

// Valider les champs du formulaire d'inscription
const validateRegistration = () => {
  if (!username.value || !password.value || !email.value || !firstName.value || !lastName.value || !birthDate.value || !phoneNumber.value) {
    errorMessage.value = 'Veuillez remplir tous les champs obligatoires';
    return false;
  }
  
  // Validation email basique
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(email.value)) {
    errorMessage.value = 'Veuillez entrer une adresse email valide';
    return false;
  }
  
  // Validation numéro de téléphone
  const phoneRegex = /^[0-9]{10}$/;
  if (!phoneRegex.test(phoneNumber.value)) {
    errorMessage.value = 'Le numéro de téléphone doit contenir 10 chiffres';
    return false;
  }
  
  return true;
};

// Gérer la soumission du formulaire
const handleSubmit = async () => {
  errorMessage.value = '';
  successMessage.value = '';
  
  if (isRegisterMode.value) {
    // Mode inscription - validation complète
    if (!validateRegistration()) {
      return;
    }
  } else {
    // Mode connexion - validation de base
    if (!username.value || !password.value) {
      errorMessage.value = 'Veuillez remplir tous les champs';
      return;
    }
  }
  
  loading.value = true;
  
  try {
    if (isRegisterMode.value) {
      // Préparation de l'objet d'inscription avec tous les champs
      const registerRequest = {
        username: username.value,
        password: password.value,
        email: email.value,
        firstName: firstName.value,
        lastName: lastName.value,
        birthDate: birthDate.value,
        phoneNumber: phoneNumber.value,
        bio: ''
      };
      
      // Appel API d'inscription avec tous les champs
      const response = await auth.register(registerRequest);
      
      // Afficher le message de succès et réinitialiser
      successMessage.value = response;
      registrationSuccess.value = true;
      resetForm();
      isRegisterMode.value = false; // Retour au mode connexion
    } else {
      // Connexion standard
      await auth.login(username.value, password.value);
      router.push('/'); // Redirection vers la page d'accueil
    }
  } catch (error: any) {
    errorMessage.value = error.response?.data || 'Une erreur est survenue';
  } finally {
    loading.value = false;
  }
};
</script>

<template>
  <div class="login-container">
    <div class="login-card">
      <h2>{{ isRegisterMode ? 'Créer un compte' : 'Connexion' }}</h2>
      
      <!-- Message de succès après création de compte -->
      <div v-if="registrationSuccess && successMessage" class="success-message">
        {{ successMessage }}
      </div>
      
      <form @submit.prevent="handleSubmit" class="login-form">
        <div class="form-group">
          <label for="username">Nom d'utilisateur</label>
          <input 
            id="username"
            v-model="username"
            type="text" 
            placeholder="Entrez votre nom d'utilisateur"
            autocomplete="username"
          />
        </div>
        
        <div class="form-group">
          <label for="password">Mot de passe</label>
          <input 
            id="password"
            v-model="password" 
            type="password" 
            placeholder="Entrez votre mot de passe"
            autocomplete="current-password"
          />
        </div>
        
        <!-- Champs supplémentaires pour l'inscription -->
        <template v-if="isRegisterMode">
          <div class="form-group">
            <label for="email">Email</label>
            <input 
              id="email"
              v-model="email" 
              type="email" 
              placeholder="Entrez votre email"
              autocomplete="email"
            />
          </div>
          
          <div class="form-row">
            <div class="form-group">
              <label for="firstName">Prénom</label>
              <input 
                id="firstName"
                v-model="firstName" 
                type="text" 
                placeholder="Prénom"
                autocomplete="given-name"
              />
            </div>
            
            <div class="form-group">
              <label for="lastName">Nom</label>
              <input 
                id="lastName"
                v-model="lastName" 
                type="text" 
                placeholder="Nom"
                autocomplete="family-name"
              />
            </div>
          </div>
          
          <div class="form-group">
            <label for="birthDate">Date de naissance</label>
            <input 
              id="birthDate"
              v-model="birthDate" 
              type="date" 
              autocomplete="bday"
            />
          </div>
          
          <div class="form-group">
            <label for="phoneNumber">Téléphone</label>
            <input 
              id="phoneNumber"
              v-model="phoneNumber" 
              type="tel" 
              placeholder="Numéro de téléphone (10 chiffres)"
              autocomplete="tel"
            />
          </div>
        </template>
        
        <div v-if="errorMessage" class="error-message">
          {{ errorMessage }}
        </div>
        
        <button 
          type="submit" 
          class="submit-button"
          :disabled="loading"
        >
          {{ isRegisterMode ? 'S\'inscrire' : 'Se connecter' }}
          <span v-if="loading" class="spinner-small"></span>
        </button>
      </form>
      
      <div class="toggle-mode">
        <span v-if="isRegisterMode">Déjà inscrit ?</span>
        <span v-else>Pas encore de compte ?</span>
        <button @click="toggleMode" class="toggle-button">
          {{ isRegisterMode ? 'Se connecter' : 'Créer un compte' }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Fond avec gradient moderne */
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4ecfb 100%);
  overflow: hidden;
  position: relative;
}

/* Animation de fond pour l'ambiance */
.login-container::before {
  content: "";
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, rgba(255,255,255,0) 70%);
  transform: rotate(0deg);
  animation: bgShift 20s infinite linear;
  z-index: 0;
  opacity: 0.6;
}

@keyframes bgShift {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* Carte de connexion avec effet de profondeur */
.login-card {
  background-color: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1), 
              0 1px 8px rgba(0, 0, 0, 0.05);
  width: 100%;
  max-width: 400px;
  padding: 36px;
  position: relative;
  z-index: 1;
  transition: all 0.5s cubic-bezier(0.23, 1, 0.32, 1);
  overflow: hidden;
  backdrop-filter: blur(10px);
  transform-origin: center;
  transform: perspective(1000px) rotateX(0deg);
}

.login-card:hover {
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.15), 
              0 3px 10px rgba(0, 0, 0, 0.1);
  transform: perspective(1000px) rotateX(2deg);
}

/* Animation du titre lors du changement de mode */
h2 {
  text-align: center;
  margin-bottom: 28px;
  color: #2d3748;
  position: relative;
  font-weight: 700;
  letter-spacing: -0.5px;
  transition: transform 0.4s ease, opacity 0.3s ease;
}

h2::after {
  content: '';
  position: absolute;
  bottom: -8px;
  left: 50%;
  width: 40px;
  height: 3px;
  background: linear-gradient(to right, #0062cc, #007bff);
  border-radius: 3px;
  transform: translateX(-50%);
  transition: width 0.3s ease-in-out;
}

h2:hover::after {
  width: 100px;
}

/* Formulaire avec animation de défilement */
.login-form {
  display: flex;
  flex-direction: column;
  gap: 18px;
  margin-bottom: 24px;
}

/* Disposition en ligne pour prénom et nom */
.form-row {
  display: flex;
  gap: 12px;
  width: 100%;
}

.form-row .form-group {
  flex: 1;
  width: 100%;
  min-width: 0; /* Correction pour éviter le débordement */
}

.form-group {
  display: flex;
  flex-direction: column;
  transition: transform 0.3s ease, opacity 0.3s ease;
}

label {
  font-weight: 600;
  margin-bottom: 10px;
  font-size: 14px;
  color: #4a5568;
  transition: color 0.3s ease;
}

/* Champs de saisie améliorés */
input {
  width: 100%;
  padding: 12px;
  border-radius: 8px;
  border: 1px solid #cbd5e0;
  background-color: #f8fafc;
  font-size: 16px;
  transition: all 0.3s ease;
  box-sizing: border-box; /* Empêche les inputs de dépasser */
  overflow: hidden;
  text-overflow: ellipsis;
}

input:hover {
  border-color: #cbd5e0;
  background-color: #fff;
}

input:focus {
  border-color: #3182ce;
  outline: none;
  box-shadow: 0 0 0 3px rgba(66, 153, 225, 0.15);
  background-color: #fff;
  transform: translateY(-1px);
}

/* Bouton de soumission avec effet de pulsation */
.submit-button {
  background: linear-gradient(to right, #0062cc, #0069d9);
  color: white;
  border: none;
  padding: 14px;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  justify-content: center;
  align-items: center;
  letter-spacing: 0.5px;
  box-shadow: 0 4px 6px rgba(50, 50, 93, 0.11), 0 1px 3px rgba(0, 0, 0, 0.08);
  position: relative;
  overflow: hidden;
}

.submit-button::before {
  content: "";
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, rgba(255,255,255,0) 0%, rgba(255,255,255,0.2) 50%, rgba(255,255,255,0) 100%);
  transition: left 0.5s ease-in-out;
}

.submit-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 7px 14px rgba(50, 50, 93, 0.1), 0 3px 6px rgba(0, 0, 0, 0.08);
  background: linear-gradient(to right, #0051a9, #0062cc);
}

.submit-button:hover::before {
  left: 100%;
}

.submit-button:disabled {
  background: #cbd5e0;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

/* Section de basculement avec animation */
.toggle-mode {
  margin-top: 28px;
  text-align: center;
  color: #718096;
  transition: opacity 0.3s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.toggle-button {
  background: none;
  border: none;
  color: #3182ce;
  cursor: pointer;
  font-weight: 600;
  padding: 8px 16px;
  border-radius: 6px;
  transition: all 0.3s ease;
  position: relative;
}

.toggle-button::after {
  content: '';
  position: absolute;
  bottom: 5px;
  left: 50%;
  width: 0;
  height: 2px;
  background-color: #3182ce;
  transition: width 0.3s ease, left 0.3s ease;
  transform: translateX(-50%);
}

.toggle-button:hover {
  color: #2c5282;
  background-color: rgba(49, 130, 206, 0.05);
}

.toggle-button:hover::after {
  width: calc(100% - 30px);
}

.toggle-button:focus {
  outline: none;
}

/* Message d'erreur avec animation */
.error-message {
  color: #e53e3e;
  font-size: 14px;
  padding: 10px;
  border-radius: 6px;
  background-color: rgba(229, 62, 62, 0.1);
  border-left: 3px solid #e53e3e;
  animation: shakeError 0.5s ease-in-out;
}

@keyframes shakeError {
  0%, 100% { transform: translateX(0); }
  20%, 60% { transform: translateX(-5px); }
  40%, 80% { transform: translateX(5px); }
}

/* Animation de succès */
.success-message {
  background-color: #d4edda;
  border-color: #c3e6cb;
  color: #155724;
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 16px;
  text-align: center;
  position: relative;
  animation: successPulse 2s infinite;
}

@keyframes successPulse {
  0% { box-shadow: 0 0 0 0 rgba(21, 87, 36, 0.4); }
  70% { box-shadow: 0 0 0 10px rgba(21, 87, 36, 0); }
  100% { box-shadow: 0 0 0 0 rgba(21, 87, 36, 0); }
}

/* Spinner amélioré */
.spinner-small {
  display: inline-block;
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  margin-left: 10px;
  animation: spin 0.8s ease-in-out infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* Animation globale pour transition entre modes */
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes fadeOut {
  from { opacity: 1; transform: translateY(0); }
  to { opacity: 0; transform: translateY(-20px); }
}

/* Media queries pour responsive */
@media (max-width: 480px) {
  .login-card {
    padding: 24px;
    border-radius: 12px;
  }
  
  input, .submit-button {
    padding: 12px;
  }
  
  h2 {
    font-size: 20px;
  }
}
</style>
