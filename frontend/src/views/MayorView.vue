<script setup>
import { computed, onMounted, ref } from 'vue'
import { http, mensajeError } from '../api/http'
import { fmt, fechaCorta } from '../utils/format'

const cuentas = ref([])
const cuentaSeleccionada = ref('')
const movimientos = ref([])
const cargando = ref(true)
const error = ref('')

const cuentaInfo = computed(() =>
  cuentas.value.find((c) => c.id === Number(cuentaSeleccionada.value)) || null,
)

const totalDebe = computed(() =>
  movimientos.value.reduce((t, m) => t + Number(m.montoDebe || 0), 0),
)
const totalHaber = computed(() =>
  movimientos.value.reduce((t, m) => t + Number(m.montoHaber || 0), 0),
)
const saldoFinal = computed(() => totalDebe.value - totalHaber.value)
const tipoSaldo = computed(() => (saldoFinal.value >= 0 ? 'DEUDOR' : 'ACREEDOR'))

async function cargar() {
  cargando.value = true
  try {
    const { data } = await http.get('/api/cuentas')
    cuentas.value = data
  } catch (e) {
    error.value = mensajeError(e)
  } finally {
    cargando.value = false
  }
}

async function consultarMayor() {
  if (!cuentaSeleccionada.value) return
  movimientos.value = []
  error.value = ''
  cargando.value = true
  try {
    const { data } = await http.get(`/api/asientos/cuenta/${cuentaSeleccionada.value}`)
    movimientos.value = data
  } catch (e) {
    error.value = mensajeError(e)
  } finally {
    cargando.value = false
  }
}

onMounted(cargar)
</script>

<template>
  <div class="p-8 max-w-6xl mx-auto space-y-6">
    <div>
      <h1 class="text-2xl font-bold">Libro Mayor</h1>
      <p class="text-sm text-slate-500 mt-1">
        En cuentas PRINCIPALES se consolida el control del Debe/Haber por asiento (suma de todas sus subcuentas);
        en cuentas de movimiento se muestra cada partida en parcial
      </p>
    </div>

    <p v-if="error" class="rounded-lg bg-red-50 border border-red-200 text-red-700 text-sm px-3 py-2">{{ error }}</p>

    <!-- Selector -->
    <div class="flex items-end gap-3 bg-white rounded-xl border border-slate-200 p-4">
      <div class="flex-1">
        <label class="block text-xs font-medium text-slate-500 mb-1">Cuenta del catálogo</label>
        <select v-model="cuentaSeleccionada" @change="consultarMayor"
          class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500">
          <option value="">-- Seleccionar cuenta --</option>
          <option v-for="cuenta in cuentas" :key="cuenta.id" :value="cuenta.id">
            {{ cuenta.codigo }} · {{ cuenta.nombre }} {{ cuenta.aceptaMovimientos ? '(movimiento)' : '(control)' }}
          </option>
        </select>
      </div>
    </div>

    <!-- Saldo consolidado de la cuenta -->
    <div v-if="cuentaInfo" class="grid grid-cols-1 md:grid-cols-4 gap-3">
      <div class="rounded-xl bg-white border border-slate-200 px-4 py-3">
        <p class="text-[11px] uppercase text-slate-400 font-semibold">Cuenta</p>
        <p class="font-semibold">{{ cuentaInfo.nombre }}</p>
        <p class="font-mono text-xs text-slate-400">{{ cuentaInfo.codigo }}</p>
      </div>
      <div class="rounded-xl bg-blue-50 border border-blue-100 px-4 py-3">
        <p class="text-[11px] uppercase text-blue-400 font-semibold">Saldo Debe</p>
        <p class="text-lg font-bold text-blue-700">{{ fmt(cuentaInfo.saldoDebe) }}</p>
      </div>
      <div class="rounded-xl bg-amber-50 border border-amber-100 px-4 py-3">
        <p class="text-[11px] uppercase text-amber-500 font-semibold">Saldo Haber</p>
        <p class="text-lg font-bold text-amber-700">{{ fmt(cuentaInfo.saldoHaber) }}</p>
      </div>
      <div class="rounded-xl bg-emerald-50 border border-emerald-100 px-4 py-3">
        <p class="text-[11px] uppercase text-emerald-500 font-semibold">Saldo actual</p>
        <p class="text-lg font-bold text-emerald-700">
          {{ fmt(Math.abs(cuentaInfo.saldoNeto)) }}
          <span class="text-xs font-medium">{{ cuentaInfo.tipoSaldo }}</span>
        </p>
      </div>
    </div>

    <!-- Movimientos -->
    <section v-if="cuentaSeleccionada" class="bg-white rounded-xl border border-slate-200 overflow-hidden">
      <p v-if="cuentaInfo && !cuentaInfo.aceptaMovimientos"
        class="px-6 py-2 bg-amber-50 border-b border-amber-100 text-xs text-amber-800">
        👁 Cuenta principal: vista CONSOLIDADA — una línea por asiento con la suma del Debe/Haber
        de todas sus cuentas de movimiento (parciales).
      </p>
      <table class="w-full text-sm">
        <thead>
          <tr class="text-left text-[11px] uppercase tracking-wide text-slate-400 border-b border-slate-100">
            <th class="px-6 py-2">Asiento</th>
            <th class="px-4 py-2">Fecha</th>
            <th class="px-4 py-2">Concepto</th>
            <th class="px-4 py-2 text-right">Debe</th>
            <th class="px-4 py-2 text-right">Haber</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-slate-100">
          <tr v-for="(mov, i) in movimientos" :key="i" class="hover:bg-slate-50">
            <td class="px-6 py-2 font-mono">#{{ mov.numeroAsiento }}</td>
            <td class="px-4 py-2">{{ fechaCorta(mov.fecha) }}</td>
            <td class="px-4 py-2">
              <span class="font-medium">{{ mov.conceptoAsiento }}</span>
              <span v-if="mov.conceptoLinea && mov.conceptoLinea !== mov.conceptoAsiento" class="text-slate-400">
                — {{ mov.conceptoLinea }}
              </span>
            </td>
            <td class="px-4 py-2 text-right text-blue-700">{{ mov.montoDebe ? fmt(mov.montoDebe) : '' }}</td>
            <td class="px-4 py-2 text-right text-amber-700">{{ mov.montoHaber ? fmt(mov.montoHaber) : '' }}</td>
          </tr>
          <tr v-if="!movimientos.length">
            <td colspan="5" class="px-6 py-8 text-center text-slate-400">Sin movimientos para esta cuenta</td>
          </tr>
        </tbody>
        <tfoot v-if="movimientos.length" class="border-t-2 border-slate-200 font-bold">
          <tr>
            <td colspan="3" class="px-6 py-3 text-right">Totales</td>
            <td class="px-4 py-3 text-right text-blue-700">{{ fmt(totalDebe) }}</td>
            <td class="px-4 py-3 text-right text-amber-700">{{ fmt(totalHaber) }}</td>
          </tr>
          <tr class="bg-emerald-50">
            <td colspan="3" class="px-6 py-3 text-right text-emerald-700">Saldo actual ({{ tipoSaldo }})</td>
            <td colspan="2" class="px-4 py-3 text-right text-emerald-700 font-bold">
              {{ fmt(Math.abs(saldoFinal)) }}
            </td>
          </tr>
        </tfoot>
      </table>
    </section>

    <div v-if="cargando" class="text-sm text-slate-500">Cargando...</div>
  </div>
</template>
