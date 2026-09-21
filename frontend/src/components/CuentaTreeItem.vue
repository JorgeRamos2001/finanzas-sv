<script setup>
import { computed, ref } from 'vue'
import { fmt } from '../utils/format'

const props = defineProps({
  cuenta: { type: Object, required: true },
  puedeEliminar: { type: Boolean, default: false },
})
const emit = defineEmits(['desactivar'])

const expandida = ref(true)
const tieneHijos = computed(() => (props.cuenta.subCuentas ?? []).length > 0)

const margen = computed(() => (props.cuenta.nivel - 1) * 1.5 + 'rem')
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
        class="w-5 h-5 flex items-center justify-center rounded text-slate-400 hover:bg-slate-200 text-xs"
      >
        {{ expandida ? '▼' : '▶' }}
      </button>
      <span v-else class="w-5"></span>

      <span class="font-mono text-xs bg-slate-100 rounded px-1.5 py-0.5 text-slate-600 min-w-14 text-center">
        {{ cuenta.codigo }}
      </span>

      <span class="text-sm font-medium flex-1 truncate" :class="cuenta.activo ? '' : 'text-slate-400 line-through'">
        {{ cuenta.nombre }}
      </span>

      <span
        v-if="cuenta.aceptaMovimientos"
        class="text-[10px] uppercase tracking-wide bg-emerald-100 text-emerald-700 rounded-full px-2 py-0.5"
      >
        movimiento
      </span>
      <span
        v-else
        class="text-[10px] uppercase tracking-wide bg-slate-100 text-slate-500 rounded-full px-2 py-0.5"
      >
        control
      </span>

      <span class="text-[10px] uppercase rounded-full px-2 py-0.5"
        :class="cuenta.tipoSaldo === 'DEUDOR' ? 'bg-blue-100 text-blue-700' : 'bg-amber-100 text-amber-700'">
        {{ cuenta.tipoSaldo }}
      </span>

      <span class="text-xs text-slate-500 w-24 text-right">{{ fmt(cuenta.saldoDebe) }}</span>
      <span class="text-xs text-slate-500 w-24 text-right">{{ fmt(cuenta.saldoHaber) }}</span>
      <span
        class="text-xs font-semibold w-28 text-right"
        :class="cuenta.saldoNeto >= 0 ? 'text-blue-700' : 'text-amber-700'"
      >
        {{ fmt(cuenta.saldoNeto) }}
      </span>

      <button
        v-if="puedeEliminar && cuenta.activo && !tieneHijos"
        @click="emit('desactivar', cuenta)"
        title="Desactivar cuenta"
        class="text-slate-300 hover:text-red-600 transition px-1"
      >
        ✕
      </button>
      <span v-else class="w-6"></span>
    </div>

    <div v-if="expandida && tieneHijos">
      <CuentaTreeItem
        v-for="hija in cuenta.subCuentas"
        :key="hija.id"
        :cuenta="hija"
        :puede-eliminar="puedeEliminar"
        @desactivar="emit('desactivar', hija)"
      />
    </div>
  </div>
</template>
