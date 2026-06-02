import { useEffect, useState } from 'react'
import { api } from '../api/client'
import PageHeader from '../components/PageHeader'
import { useAuth } from '../AuthContext'

const emptyForm = { idProducto: '', stock: '', min: '', max: '', activo: true }

export default function Stock() {
  const { session } = useAuth()

  const [almacen, setAlmacen] = useState(null)
  const [inventario, setInventario] = useState([])
  const [productosDisponibles, setProductosDisponibles] = useState([])
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [msg, setMsg] = useState({ text: '', ok: true })
  const [deletingKey, setDeletingKey] = useState(null)
  const [createForm, setCreateForm] = useState(emptyForm)
  const [editModal, setEditModal] = useState({ open: false, idAlmacen: null, idProducto: null, form: emptyForm })

  const showMsg = (text, ok = true) => setMsg({ text, ok })

  const cargarDatos = async (almacenId) => {
    setLoading(true)
    setMsg({ text: '', ok: true })
    try {
      const [provData, almData] = await Promise.all([
        api.get('/api/v1/proveedores'),
        api.get('/api/v1/almacenes'),
      ])

      const prov = (provData || []).find(p => p.idEmpresa?.id === session?.id_empresa)
      if (!prov) {
        showMsg('Tu empresa no tiene un perfil de proveedor activo.', false)
        setLoading(false)
        return
      }

      const empresaNombre = prov.idEmpresa?.nombre
      const alm = (almData || []).find(a => a.nombreProveedor === empresaNombre)
      if (!alm) {
        showMsg('Tu empresa no tiene un almacén registrado.', false)
        setLoading(false)
        return
      }

      const resolvedAlmacenId = almacenId ?? alm.id
      setAlmacen(alm)

      const [invData, prodData] = await Promise.all([
        api.get(`/api/v1/producto-almacen/almacen/${resolvedAlmacenId}`),
        api.get(`/api/v1/products/proveedor/${prov.id}`),
      ])

      const inv = invData || []
      setInventario(inv)

      const vinculadosIds = new Set(inv.map(i => i.idProducto))
      setProductosDisponibles((prodData || []).filter(p => !vinculadosIds.has(p.id)))
    } catch (e) {
      showMsg(`Error cargando datos: ${e.message}`, false)
    }
    setLoading(false)
  }

  useEffect(() => { cargarDatos() }, [])

  const vincular = async () => {
    setMsg({ text: '', ok: true })
    if (!createForm.idProducto || createForm.stock === '') {
      showMsg('Selecciona un producto e ingresa el stock inicial.', false)
      return
    }
    if (!almacen) {
      showMsg('No se encontró el almacén.', false)
      return
    }
    setSaving(true)
    try {
      await api.post('/api/v1/producto-almacen', {
        idAlmacen: almacen.id,
        idProducto: createForm.idProducto,
        stock: Number(createForm.stock),
        min: createForm.min !== '' ? Number(createForm.min) : null,
        max: createForm.max !== '' ? Number(createForm.max) : null,
        activo: createForm.activo,
      })
      showMsg('Producto vinculado al almacén.')
      setCreateForm(emptyForm)
      await cargarDatos(almacen.id)
    } catch (e) {
      showMsg(`Error: ${e.message}`, false)
    }
    setSaving(false)
  }

  const guardarEdicion = async () => {
    const { idAlmacen, idProducto, form } = editModal
    setSaving(true)
    try {
      await api.put(`/api/v1/producto-almacen/${idAlmacen}/${idProducto}`, {
        stock: Number(form.stock),
        min: form.min !== '' ? Number(form.min) : null,
        max: form.max !== '' ? Number(form.max) : null,
        activo: form.activo,
      })
      showMsg('Stock actualizado.')
      setEditModal({ open: false, idAlmacen: null, idProducto: null, form: emptyForm })
      await cargarDatos(almacen.id)
    } catch (e) {
      showMsg(`Error actualizando: ${e.message}`, false)
    }
    setSaving(false)
  }

  const desvincular = async (idAlmacen, idProducto) => {
    try {
      await api.delete(`/api/v1/producto-almacen/${idAlmacen}/${idProducto}`)
      setDeletingKey(null)
      showMsg('Producto desvinculado del almacén.')
      await cargarDatos(almacen.id)
    } catch (e) {
      showMsg(`Error eliminando: ${e.message}`, false)
    }
  }

  const abrirEditar = (item) =>
    setEditModal({
      open: true,
      idAlmacen: item.idAlmacen,
      idProducto: item.idProducto,
      form: {
        stock: item.stock ?? 0,
        min: item.min ?? '',
        max: item.max ?? '',
        activo: item.activo ?? true,
      },
    })

  const deletingKeyStr = (idAlmacen, idProducto) => `${idAlmacen}-${idProducto}`

  return (
    <div>
      <PageHeader
        title="Stock de almacenes"
        subtitle={almacen ? `Almacén: ${almacen.nombre}${almacen.direccion ? ` · ${almacen.direccion}` : ''}` : 'Cargando almacén...'}
        action={<button onClick={() => cargarDatos(almacen?.id)} style={styles.refreshBtn}>↺ Actualizar</button>}
      />

      {msg.text && (
        <div style={{
          ...styles.msg,
          borderColor: msg.ok ? '#bbf7d0' : '#fecaca',
          background: msg.ok ? '#f0fdf4' : '#fef2f2',
          color: msg.ok ? '#15803d' : '#b91c1c',
        }}>
          {msg.text}
        </div>
      )}

      {/* Formulario vincular */}
      <div style={styles.card}>
        <h3 style={styles.cardTitle}>Vincular producto al almacén</h3>
        <div style={styles.grid}>
          <div style={styles.full}>
            <label style={styles.label}>Producto</label>
            <select style={styles.input} value={createForm.idProducto}
              onChange={e => setCreateForm({ ...createForm, idProducto: e.target.value })}>
              <option value="">Selecciona un producto</option>
              {productosDisponibles.map(p => (
                <option key={p.id} value={p.id}>{p.nombre} {p.sku ? `(${p.sku})` : ''}</option>
              ))}
            </select>
            {productosDisponibles.length === 0 && !loading && (
              <p style={styles.hint}>Todos los productos ya están vinculados o no hay productos registrados.</p>
            )}
          </div>
          <div>
            <label style={styles.label}>Stock inicial</label>
            <input style={styles.input} type="number" min="0" value={createForm.stock}
              onChange={e => setCreateForm({ ...createForm, stock: e.target.value })}
              placeholder="0" />
          </div>
          <div>
            <label style={styles.label}>Mínimo (opcional)</label>
            <input style={styles.input} type="number" min="0" value={createForm.min}
              onChange={e => setCreateForm({ ...createForm, min: e.target.value })}
              placeholder="Sin límite" />
          </div>
          <div>
            <label style={styles.label}>Máximo (opcional)</label>
            <input style={styles.input} type="number" min="0" value={createForm.max}
              onChange={e => setCreateForm({ ...createForm, max: e.target.value })}
              placeholder="Sin límite" />
          </div>
          <div style={{ display: 'flex', alignItems: 'center', paddingTop: '22px' }}>
            <label style={{ ...styles.label, display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer', marginBottom: 0 }}>
              <input type="checkbox" checked={createForm.activo}
                onChange={e => setCreateForm({ ...createForm, activo: e.target.checked })} />
              Activo
            </label>
          </div>
        </div>
        <div style={styles.actions}>
          <button style={styles.clearBtn} onClick={() => setCreateForm(emptyForm)} disabled={saving}>Limpiar</button>
          <button style={styles.saveBtn} onClick={vincular} disabled={saving || !almacen}>
            {saving ? 'Guardando...' : 'Vincular producto'}
          </button>
        </div>
      </div>

      {/* Tabla inventario */}
      <div style={styles.tableWrapper}>
        {loading ? (
          <div style={styles.empty}><div style={styles.spinner} /> Cargando...</div>
        ) : inventario.length === 0 ? (
          <p style={styles.emptyText}>No hay productos vinculados a este almacén.</p>
        ) : (
          <table style={styles.table}>
            <thead>
              <tr>
                {['Producto', 'Stock', 'Mínimo', 'Máximo', 'Estado', 'Acciones'].map(h => (
                  <th key={h} style={styles.th}>{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {inventario.map((item, i) => {
                const key = deletingKeyStr(item.idAlmacen, item.idProducto)
                const stockBajo = item.min != null && item.stock < item.min
                return (
                  <tr key={key} style={{ background: i % 2 === 0 ? '#fff' : '#f8fafc' }}>
                    <td style={styles.td}>{item.nombreProducto}</td>
                    <td style={styles.td}>
                      <span style={{ fontWeight: 600, color: stockBajo ? '#dc2626' : '#0f172a' }}>
                        {item.stock}
                        {stockBajo && <span style={{ fontSize: '11px', marginLeft: '6px', color: '#dc2626' }}>⚠ bajo mínimo</span>}
                      </span>
                    </td>
                    <td style={styles.td}>{item.min ?? '—'}</td>
                    <td style={styles.td}>{item.max ?? '—'}</td>
                    <td style={styles.td}>
                      <span style={{ color: item.activo ? '#16a34a' : '#dc2626', fontWeight: 600 }}>
                        {item.activo ? 'Activo' : 'Inactivo'}
                      </span>
                    </td>
                    <td style={styles.td}>
                      {deletingKey === key ? (
                        <span style={{ display: 'flex', gap: '6px', alignItems: 'center' }}>
                          <span style={{ fontSize: '12px', color: '#64748b' }}>¿Desvincular?</span>
                          <button style={styles.btnDanger} onClick={() => desvincular(item.idAlmacen, item.idProducto)}>Sí</button>
                          <button style={styles.btnGhost} onClick={() => setDeletingKey(null)}>No</button>
                        </span>
                      ) : (
                        <span style={{ display: 'flex', gap: '6px' }}>
                          <button style={styles.btnEdit} onClick={() => abrirEditar(item)}>Editar</button>
                          <button style={styles.btnDel} onClick={() => setDeletingKey(key)}>Desvincular</button>
                        </span>
                      )}
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        )}
      </div>

      {/* Modal editar stock */}
      {editModal.open && (
        <div style={styles.overlay} onClick={() => setEditModal({ open: false, idAlmacen: null, idProducto: null, form: emptyForm })}>
          <div style={styles.modal} onClick={e => e.stopPropagation()}>
            <h3 style={{ margin: '0 0 1rem', fontSize: '16px', color: '#0f172a' }}>Editar stock</h3>
            <div style={styles.grid}>
              <div>
                <label style={styles.label}>Stock actual</label>
                <input style={styles.input} type="number" min="0" value={editModal.form.stock}
                  onChange={e => setEditModal({ ...editModal, form: { ...editModal.form, stock: e.target.value } })} />
              </div>
              <div>
                <label style={styles.label}>Mínimo</label>
                <input style={styles.input} type="number" min="0" value={editModal.form.min}
                  onChange={e => setEditModal({ ...editModal, form: { ...editModal.form, min: e.target.value } })}
                  placeholder="Sin límite" />
              </div>
              <div>
                <label style={styles.label}>Máximo</label>
                <input style={styles.input} type="number" min="0" value={editModal.form.max}
                  onChange={e => setEditModal({ ...editModal, form: { ...editModal.form, max: e.target.value } })}
                  placeholder="Sin límite" />
              </div>
              <div style={{ display: 'flex', alignItems: 'center', paddingTop: '22px' }}>
                <label style={{ ...styles.label, display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer', marginBottom: 0 }}>
                  <input type="checkbox" checked={editModal.form.activo}
                    onChange={e => setEditModal({ ...editModal, form: { ...editModal.form, activo: e.target.checked } })} />
                  Activo
                </label>
              </div>
            </div>
            <div style={styles.actions}>
              <button style={styles.clearBtn}
                onClick={() => setEditModal({ open: false, idAlmacen: null, idProducto: null, form: emptyForm })}
                disabled={saving}>Cancelar</button>
              <button style={styles.saveBtn} onClick={guardarEdicion} disabled={saving}>
                {saving ? 'Guardando...' : 'Guardar cambios'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

const styles = {
  refreshBtn: { background: '#fff', border: '1px solid #e2e8f0', borderRadius: '8px', padding: '8px 16px', fontSize: '13px', cursor: 'pointer', color: '#475569' },
  msg: { border: '1px solid', borderRadius: '8px', padding: '10px 14px', marginBottom: '1rem', fontSize: '13px' },
  card: { background: '#fff', border: '1px solid #e2e8f0', borderRadius: '12px', padding: '1rem', marginBottom: '1rem' },
  cardTitle: { margin: '0 0 1rem', fontSize: '16px', color: '#0f172a' },
  grid: { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' },
  full: { gridColumn: '1 / -1' },
  label: { display: 'block', fontSize: '13px', fontWeight: '600', color: '#475569', marginBottom: '6px' },
  hint: { margin: '4px 0 0', fontSize: '12px', color: '#94a3b8' },
  input: { width: '100%', padding: '10px 12px', border: '1.5px solid #e2e8f0', borderRadius: '8px', fontSize: '14px', boxSizing: 'border-box', background: '#fff', color: '#0f172a', outline: 'none' },
  actions: { marginTop: '1rem', display: 'flex', justifyContent: 'flex-end', gap: '8px' },
  clearBtn: { padding: '10px 16px', background: '#fff', color: '#475569', border: '1.5px solid #e2e8f0', borderRadius: '8px', fontSize: '14px', fontWeight: '600', cursor: 'pointer' },
  saveBtn: { padding: '10px 16px', background: '#1e293b', color: '#fff', border: 'none', borderRadius: '8px', fontSize: '14px', fontWeight: '600', cursor: 'pointer' },
  tableWrapper: { overflowX: 'auto', border: '1px solid #e2e8f0', borderRadius: '10px', background: '#fff' },
  table: { width: '100%', borderCollapse: 'collapse', fontSize: '13px' },
  th: { padding: '10px 14px', background: '#f1f5f9', color: '#475569', fontWeight: '600', textAlign: 'left', whiteSpace: 'nowrap', borderBottom: '1px solid #e2e8f0' },
  td: { padding: '9px 14px', color: '#334155', borderBottom: '1px solid #f1f5f9', whiteSpace: 'nowrap' },
  empty: { display: 'flex', alignItems: 'center', gap: '10px', padding: '2rem', color: '#94a3b8', fontSize: '14px' },
  emptyText: { color: '#94a3b8', padding: '1.5rem', fontSize: '14px', margin: 0 },
  spinner: { width: '16px', height: '16px', border: '2px solid #e2e8f0', borderTop: '2px solid #3b82f6', borderRadius: '50%', animation: 'spin 0.8s linear infinite' },
  btnEdit: { padding: '5px 12px', fontSize: '12px', fontWeight: '600', background: '#eff6ff', color: '#1d4ed8', border: '1px solid #bfdbfe', borderRadius: '6px', cursor: 'pointer' },
  btnDel: { padding: '5px 12px', fontSize: '12px', fontWeight: '600', background: '#fff', color: '#dc2626', border: '1px solid #fecaca', borderRadius: '6px', cursor: 'pointer' },
  btnDanger: { padding: '4px 10px', fontSize: '12px', fontWeight: '600', background: '#dc2626', color: '#fff', border: 'none', borderRadius: '6px', cursor: 'pointer' },
  btnGhost: { padding: '4px 10px', fontSize: '12px', fontWeight: '600', background: '#f1f5f9', color: '#475569', border: '1px solid #e2e8f0', borderRadius: '6px', cursor: 'pointer' },
  overlay: { position: 'fixed', inset: 0, background: 'rgba(15,23,42,0.45)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 },
  modal: { background: '#fff', borderRadius: '12px', padding: '1.5rem', width: '480px', maxWidth: '95vw', boxShadow: '0 20px 60px rgba(0,0,0,0.25)' },
}
