import { useEffect, useState } from 'react'
import { api } from '../api/client'
import { useAuth } from '../AuthContext'
import DataTable from '../components/DataTable'

function Icon({ name, size = 16 }) {
  const paths = {
    box: [
      'M21 16V8a2 2 0 00-1-1.73l-7-4a2 2 0 00-2 0l-7 4A2 2 0 003 8v8a2 2 0 001 1.73l7 4a2 2 0 002 0l7-4A2 2 0 0021 16z',
      'M3.27 6.96L12 12.01l8.73-5.05',
      'M12 22.08V12',
    ],
    tag: [
      'M20.59 13.41l-7.17 7.17a2 2 0 01-2.83 0L2 12V2h10l8.59 8.59a2 2 0 010 2.82z',
      'M7 7h.01',
    ],
    users: [
      'M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2',
      'M9 11a4 4 0 100-8 4 4 0 000 8z',
      'M23 21v-2a4 4 0 00-3-3.87',
      'M16 3.13a4 4 0 010 7.75',
    ],
    clipboard: [
      'M16 4h2a2 2 0 012 2v14a2 2 0 01-2 2H6a2 2 0 01-2-2V6a2 2 0 012-2h2',
      'M9 2h6a1 1 0 010 2H9a1 1 0 010-2z',
      'M9 12h6',
      'M9 16h4',
    ],
    file: [
      'M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z',
      'M14 2v6h6',
      'M16 13H8',
      'M16 17H8',
    ],
    receipt: [
      'M4 2v20l3-3 3 3 3-3 3 3 3-3 3 3V2H4z',
      'M8 10h8',
      'M8 14h6',
    ],
    sliders: [
      'M4 21v-7',
      'M4 10V3',
      'M12 21v-9',
      'M12 8V3',
      'M20 21v-5',
      'M20 12V3',
      'M1 14h6',
      'M9 8h6',
      'M17 16h6',
    ],
    'bar-chart': [
      'M18 20V10',
      'M12 20V4',
      'M6 20v-6',
    ],
  }
  const ps = paths[name] || []
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none"
      stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      {ps.map((d, i) => <path key={i} d={d} />)}
    </svg>
  )
}

export default function Dashboard() {
  const { session } = useAuth()
  const isProveedor = session?.rol === 'proveedor'
  const isAdmin     = session?.rol === 'admin'

  const [counts, setCounts]           = useState({})
  const [tableData, setTableData]     = useState([])
  const [tableLoading, setTableLoading] = useState(false)
  const [activeCard, setActiveCard]   = useState(null)
  const [tablePage, setTablePage]     = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [totalPages, setTotalPages]   = useState(0)
  const [serverPaged, setServerPaged] = useState(false)

  const PAGE_SIZE = 10

  const cards = isAdmin
    ? [
        { key: 'productos', label: 'Productos', color: '#06175D', icon: 'tag',       path: '/api/v1/products',          pagedPath: '/api/v1/products/paged' },
        { key: 'usuarios',  label: 'Usuarios',  color: '#000053', icon: 'users',     path: '/api/v1/usuarios?size=1000' },
        { key: 'logs',      label: 'Logs hoy',  color: '#4A5BAD', icon: 'clipboard', path: null },
      ]
    : isProveedor
    ? [
        { key: 'ordenes',   label: 'Órdenes',         color: '#06175D', icon: 'box',      path: '/api/v1/ordenes-compra',   pagedPath: '/api/v1/ordenes-compra/paged' },
        { key: 'productos', label: 'Productos',        color: '#000053', icon: 'tag',      path: '/api/v1/products',         pagedPath: '/api/v1/products/paged' },
        { key: 'contratos', label: 'Contratos tarifa', color: '#4A5BAD', icon: 'file',     path: '/api/v1/contratos-tarifa', pagedPath: '/api/v1/contratos-tarifa/paged' },
        { key: 'reglas',    label: 'Reglas tarifa',    color: '#6B7DB8', icon: 'sliders',  path: '/api/v1/tarifas-reglas',   pagedPath: '/api/v1/tarifas-reglas/paged' },
      ]
    : [
        { key: 'ordenes',   label: 'Órdenes',          color: '#06175D', icon: 'box',     path: '/api/v1/ordenes-compra',   pagedPath: '/api/v1/ordenes-compra/paged' },
        { key: 'facturas',  label: 'Facturas',          color: '#000053', icon: 'receipt', path: '/api/v1/facturas',         pagedPath: '/api/v1/facturas/paged' },
        { key: 'contratos', label: 'Contratos tarifa',  color: '#4A5BAD', icon: 'file',    path: '/api/v1/contratos-tarifa', pagedPath: '/api/v1/contratos-tarifa/paged' },
        { key: 'productos', label: 'Productos',         color: '#6B7DB8', icon: 'tag',     path: '/api/v1/products',         pagedPath: '/api/v1/products/paged' },
      ]

  useEffect(() => {
    async function loadCounts() {
      const validCards = cards.filter(c => c.path)
      const results = await Promise.allSettled(validCards.map(c => api.get(c.path)))
      const obj = {}
      validCards.forEach((c, i) => {
        const val = results[i].status === 'fulfilled' ? results[i].value : null
        obj[c.key] = Array.isArray(val) ? val.length : (val?.totalElements ?? val?.content?.length ?? '—')
      })
      setCounts(obj)
    }
    loadCounts()
  }, [])

  const loadCardData = async (card, page = 0) => {
    if (!card.path && !card.pagedPath) return
    setActiveCard(card.key)
    setTablePage(page)
    setTableLoading(true)
    try {
      if (card.pagedPath) {
        const data = await api.get(`${card.pagedPath}?page=${page}&size=${PAGE_SIZE}`)
        setTableData(data.content ?? [])
        setTotalElements(data.totalElements ?? 0)
        setTotalPages(data.totalPages ?? 0)
        setServerPaged(true)
      } else {
        const data = await api.get(card.path)
        setTableData(Array.isArray(data) ? data : (data?.content ?? []))
        setTotalElements(0)
        setTotalPages(0)
        setServerPaged(false)
      }
    } catch {
      setTableData([])
      setTotalElements(0)
      setTotalPages(0)
    }
    setTableLoading(false)
  }

  const changePage = async (p) => {
    const card = cards.find(c => c.key === activeCard)
    if (serverPaged && card) {
      await loadCardData(card, p)
    } else {
      setTablePage(p)
    }
  }

  const greeting = () => {
    const h = new Date().getHours()
    if (h < 12) return 'Buenos días'
    if (h < 18) return 'Buenas tardes'
    return 'Buenas noches'
  }

  return (
    <div>
      {/* Greeting */}
      <div style={s.greeting}>
        <div>
          <h1 style={s.greetTitle}>{greeting()}, {session?.nombre?.split(' ')[0] ?? 'usuario'}</h1>
          <p style={s.greetSub}>
            {new Date().toLocaleDateString('es-BO', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}
            {session?.nombreEmpresa && <span style={s.empresa}> · {session.nombreEmpresa}</span>}
          </p>
        </div>
      </div>

      {/* Stats cards */}
      <div style={s.cardGrid}>
        {cards.map(c => (
          <button key={c.key} style={s.card} onClick={() => loadCardData(c, 0)}>
            <div style={{ ...s.cardAccent, background: c.color }} />
            <div style={s.cardBody}>
              <div style={s.cardHeader}>
                <div style={{ ...s.iconBox, background: c.color + '18', color: c.color }}>
                  <Icon name={c.icon} size={15} />
                </div>
                <span style={s.cardLabel}>{c.label}</span>
              </div>
              <div style={{ ...s.cardCount, color: c.color }}>{counts[c.key] ?? '—'}</div>
              <p style={s.cardHint}>Ver detalle</p>
            </div>
          </button>
        ))}
      </div>

      {/* Table section */}
      {activeCard ? (() => {
        const displayTotalPages = serverPaged ? totalPages : Math.ceil(tableData.length / PAGE_SIZE)
        const displayTotal      = serverPaged ? totalElements : tableData.length
        const paged             = serverPaged ? tableData : tableData.slice(tablePage * PAGE_SIZE, (tablePage + 1) * PAGE_SIZE)
        const activeCardObj     = cards.find(c => c.key === activeCard)
        return (
          <div style={s.tableSection}>
            <div style={s.tableHeader}>
              <div style={s.tableTitleRow}>
                <div style={{ ...s.tableDot, background: activeCardObj?.color }} />
                <p style={s.tableTitle}>{activeCardObj?.label}</p>
                {!tableLoading && displayTotal > 0 && (
                  <span style={s.tableCount}>{displayTotal} registros</span>
                )}
              </div>
              <button style={s.closeBtn} onClick={() => { setActiveCard(null); setTableData([]); setTablePage(0); setServerPaged(false) }}>
                Cerrar
              </button>
            </div>
            <DataTable data={paged} loading={tableLoading} />
            {!tableLoading && displayTotalPages > 1 && (
              <div style={s.pagination}>
                <span style={s.pageInfo}>
                  {tablePage * PAGE_SIZE + 1}–{Math.min((tablePage + 1) * PAGE_SIZE, displayTotal)} de {displayTotal}
                </span>
                <div style={{ display: 'flex', gap: '4px' }}>
                  <button style={s.pageBtn} onClick={() => changePage(tablePage - 1)} disabled={tablePage === 0}>‹</button>
                  {Array.from({ length: displayTotalPages }, (_, i) => (
                    <button
                      key={i}
                      style={{ ...s.pageBtn, ...(i === tablePage ? s.pageBtnActive : {}) }}
                      onClick={() => changePage(i)}
                    >
                      {i + 1}
                    </button>
                  ))}
                  <button style={s.pageBtn} onClick={() => changePage(tablePage + 1)} disabled={tablePage >= displayTotalPages - 1}>›</button>
                </div>
              </div>
            )}
          </div>
        )
      })() : (
        <div style={s.emptyHint}>
          <div style={s.emptyIconWrap}>
            <Icon name="bar-chart" size={32} />
          </div>
          <p style={s.emptyTitle}>Selecciona una tarjeta para ver el detalle</p>
          <p style={s.emptySub}>Haz clic en cualquier tarjeta de arriba para explorar los datos</p>
        </div>
      )}
    </div>
  )
}

const s = {
  greeting:   { marginBottom: '1.75rem' },
  greetTitle: { fontSize: '22px', fontWeight: '800', color: '#06175D', marginBottom: '4px' },
  greetSub:   { fontSize: '13px', color: '#9599AE' },
  empresa:    { color: '#06175D', fontWeight: '600' },

  cardGrid:   { display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(190px, 1fr))', gap: '1rem', marginBottom: '1.75rem' },
  card:       { background: '#fff', border: '1px solid #DDE0EE', borderRadius: '12px', padding: 0,
                textAlign: 'left', cursor: 'pointer', overflow: 'hidden',
                boxShadow: '0 1px 4px rgba(6,23,93,0.06)', transition: 'box-shadow .2s, transform .2s',
                display: 'flex', flexDirection: 'column' },
  cardAccent: { height: '4px', width: '100%' },
  cardBody:   { padding: '1rem 1.25rem 1.25rem' },
  cardHeader: { display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '10px' },
  iconBox:    { width: '28px', height: '28px', borderRadius: '7px', display: 'flex',
                alignItems: 'center', justifyContent: 'center', flexShrink: 0 },
  cardLabel:  { fontSize: '12px', fontWeight: '700', color: '#9599AE', textTransform: 'uppercase', letterSpacing: '.5px' },
  cardCount:  { fontSize: '34px', fontWeight: '800', lineHeight: 1, marginBottom: '6px' },
  cardHint:   { fontSize: '11px', color: '#9599AE' },

  tableSection:  { background: '#fff', borderRadius: '12px', border: '1px solid #DDE0EE',
                   overflow: 'hidden', boxShadow: '0 1px 4px rgba(6,23,93,0.06)' },
  tableHeader:   { display: 'flex', justifyContent: 'space-between', alignItems: 'center',
                   padding: '1rem 1.25rem', borderBottom: '1px solid #F0F2FA' },
  tableTitleRow: { display: 'flex', alignItems: 'center', gap: '10px' },
  tableDot:      { width: '10px', height: '10px', borderRadius: '50%' },
  tableTitle:    { fontWeight: '700', fontSize: '14px', color: '#06175D' },
  closeBtn:      { background: '#F0F2FA', border: 'none', borderRadius: '7px',
                   padding: '5px 14px', fontSize: '12px', color: '#9599AE', cursor: 'pointer', fontWeight: '600' },
  tableCount:    { fontSize: '12px', color: '#9599AE', background: '#F0F2FA', borderRadius: '20px',
                   padding: '2px 10px', fontWeight: '600' },

  pagination:    { display: 'flex', alignItems: 'center', justifyContent: 'space-between',
                   padding: '10px 14px', borderTop: '1px solid #F0F2FA' },
  pageInfo:      { fontSize: '12px', color: '#9599AE' },
  pageBtn:       { padding: '4px 10px', fontSize: '12px', fontWeight: '600', background: '#fff',
                   color: '#9599AE', border: '1px solid #DDE0EE', borderRadius: '6px', cursor: 'pointer' },
  pageBtnActive: { background: '#06175D', color: '#fff', borderColor: '#06175D' },

  emptyHint:     { background: '#fff', border: '1px dashed #DDE0EE', borderRadius: '12px',
                   padding: '3rem', textAlign: 'center' },
  emptyIconWrap: { color: '#DDE0EE', marginBottom: '1rem', display: 'flex', justifyContent: 'center' },
  emptyTitle:    { fontSize: '15px', fontWeight: '700', color: '#1A1D3B', marginBottom: '6px' },
  emptySub:      { fontSize: '13px', color: '#9599AE' },
}
