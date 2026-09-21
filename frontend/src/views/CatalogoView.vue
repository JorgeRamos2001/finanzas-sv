<script setup>
import { computed, onMounted, ref } from 'vue'
import { http, mensajeError } from '../api/http'
import { fmt } from '../utils/format'
import { useAuthStore } from '../stores/auth'
import CuentaTreeItem from '../components/CuentaTreeItem.vue'

const auth = useAuthStore()
const cuentas = ref([])
const cargando = ref(true)
const error = ref('')
const ok = ref('')

// Formulario de nueva cuenta (solo ADMIN)
const mostrandoForm = ref(false)
const enviando = ref(false)
const form = ref({ codigo: '', nombre: '', cuentaPadreId: '', naturaleza: 'DEUDOR', aceptaMovimientos: false })

const raices = computed(() => {
  const porId = new Map(cuentas.value.map((c) => [c.id, { ...c, subCuentas: [] }]))
  const raices = []
  for (const nodo of porId.values()) {
    if (nodo.cuentaPadreId && porId.has(nodo.cuentaPadreId)) {
      porId.get(nodo.cuentaPadreId).subCuentas.push(nodo)
    } else {
      raices.push(nodo)
    }
  }
  return raices
})

const padresPosibles = computed(() =>
  cuentas.value.filter((c) => !c.aceptaMovimientos && c.nivel < 6 && c.activo),
)

async function cargar() {
  cargando.value = true
  error.value = ''
  try {
    const { data } = await http.get('/api/cuentas')
    cuentas.value = data
  } catch (e) {
    error.value = mensajeError(e)
  } finally {
    cargando.value = false
  }
}

async function crearCuenta() {
  ok.value = ''
  error.value = ''
  cargando.value = true
  try {
    const payload = {
      ...form.value,
      cuentaPadreId: form.value.cuentaPadreId || null,
    }
    await http.post('/api/cuentas', payload)
    ok.value = `Cuenta ${form.value.codigo} ${form.value.nombre} creada`
    form.value = { codigo: '', nombre: '', cuentaPadreId: '', naturaleza: 'DEUDOR', aceptaMovimientos: false }
    mostrandoForm.value = false
    await cargar()
  } catch (e) {
    error.value = mensajeError(e)
  } finally {
    cargando.value = false
  }
}

async function desactivar(cuenta) {
  ok.value = ''
  error.value = ''
  cargando.value = true
  try {
    await http.delete(`/api/cuentas/${cuenta.id}`)
    ok.value = `Cuenta ${cuenta.codigo} desactivada`
    await cargar()
  } catch (e) {
    error.value = mensajeError(e)
  } finally {
    cargando.value = false
  }
}

onMounted(cargar)
</script>

<template>
  <div class="p-8 max-w-6xl mx-auto">
    <div class="flex items-center justify-between mb-6">
      <div>
        <h1 class="text-2xl font-bold">Catálogo de Cuentas</h1>
        <p class="text-sm text-slate-500 mt-1">
          Libro Mayor dinámico: los saldos de las cuentas principales se consolidan automáticamente
        </p>
      </div>
      <button
        v-if="auth.esAdmin"
        @click="mostrandoForm = !mostrandoForm"
        class="rounded-lg bg-emerald-600 hover:bg-emerald-700 text-white text-sm font-semibold px-4 py-2.5 transition"
      >
        {{ mostrandoForm ? 'Cancelar' : '+ Nueva cuenta' }}
      </button>
    </div>

    <!-- Formulario nueva cuenta -->
    <form
      v-if="mostrandoForm"
      @submit.prevent="crearCuenta"
      class="mb-6 grid grid-cols-1 md:grid-cols-5 gap-3 items-end bg-white rounded-xl border border-slate-200 p-4"
    >
      <div>
        <label class="block text-xs font-medium text-slate-500 mb-1">Código</label>
        <input v-model="form.codigo" required maxlength="20" placeholder="1.1.3"
          class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500" />
      </div>
      <div>
        <label class="block text-xs font-medium text-slate-500 mb-1">Nombre</label>
        <input v-model="form.nombre" required maxlength="120" placeholder="Nombre de la cuenta"
          class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500" />
      </div>
      <div>
        <label class="block text-xs font-medium text-slate-500 mb-1">Cuenta padre</label>
        <select v-model="form.cuentaPadreId"
          class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500">
          <option value="">(nivel 1 — sin padre)</option>
          <option v-for="padre in padresPosibles" :key="padre.id" :value="padre.id">
            {{ padre.codigo }} · {{ padre.nombre }}
          </option>
        </select>
      </div>
      <div>
        <label class="block text-xs font-medium text-slate-500 mb-1">Naturaleza</label>
        <select v-model="form.naturaleza"
          class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500">
          <option value="DEUDOR">DEUDOR</option>
          <option value="ACREEDOR">ACREEDOR</option>
        </select>
      </div>
      <div class="flex items-center gap-2">
        <label class="flex items-center gap-2 text-sm text-slate-600">
          <input v-model="form.aceptaMovimientos" type="checkbox" class="rounded" />
          Acepta movimientos
        </label>
        <button type="submit" :disabled="cargando"
          class="flex-1 rounded-lg bg-emerald-600 hover:bg-emerald-700 disabled:opacity-50 text-white text-sm font-semibold px-3 py-2">
          Guardar
        </button>
      </div>
    </form>

    <p v-if="error" class="mb-4 rounded-lg bg-red-50 border border-red-200 text-red-700 text-sm px-3 py-2">{{ error }}</p>
    <p v-if="ok" class="mb-4 rounded-lg bg-emerald-50 border border-emerald-200 text-emerald-700 text-sm px-3 py-2">{{ ok }}</p>

    <!-- Árbol -->
    <div v-if="cargando" class="text-slate-500 text-sm">Cargando catálogo...</div>
    <div v-else class="bg-white rounded-xl border border-slate-200 divide-y divide-slate-100">
      <CuentaTreeItem
        v-for="raiz in raices"
        :key="raiz.id"
        :cuenta="raiz"
        :puede-eliminar="auth.esAdmin"
        @desactivar="desactivar"
      />
    </div>
  </div>
</template>
