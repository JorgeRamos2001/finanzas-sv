<script setup>
import { onMounted, ref } from 'vue'
import { http, mensajeError } from '../api/http'
import { fmt } from '../utils/format'

const reporte = ref(null)
const cargando = ref(true)
const error = ref('')

onMounted(async () => {
  try {
    const { data } = await http.get('/api/reportes/estado-resultados')
    reporte.value = data
  } catch (e) {
    error.value = mensajeError(e)
  } finally {
    cargando.value = false
  }
})
</script>

<template>
  <div class="p-8 max-w-4xl mx-auto space-y-6">
    <div>
      <h1 class="text-2xl font-bold">Estado de Resultados</h1>
      <p class="text-sm text-slate-500 mt-1">
        Generado dinámicamente con el primer dígito del código:
        5 = Ingresos · 4 = Costos y Gastos
      </p>
    </div>

    <div v-if="cargando" class="text-sm text-slate-500">Generando reporte...</div>
    <p v-else-if="error" class="rounded-lg bg-red-50 border border-red-200 text-red-700 text-sm px-3 py-2">{{ error }}</p>

    <template v-else-if="reporte">
      <!-- Ingresos -->
      <section class="bg-white rounded-xl border border-slate-200 overflow-hidden">
        <h3 class="font-semibold px-6 py-3 border-b border-slate-100 bg-amber-50">
          Ingresos (código 5)
        </h3>
        <table class="w-full text-sm">
          <tbody class="divide-y divide-slate-100">
            <tr v-for="linea in reporte.ingresos" :key="linea.codigo">
              <td class="px-6 py-2 font-mono text-xs text-slate-400">{{ linea.codigo }}</td>
              <td class="px-2 py-2">{{ linea.nombre }}</td>
              <td class="px-6 py-2 text-right font-semibold text-amber-700">
                {{ fmt(Number(linea.saldoHaber) - Number(linea.saldoDebe)) }}
              </td>
            </tr>
            <tr v-if="!reporte.ingresos.length">
              <td colspan="3" class="px-6 py-4 text-center text-slate-400 text-xs">Sin cuentas de ingresos</td>
            </tr>
          </tbody>
          <tfoot class="border-t-2 border-slate-200 font-bold bg-amber-50">
            <tr>
              <td colspan="2" class="px-6 py-3 text-right">Total Ingresos</td>
              <td class="px-6 py-3 text-right text-amber-700">{{ fmt(reporte.totalIngresos) }}</td>
            </tr>
          </tfoot>
        </table>
      </section>

      <!-- Costos y Gastos -->
      <section class="bg-white rounded-xl border border-slate-200 overflow-hidden">
        <h3 class="font-semibold px-6 py-3 border-b border-slate-100 bg-blue-50">
          Costos y Gastos (código 4)
        </h3>
        <table class="w-full text-sm">
          <tbody class="divide-y divide-slate-100">
            <tr v-for="linea in reporte.costosYGastos" :key="linea.codigo">
              <td class="px-6 py-2 font-mono text-xs text-slate-400">{{ linea.codigo }}</td>
              <td class="px-2 py-2">{{ linea.nombre }}</td>
              <td class="px-6 py-2 text-right font-semibold text-blue-700">
                {{ fmt(Number(linea.saldoDebe) - Number(linea.saldoHaber)) }}
              </td>
            </tr>
            <tr v-if="!reporte.costosYGastos.length">
              <td colspan="3" class="px-6 py-4 text-center text-slate-400 text-xs">Sin cuentas de costos y gastos</td>
            </tr>
          </tbody>
          <tfoot class="border-t-2 border-slate-200 font-bold bg-blue-50">
            <tr>
              <td colspan="2" class="px-6 py-3 text-right">Total Costos y Gastos</td>
              <td class="px-6 py-3 text-right text-blue-700">{{ fmt(reporte.totalCostosYGastos) }}</td>
            </tr>
          </tfoot>
        </table>
      </section>

      <!-- Utilidad -->
      <section class="rounded-xl px-6 py-5 text-white flex items-center justify-between"
        :class="reporte.utilidad >= 0 ? 'bg-emerald-600' : 'bg-red-600'">
        <div>
          <p class="text-xs uppercase tracking-wide font-semibold opacity-80">
            Ingresos − Costos y Gastos =
          </p>
          <p class="text-3xl font-bold mt-1">{{ fmt(reporte.utilidad) }}</p>
        </div>
        <div class="text-right">
          <p class="text-2xl font-extrabold">{{ reporte.resultado }}</p>
          <p class="text-xs opacity-80 mt-1">
            {{ fmt(reporte.totalIngresos) }} − {{ fmt(reporte.totalCostosYGastos) }}
          </p>
        </div>
      </section>
    </template>
  </div>
</template>
