# Prompt para Generar Pantalla de Creación de Contratos (Maestro-Detalle)

Este prompt está diseñado para que se lo entregues a un asistente de IA (como Gemini) o a un desarrollador frontend. Contiene todo el contexto técnico, la estructura de datos y los requisitos de UI para construir la interfaz.

---

### Contexto del Proyecto
Estamos desarrollando un sistema B2B. Necesitamos implementar una pantalla de **"Creación de Contratos"** con un diseño **Maestro-Detalle**. 
El contrato principal (`contrato_empresa_tarifas`) contiene la información del acuerdo entre una Empresa cliente y un Proveedor. Cada contrato tiene una lista de detalles (`contrato_empresa_detalle`) que especifica qué productos (`producto`) tienen descuento y qué porcentaje se aplica.

Para garantizar la consistencia, el backend procesa toda la creación en una única llamada transaccional (`POST /api/v1/contratos-tarifa`) que recibe el contrato junto con su lista de detalles.

---

### Endpoints de la API Backend

1. **Guardar Contrato Completo**:
   * **URL**: `POST /api/v1/contratos-tarifa`
   * **Payload (JSON)**:
     ```json
     {
       "idEmpresa": "uuid-de-la-empresa",
       "idProveedor": "uuid-del-proveedor",
       "idRegla": "uuid-de-la-regla-tarifa",
       "vigenteDesde": "2026-06-23T00:00:00Z",
       "vigenteHasta": "2027-06-23T00:00:00Z",
       "activo": true,
       "detalles": [
         {
           "idProducto": "uuid-del-producto-1",
           "porcentajeDescuento": 12.50
         },
         {
           "idProducto": "uuid-del-producto-2",
           "porcentajeDescuento": 5.00
         }
       ]
     }
     ```

2. **Carga de Catálogos (para Dropdowns/Selectores)**:
   * **Empresas**: `GET /api/v1/empresas` (o paginado `/api/v1/empresas/paged`)
   * **Proveedores**: `GET /api/v1/proveedores`
   * **Reglas de Tarifas**: `GET /api/v1/tarifas-reglas`
   * **Productos**: `GET /api/v1/products`

---

### Requisitos del Frontend

#### 1. Estructura Visual (Diseño Limpio y Premium)
* **Sección Cabecera (Maestro)**:
  * Selectores (Dropdown) con buscador integrado para **Empresa**, **Proveedor** y **Regla de Tarifa**.
  * Selectores de Fecha para **Vigente Desde** (obligatorio) y **Vigente Hasta** (opcional).
  * Interruptor/Checkbox para marcar el contrato como **Activo** (por defecto `true`).
* **Sección Detalle de Productos (Tabla Dinámica)**:
  * Un subformulario de inserción rápida con:
    * Dropdown/Selector de **Producto**.
    * Input numérico para **% Descuento** (con límite de `0.00` a `100.00`).
    * Botón **"Agregar Producto"** (añade el producto seleccionado a la tabla inferior).
  * Tabla con columnas: `Producto (SKU/Nombre)`, `% Descuento` y una columna de `Acciones` (botón de eliminar fila).
* **Acción Principal**:
  * Botón **"Guardar Contrato"** en la parte inferior.

#### 2. Validaciones Requeridas
* **Campos Requeridos**: Empresa, Proveedor, Regla de Tarifa y Vigente Desde deben ser obligatorios.
* **Consistencia de Fechas**: La fecha "Vigente Hasta" no puede ser anterior a "Vigente Desde".
* **Validación de Detalles**: 
  * Se requiere que haya al menos 1 producto agregado al detalle antes de poder guardar el contrato.
  * No se puede agregar el mismo producto dos veces al detalle (evitar duplicados locales en la tabla).
  * El porcentaje de descuento debe ser mayor que 0 y menor o igual a 100.

#### 3. Comportamiento del Estado
* Al seleccionar productos y definir descuentos, se deben agregar a un estado de array local (`detalles`).
* Al presionar "Eliminar" en una fila, se remueve el ítem del estado local.
* Al hacer clic en "Guardar Contrato", se debe construir el payload JSON correspondiente y enviarlo mediante `POST` a `/api/v1/contratos-tarifa`. Si es exitoso, mostrar un mensaje de confirmación y limpiar el formulario o redirigir a la lista de contratos.

---

### Tarea
Implementa esta pantalla utilizando **React, TypeScript y TailwindCSS** (o el framework de tu preferencia, ej: Vue/Angular) utilizando componentes estilizados modernos, manejo de estados locales, y Axios/Fetch para consumir la API. Asegúrate de incluir el manejo de estados de carga (`loading`) y visualización de errores.
