import { useEffect, useState } from 'react'
import { api } from '../../api/client'
import PageHeader from '../../components/PageHeader'

export default function AdminProductos() {
  const [data, setData] = useState([])
  const [loading, setLoading] = useState(true)
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const PAGE_SIZE = 10

  const cargar = async (p) => {
    setLoading(true)
    try {
      const res = await api.get(`/api/v1/products/paged?page=${p}&size=${PAGE_SIZE}`)
      setData(res.content ?? [])
      setTotalPages(res.totalPages ?? 0)
      setTotalElements(res.totalElements ?? 0)
    } catch {
      setData([])
    }
    setLoading(false)
  }

  useEffect(() => { cargar(0) }, [])

  const cambiarPagina = (p) => { setPage(p); cargar(p) }

  return (
    <div>
      <PageHeader title="Productos" subtitle={`${totalElements} productos registrados`} />
      <div style={styles.tableWrapper}>
        {loading ? <p style={styles.empty}>Cargando...</p> : data.length === 0 ? <p style={styles.empty}>Sin datos.</p> : (
          <>
            <table style={styles.table}>
              <thead>
                <tr>{['SKU', 'Nombre', 'Descripción', 'Unidad', 'Categoría', 'Proveedor', 'Estado'].map(h => (
                  <th key={h} style={styles.th}>{h}</th>
                ))}</tr>
              </thead>
              <tbody>
                {data.map((p, i) => (
                  <tr key={p.id} style={{ background: i % 2 === 0 ? '#fff' : '#F7F8FC' }}>
                    <td style={styles.td}>{p.sku || '—'}</td>
                    <td style={styles.td}>{p.nombre}</td>
                    <td style={{ ...styles.td, maxWidth: 160, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{p.descripcion || '—'}</td>
                    <td style={styles.td}>{p.unidadMedida || '—'}</td>
                    <td style={styles.td}>{p.nombreCategoria || '—'}</td>
                    <td style={styles.td}>{p.nombreProveedor || '—'}</td>
                    <td style={styles.td}>
                      <span style={{ color: p.activo ? '#16a34a' : '#dc2626', fontWeight: 600 }}>{p.activo ? 'Activo' : 'Inactivo'}</span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            <Paginacion page={page} totalPages={totalPages} totalElements={totalElements} pageSize={PAGE_SIZE} onChange={cambiarPagina} />
          </>
        )}
      </div>
    </div>
  )
}

function Paginacion({ page, totalPages, totalElements, pageSize, onChange }) {
  return (
    <div style={styles.pagination}>
      <span style={styles.pageInfo}>{page * pageSize + 1}–{Math.min((page + 1) * pageSize, totalElements)} de {totalElements}</span>
      <div style={{ display: 'flex', gap: 4 }}>
        <button style={styles.pageBtn} onClick={() => onChange(page - 1)} disabled={page === 0}>‹</button>
        {Array.from({ length: totalPages }, (_, i) => (
          <button key={i} style={{ ...styles.pageBtn, ...(i === page ? styles.pageBtnActive : {}) }} onClick={() => onChange(i)}>{i + 1}</button>
        ))}
        <button style={styles.pageBtn} onClick={() => onChange(page + 1)} disabled={page >= totalPages - 1}>›</button>
      </div>
    </div>
  )
}

const styles = {
  tableWrapper: { background: '#fff', border: '1px solid #DDE0EE', borderRadius: '10px', overflow: 'hidden' },
  table: { width: '100%', borderCollapse: 'collapse', fontSize: '13px' },
  th: { padding: '10px 14px', background: '#EEF1FB', color: '#06175D', fontWeight: '700', textAlign: 'left', borderBottom: '1px solid #DDE0EE', whiteSpace: 'nowrap' },
  td: { padding: '9px 14px', color: '#1A1D3B', borderBottom: '1px solid #F0F2FA', whiteSpace: 'nowrap' },
  empty: { padding: '2rem', color: '#9599AE', fontSize: '14px' },
  pagination: { display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '10px 14px', borderTop: '1px solid #F0F2FA' },
  pageInfo: { fontSize: '12px', color: '#9599AE' },
  pageBtn: { padding: '4px 10px', fontSize: '12px', fontWeight: '600', background: '#fff', color: '#9599AE', border: '1px solid #DDE0EE', borderRadius: '6px', cursor: 'pointer' },
  pageBtnActive: { background: '#06175D', color: '#fff', borderColor: '#06175D' },
}
