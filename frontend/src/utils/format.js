export const fmt = (valor) =>
  new Intl.NumberFormat('es-SV', {
    style: 'currency',
    currency: 'USD',
    minimumFractionDigits: 2,
  }).format(Number(valor ?? 0))

/** Fecha de HOY en zona horaria local (no UTC) para inputs type=date. */
export const hoy = () => {
  const ahora = new Date()
  const local = new Date(ahora.getTime() - ahora.getTimezoneOffset() * 60000)
  return local.toISOString().slice(0, 10)
}

export const fechaCorta = (fecha) => {
  if (!fecha) return ''
  return fecha.slice(0, 10)
}
