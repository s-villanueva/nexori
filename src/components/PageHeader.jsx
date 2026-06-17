export default function PageHeader({ title, subtitle, action }) {
  return (
    <div style={s.wrap}>
      <div>
        <h1 style={s.title}>{title}</h1>
        {subtitle && <p style={s.sub}>{subtitle}</p>}
      </div>
      {action && <div>{action}</div>}
    </div>
  )
}

const s = {
  wrap:  { display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between',
           marginBottom: '1.5rem' },
  title: { fontSize: '20px', fontWeight: '700', color: '#06175D', margin: 0 },
  sub:   { fontSize: '13px', color: '#9599AE', marginTop: '3px' },
}
