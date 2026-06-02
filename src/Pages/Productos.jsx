import { useEffect, useState } from 'react'
import { api } from '../api/client'
import PageHeader from '../components/PageHeader'
import { useAuth } from '../AuthContext'

const emptyForm = { sku: '', nombre: '', descripcion: '', unidadMedida: '', idCategoria: '', activo: true }

export default function Productos() {
  const { session } = useAuth()

  const [categorias, setCategorias] = useState([])
  const [productos, setProductos] = useState([])
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [msg, setMsg] = useState({ text: '', ok: true })
  const [idProveedorActual, setIdProveedorActual] = useState(null)
  const [deletingId, setDeletingId] = useState(null)
  const [createForm, setCreateForm] = useState(emptyForm)
  const [editModal, setEditModal] = useState({ open: false, id: null, form: emptyForm })

  const showMsg = (text, ok = true) => setMsg({ text, ok })

  const cargarDatos = async (idProv) => {
    setLoading(true)
    setMsg({ text: '', ok: true })
    try {
      const [catData, provData] = await Promise.all([
        api.get('/api/v1/categorias'),
        api.get('/api/v1/proveedores'),
      ])
      setCategorias(catData || [])
      const prov = (provData || []).find(p => p.idEmpresa?.id === session?.id_empresa)
      const resolvedId = idProv ?? prov?.id ?? null
      setIdProveedorActual(resolvedId)
      if (resolvedId) {
        const prodData = await api.get(`/api/v1/products/proveedor/${resolvedId}`)
        setProductos(prodData || [])
      } else {
        setProductos([])
      }
    } catch (e) {
      showMsg(`Error cargando datos: ${e.message}`, false)
    }
    setLoading(false)
  }

  useEffect(() => { cargarDatos() }, [])

  const crearProducto = async () => {
    setMsg({ text: '', ok: true })
    if (!createForm.sku || !createForm.nombre || !createForm.unidadMedida || !createForm.idCategoria) {
      showMsg('Completa SKU, nombre, unidad de medida y categoría.', false)
      return
    }
    if (!idProveedorActual) {
      showMsg('Tu empresa no tiene un perfil de proveedor activo.', false)
      return
    }
    setSaving(true)
    try {
      await api.post('/api/v1/products', {
        sku: createForm.sku,
        nombre: createForm.nombre,
        descripcion: createForm.descripcion,
        unidadMedida: createForm.unidadMedida,
        activo: true,
        idCategoria: createForm.idCategoria,
        idProveedor: idProveedorActual,
      })
      showMsg('Producto agregado correctamente.')
      setCreateForm(emptyForm)
      await cargarDatos(idProveedorActual)
    } catch (e) {
      showMsg(`Error: ${e.message}`, false)
    }
    setSaving(false)
  }

  const guardarEdicion = async () => {
    const { id, form } = editModal
    setSaving(true)
    try {
      await api.put(`/api/v1/products/${id}`, {
        sku: form.sku,
        nombre: form.nombre,
        descripcion: form.descripcion,
        unidadMedida: form.unidadMedida,
        activo: form.activo,
        idCategoria: form.idCategoria,
        idProveedor: idProveedorActual,
      })
      showMsg('Producto actualizado.')
      setEditModal({ open: false, id: null, form: emptyForm })
      await cargarDatos(idProveedorActual)
    } catch (e) {
      showMsg(`Error actualizando: ${e.message}`, false)
    }
    setSaving(false)
  }

  const eliminar = async (id) => {
    try {
      await api.delete(`/api/v1/products/${id}`)
      setDeletingId(null)
      showMsg('Producto eliminado.')
      await cargarDatos(idProveedorActual)
    } catch (e) {
      showMsg(`Error eliminando: ${e.message}`, false)
    }
  }

  const abrirEditar = (p) =>
    setEditModal({
      open: true,
      id: p.id,
      form: {
        sku: p.sku || '',
        nombre: p.nombre || '',
        descripcion: p.descripcion || '',
        unidadMedida: p.unidadMedida || '',
        idCategoria: p.idCategoria?.id || '',
        activo: p.activo ?? true,
      },
    })

  return (
    <div>
      <PageHeader
        title="Productos"
        subtitle="Productos registrados en el almacén de tu empresa"
        action={<button onClick={() => cargarDatos(idProveedorActual)} style={styles.refreshBtn}>↺ Actualizar</button>}
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

      {/* Formulario crear */}
      <div style={styles.card}>
        <h3 style={styles.cardTitle}>Agregar producto</h3>
        <div style={styles.grid}>
          <div>
            <label style={styles.label}>SKU</label>
            <input style={styles.input} value={createForm.sku}
              onChange={e => setCreateForm({ ...createForm, sku: e.target.value })}
              placeholder="Ej: PROD-001" />
          </div>
          <div>
            <label style={styles.label}>Nombre</label>
            <input style={styles.input} value={createForm.nombre}
              onChange={e => setCreateForm({ ...createForm, nombre: e.target.value })}
              placeholder="Ej: Arroz 1kg" />
          </div>
          <div>
            <label style={styles.label}>Unidad de medida</label>
            <input style={styles.input} value={createForm.unidadMedida}
              onChange={e => setCreateForm({ ...createForm, unidadMedida: e.target.value })}
              placeholder="Ej: unidad, kg, caja" />
          </div>
          <div>
            <label style={styles.label}>Categoría</label>
            <select style={styles.input} value={createForm.idCategoria}
              onChange={e => setCreateForm({ ...createForm, idCategoria: e.target.value })}>
              <option value="">Selecciona una categoría</option>
              {categorias.map(c => <option key={c.id} value={c.id}>{c.nombre}</option>)}
            </select>
          </div>
          <div style={styles.full}>
            <label style={styles.label}>Descripción</label>
            <textarea style={{ ...styles.input, minHeight: '70px', resize: 'vertical' }}
              value={createForm.descripcion}
              onChange={e => setCreateForm({ ...createForm, descripcion: e.target.value })}
              placeholder="Descripción del producto" />
          </div>
        </div>
        <div style={styles.actions}>
          <button style={styles.clearBtn} onClick={() => setCreateForm(emptyForm)} disabled={saving}>Limpiar</button>
          <button style={styles.saveBtn} onClick={crearProducto} disabled={saving}>
            {saving ? 'Guardando...' : 'Agregar producto'}
          </button>
        </div>
      </div>

      {/* Tabla de productos */}
      <div style={styles.tableWrapper}>
        {loading ? (
          <div style={styles.empty}><div style={styles.spinner} /> Cargando...</div>
        ) : productos.length === 0 ? (
          <p style={styles.emptyText}>No hay productos registrados en tu almacén.</p>
        ) : (
          <table style={styles.table}>
            <thead>
              <tr>
                {['SKU', 'Nombre', 'Descripción', 'Unidad', 'Categoría', 'Estado', 'Acciones'].map(h => (
                  <th key={h} style={styles.th}>{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {productos.map((p, i) => (
                <tr key={p.id} style={{ background: i % 2 === 0 ? '#fff' : '#f8fafc' }}>
                  <td style={styles.td}>{p.sku || '—'}</td>
                  <td style={styles.td}>{p.nombre}</td>
                  <td style={{ ...styles.td, maxWidth: '180px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                    {p.descripcion || '—'}
                  </td>
                  <td style={styles.td}>{p.unidadMedida || '—'}</td>
                  <td style={styles.td}>{p.idCategoria?.nombre || p.nombreCategoria || '—'}</td>
                  <td style={styles.td}>
                    <span style={{ color: p.activo ? '#16a34a' : '#dc2626', fontWeight: 600 }}>
                      {p.activo ? 'Activo' : 'Inactivo'}
                    </span>
                  </td>
                  <td style={styles.td}>
                    {deletingId === p.id ? (
                      <span style={{ display: 'flex', gap: '6px', alignItems: 'center' }}>
                        <span style={{ fontSize: '12px', color: '#64748b' }}>¿Confirmar?</span>
                        <button style={styles.btnDanger} onClick={() => eliminar(p.id)}>Sí</button>
                        <button style={styles.btnGhost} onClick={() => setDeletingId(null)}>No</button>
                      </span>
                    ) : (
                      <span style={{ display: 'flex', gap: '6px' }}>
                        <button style={styles.btnEdit} onClick={() => abrirEditar(p)}>Editar</button>
                        <button style={styles.btnDel} onClick={() => setDeletingId(p.id)}>Eliminar</button>
                      </span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* Modal editar */}
      {editModal.open && (
        <div style={styles.overlay} onClick={() => setEditModal({ open: false, id: null, form: emptyForm })}>
          <div style={styles.modal} onClick={e => e.stopPropagation()}>
            <h3 style={{ margin: '0 0 1rem', fontSize: '16px', color: '#0f172a' }}>Editar producto</h3>
            <div style={styles.grid}>
              <div>
                <label style={styles.label}>SKU</label>
                <input style={styles.input} value={editModal.form.sku}
                  onChange={e => setEditModal({ ...editModal, form: { ...editModal.form, sku: e.target.value } })} />
              </div>
              <div>
                <label style={styles.label}>Nombre</label>
                <input style={styles.input} value={editModal.form.nombre}
                  onChange={e => setEditModal({ ...editModal, form: { ...editModal.form, nombre: e.target.value } })} />
              </div>
              <div>
                <label style={styles.label}>Unidad de medida</label>
                <input style={styles.input} value={editModal.form.unidadMedida}
                  onChange={e => setEditModal({ ...editModal, form: { ...editModal.form, unidadMedida: e.target.value } })} />
              </div>
              <div>
                <label style={styles.label}>Categoría</label>
                <select style={styles.input} value={editModal.form.idCategoria}
                  onChange={e => setEditModal({ ...editModal, form: { ...editModal.form, idCategoria: e.target.value } })}>
                  <option value="">Selecciona una categoría</option>
                  {categorias.map(c => <option key={c.id} value={c.id}>{c.nombre}</option>)}
                </select>
              </div>
              <div style={styles.full}>
                <label style={styles.label}>Descripción</label>
                <textarea style={{ ...styles.input, minHeight: '70px', resize: 'vertical' }}
                  value={editModal.form.descripcion}
                  onChange={e => setEditModal({ ...editModal, form: { ...editModal.form, descripcion: e.target.value } })} />
              </div>
              <div style={styles.full}>
                <label style={{ ...styles.label, display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer' }}>
                  <input type="checkbox" checked={editModal.form.activo}
                    onChange={e => setEditModal({ ...editModal, form: { ...editModal.form, activo: e.target.checked } })} />
                  Activo
                </label>
              </div>
            </div>
            <div style={styles.actions}>
              <button style={styles.clearBtn}
                onClick={() => setEditModal({ open: false, id: null, form: emptyForm })}
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
  modal: { background: '#fff', borderRadius: '12px', padding: '1.5rem', width: '560px', maxWidth: '95vw', maxHeight: '90vh', overflowY: 'auto', boxShadow: '0 20px 60px rgba(0,0,0,0.25)' },
}
