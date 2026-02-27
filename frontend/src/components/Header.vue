<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { auth, authState } from '../auth';

const router = useRouter();

// Utiliser l'état d'authentification réactif
const username = computed(() => {
  return authState.user.value?.username || '';
});

const isLoggedIn = computed(() => {
  return authState.isAuthenticated.value;
});

const initials = computed(() => {
  const firstName = authState.user.value?.firstName || '';
  const lastName = authState.user.value?.lastName || '';
  return (firstName.charAt(0) + lastName.charAt(0)).toUpperCase();
});

const goToProfile = () => {
  router.push('/profile');
};

const logout = async () => {
  await auth.logout();
  router.push('/login');
};
</script>

<template>
  <header class="app-header">
    <div class="header-content">
      <!-- Afficher "Galerie d'images" uniquement si l'utilisateur n'est pas connecté -->
      <h1 v-if="!isLoggedIn" class="app-title">
        <router-link to="/">Galerie d'images</router-link>
      </h1>
      
      <!-- Afficher le message de bienvenue avec animation si l'utilisateur est connecté -->
      <h1 v-if="isLoggedIn" class="app-title welcome-title">
        <router-link to="/">
          <span class="welcome-prefix">Bonjour</span>
          <span class="username-highlight">{{ username }}</span>
          <!-- Emoji main qui wave, juste après le username -->
          <span class="welcome-emoji">👋</span>
        </router-link>
      </h1>
      
      <!-- Avatar de profil si l'utilisateur est connecté -->
      <div v-if="isLoggedIn" class="profile-avatar" @click="goToProfile">
        <div class="avatar-placeholder">
          <span>{{ initials }}</span>
        </div>
        <div class="profile-tooltip">Mon profil</div>
      </div>
      
      <!-- Section utilisateur avec bouton déconnexion -->
      <div v-if="isLoggedIn" class="user-section">
        <button @click="logout" class="logout-button">
          <span class="logout-icon">🚪</span>
          Se déconnecter
        </button>
      </div>
    </div>
  </header>
</template>

<style scoped>
/* Styles pour l'avatar de profil */
.profile-avatar {
  position: relative;
  cursor: pointer;
  margin: 0 16px;
  transition: transform 0.2s ease;
}

.profile-avatar:hover {
  transform: scale(1.05);
}

.profile-avatar:hover .profile-tooltip {
  opacity: 1;
  transform: translateY(0);
}

.avatar-placeholder {
  width: 35px;
  height: 35px;
  border-radius: 50%;
  background: linear-gradient(135deg, #007bff, #0056b3);
  color: white;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 14px;
  font-weight: 600;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.avatar-placeholder:hover {
  box-shadow: 0 3px 6px rgba(0, 0, 0, 0.15);
  transform: translateY(-1px);
}

.profile-tooltip {
  position: absolute;
  bottom: -30px;
  left: 50%;
  transform: translateX(-50%) translateY(5px);
  background-color: #333;
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  opacity: 0;
  transition: all 0.3s ease;
  white-space: nowrap;
  pointer-events: none;
  z-index: 100;
}

.profile-tooltip:before {
  content: '';
  position: absolute;
  top: -5px;
  left: 50%;
  transform: translateX(-50%);
  border-left: 5px solid transparent;
  border-right: 5px solid transparent;
  border-bottom: 5px solid #333;
}

.app-header {
  background: linear-gradient(to right, #ffffff, #f8f9fa);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  padding: 16px 0;
  position: sticky;
  top: 0;
  z-index: 100;
  border-bottom: 1px solid rgba(0, 123, 255, 0.1);
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.app-title {
  margin: 0;
  font-size: 22px;
  position: relative;
}

.app-title a {
  color: #333;
  text-decoration: none;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
}

.app-title a:hover {
  color: #007bff;
}

/* Styles spécifiques au titre de bienvenue */
.welcome-title a {
  background: linear-gradient(to right, #2d3748, #4a5568);
  background-clip: text;
  -webkit-background-clip: text;
  color: transparent;
  position: relative;
  font-weight: 700;
}

/* Suppression complète de l'effet de ligne sous le nom d'utilisateur */

.welcome-prefix {
  font-weight: 500;
  margin-right: 6px;
  animation: fadeIn 0.8s ease-out;
}

.username-highlight {
  color: #007bff;
  font-weight: 700;
  position: relative;
  padding: 0 4px;
  animation: popIn 0.5s ease-out 0.2s both;
}

.username-highlight::before {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 6px;
  background-color: rgba(0, 123, 255, 0.2);
  z-index: -1;
  border-radius: 3px;
}

.welcome-emoji {
  font-size: 1.3em;
  margin-left: 6px;
  display: inline-block !important;
  animation: waveHand 1.5s ease-in-out infinite;
  transform-origin: 70% 70%;
  opacity: 1 !important;
  visibility: visible !important;
  position: relative;
  z-index: 10; /* S'assurer qu'il est au premier plan */
  color: #007bff; /* Couleur bleue distincte */
  text-shadow: 0 0 2px rgba(0,0,0,0.1); /* Léger effet d'ombre */
}

@keyframes waveHand {
  0% { transform: rotate(0deg); }
  10% { transform: rotate(14deg); }
  20% { transform: rotate(-8deg); }
  30% { transform: rotate(14deg); }
  40% { transform: rotate(-4deg); }
  50% { transform: rotate(10deg); }
  60% { transform: rotate(0deg); }
  100% { transform: rotate(0deg); }
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes popIn {
  0% { opacity: 0; transform: scale(0.8); }
  70% { opacity: 1; transform: scale(1.1); }
  100% { opacity: 1; transform: scale(1); }
}

.user-section {
  display: flex;
  align-items: center;
  gap: 16px;
  animation: slideIn 0.5s ease-out;
}

@keyframes slideIn {
  from { opacity: 0; transform: translateX(20px); }
  to { opacity: 1; transform: translateX(0); }
}

.logout-button {
  background: #f8f9fa;
  border: 1px solid #e2e8f0;
  color: #4a5568;
  padding: 10px 18px;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.logout-button:hover {
  background-color: #f1f3f5;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.logout-button:active {
  transform: translateY(0);
}

.logout-icon {
  font-size: 1.1em;
  transition: transform 0.3s ease;
}

.logout-button:hover .logout-icon {
  transform: translateX(3px);
}

/* Media queries pour responsive */
@media (max-width: 640px) {
  .app-title {
    font-size: 18px;
  }
  
  .logout-button {
    padding: 8px 12px;
    font-size: 14px;
  }
}
</style>
