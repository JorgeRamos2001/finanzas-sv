<script setup>
import { computed, ref } from 'vue'
import { fmt } from '../utils/format'

const props = defineProps({
  cuenta: { type: Object, required: true },
  puedeEliminar: { type: Boolean, default: false },
  profundidad: { type: Number, default: 0 },
})
const emit = defineEmits(['desactivar'])

const expandida = ref(true)
const tieneHijos = computed(() => (props.cuenta.subCuentas ?? []).length > 0)

const margen = computed(() => props.profundidad * 1.25 + 'rem')

/** Nombre del nivel segun la estructura del catalogo de clase. */
const etiquetaNivel = computed(() => {
  const etiquetas = {
    1: 'rubro',
    2: 'agrupación',
    3: 'mayor',
    4: 'subcuenta',
    5: 'detalle',
    6: 'analítica',
  }
  return etiquetas[props.cuenta.nivel] || 'cuenta'
})
</script>

<template>
  <div>
    <div
      class="flex items-center gap-2 px-3 py-2 hover:bg-slate-50 transition"
      :style="{ paddingLeft: margen }"
    >
      <button
        v-if="tieneHijos"
        @click="expandida = !expandida"
        class="w-5 h-5 shrink-0 flex items-center justify-center rounded text-slate-400 hover:bg-slate-200 text-xs"
      >
        {{ expandida ? '▼' : '▶' }}
      </button>
      <span v-else class="w-5 shrink-0"></span>

      <span class="font-mono text-xs bg-slate-100 rounded px-1.5 py-0.5 text-slate-600 min-w-20 text-center">
        {{ cuenta.codigo }}
      </span>

      <span class="text-sm font-medium flex-1 truncate" :class="cuenta.activo ? '' : 'text-slate-400 line-through'">
        {{ cuenta.nombre }}
      </span>

      <span
        class="text-[10px] uppercase tracking-wide rounded-full px-2 py-0.5 shrink-0"
        :class="cuenta.aceptaMovimientos
          ? 'bg-emerald-100 text-emerald-700'
          : 'bg-slate-100 text-slate-500'"
        :title="cuenta.aceptaMovimientos ? 'Cuenta de movimiento (acepta asientos)' : 'Cuenta de control (consolida saldos)'"
      >
        {{ etiquetaNivel }}
      </span>

      <span class="text-[10px] uppercase rounded-full px-2 py-0.5 shrink-0"
        :class="cuenta.tipoSaldo === 'DEUDOR' ? 'bg-blue-100 text-blue-700' : 'bg-amber-100 text-amber-700'">
        {{ cuenta.tipoSaldo }}
      </span>

      <span class="text-xs text-slate-500 w-24 text-right shrink-0">{{ fmt(cuenta.saldoDebe) }}</span>
      <span class="text-xs text-slate-500 w-24 text-right shrink-0">{{ fmt(cuenta.saldoHaber) }}</span>
      <span
        class="text-xs font-semibold w-28 text-right shrink-0"
        :class="cuenta.saldoNeto >= 0 ? 'text-blue-700' : 'text-amber-700'"
      >
        {{ fmt(cuenta.saldoNeto) }}
      </span>

      <button
        v-if="puedeEliminar && cuenta.activo && !tieneHijos"
        @click="emit('desactivar', cuenta)"
        title="Desactivar cuenta"
        class="text-slate-300 hover:text-red-600 transition px-1 shrink-0"
      >
        ✕
      </button>
      <span v-else class="w-6 shrink-0"></span>
    </div>

    <div v-if="expandida && tieneHijos">
      <CuentaTreeItem
        v-for="hija in cuenta.subCuentas"
        :key="hija.id"
        :cuenta="hija"
        :profundidad="profundidad + 1"
        :puede-eliminar="puedeEliminar"
        @desactivar="emit('desactivar', hija)"
      />
    </div>
  </div>
</template>
