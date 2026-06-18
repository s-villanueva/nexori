import { useState } from 'react'
import { api } from '../api/client'
import { useNavigate } from 'react-router-dom'

const STEPS = ['Empresa', 'Contacto', 'Sucursal', 'Usuario']

export default function Registro() {
  const navigate = useNavigate()

  const [step, setStep] = useState(0)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState(false)

  const [empresa, setEmpresa] = useState({ nombre: '', razon_social: '', nit: '', dominio: '' })
  const [contacto, setContacto] = useState({ nombres: '', apellidos: '', cargo: '' })
  const [sucursal, setSucursal] = useState({ nombre: '', direccion: '' })
  const [usuario, setUsuario] = useState({ nombre: '', email: '', password: '' })

  const next = () => {
    setError('')
    if (step === 0 && (!empresa.nombre || !empresa.razon_social || !empresa.nit || !empresa.dominio)) {
      setError('Completa todos los datos de la empresa.')
      return
    }
    if (step === 1 && (!contacto.nombres || !contacto.apellidos)) {
      setError('Completa los datos del contacto.')
      return
    }
    if (step === 2 && (!sucursal.nombre || !sucursal.direccion)) {
      setError('Completa todos los datos de la sucursal.')
      return
    }
    setStep((s) => s + 1)
  }

  const back = () => { setError(''); setStep((s) => s - 1) }

  const handleSubmit = async () => {
    setError('')
    if (!usuario.nombre || !usuario.email || !usuario.password) {
      setError('Completa los datos del usuario.')
      return
    }
    setLoading(true)
    try {
      await api.post('/api/v1/auth/register', {
        empresa: {
          nombre:       empresa.nombre,
          razon_social: empresa.razon_social,
          nit:          empresa.nit,
          dominio:      empresa.dominio,
        },
        contacto: {
          nombres:   contacto.nombres,
          apellidos: contacto.apellidos,
          cargo:     contacto.cargo,
        },
        sucursal: {
          nombre:    sucursal.nombre,
          direccion: sucursal.direccion,
        },
        usuario: {
          nombre:   usuario.nombre,
          email:    usuario.email,
          password: usuario.password,
        },
      })
      setSuccess(true)
    } catch (e) {
      setError(e.message || 'Error durante el registro.')
    }
    setLoading(false)
  }

  if (success) {
    return (
      <div style={styles.bg}>
        <div style={styles.card}>
          <div style={styles.successIcon}>✓</div>
          <p style={styles.successTitle}>¡Empresa registrada!</p>
          <p style={styles.successSub}>Ya podés iniciar sesión con <strong>{usuario.email}</strong></p>
          <button style={styles.submit} onClick={() => navigate('/login')}>Ir al login →</button>
        </div>
      </div>
    )
  }

  return (
    <div style={styles.bg}>
      <div style={styles.card}>
        <div style={styles.logoRow}>
          <div style={styles.logoCircle}>
            <svg width="28" height="28" viewBox="0 0 28 28" fill="none">
              <rect width="28" height="28" rx="8" fill="#06175D" />
              <path d="M7 20L14 8L21 20H7Z" fill="white" opacity="0.9" />
            </svg>
          </div>
          <div>
            <p style={styles.logoTitle}>Marketplace B2B</p>
            <p style={styles.logoSub}>Registro de empresa</p>
          </div>
        </div>

        <div style={styles.stepper}>
          {STEPS.map((s, i) => (
            <div key={s} style={styles.stepItem}>
              <div style={{ ...styles.stepCircle, background: i <= step ? '#06175D' : '#DDE0EE', color: i <= step ? '#fff' : '#9599AE' }}>
                {i < step ? '✓' : i + 1}
              </div>
              <span style={{ ...styles.stepLabel, color: i <= step ? '#1A1D3B' : '#9599AE' }}>{s}</span>
              {i < STEPS.length - 1 && <div style={{ ...styles.stepLine, background: i < step ? '#06175D' : '#DDE0EE' }} />}
            </div>
          ))}
        </div>

        {error && <p style={styles.error}>{error}</p>}

        {step === 0 && (
          <div style={styles.form}>
            <p style={styles.stepTitle}>Datos de la empresa</p>
            <Field label="Nombre comercial" value={empresa.nombre} onChange={(v) => setEmpresa({ ...empresa, nombre: v })} placeholder="Ej: TechCorp" />
            <Field label="Razón social" value={empresa.razon_social} onChange={(v) => setEmpresa({ ...empresa, razon_social: v })} placeholder="Ej: TechCorp S.R.L." />
            <Field label="NIT" value={empresa.nit} onChange={(v) => setEmpresa({ ...empresa, nit: v })} placeholder="Ej: 12345678" />
            <Field label="Dominio" value={empresa.dominio} onChange={(v) => setEmpresa({ ...empresa, dominio: v })} placeholder="Ej: techcorp.com" />
          </div>
        )}

        {step === 1 && (
          <div style={styles.form}>
            <p style={styles.stepTitle}>Contacto principal</p>
            <Field label="Nombres" value={contacto.nombres} onChange={(v) => setContacto({ ...contacto, nombres: v })} placeholder="Ej: Ana" />
            <Field label="Apellidos" value={contacto.apellidos} onChange={(v) => setContacto({ ...contacto, apellidos: v })} placeholder="Ej: García" />
            <Field label="Cargo (opcional)" value={contacto.cargo} onChange={(v) => setContacto({ ...contacto, cargo: v })} placeholder="Ej: Gerente General" />
          </div>
        )}

        {step === 2 && (
          <div style={styles.form}>
            <p style={styles.stepTitle}>Sucursal principal</p>
            <Field label="Nombre de la sucursal" value={sucursal.nombre} onChange={(v) => setSucursal({ ...sucursal, nombre: v })} placeholder="Ej: Oficina Central" />
            <Field label="Dirección" value={sucursal.direccion} onChange={(v) => setSucursal({ ...sucursal, direccion: v })} placeholder="Ej: Av. Bush 123, Santa Cruz" />
          </div>
        )}

        {step === 3 && (
          <div style={styles.form}>
            <p style={styles.stepTitle}>Usuario de acceso</p>
            <Field label="Nombre completo" value={usuario.nombre} onChange={(v) => setUsuario({ ...usuario, nombre: v })} placeholder="Ej: Ana García" />
            <Field label="Email" value={usuario.email} onChange={(v) => setUsuario({ ...usuario, email: v })} placeholder="Ej: ana@techcorp.com" type="email" />
            <Field label="Contraseña" value={usuario.password} onChange={(v) => setUsuario({ ...usuario, password: v })} placeholder="••••••••" type="password" />
            <p style={{ margin: 0, fontSize: '12px', color: '#9599AE' }}>
              Tu cuenta será creada con rol de empresa compradora.
            </p>
          </div>
        )}

        <div style={styles.navRow}>
          {step > 0 && <button style={styles.backBtn} onClick={back} disabled={loading}>← Atrás</button>}
          {step < STEPS.length - 1 && <button style={{ ...styles.submit, marginTop: 0 }} onClick={next}>Siguiente →</button>}
          {step === STEPS.length - 1 && (
            <button style={{ ...styles.submit, marginTop: 0 }} onClick={handleSubmit} disabled={loading}>
              {loading ? 'Registrando...' : 'Crear cuenta →'}
            </button>
          )}
        </div>

        <p style={styles.loginLink}>
          ¿Ya tenés cuenta?{' '}
          <span style={{ color: '#06175D', cursor: 'pointer', fontWeight: 600 }} onClick={() => navigate('/login')}>
            Iniciá sesión
          </span>
        </p>
      </div>
    </div>
  )
}

function Field({ label, value, onChange, placeholder, type = 'text' }) {
  return (
    <div>
      <label style={styles.label}>{label}</label>
      <input style={styles.input} type={type} value={value} onChange={(e) => onChange(e.target.value)} placeholder={placeholder} />
    </div>
  )
}

const styles = {
  bg: { minHeight: '100vh', background: '#F0F2FA', display: 'flex', alignItems: 'center', justifyContent: 'center' },
  card: { background: '#fff', borderRadius: '16px', padding: '2.5rem', width: '100%', maxWidth: '440px', boxShadow: '0 4px 24px rgba(6,23,93,0.10)' },
  logoRow: { display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '2rem' },
  logoCircle: { width: '44px', height: '44px', borderRadius: '10px', overflow: 'hidden', flexShrink: 0 },
  logoTitle: { margin: 0, fontWeight: '700', fontSize: '15px', color: '#06175D' },
  logoSub: { margin: 0, fontSize: '12px', color: '#9599AE' },
  stepper: { display: 'flex', alignItems: 'center', marginBottom: '1.75rem' },
  stepItem: { display: 'flex', alignItems: 'center', flex: 1 },
  stepCircle: { width: '26px', height: '26px', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '11px', fontWeight: '700', flexShrink: 0 },
  stepLabel: { fontSize: '11px', fontWeight: '500', marginLeft: '5px', whiteSpace: 'nowrap' },
  stepLine: { flex: 1, height: '2px', margin: '0 6px' },
  stepTitle: { margin: '0 0 1rem', fontWeight: '600', fontSize: '15px', color: '#1A1D3B' },
  form: { display: 'flex', flexDirection: 'column', gap: '0.85rem', marginBottom: '1.25rem' },
  label: { display: 'block', fontSize: '13px', fontWeight: '500', color: '#9599AE', marginBottom: '5px' },
  input: { width: '100%', padding: '10px 12px', border: '1.5px solid #DDE0EE', borderRadius: '8px', fontSize: '14px', color: '#1A1D3B', outline: 'none', boxSizing: 'border-box', background: '#fff' },
  error: { color: '#dc2626', fontSize: '13px', marginBottom: '0.75rem', background: '#fef2f2', padding: '8px 12px', borderRadius: '6px' },
  navRow: { display: 'flex', gap: '8px', justifyContent: 'flex-end' },
  backBtn: { padding: '10px 18px', border: '1.5px solid #DDE0EE', borderRadius: '8px', background: '#fff', fontSize: '14px', fontWeight: '500', color: '#9599AE', cursor: 'pointer' },
  submit: { padding: '10px 18px', background: '#06175D', color: '#fff', border: 'none', borderRadius: '8px', fontSize: '14px', fontWeight: '600', cursor: 'pointer', marginTop: '0.5rem' },
  loginLink: { marginTop: '1.25rem', fontSize: '13px', color: '#9599AE', textAlign: 'center' },
  successIcon: { width: '56px', height: '56px', borderRadius: '50%', background: '#dcfce7', color: '#166534', fontSize: '24px', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 1rem' },
  successTitle: { margin: '0 0 6px', fontWeight: '700', fontSize: '20px', color: '#1A1D3B', textAlign: 'center' },
  successSub: { margin: '0 0 1.5rem', fontSize: '14px', color: '#9599AE', textAlign: 'center' },
}
