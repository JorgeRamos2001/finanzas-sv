import { defineStore } from 'pinia'

const CLAVE_TOKEN = 'finanzassv.token'
const CLAVE_USUARIO = 'finanzassv.usuario'

/**
 * Sesion del usuario: token JWT + perfil con rol.
 * Roles: ADMIN (todo), CONTADOR (asientos), CONSULTA (solo lectura).
 */
export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem(CLAVE_TOKEN) || '',
    usuario: JSON.parse(localStorage.getItem(CLAVE_USUARIO) || 'null'),
  }),
  getters: {
    estaAutenticado: (estado) => !!estado.token,
    rol: (estado) => estado.usuario?.rol || null,
    esAdmin: (estado) => estado.usuario?.rol === 'ADMIN',
    puedeRegistrar: (estado) =>
      ['ADMIN', 'CONTADOR'].includes(estado.usuario?.rol),
    inicial: (estado) => (estado.usuario?.nombreCompleto || '?').charAt(0).toUpperCase(),
  },
  actions: {
    establecerSesion(token, usuario) {
      this.token = token
      this.usuario = usuario
      localStorage.setItem(CLAVE_TOKEN, token)
      localStorage.setItem(CLAVE_USUARIO, JSON.stringify(usuario))
    },
    cerrarSesion() {
      this.token = ''
      this.usuario = null
      localStorage.removeItem(CLAVE_TOKEN)
      localStorage.removeItem(CLAVE_USUARIO)
    },
  },
})
