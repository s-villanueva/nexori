import { useEffect, useState } from 'react'
import { api } from '../api/client'
import PageHeader from '../components/PageHeader'

export default function Proveedores() {
  const [proveedores, setProveedores] = useState([])
  const [loading, setLoading] = useState(true)
  const [msg, setMsg] = useState('')
  const [busqueda, setBusqueda] = useState('')

  const cargar = async () => {
    setLoading(true)
    setMsg('')
    try {
      const [provData, empData] = await Promise.all([
        api.get('/api/v1/proveedores'),
        api.get('/api/v1/empresas'),
      ])
      const empresasMap = Object.fromEntries((empData || []).map(e => [e.id, e]))
      const lista = (provData || [])
        .filter(p => p.activo)
        .map(p => {
          const empresa = p.idEmpresa || empresasMap[p.idEmpresa?.id] || {}
          return {
            id: p.id,
            nombre: empresa.nombre || '—',
            email: empresa.email || '—',
            telefono: empresa.telefono || '—',
            direccion: empresa.direccion || '—',
            ruc: empresa.ruc || empresa.nit || '—',
          }
        })
      setProveedores(lista)
    } catch (e) {
      setMsg(`Error cargando proveedores: ${e.message}`)
    }
    setLoading(false)
  }

  useEffect(() => { cargar() }, [])

  const filtrados = proveedores.filter(p =>
    p.nombre.toLowerCase().includes(busqueda.toLowerCase()) ||
    p.email.toLowerCase().includes(busqueda.toLowerCase())
  )

  return (
    <div>
      <PageHeader
        title="Proveedores"
        subtitle="Listado de proveedores activos en la plataforma"
        action={
          <button onClick={cargar} style={styles.refreshBtn}>↺ Actualizar</button>
        }
      />

      {msg && <div style={styles.msg}>{msg}</div>}

      <div style={styles.searchWrap}>
        <input
          style={styles.searchInput}
          placeholder="Buscar por nombre o email..."
          value={busqueda}
          onChange={e => setBusqueda(e.target.value)}
        />
      </div>

      {loading ? (
        <p style={{ color: '#94a3b8' }}>Cargando proveedores...</p>
      ) : filtrados.length === 0 ? (
        <p style={{ color: '#94a3b8' }}>No hay proveedores disponibles.</p>
      ) : (
        <div style={styles.grid}>
          {filtrados.map(p => (
            <div key={p.id} style={styles.card}>
              <div style={styles.avatar}>{p.nombre.charAt(0).toUpperCase()}</div>
              <div style={styles.info}>
                <p style={styles.nombre}>{p.nombre}</p>
                {p.email !== '—' && <p style={styles.detalle}>Email: {p.email}</p>}
                {p.telefono !== '—' && <p style={styles.detalle}>Tel: {p.telefono}</p>}
                {p.direccion !== '—' && <p style={styles.detalle}>Dir: {p.direccion}</p>}
                {p.ruc !== '—' && <p style={styles.detalle}>RUC/NIT: {p.ruc}</p>}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

const styles = {
  refreshBtn: { background: '#fff', border: '1px solid #DDE0EE', borderRadius: '8px', padding: '8px 16px', fontSize: '13px', cursor: 'pointer', color: '#9599AE' },
  msg: { background: '#fef2f2', border: '1px solid #fca5a5', borderRadius: '8px', padding: '10px 14px', marginBottom: '1rem', color: '#dc2626', fontSize: '13px' },
  searchWrap: { marginBottom: '1.25rem' },
  searchInput: { width: '100%', maxWidth: '360px', padding: '10px 14px', border: '1.5px solid #DDE0EE', borderRadius: '8px', fontSize: '14px', outline: 'none', boxSizing: 'border-box', color: '#1A1D3B', background: '#fff' },
  grid: { display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: '1rem' },
  card: { background: '#fff', border: '1px solid #DDE0EE', borderRadius: '12px', padding: '1.1rem', display: 'flex', gap: '12px', alignItems: 'flex-start' },
  avatar: { width: '42px', height: '42px', borderRadius: '10px', background: '#06175D', color: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: '700', fontSize: '18px', flexShrink: 0 },
  info: { minWidth: 0 },
  nombre: { margin: '0 0 6px', fontWeight: '700', fontSize: '14px', color: '#1A1D3B' },
  detalle: { margin: '0 0 3px', fontSize: '12px', color: '#9599AE', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' },
}
