import axios, { type AxiosResponse } from 'axios';
import type { ImageType } from './image';

// Interfaces manquantes
export interface UserSearchResult {
  id: number;
  username: string;
  firstName?: string;
  lastName?: string;
  email?: string;
  hasProfilePicture?: boolean;
}

export interface ImageSearchParams {
  name?: string;
  minSize?: number;
  maxSize?: number;
  format?: string;
}

const instance = axios.create({
  baseURL: import.meta.env.VITE_API_URL || "/",
  timeout: 60000,
});

// Ajout de l'intercepteur pour inclure le token dans toutes les requêtes
instance.interceptors.request.use((config) => {
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  if (user && user.token && config.headers) {
    config.headers.Authorization = `Bearer ${user.token}`;
  }
  return config;
});

const responseBody = (response: AxiosResponse) => response.data;

const requests = {
  get: (url: string, config = {}) => {
    if (config && (config as any).responseType === "blob") {
      return instance.get(url, { ...config, responseType: "blob" }).then(response => response.data);
    }
    return instance.get(url, config).then(responseBody);
  },
  post: (url: string, body: unknown, config = {}) =>
    instance.post(url, body, {
      ...config,
      headers: {
        "Content-Type": "multipart/form-data",
        ...(config as { headers?: Record<string, string> })?.headers
      }
    }).then(responseBody),
  put: (url: string, body: unknown = {}, config = {}) =>
    instance.put(url, body, config).then(responseBody),
  delete: (url: string) => instance.delete(url).then(responseBody)
};

export interface SimilarImageType extends ImageType {
  similarity: number;
}

export const api = {
  toggleFavorite: (id: number): Promise<void> =>
    requests.put(`images/${id}/favorite`),

  toggleSave: (id: number): Promise<void> =>
    requests.put(`images/${id}/save`),

  togglePublish: (id: number): Promise<void> =>
    requests.put(`images/${id}/publish`),

  getFavorites: (): Promise<ImageType[]> =>
    requests.get('images/favorites'),

  getImageList: (): Promise<ImageType[]> =>
    requests.get('images'),

  getImage: (id: number): Promise<Blob> =>
    requests.get(`images/${id}`, {
      responseType: "blob"
    }),

  addImage: (formData: FormData): Promise<number> => {
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    if (!user || !user.token) {
      throw new Error('Non authentifié');
    }
    formData.append('token', user.token);
    return requests.post('images', formData);
  },

  deleteImage: (id: number): Promise<void> =>
    requests.delete(`images/${id}`),

  getFeed: (): Promise<ImageType[]> =>
    requests.get('images/feed'),

  getSimilarImages: (
    id: number,
    number: number = 1,
    descriptor: 'RGB3D' | 'HS' = 'RGB3D'
  ): Promise<SimilarImageType[]> =>
    requests.get(`images/${id}/similar`, {
      params: { number, descriptor }
    }),

  // Méthodes pour la recherche
  searchUsers: (query: string): Promise<UserSearchResult[]> =>
    requests.get('auth/users/search', {
      params: { query }
    }),

  searchImages: (params: ImageSearchParams): Promise<ImageType[]> =>
    requests.get('images/search', { params }),

  getMyGallery: () => requests.get('/images/my-gallery'),
  getSavedImages: (): Promise<ImageType[]> =>
    requests.get('images/saved'),
};