<script setup>
import { computed, onMounted, ref } from 'vue'
import { http, mensajeError } from '../api/http'
import { fmt, hoy, fechaCorta } from '../utils/format'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()

// ------------------- Listado de asientos -------------------
const asientos = ref([])
const detalleActual = ref(null)
const cargando = ref(true)
const error = ref('')
const ok = ref('')

// ------------------- Formulario de asiento -------------------
const fecha = ref(hoy())
const concepto = ref('')
const cuentasMovimiento = ref([])
const lineas = ref([nuevaLinea()])
const guardando = ref(false)

function nuevaLinea() {
  return { cuentaId: '', concepto: '', debe: '', haber: '' }
}

const totalDebe = computed(() =>
  lineas.value.reduce((t, l) => t + (parseFloat(l.debe) || 0), 0),
)
const totalHaber = computed(() =>
  lineas.value.reduce((t, l) => t + (parseFloat(l.haber) || 0), 0),
)
const diferencia = computed(() => Math.abs(totalDebe.value - totalHaber.value))
const balanceado = computed(() => diferencia.value < 0.005 && totalDebe.value > 0)

const lineasValidas = computed(() =>
  lineas.value.every((l) => {
    const debe = parseFloat(l.debe) || 0
    const haber = parseFloat(l.haber) || 0
    return l.cuentaId && ((debe > 0 && haber === 0) || (haber > 0 && debe === 0))
  }),
)

const puedeGuardar = computed(
  () => balanceado.value && lineasValidas.value && concepto.value.trim() && !guardando.value,
)

async function cargar() {
  cargando.value = true
  error.value = ''
  try {
    const [asientosRes, cuentasRes] = await Promise.all([
      http.get('/api/asientos'),
      http.get('/api/cuentas/movimiento'),
    ])
    asientos.value = asientosRes.data
    cuentasMovimiento.value = cuentasRes.data
  } catch (e) {
    error.value = mensajeError(e)
  } finally {
    cargando.value = false
  }
}

function agregarLinea() {
  lineas.value.push(nuevaLinea())
}

function quitarLinea(indice) {
  if (lineas.value.length > 1) {
    lineas.value.splice(indice, 1)
  }
}

async function guardar() {
  ok.value = ''
  error.value = ''
  guardando.value = true
  try {
    const payload = {
      fecha: fecha.value,
      concepto: concepto.value.trim(),
      lineas: lineas.value.map((l) => ({
        cuentaId: Number(l.cuentaId),
        concepto: l.concepto,
        montoDebe: parseFloat(l.debe) || 0,
        montoHaber: parseFloat(l.haber) || 0,
      })),
    }
    const { data } = await http.post('/api/asientos', payload)
    ok.value = `Asiento #${data.numeroAsiento} registrado y mayorizado correctamente`
    concepto.value = ''
    lineas.value = [nuevaLinea()]
    await cargar()
  } catch (e) {
    error.value = mensajeError(e)
  } finally {
    guardando.value = false
  }
}

async function verDetalle(asiento) {
  error.value = ''
  try {
    const { data } = await http.get(`/api/asientos/${asiento.id}`)
    detalleActual.value = data
  } catch (e) {
    error.value = mensajeError(e)
  }
}

async function anular(asiento) {
  ok.value = ''
  error.value = ''
  try {
    await http.post(`/api/asientos/${asiento.id}/anular`)
    ok.value = `Asiento #${asiento.numeroAsiento} anulado; saldos revertidos`
    detalleActual.value = null
    await cargar()
  } catch (e) {
    error.value = mensajeError(e)
  }
}

onMounted(cargar)
</script>

<template>
  <div class="p-8 max-w-6xl mx-auto space-y-8">
    <div>
      <h1 class="text-2xl font-bold">Libro Diario</h1>
      <p class="text-sm text-slate-500 mt-1">
        Registro de asientos con Ley de la Partida Doble y mayorización automática
      </p>
    </div>

    <p v-if="error" class="rounded-lg bg-red-50 border border-red-200 text-red-700 text-sm px-3 py-2">{{ error }}</p>
    <p v-if="ok" class="rounded-lg bg-emerald-50 border border-emerald-200 text-emerald-700 text-sm px-3 py-2">{{ ok }}</p>

    <!-- ================= FORMULARIO ================= -->
    <section v-if="auth.puedeRegistrar" class="bg-white rounded-xl border border-slate-200 p-6">
      <h2 class="font-semibold text-lg mb-4">Nuevo asiento</h2>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
        <div>
          <label class="block text-xs font-medium text-slate-500 mb-1">Fecha</label>
          <input v-model="fecha" type="date" :max="hoy()"
            class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500" />
        </div>
        <div>
          <label class="block text-xs font-medium text-slate-500 mb-1">Concepto general</label>
          <input v-model="concepto" maxlength="255" placeholder="Ej. Venta al contado"
            class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500" />
        </div>
      </div>

      <!-- Lineas -->
      <div class="space-y-2">
        <div class="grid grid-cols-12 gap-2 text-[11px] uppercase tracking-wide text-slate-400 font-semibold px-1">
          <span class="col-span-5">Cuenta de movimiento</span>
          <span class="col-span-3">Concepto</span>
          <span class="col-span-2">Debe</span>
          <span class="col-span-2">Haber</span>
        </div>

        <div
          v-for="(linea, i) in lineas"
          :key="i"
          class="grid grid-cols-12 gap-2 items-center"
        >
          <select v-model="linea.cuentaId"
            class="col-span-5 rounded-lg border border-slate-300 px-2 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500">
            <option value="">-- Seleccionar cuenta --</option>
            <option v-for="cuenta in cuentasMovimiento" :key="cuenta.id" :value="cuenta.id">
              {{ cuenta.codigo }} · {{ cuenta.nombre }}
            </option>
          </select>
          <input v-model="linea.concepto" placeholder="Detalle de la línea"
            class="col-span-3 rounded-lg border border-slate-300 px-2 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500" />
          <input v-model="linea.debe" type="number" step="0.01" min="0" placeholder="0.00"
            class="col-span-2 rounded-lg border border-slate-300 px-2 py-2 text-sm text-right focus:outline-none focus:ring-2 focus:ring-blue-500" />
          <div class="col-span-2 flex items-center gap-1 min-w-0">
            <input v-model="linea.haber" type="number" step="0.01" min="0" placeholder="0.00"
              class="w-full min-w-0 rounded-lg border border-slate-300 px-2 py-2 text-sm text-right focus:outline-none focus:ring-2 focus:ring-amber-500" />
            <button v-if="lineas.length > 1" @click="quitarLinea(i)"
              class="shrink-0 text-slate-300 hover:text-red-600 px-0.5" title="Quitar línea">✕</button>
          </div>
        </div>
      </div>

      <button @click="agregarLinea" class="mt-2 text-sm text-emerald-700 hover:text-emerald-800 font-medium">
        + Agregar línea
      </button>

      <!-- Totales con validacion en vivo -->
      <div class="mt-6 grid grid-cols-1 md:grid-cols-4 gap-3 items-center">
        <div class="rounded-lg bg-blue-50 border border-blue-100 px-4 py-3">
          <p class="text-[11px] uppercase text-blue-400 font-semibold">Total Debe</p>
          <p class="text-lg font-bold text-blue-700">{{ fmt(totalDebe) }}</p>
        </div>
        <div class="rounded-lg bg-amber-50 border border-amber-100 px-4 py-3">
          <p class="text-[11px] uppercase text-amber-500 font-semibold">Total Haber</p>
          <p class="text-lg font-bold text-amber-700">{{ fmt(totalHaber) }}</p>
        </div>
        <div class="rounded-lg px-4 py-3"
          :class="balanceado ? 'bg-emerald-50 border border-emerald-200' : 'bg-red-50 border border-red-200'">
          <p class="text-[11px] uppercase font-semibold"
            :class="balanceado ? 'text-emerald-500' : 'text-red-400'">
            {{ balanceado ? 'Partida doble ✓' : 'Diferencia' }}
          </p>
          <p class="text-lg font-bold" :class="balanceado ? 'text-emerald-700' : 'text-red-700'">
            {{ balanceado ? 'Debe = Haber' : fmt(diferencia) }}
          </p>
        </div>
        <button @click="guardar" :disabled="!puedeGuardar"
          class="rounded-lg bg-emerald-600 hover:bg-emerald-700 disabled:opacity-40 disabled:cursor-not-allowed text-white font-semibold py-3 transition"
          :title="puedeGuardar ? '' : 'Debe = Haber, todas las líneas completas y concepto requerido'">
          {{ guardando ? 'Guardando...' : 'Guardar asiento' }}
        </button>
      </div>
      <p v-if="!balanceado && (totalDebe > 0 || totalHaber > 0)"
        class="mt-2 text-xs text-red-600 font-medium">
        ⚠ El asiento no se puede guardar: la Ley de la Partida Doble exige Debe = Haber.
      </p>
    </section>

    <section v-else class="bg-amber-50 border border-amber-200 rounded-xl px-4 py-3 text-sm text-amber-800">
      Tu rol ({{ auth.rol }}) es de solo lectura: puedes consultar el Libro Diario pero no registrar asientos.
    </section>

    <!-- ================= LISTADO ================= -->
    <section class="bg-white rounded-xl border border-slate-200 overflow-hidden">
      <h2 class="font-semibold px-6 py-4 border-b border-slate-100">Asientos registrados</h2>
      <div v-if="cargando" class="px-6 py-4 text-sm text-slate-500">Cargando...</div>
      <table v-else class="w-full text-sm">
        <thead>
          <tr class="text-left text-[11px] uppercase tracking-wide text-slate-400 border-b border-slate-100">
            <th class="px-6 py-2">#</th>
            <th class="px-4 py-2">Fecha</th>
            <th class="px-4 py-2">Concepto</th>
            <th class="px-4 py-2">Registrado por</th>
            <th class="px-4 py-2 text-right">Total</th>
            <th class="px-4 py-2">Estado</th>
            <th class="px-4 py-2"></th>
          </tr>
        </thead>
        <tbody class="divide-y divide-slate-100">
          <tr v-for="asiento in asientos" :key="asiento.id" class="hover:bg-slate-50">
            <td class="px-6 py-3 font-mono">{{ asiento.numeroAsiento }}</td>
            <td class="px-4 py-3">{{ fechaCorta(asiento.fecha) }}</td>
            <td class="px-4 py-3">{{ asiento.concepto }}</td>
            <td class="px-4 py-3 text-slate-500">{{ asiento.usuarioNombre }}</td>
            <td class="px-4 py-3 text-right font-semibold">{{ fmt(asiento.totalDebe) }}</td>
            <td class="px-4 py-3">
              <span class="text-[10px] uppercase rounded-full px-2 py-0.5"
                :class="asiento.estado === 'REGISTRADO' ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-200 text-slate-500 line-through'">
                {{ asiento.estado }}
              </span>
            </td>
            <td class="px-4 py-3 text-right space-x-2 whitespace-nowrap">
              <button @click="verDetalle(asiento)" class="text-blue-600 hover:underline text-xs font-medium">Ver</button>
              <button v-if="auth.puedeRegistrar && asiento.estado === 'REGISTRADO'"
                @click="anular(asiento)" class="text-red-600 hover:underline text-xs font-medium">Anular</button>
            </td>
          </tr>
          <tr v-if="!asientos.length">
            <td colspan="7" class="px-6 py-8 text-center text-slate-400">Aún no hay asientos registrados</td>
          </tr>
        </tbody>
      </table>
    </section>

    <!-- ================= DETALLE ================= -->
    <section v-if="detalleActual" class="bg-white rounded-xl border border-slate-200 overflow-hidden">
      <div class="flex items-center justify-between px-6 py-4 border-b border-slate-100">
        <h2 class="font-semibold">
          Asiento #{{ detalleActual.numeroAsiento }} · {{ fechaCorta(detalleActual.fecha) }} ·
          {{ detalleActual.usuarioNombre }}
        </h2>
        <button @click="detalleActual = null" class="text-slate-400 hover:text-slate-700">✕</button>
      </div>
      <table class="w-full text-sm">
        <thead>
          <tr class="text-left text-[11px] uppercase tracking-wide text-slate-400 border-b border-slate-100">
            <th class="px-6 py-2">Código</th>
            <th class="px-4 py-2">Cuenta</th>
            <th class="px-4 py-2">Concepto</th>
            <th class="px-4 py-2 text-right">Debe</th>
            <th class="px-4 py-2 text-right">Haber</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-slate-100">
          <tr v-for="linea in detalleActual.detalles" :key="linea.id">
            <td class="px-6 py-2 font-mono text-xs">{{ linea.cuentaCodigo }}</td>
            <td class="px-4 py-2">{{ linea.cuentaNombre }}</td>
            <td class="px-4 py-2 text-slate-600">{{ linea.concepto }}</td>
            <td class="px-4 py-2 text-right text-blue-700">{{ linea.montoDebe ? fmt(linea.montoDebe) : '' }}</td>
            <td class="px-4 py-2 text-right text-amber-700">{{ linea.montoHaber ? fmt(linea.montoHaber) : '' }}</td>
          </tr>
        </tbody>
        <tfoot class="border-t-2 border-slate-200 font-bold">
          <tr>
            <td colspan="3" class="px-6 py-3 text-right">Totales</td>
            <td class="px-4 py-3 text-right text-blue-700">{{ fmt(detalleActual.totalDebe) }}</td>
            <td class="px-4 py-3 text-right text-amber-700">{{ fmt(detalleActual.totalHaber) }}</td>
          </tr>
        </tfoot>
      </table>
    </section>
  </div>
</template>
