export const fmt = (valor) =>
  new Intl.NumberFormat('es-SV', {
    style: 'currency',
    currency: 'USD',
    minimumFractionDigits: 2,
  }).format(Number(valor ?? 0))

export const hoy = () => new Date().toISOString().slice(0, 10)

export const fechaCorta = (fecha) => {
  if (!fecha) return ''
  return fecha.slice(0, 10)
}
