import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const rutas = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/LoginView.vue'),
    meta: { publica: true },
  },
  {
    path: '/',
    component: () => import('../components/AppLayout.vue'),
    children: [
      { path: '', redirect: '/catalogo' },
      {
        path: 'catalogo',
        name: 'Catalogo',
        component: () => import('../views/CatalogoView.vue'),
      },
      {
        path: 'diario',
        name: 'Diario',
        component: () => import('../views/DiarioView.vue'),
      },
      {
        path: 'mayor',
        name: 'Mayor',
        component: () => import('../views/MayorView.vue'),
      },
      {
        path: 'balance-general',
        name: 'BalanceGeneral',
        component: () => import('../views/BalanceGeneralView.vue'),
      },
      {
        path: 'estado-resultados',
        name: 'EstadoResultados',
        component: () => import('../views/EstadoResultadosView.vue'),
      },
      {
        path: 'usuarios',
        name: 'Usuarios',
        component: () => import('../views/UsuariosView.vue'),
        meta: { rol: 'ADMIN' },
      },
    ],
  },
  { path: '/:pathMatch(.*)*', redirect: '/' },
]

const router = createRouter({
  history: createWebHistory(),
  routes: rutas,
})

/** Guard de navegacion: exige sesion y rol permitido. */
router.beforeEach((a) => {
  const auth = useAuthStore()
  if (a.meta.publica) return true
  if (!auth.estaAutenticado) return { name: 'Login', query: { siguiente: a.fullPath } }
  if (a.meta.rol && auth.rol !== a.meta.rol) return { name: 'Catalogo' }
  return true
})

export default router
