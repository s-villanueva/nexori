BEGIN;

INSERT INTO empresa (id_empresa, nombre, dominio, activo, nit, razon_social)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'TechNova Compras', 'technova.bo', true, '100000001', 'TechNova Compras S.R.L.'),
    ('22222222-2222-2222-2222-222222222222', 'Distribuciones Andinas', 'andinas.bo', true, '100000002', 'Distribuciones Andinas S.A.'),
    ('33333333-3333-3333-3333-333333333333', 'Suministros Altiplano', 'altiplano.bo', true, '100000003', 'Suministros Altiplano S.R.L.')
ON CONFLICT (id_empresa) DO UPDATE
SET nombre = EXCLUDED.nombre,
    dominio = EXCLUDED.dominio,
    activo = EXCLUDED.activo,
    nit = EXCLUDED.nit,
    razon_social = EXCLUDED.razon_social;

INSERT INTO rol_usuario (id_rol, nombre, descripcion)
VALUES
    ('44444444-4444-4444-4444-444444444441', 'ADMIN_TEST', 'Rol administrativo para datos de prueba'),
    ('44444444-4444-4444-4444-444444444442', 'COMPRADOR_TEST', 'Rol comprador para datos de prueba')
ON CONFLICT (id_rol) DO UPDATE
SET nombre = EXCLUDED.nombre,
    descripcion = EXCLUDED.descripcion;

INSERT INTO cargo_empresa (id_cargo_empresa, nombre)
VALUES
    ('55555555-5555-5555-5555-555555555551', 'Gerente de Compras'),
    ('55555555-5555-5555-5555-555555555552', 'Analista de Abastecimiento')
ON CONFLICT (id_cargo_empresa) DO UPDATE
SET nombre = EXCLUDED.nombre;

INSERT INTO sucursal_empresa (id_sucursal, nombre, direccion, coordenadas, activo, id_empresa)
VALUES
    ('66666666-6666-6666-6666-666666666661', 'Sucursal Central TN', 'Av. Busch #100, La Paz', -16.50000, true, '11111111-1111-1111-1111-111111111111'),
    ('66666666-6666-6666-6666-666666666662', 'Sucursal Norte TN', 'Av. Banzer #240, Santa Cruz', -17.78000, true, '11111111-1111-1111-1111-111111111111')
ON CONFLICT (id_sucursal) DO UPDATE
SET nombre = EXCLUDED.nombre,
    direccion = EXCLUDED.direccion,
    coordenadas = EXCLUDED.coordenadas,
    activo = EXCLUDED.activo,
    id_empresa = EXCLUDED.id_empresa;

INSERT INTO usuario (id_usuario, nombre, email, password_hash, activo, id_empresa, id_sucursal, id_rol)
VALUES
    ('77777777-7777-7777-7777-777777777771', 'Admin TechNova', 'admin.test@technova.bo', '{bcrypt}$2a$10$GiTk0ms5obxTpxdOGTeufeKET6eOCeZgCbpV8UnBWs2ICufqnUN.G', true, '11111111-1111-1111-1111-111111111111', '66666666-6666-6666-6666-666666666661', '44444444-4444-4444-4444-444444444441'),
    ('77777777-7777-7777-7777-777777777772', 'Comprador TechNova', 'comprador.test@technova.bo', '{bcrypt}$2a$10$GiTk0ms5obxTpxdOGTeufeKET6eOCeZgCbpV8UnBWs2ICufqnUN.G', true, '11111111-1111-1111-1111-111111111111', '66666666-6666-6666-6666-666666666662', '44444444-4444-4444-4444-444444444442')
ON CONFLICT (id_usuario) DO UPDATE
SET nombre = EXCLUDED.nombre,
    email = EXCLUDED.email,
    password_hash = EXCLUDED.password_hash,
    activo = EXCLUDED.activo,
    id_empresa = EXCLUDED.id_empresa,
    id_sucursal = EXCLUDED.id_sucursal,
    id_rol = EXCLUDED.id_rol;

INSERT INTO contactos_empresa (id_contacto_empresa, nombres, apellidos, id_cargo_empresa, id_empresa)
VALUES
    ('88888888-8888-8888-8888-888888888881', 'Lucia', 'Fernandez', '55555555-5555-5555-5555-555555555551', '11111111-1111-1111-1111-111111111111'),
    ('88888888-8888-8888-8888-888888888882', 'Marco', 'Rojas', '55555555-5555-5555-5555-555555555552', '11111111-1111-1111-1111-111111111111')
ON CONFLICT (id_contacto_empresa) DO UPDATE
SET nombres = EXCLUDED.nombres,
    apellidos = EXCLUDED.apellidos,
    id_cargo_empresa = EXCLUDED.id_cargo_empresa,
    id_empresa = EXCLUDED.id_empresa;

INSERT INTO categoria (id_categoria, nombre, descripcion)
VALUES
    ('99999999-9999-9999-9999-999999999991', 'Papeleria', 'Productos de oficina y papeleria'),
    ('99999999-9999-9999-9999-999999999992', 'Tecnologia', 'Equipos y accesorios tecnologicos'),
    ('99999999-9999-9999-9999-999999999993', 'Limpieza', 'Insumos de limpieza institucional')
ON CONFLICT (id_categoria) DO UPDATE
SET nombre = EXCLUDED.nombre,
    descripcion = EXCLUDED.descripcion;

INSERT INTO proveedor (id_proveedor, activo, id_empresa)
VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', true, '22222222-2222-2222-2222-222222222222'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2', true, '33333333-3333-3333-3333-333333333333')
ON CONFLICT (id_proveedor) DO UPDATE
SET activo = EXCLUDED.activo,
    id_empresa = EXCLUDED.id_empresa;

INSERT INTO cat_proveedor (id_categoria, id_proveedor)
VALUES
    ('99999999-9999-9999-9999-999999999991', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1'),
    ('99999999-9999-9999-9999-999999999992', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1'),
    ('99999999-9999-9999-9999-999999999993', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2')
ON CONFLICT (id_categoria, id_proveedor) DO NOTHING;

INSERT INTO producto (id_producto, sku, nombre, descripcion, unidad_medida, activo, id_categoria, id_proveedor)
VALUES
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb1', 'PAP-001', 'Resma Carta', 'Resma de papel tamano carta 500 hojas', 'paquete', true, '99999999-9999-9999-9999-999999999991', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1'),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb2', 'TEC-001', 'Mouse Inalambrico', 'Mouse ergonomico USB', 'unidad', true, '99999999-9999-9999-9999-999999999992', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1'),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb3', 'LIM-001', 'Detergente Industrial', 'Detergente para limpieza de oficinas', 'bidon', true, '99999999-9999-9999-9999-999999999993', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2')
ON CONFLICT (id_producto) DO UPDATE
SET sku = EXCLUDED.sku,
    nombre = EXCLUDED.nombre,
    descripcion = EXCLUDED.descripcion,
    unidad_medida = EXCLUDED.unidad_medida,
    activo = EXCLUDED.activo,
    id_categoria = EXCLUDED.id_categoria,
    id_proveedor = EXCLUDED.id_proveedor;

INSERT INTO almacen (id_almacen, nombre, direccion, activo, id_proveedor, coordenadas)
VALUES
    ('cccccccc-cccc-cccc-cccc-ccccccccccc1', 'Almacen Andino LP', 'Zona Sur, La Paz', true, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', point(-68.1500, -16.5100)),
    ('cccccccc-cccc-cccc-cccc-ccccccccccc2', 'Almacen Altiplano SC', 'Parque Industrial, Santa Cruz', true, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2', point(-63.1800, -17.8200))
ON CONFLICT (id_almacen) DO UPDATE
SET nombre = EXCLUDED.nombre,
    direccion = EXCLUDED.direccion,
    activo = EXCLUDED.activo,
    id_proveedor = EXCLUDED.id_proveedor,
    coordenadas = EXCLUDED.coordenadas;

INSERT INTO producto_almacen (id_almacen, id_producto, stock, max, min, activo)
VALUES
    ('cccccccc-cccc-cccc-cccc-ccccccccccc1', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb1', 250, 500, 50, true),
    ('cccccccc-cccc-cccc-cccc-ccccccccccc1', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb2', 80, 150, 20, true),
    ('cccccccc-cccc-cccc-cccc-ccccccccccc2', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb3', 120, 200, 30, true)
ON CONFLICT (id_almacen, id_producto) DO UPDATE
SET stock = EXCLUDED.stock,
    max = EXCLUDED.max,
    min = EXCLUDED.min,
    activo = EXCLUDED.activo;

INSERT INTO precio_base (id_precio, precio_base, vigente_desde, vigente_hasta, id_proveedor, id_producto)
VALUES
    ('dddddddd-dddd-dddd-dddd-ddddddddddd1', 32.50, NOW() - INTERVAL '30 day', NOW() + INTERVAL '365 day', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb1'),
    ('dddddddd-dddd-dddd-dddd-ddddddddddd2', 85.00, NOW() - INTERVAL '30 day', NOW() + INTERVAL '365 day', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb2'),
    ('dddddddd-dddd-dddd-dddd-ddddddddddd3', 110.00, NOW() - INTERVAL '30 day', NOW() + INTERVAL '365 day', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb3')
ON CONFLICT (id_precio) DO UPDATE
SET precio_base = EXCLUDED.precio_base,
    vigente_desde = EXCLUDED.vigente_desde,
    vigente_hasta = EXCLUDED.vigente_hasta,
    id_proveedor = EXCLUDED.id_proveedor,
    id_producto = EXCLUDED.id_producto;

INSERT INTO tarifa_regla (id_tarifa, nombre, descripcion, id_proveedor, activo)
VALUES
    ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee1', 'Tarifa Corporativa Andina', 'Tarifa por volumen para clientes corporativos', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', true)
ON CONFLICT (id_tarifa) DO UPDATE
SET nombre = EXCLUDED.nombre,
    descripcion = EXCLUDED.descripcion,
    id_proveedor = EXCLUDED.id_proveedor,
    activo = EXCLUDED.activo;

INSERT INTO tramo_tarifa (id_tramo, tipo, cantidad_minima, cantidad_maxima, porcentaje_desc, id_regla)
VALUES
    ('ffffffff-ffff-ffff-ffff-fffffffffff1', 'volumen', 10, 49, 5.00, 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee1'),
    ('ffffffff-ffff-ffff-ffff-fffffffffff2', 'volumen', 50, 9999, 12.00, 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee1')
ON CONFLICT (id_tramo) DO UPDATE
SET tipo = EXCLUDED.tipo,
    cantidad_minima = EXCLUDED.cantidad_minima,
    cantidad_maxima = EXCLUDED.cantidad_maxima,
    porcentaje_desc = EXCLUDED.porcentaje_desc,
    id_regla = EXCLUDED.id_regla;

INSERT INTO reglas_comision (id_regla, nombre, id_proveedor, id_tipo, valor, activa, fecha_inicio, fecha_final)
VALUES
    ('12121212-1212-1212-1212-121212121212', 'Comision estándar Andina', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'porcentaje', 8.50, true, NOW() - INTERVAL '30 day', NOW() + INTERVAL '365 day')
ON CONFLICT (id_regla) DO UPDATE
SET nombre = EXCLUDED.nombre,
    id_proveedor = EXCLUDED.id_proveedor,
    id_tipo = EXCLUDED.id_tipo,
    valor = EXCLUDED.valor,
    activa = EXCLUDED.activa,
    fecha_inicio = EXCLUDED.fecha_inicio,
    fecha_final = EXCLUDED.fecha_final;

INSERT INTO contrato_empresa_tarifas (id_contrato, id_empresa, id_proveedor, id_regla, vigente_desde, vigente_hasta, activo)
VALUES
    ('13131313-1313-1313-1313-131313131313', '11111111-1111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee1', NOW() - INTERVAL '15 day', NOW() + INTERVAL '180 day', true)
ON CONFLICT (id_contrato) DO UPDATE
SET id_empresa = EXCLUDED.id_empresa,
    id_proveedor = EXCLUDED.id_proveedor,
    id_regla = EXCLUDED.id_regla,
    vigente_desde = EXCLUDED.vigente_desde,
    vigente_hasta = EXCLUDED.vigente_hasta,
    activo = EXCLUDED.activo;

INSERT INTO contrato_empresa_detalle (id_detalle, porcentaje_descuento, id_producto, id_contrato)
VALUES
    ('14141414-1414-1414-1414-141414141414', 10.00, 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb1', '13131313-1313-1313-1313-131313131313'),
    ('15151515-1515-1515-1515-151515151515', 7.50, 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb2', '13131313-1313-1313-1313-131313131313')
ON CONFLICT (id_detalle) DO UPDATE
SET porcentaje_descuento = EXCLUDED.porcentaje_descuento,
    id_producto = EXCLUDED.id_producto,
    id_contrato = EXCLUDED.id_contrato;

INSERT INTO orden_compra (id_orden, total, fecha, id_proveedor, id_empresa_compradora, id_sucursal, id_usuario, id_estado, fecha_orden)
VALUES
    ('16161616-1616-1616-1616-161616161616', 735.00, NOW() - INTERVAL '2 day', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', '11111111-1111-1111-1111-111111111111', '66666666-6666-6666-6666-666666666661', '77777777-7777-7777-7777-777777777772', 'pendiente', CURRENT_DATE - INTERVAL '2 day')
ON CONFLICT (id_orden) DO UPDATE
SET total = EXCLUDED.total,
    fecha = EXCLUDED.fecha,
    id_proveedor = EXCLUDED.id_proveedor,
    id_empresa_compradora = EXCLUDED.id_empresa_compradora,
    id_sucursal = EXCLUDED.id_sucursal,
    id_usuario = EXCLUDED.id_usuario,
    id_estado = EXCLUDED.id_estado,
    fecha_orden = EXCLUDED.fecha_orden;

INSERT INTO detalle_orden (id_detalle, cantidad, precio_unitario, subtotal, id_orden, id_producto, id_almacen)
VALUES
    ('17171717-1717-1717-1717-171717171717', 15, 32.50, 487.50, '16161616-1616-1616-1616-161616161616', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb1', 'cccccccc-cccc-cccc-cccc-ccccccccccc1'),
    ('18181818-1818-1818-1818-181818181818', 3, 82.50, 247.50, '16161616-1616-1616-1616-161616161616', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb2', 'cccccccc-cccc-cccc-cccc-ccccccccccc1')
ON CONFLICT (id_detalle) DO UPDATE
SET cantidad = EXCLUDED.cantidad,
    precio_unitario = EXCLUDED.precio_unitario,
    subtotal = EXCLUDED.subtotal,
    id_orden = EXCLUDED.id_orden,
    id_producto = EXCLUDED.id_producto,
    id_almacen = EXCLUDED.id_almacen;

INSERT INTO comision (id_comision, monto_comision, monto_proveedor, fecha, id_detalle_orden, id_proveedor, id_regla_comision)
VALUES
    ('19191919-1919-1919-1919-191919191919', 41.44, 446.06, NOW() - INTERVAL '2 day', '17171717-1717-1717-1717-171717171717', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', '12121212-1212-1212-1212-121212121212')
ON CONFLICT (id_comision) DO UPDATE
SET monto_comision = EXCLUDED.monto_comision,
    monto_proveedor = EXCLUDED.monto_proveedor,
    fecha = EXCLUDED.fecha,
    id_detalle_orden = EXCLUDED.id_detalle_orden,
    id_proveedor = EXCLUDED.id_proveedor,
    id_regla_comision = EXCLUDED.id_regla_comision;

INSERT INTO factura (id_factura, fecha, total, id_orden, id_estado)
VALUES
    ('20202020-2020-2020-2020-202020202020', NOW() - INTERVAL '1 day', 735.00, '16161616-1616-1616-1616-161616161616', 'pagada')
ON CONFLICT (id_factura) DO UPDATE
SET fecha = EXCLUDED.fecha,
    total = EXCLUDED.total,
    id_orden = EXCLUDED.id_orden,
    id_estado = EXCLUDED.id_estado;

INSERT INTO detalle_factura (id_detalle, cantidad, precio_unitario, subtotal, id_factura, id_producto)
VALUES
    ('21212121-2121-2121-2121-212121212121', 15, 32.50, 487.50, '20202020-2020-2020-2020-202020202020', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb1'),
    ('22222222-2222-2222-2222-222222222223', 3, 82.50, 247.50, '20202020-2020-2020-2020-202020202020', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb2')
ON CONFLICT (id_detalle) DO UPDATE
SET cantidad = EXCLUDED.cantidad,
    precio_unitario = EXCLUDED.precio_unitario,
    subtotal = EXCLUDED.subtotal,
    id_factura = EXCLUDED.id_factura,
    id_producto = EXCLUDED.id_producto;

COMMIT;
