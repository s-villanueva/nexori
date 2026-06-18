export default function DataTable({ data, loading, emptyMsg = 'Sin datos.' }) {
  if (loading) return (
    <div style={s.empty}>
      <div style={s.spinner} />
      <span style={{ color: '#9599AE' }}>Cargando...</span>
    </div>
  )
  if (!data || data.length === 0) return <p style={s.emptyText}>{emptyMsg}</p>

  const cols = Object.keys(data[0])

  return (
    <div style={s.wrapper}>
      <table style={s.table}>
        <thead>
          <tr>
            {cols.map(c => (
              <th key={c} style={s.th}>{c.replace(/_/g, ' ')}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {data.map((row, i) => (
            <tr key={i} style={{ background: i % 2 === 0 ? '#fff' : '#F7F8FC' }}>
              {cols.map(c => (
                <td key={c} style={s.td}>
                  {row[c] === null || row[c] === undefined
                    ? <span style={{ color: '#C5C8D8' }}>—</span>
                    : typeof row[c] === 'boolean'
                      ? (
                        <span style={{
                          display: 'inline-block',
                          width: '10px', height: '10px', borderRadius: '50%',
                          background: row[c] ? '#16a34a' : '#dc2626',
                          boxShadow: row[c] ? '0 0 0 3px #dcfce7' : '0 0 0 3px #fee2e2',
                        }} title={row[c] ? 'Activo' : 'Inactivo'} />
                      )
                      : String(row[c])}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

const s = {
  wrapper: { overflowX: 'auto', borderRadius: '10px', border: '1px solid #DDE0EE', background: '#fff' },
  table:   { width: '100%', borderCollapse: 'collapse', fontSize: '13px' },
  th:      { padding: '11px 14px', background: '#EEF1FB', color: '#06175D', fontWeight: '700',
             textAlign: 'left', whiteSpace: 'nowrap', borderBottom: '1px solid #DDE0EE',
             textTransform: 'capitalize', fontSize: '12px', letterSpacing: '.3px' },
  td:      { padding: '10px 14px', color: '#1A1D3B', borderBottom: '1px solid #F0F2FA', whiteSpace: 'nowrap' },
  empty:   { display: 'flex', alignItems: 'center', gap: '10px', padding: '2.5rem', justifyContent: 'center' },
  emptyText: { color: '#9599AE', padding: '2rem', fontSize: '13px', textAlign: 'center' },
  spinner: { width: '18px', height: '18px', border: '2px solid #DDE0EE',
             borderTop: '2px solid #06175D', borderRadius: '50%', animation: 'spin 0.8s linear infinite' },
}
