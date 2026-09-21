<script setup>
import { onMounted, ref } from 'vue'
import { http, mensajeError } from '../api/http'

const usuarios = ref([])
const cargando = ref(true)
const error = ref('')
const ok = ref('')
const enviando = ref(false)

const form = ref({ username: '', password: '', nombreCompleto: '', rol: 'CONSULTA' })

const roles = [
  { valor: 'ADMIN', descripcion: 'Acceso total: usuarios, catálogo y asientos' },
  { valor: 'CONTADOR', descripcion: 'Registra asientos y consulta reportes' },
  { valor: 'CONSULTA', descripcion: 'Solo consulta reportes' },
]

async function cargar() {
  cargando.value = true
  try {
    const { data } = await http.get('/api/usuarios')
    usuarios.value = data
  } catch (e) {
    error.value = mensajeError(e)
  } finally {
    cargando.value = false
  }
}

async function crear() {
  ok.value = ''
  error.value = ''
  enviando.value = true
  try {
    await http.post('/api/usuarios', form.value)
    ok.value = `Usuario "${form.value.username}" creado con rol ${form.value.rol}`
    form.value = { username: '', password: '', nombreCompleto: '', rol: 'CONSULTA' }
    await cargar()
  } catch (e) {
    error.value = mensajeError(e)
  } finally {
    enviando.value = false
  }
}

async function desactivar(usuario) {
  ok.value = ''
  error.value = ''
  try {
    await http.delete(`/api/usuarios/${usuario.id}`)
    ok.value = `Usuario "${usuario.username}" desactivado`
    await cargar()
  } catch (e) {
    error.value = mensajeError(e)
  }
}

onMounted(cargar)
</script>

<template>
  <div class="p-8 max-w-5xl mx-auto space-y-6">
    <div>
      <h1 class="text-2xl font-bold">Gestión de Usuarios</h1>
      <p class="text-sm text-slate-500 mt-1">
        Roles del sistema: ADMIN (acceso total), CONTADOR (asientos), CONSULTA (solo lectura)
      </p>
    </div>

    <p v-if="error" class="rounded-lg bg-red-50 border border-red-200 text-red-700 text-sm px-3 py-2">{{ error }}</p>
    <p v-if="ok" class="rounded-lg bg-emerald-50 border border-emerald-200 text-emerald-700 text-sm px-3 py-2">{{ ok }}</p>

    <!-- Formulario -->
    <form @submit.prevent="crear" class="bg-white rounded-xl border border-slate-200 p-5">
      <h2 class="font-semibold mb-4">Nuevo usuario</h2>
      <div class="grid grid-cols-1 md:grid-cols-5 gap-3 items-end">
        <div>
          <label class="block text-xs font-medium text-slate-500 mb-1">Usuario</label>
          <input v-model="form.username" required minlength="3" maxlength="50"
            class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500" />
        </div>
        <div>
          <label class="block text-xs font-medium text-slate-500 mb-1">Contraseña</label>
          <input v-model="form.password" required minlength="6" type="password"
            class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500" />
        </div>
        <div>
          <label class="block text-xs font-medium text-slate-500 mb-1">Nombre completo</label>
          <input v-model="form.nombreCompleto" required maxlength="120"
            class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500" />
        </div>
        <div>
          <label class="block text-xs font-medium text-slate-500 mb-1">Rol</label>
          <select v-model="form.rol"
            class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500">
            <option v-for="r in roles" :key="r.valor" :value="r.valor">{{ r.valor }}</option>
          </select>
        </div>
        <button type="submit" :disabled="enviando"
          class="rounded-lg bg-emerald-600 hover:bg-emerald-700 disabled:opacity-50 text-white text-sm font-semibold px-4 py-2.5 transition">
          {{ enviando ? 'Creando...' : 'Crear usuario' }}
        </button>
      </div>
    </form>

    <!-- Tabla -->
    <section class="bg-white rounded-xl border border-slate-200 overflow-hidden">
      <table class="w-full text-sm">
        <thead>
          <tr class="text-left text-[11px] uppercase tracking-wide text-slate-400 border-b border-slate-100">
            <th class="px-6 py-2">Usuario</th>
            <th class="px-4 py-2">Nombre completo</th>
            <th class="px-4 py-2">Rol</th>
            <th class="px-4 py-2">Estado</th>
            <th class="px-6 py-2 text-right">Acción</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-slate-100">
          <tr v-for="usuario in usuarios" :key="usuario.id" class="hover:bg-slate-50">
            <td class="px-6 py-3 font-medium">{{ usuario.username }}</td>
            <td class="px-4 py-3">{{ usuario.nombreCompleto }}</td>
            <td class="px-4 py-3">
              <span class="text-[10px] uppercase rounded-full px-2 py-0.5 font-semibold"
                :class="usuario.rol === 'ADMIN' ? 'bg-purple-100 text-purple-700'
                  : usuario.rol === 'CONTADOR' ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-100 text-slate-600'">
                {{ usuario.rol }}
              </span>
            </td>
            <td class="px-4 py-3">
              <span :class="usuario.activo ? 'text-emerald-600' : 'text-slate-400'" class="text-xs font-medium">
                {{ usuario.activo ? '● Activo' : '● Desactivado' }}
              </span>
            </td>
            <td class="px-6 py-3 text-right">
              <button v-if="usuario.activo" @click="desactivar(usuario)"
                class="text-red-600 hover:underline text-xs font-medium">Desactivar</button>
            </td>
          </tr>
        </tbody>
      </table>
    </section>

    <!-- Ayuda de roles -->
    <section class="bg-slate-50 rounded-xl border border-slate-200 p-5">
      <h3 class="text-sm font-semibold mb-2">Tabla de Roles</h3>
      <ul class="text-xs text-slate-600 space-y-1">
        <li v-for="r in roles" :key="r.valor">
          <span class="font-semibold">{{ r.valor }}</span> — {{ r.descripcion }}
        </li>
      </ul>
    </section>
  </div>
</template>
