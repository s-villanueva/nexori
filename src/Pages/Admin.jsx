import { useNavigate } from 'react-router-dom'
import { useAuth } from '../AuthContext'

const CARD_ICONS = {
  empresas:    <><path d="M3 9l9-6 9 6v11a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1z"/><line x1="9" y1="22" x2="9" y2="12"/><line x1="15" y1="22" x2="15" y2="12"/><line x1="9" y1="12" x2="15" y2="12"/></>,
  usuarios:    <><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></>,
  proveedores: <><rect x="1" y="3" width="15" height="13"/><polygon points="16 8 20 8 23 11 23 16 16 16 16 8"/><circle cx="5.5" cy="18.5" r="2.5"/><circle cx="18.5" cy="18.5" r="2.5"/></>,
  logs:        <><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></>,
}

const CARDS = [
  { icon: 'empresas',    title: 'Empresas',    sub: 'Gestionar empresas registradas',    to: '/admin/empresas'    },
  { icon: 'usuarios',    title: 'Usuarios',    sub: 'Gestionar usuarios del sistema',    to: '/admin/usuarios'    },
  { icon: 'proveedores', title: 'Proveedores', sub: 'Aprobar y gestionar proveedores',   to: '/admin/proveedores' },
  { icon: 'logs',        title: 'Logs',        sub: 'Auditoría del sistema',             to: '/admin/logs'        },
]

export default function Admin() {
  const { session, logout } = useAuth()
  const navigate = useNavigate()

  return (
    <div style={styles.bg}>
      <div style={styles.topBar}>
        <span style={styles.brand}>Panel Admin</span>
        <div style={styles.userInfo}>
          <span style={styles.userName}>{session?.nombre}</span>
          <button style={styles.logoutBtn} onClick={logout}>Cerrar sesión</button>
        </div>
      </div>

      <div style={styles.content}>
        <h2 style={styles.title}>Bienvenido, {session?.nombre}</h2>
        <p style={styles.sub}>Selecciona una sección para administrar</p>

        <div style={styles.grid}>
          {CARDS.map(c => (
            <div key={c.to} style={styles.card} onClick={() => navigate(c.to)}>
              <div style={styles.icon}>
                <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#06175D" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                  {CARD_ICONS[c.icon]}
                </svg>
              </div>
              <p style={styles.cardTitle}>{c.title}</p>
              <p style={styles.cardSub}>{c.sub}</p>
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}

const styles = {
  bg: { minHeight: '100vh', background: '#F0F2FA' },
  topBar: { background: '#06175D', padding: '0 2rem', height: '56px', display: 'flex', alignItems: 'center', justifyContent: 'space-between' },
  brand: { color: '#fff', fontWeight: '700', fontSize: '16px' },
  userInfo: { display: 'flex', alignItems: 'center', gap: '1rem' },
  userName: { color: 'rgba(255,255,255,0.65)', fontSize: '14px' },
  logoutBtn: { padding: '6px 14px', background: 'transparent', color: '#fff', border: '1px solid rgba(255,255,255,0.3)', borderRadius: '6px', fontSize: '13px', cursor: 'pointer' },
  content: { padding: '2rem' },
  title: { margin: '0 0 4px', fontSize: '22px', fontWeight: '700', color: '#06175D' },
  sub: { margin: '0 0 2rem', fontSize: '14px', color: '#9599AE' },
  grid: { display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))', gap: '1rem' },
  card: { background: '#fff', border: '1px solid #DDE0EE', borderRadius: '12px', padding: '1.5rem', cursor: 'pointer', transition: 'box-shadow 0.2s', boxShadow: '0 1px 4px rgba(6,23,93,0.06)' },
  icon: { marginBottom: '0.25rem' },
  cardTitle: { margin: '0.75rem 0 4px', fontWeight: '700', fontSize: '15px', color: '#1A1D3B' },
  cardSub: { margin: 0, fontSize: '13px', color: '#9599AE' },
}
