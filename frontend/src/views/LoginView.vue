<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { http, mensajeError } from '../api/http'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const ruta = useRoute()
const auth = useAuthStore()

const username = ref('')
const password = ref('')
const cargando = ref(false)
const error = ref('')

async function entrar() {
  error.value = ''
  cargando.value = true
  try {
    const { data } = await http.post('/api/auth/login', {
      username: username.value,
      password: password.value,
    })
    auth.establecerSesion(data.token, data.usuario)
    router.push(ruta.query.siguiente || '/catalogo')
  } catch (e) {
    error.value = mensajeError(e)
  } finally {
    cargando.value = false
  }
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-slate-900 p-4">
    <div class="w-full max-w-sm bg-white rounded-2xl shadow-2xl p-8">
      <div class="text-center mb-8">
        <p class="text-2xl font-bold tracking-tight text-slate-900">
          Finanzas<span class="text-emerald-600">SV</span>
        </p>
        <p class="text-sm text-slate-500 mt-1">Módulo de Contabilidad Automatizado</p>
      </div>

      <form @submit.prevent="entrar" class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-slate-700 mb-1">Usuario</label>
          <input
            v-model="username"
            type="text"
            required
            autocomplete="username"
            placeholder="admin"
            class="w-full rounded-lg border border-slate-300 px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500"
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-slate-700 mb-1">Contraseña</label>
          <input
            v-model="password"
            type="password"
            required
            autocomplete="current-password"
            placeholder="••••••••"
            class="w-full rounded-lg border border-slate-300 px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500"
          />
        </div>

        <p v-if="error" class="rounded-lg bg-red-50 border border-red-200 text-red-700 text-sm px-3 py-2">
          {{ error }}
        </p>

        <button
          type="submit"
          :disabled="cargando"
          class="w-full rounded-lg bg-emerald-600 hover:bg-emerald-700 disabled:opacity-50 text-white font-semibold py-2.5 transition"
        >
          {{ cargando ? 'Ingresando...' : 'Iniciar sesión' }}
        </button>
      </form>

      <div class="mt-6 text-xs text-slate-400 text-center">
        admin / admin123 · contador / contador123 · consulta / consulta123
      </div>
    </div>
  </div>
</template>
