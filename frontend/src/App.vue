<script setup lang="ts">
import { computed } from 'vue';
import Header from './components/Header.vue';
import { authState } from './auth';

// Utiliser l'état d'authentification réactif partagé
const isLoggedIn = computed(() => {
  return authState.isAuthenticated.value;
});
</script>

<template>
  <div id="app">
    <!-- Utilisation du composant Header pour la navigation et l'affichage du nom d'utilisateur -->
    <Header />
    
    <!-- Menu de navigation visible uniquement pour les utilisateurs connectés -->
    <nav v-if="isLoggedIn">
      <div class="nav-container">
        <ul>
          <li>
            <router-link to="/feed">📰 Fil d'actualité</router-link>
          </li>
          <li>
            <router-link to="/">🔍 Recherche</router-link>
          </li>
          <li>
            <router-link to="/gallery">🖼️ Gallery</router-link>
          </li>
          <li>
            <router-link to="/upload">📤 Upload</router-link>
          </li>
        </ul>
      </div>
    </nav>



    <!-- Zone principale pour afficher les différentes vues -->
    <div class="container">
      <router-view />
    </div>
  </div>
</template>

<style>
body {
  margin: 0;
  padding: 0;
}

#app {
  font-family: 'Poppins', 'Segoe UI', Arial, sans-serif;
  color: #2c3e50;
}

nav {
  background: linear-gradient(to right, #0062cc, #007bff);
  padding: 12px 0;
  box-shadow: 0 4px 12px rgba(0, 123, 255, 0.2);
  margin-bottom: 30px;
  width: 100%;
  left: 0;
  right: 0;
}

.nav-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

ul {
  list-style-type: none;
  margin: 0;
  padding: 0;
  display: flex;
  justify-content: center;
}

li {
  margin: 0 5px;
}

li a {
  display: block;
  color: white;
  text-align: center;
  padding: 12px 24px;
  text-decoration: none;
  font-weight: 500;
  border-radius: 6px;
  transition: all 0.3s ease;
  letter-spacing: 0.5px;
}

li a:hover {
  background-color: rgba(255, 255, 255, 0.2);
  transform: translateY(-3px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

li a.router-link-active {
  background-color: rgba(255, 255, 255, 0.3);
  font-weight: 600;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.05);
}

h1 {
  margin: 20px 0 40px;
  font-size: 38px;
  color: #333;
  font-weight: 700;
  letter-spacing: -0.5px;
  position: relative;
  display: inline-block;
}

h1:after {
  content: '';
  position: absolute;
  bottom: -8px;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 4px;
  background: linear-gradient(to right, #0062cc, #007bff);
  border-radius: 2px;
  text-align: center;
}

.error {
  color: #dc3545;
  font-weight: 600;
  list-style-type: none;
  margin-top: 16px;
  padding: 12px;
  background-color: rgba(220, 53, 69, 0.1);
  border-radius: 6px;
  border-left: 4px solid #dc3545;
}

@media (max-width: 768px) {
  ul {
    flex-direction: column;
  }
  
  li {
    margin: 5px 0;
  }
  
  h1 {
    font-size: 32px;
  }
}
</style>