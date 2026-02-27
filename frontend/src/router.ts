import { createWebHistory, createRouter } from "vue-router";
import type { RouteRecordRaw } from "vue-router";
import ImageEditor from './components/ImageEditor.vue';
import { auth } from './auth';

const routes: Array<RouteRecordRaw> = [
  {
    path: "/feed",
    name: "feed",
    component: () => import("./components/Feed.vue"),
    props: true,
    meta: { requiresAuth: true }
  },
  {
    path: "/",
    name: "home",
    component: () => import("./components/Home.vue"),
    props: true,
    meta: { requiresAuth: true }
  },
  {
    path: "/gallery",
    name: "gallery",
    component: () => import("./components/Gallery.vue"),
    props: true,
    meta: { requiresAuth: true }
  },
  {
    path: "/image/:id",
    name: "image",
    component: () => import("./components/Image.vue"),
    props: ({ params }) => ({ id: Number(params.id) || 0 }),
    meta: { requiresAuth: true }
  },
  {
    path: "/upload",
    name: "upload",
    component: () => import("./components/Upload.vue"),
    props: true,
    meta: { requiresAuth: true }
  },
  {
    path: '/edit/:id',
    name: 'edit',
    component: ImageEditor,
    props: route => ({ id: Number(route.params.id) || 0 }),
    meta: { requiresAuth: true }
  },
  {
    path: '/login',
    name: 'login',
    component: () => import("./components/Login.vue"),
  },
  {
    path: '/profile',
    name: 'profile',
    component: () => import("./components/Profile.vue"),
    meta: { requiresAuth: true }
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

// Navigation guard pour vérifier l'authentification
router.beforeEach((to, _from, next) => {
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth);
  const isLoggedIn = auth.isLoggedIn();

  if (requiresAuth && !isLoggedIn) {
    // Rediriger vers la page de login si la route nécessite une authentification
    // et que l'utilisateur n'est pas connecté
    next('/login');
  } else if (to.path === '/login' && isLoggedIn) {
    // Si l'utilisateur est déjà connecté et essaie d'accéder à la page de login,
    // le rediriger vers la page d'accueil
    next('/');
  } else {
    next(); // Continuer normalement
  }
});

export default router;