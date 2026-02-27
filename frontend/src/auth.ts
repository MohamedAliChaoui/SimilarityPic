import axios from 'axios';
import { ref, readonly } from 'vue';

// Types pour l'authentification
export interface User {
  id: number;
  username: string;
  token: string;
  email?: string;
  firstName?: string;
  lastName?: string;
  birthDate?: string;
  phoneNumber?: string;
  bio?: string;
  hasProfilePicture?: boolean;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  email: string;
  firstName: string;
  lastName: string;
  birthDate: string;
  phoneNumber: string;
  bio?: string;
}

// Clé pour le stockage local
const USER_STORAGE_KEY = 'user';

// État réactif de l'authentification
const currentUser = ref<User | null>(null);
const isAuthenticated = ref(false);

// Initialiser l'état d'authentification depuis localStorage
const storedUser = localStorage.getItem(USER_STORAGE_KEY);
if (storedUser) {
  try {
    currentUser.value = JSON.parse(storedUser);
    isAuthenticated.value = true;
  } catch (e) {
    localStorage.removeItem(USER_STORAGE_KEY);
  }
}

// Expose les états réactifs en lecture seule
export const authState = {
  user: readonly(currentUser),
  isAuthenticated: readonly(isAuthenticated)
};

export const auth = {
  // Mise à jour du profil utilisateur
  updateProfile: async (updateData: Partial<User>): Promise<User> => {
    const user = currentUser.value;
    if (!user) throw new Error('Utilisateur non connecté');
    
    const response = await axios.post('/auth/update-profile', {
      ...updateData,
      token: user.token
    });
    
    const updatedUser = { ...user, ...response.data };
    
    // Mettre à jour localStorage
    localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(updatedUser));
    
    // Mettre à jour l'état réactif
    currentUser.value = updatedUser;
    
    return updatedUser;
  },
  
  // Upload de la photo de profil
  uploadProfilePicture: async (formData: FormData): Promise<User> => {
    const user = currentUser.value;
    if (!user) throw new Error('Utilisateur non connecté');
    
    // S'assurer que nous avons bien un token valide
    if (!user.token) {
      throw new Error('Token de session invalide');
    }
    
    // Ajouter le token à formData
    formData.append('token', user.token);
    
    // Ajouter l'userId comme alternative d'authentification
    formData.append('userId', user.id.toString());
    
    console.log('Envoi de la photo de profil avec token:', user.token);
    console.log('Envoi de la photo de profil avec userId:', user.id);
    
    await axios.post('/auth/upload-profile-picture', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
    
    const updatedUser = { ...user, hasProfilePicture: true };
    
    // Mettre à jour localStorage
    localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(updatedUser));
    
    // Mettre à jour l'état réactif
    currentUser.value = updatedUser;
    
    return updatedUser;
  },
  
  // Récupérer l'URL de la photo de profil
  getProfilePictureUrl: (userId?: number): string => {
    const user = userId ? { id: userId } : currentUser.value;
    if (!user) return '';
    
    // Ajouter un timestamp pour éviter la mise en cache du navigateur
    return `/api/users/${user.id}/profile-picture?t=${new Date().getTime()}`;
  },
  // Connexion utilisateur
  login: async (username: string, password: string): Promise<User> => {
    const response = await axios.post('/auth/login', { username, password });
    const user = response.data;
    
    // Stocker les informations utilisateur dans localStorage
    localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(user));
    
    // Mettre à jour l'état réactif
    currentUser.value = user;
    isAuthenticated.value = true;
    
    return user;
  },
  
  // Inscription utilisateur
  register: async (request: RegisterRequest): Promise<any> => {
    // Pas besoin de stocker l'utilisateur dans le localStorage lors de l'inscription
    // car nous redirigeons vers la page de login
    const response = await axios.post('/auth/register', request);
    return response.data;
  },
  
  // Déconnexion
  logout: async (): Promise<void> => {
    const user = currentUser.value;
    if (user) {
      try {
        await axios.post('/auth/logout', { token: user.token });
      } catch (error) {
        console.error('Erreur lors de la déconnexion', error);
      }
    }
    
    // Supprimer l'utilisateur du localStorage
    localStorage.removeItem(USER_STORAGE_KEY);
    
    // Mettre à jour l'état réactif
    currentUser.value = null;
    isAuthenticated.value = false;
  },
  
  // Récupérer l'utilisateur courant 
  getCurrentUser: (): User | null => {
    return currentUser.value;
  },
  
  // Vérifier si l'utilisateur est connecté
  isLoggedIn: (): boolean => {
    return isAuthenticated.value;
  }
};
