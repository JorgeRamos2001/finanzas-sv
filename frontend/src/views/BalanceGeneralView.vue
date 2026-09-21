<script setup>
import { onMounted, ref } from 'vue'
import { http, mensajeError } from '../api/http'
import { fmt } from '../utils/format'

const balance = ref(null)
const cargando = ref(true)
const error = ref('')

onMounted(async () => {
  try {
    const { data } = await http.get('/api/reportes/balance-general')
    balance.value = data
  } catch (e) {
    error.value = mensajeError(e)
  } finally {
    cargando.value = false
  }
})
</script>

<template>
  <div class="p-8 max-w-6xl mx-auto space-y-6">
    <div>
      <h1 class="text-2xl font-bold">Balance General</h1>
      <p class="text-sm text-slate-500 mt-1">
        Generado dinámicamente con el primer dígito del código de cuenta:
        1 = Activo · 2 = Pasivo · 3 = Capital Contable
      </p>
    </div>

    <div v-if="cargando" class="text-sm text-slate-500">Generando reporte...</div>
    <p v-else-if="error" class="rounded-lg bg-red-50 border border-red-200 text-red-700 text-sm px-3 py-2">{{ error }}</p>

    <template v-else-if="balance">
      <!-- Ecuacion contable -->
      <section class="grid grid-cols-1 md:grid-cols-4 gap-3">
        <div class="rounded-xl bg-blue-600 text-white px-5 py-4">
          <p class="text-xs uppercase tracking-wide text-blue-200 font-semibold">Activo</p>
          <p class="text-2xl font-bold mt-1">{{ fmt(balance.totalActivo) }}</p>
        </div>
        <div class="rounded-xl bg-white border border-slate-200 px-5 py-4 flex items-center justify-center text-2xl font-bold text-slate-300">
          =
        </div>
        <div class="rounded-xl bg-amber-500 text-white px-5 py-4">
          <p class="text-xs uppercase tracking-wide text-amber-100 font-semibold">Pasivo</p>
          <p class="text-2xl font-bold mt-1">{{ fmt(balance.totalPasivo) }}</p>
        </div>
        <div class="rounded-xl bg-purple-600 text-white px-5 py-4">
          <p class="text-xs uppercase tracking-wide text-purple-200 font-semibold">Capital Contable</p>
          <p class="text-2xl font-bold mt-1">{{ fmt(balance.totalCapitalContable) }}</p>
        </div>
      </section>

      <section class="rounded-xl border px-5 py-4"
        :class="balance.ecuacionCumplida ? 'bg-emerald-50 border-emerald-200' : 'bg-red-50 border-red-200'">
        <div class="flex flex-wrap items-center justify-between gap-2">
          <p class="font-semibold" :class="balance.ecuacionCumplida ? 'text-emerald-700' : 'text-red-700'">
            {{ balance.ecuacionCumplida ? '✓ Ecuación contable cumplida: Activo = Pasivo + Capital Contable'
              : '✗ La ecuación contable no está balanceada' }}
          </p>
          <p class="text-sm text-slate-600">
            Pasivo + Capital + Utilidad del periodo =
            <span class="font-bold">{{ fmt(balance.pasivoMasCapital) }}</span>
            · Utilidad: <span class="font-bold">{{ fmt(balance.utilidadPeriodo) }}</span>
            · Diferencia: <span class="font-bold">{{ fmt(balance.diferencia) }}</span>
          </p>
        </div>
      </section>

      <!-- Detalle por grupo -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-4">
        <section v-for="grupo in [
            { titulo: 'Activo (código 1)', lineas: balance.activos, color: 'text-blue-700' },
            { titulo: 'Pasivo (código 2)', lineas: balance.pasivos, color: 'text-amber-700' },
            { titulo: 'Capital Contable (código 3)', lineas: balance.capitalContable, color: 'text-purple-700' },
          ]" :key="grupo.titulo" class="bg-white rounded-xl border border-slate-200 overflow-hidden">
          <h3 class="font-semibold px-4 py-3 border-b border-slate-100">{{ grupo.titulo }}</h3>
          <table class="w-full text-sm">
            <tbody class="divide-y divide-slate-100">
              <tr v-for="linea in grupo.lineas" :key="linea.codigo">
                <td class="px-4 py-2 font-mono text-xs text-slate-400">{{ linea.codigo }}</td>
                <td class="px-2 py-2">{{ linea.nombre }}</td>
                <td class="px-4 py-2 text-right font-semibold" :class="grupo.color">
                  {{ linea.naturaleza === 'DEUDOR' ? fmt(linea.saldoNeto) : fmt(Math.abs(linea.saldoNeto)) }}
                </td>
              </tr>
              <tr v-if="!grupo.lineas.length">
                <td colspan="3" class="px-4 py-4 text-center text-slate-400 text-xs">Sin cuentas</td>
              </tr>
            </tbody>
          </table>
        </section>
      </div>
    </template>
  </div>
</template>
