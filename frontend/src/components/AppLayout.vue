<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()

const enlaces = [
  { nombre: 'Catálogo de Cuentas', ruta: '/catalogo', icono: '📁', roles: ['ADMIN', 'CONTADOR', 'CONSULTA'] },
  { nombre: 'Libro Diario', ruta: '/diario', icono: '📒', roles: ['ADMIN', 'CONTADOR', 'CONSULTA'] },
  { nombre: 'Libro Mayor', ruta: '/mayor', icono: '📊', roles: ['ADMIN', 'CONTADOR', 'CONSULTA'] },
  { nombre: 'Balance General', ruta: '/balance-general', icono: '⚖️', roles: ['ADMIN', 'CONTADOR', 'CONSULTA'] },
  { nombre: 'Estado de Resultados', ruta: '/estado-resultados', icono: '📈', roles: ['ADMIN', 'CONTADOR', 'CONSULTA'] },
  { nombre: 'Usuarios', ruta: '/usuarios', icono: '👥', roles: ['ADMIN'] },
]

const visibles = enlaces.filter((e) => e.roles.includes(auth.rol))

function salir() {
  auth.cerrarSesion()
  router.push('/login')
}
</script>

<template>
  <div class="flex min-h-screen">
    <!-- Barra lateral -->
    <aside class="w-64 shrink-0 bg-slate-900 text-slate-100 flex flex-col">
      <div class="px-5 py-6 border-b border-slate-700">
        <p class="text-lg font-bold tracking-tight">Finanzas<span class="text-emerald-400">SV</span></p>
        <p class="text-xs text-slate-400 mt-1">Módulo de Contabilidad</p>
      </div>
      <nav class="flex-1 px-3 py-4 space-y-1">
        <router-link
          v-for="enlace in visibles"
          :key="enlace.ruta"
          :to="enlace.ruta"
          class="flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium text-slate-300 hover:bg-slate-800 hover:text-white transition"
          active-class="bg-emerald-600/20 text-white border-l-2 border-emerald-400"
        >
          <span>{{ enlace.icono }}</span>
          {{ enlace.nombre }}
        </router-link>
      </nav>
      <div class="px-4 py-4 border-t border-slate-700">
        <div class="flex items-center gap-3">
          <div class="w-9 h-9 rounded-full bg-emerald-600 flex items-center justify-center font-bold">
            {{ auth.inicial }}
          </div>
          <div class="flex-1 min-w-0">
            <p class="text-sm font-semibold truncate">{{ auth.usuario?.nombreCompleto }}</p>
            <p class="text-xs text-slate-400">{{ auth.rol }}</p>
          </div>
        </div>
        <button
          @click="salir"
          class="mt-3 w-full rounded-lg bg-slate-800 hover:bg-red-600 px-3 py-2 text-sm font-medium transition"
        >
          Cerrar sesión
        </button>
      </div>
    </aside>

    <!-- Contenido -->
    <main class="flex-1 overflow-y-auto">
      <router-view />
    </main>
  </div>
</template>
