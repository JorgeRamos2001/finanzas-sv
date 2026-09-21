import axios from 'axios'

/**
 * Cliente HTTP central de la API del backend.
 * Agrega el token JWT y maneja expiraciones de sesion (401).
 */
export const http = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('finanzassv.token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (respuesta) => respuesta,
  (error) => {
    if (error.response?.status === 401 && !error.config?.url?.includes('/api/auth/login')) {
      localStorage.removeItem('finanzassv.token')
      localStorage.removeItem('finanzassv.usuario')
      if (!window.location.pathname.startsWith('/login')) {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  },
)

/** Extrae el mensaje legible de un ErrorResponse del backend. */
export function mensajeError(error) {
  return (
    error.response?.data?.mensaje ||
    error.response?.data?.error ||
    error.message ||
    'Error inesperado'
  )
}
