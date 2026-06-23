# Specification & Prompts for Stitch: B2B Procurement and Payment Frontend

This document contains a structured, high-fidelity prompt and technical specification designed to be ingested by **Stitch** (or any AI frontend builder). It details the backend capabilities, database models, and API interfaces, and specifies the exact structure, design system, and code logic needed to generate a professional, elegant B2B dashboard.

---

# 🤖 System Prompt for Stitch

You are a senior frontend engineer and UI/UX expert. Your task is to build a beautiful, elegant, and professional **B2B Procurement and Payment Platform Dashboard** using HTML, Vanilla CSS, and JavaScript. 

The application must support two operational modes:
1. **Live API Mode:** Connects to a local Spring Boot backend running on `http://localhost:8080` (utilizing JWT headers and WebSocket STOMP connections).
2. **Simulation Mode (Default):** Runs completely client-side in the browser, mocking all API responses, database collections, payment processing timers, and WebSocket events. This allows users to test the entire procurement and checkout cycle instantly without starting the backend.

---

## 🎨 Design System & Visual Guidelines

Your output should look highly premium, modern, and state-of-the-art. Avoid simple, generic layouts. Use the following design tokens:

- **Colors:**
  - *Backgrounds:* Deep dark space / cyber slate gradients. Core backgrounds: `#060913` (darker) to `#0a0f24` (dark).
  - *Cards & Panels:* Translucent glassmorphism containers (`rgba(15, 23, 49, 0.65)`) with fine borders (`1px solid rgba(255, 255, 255, 0.06)`) and heavy backdrop filters (`blur(16px)`).
  - *Primary Accents:* Neon Cyan (`#06b6d4`) and Electric Purple (`#8b5cf6`).
  - *Status Indicators:* 
    - Pending: Amber (`#f59e0b`, background `rgba(245, 158, 11, 0.1)`)
    - Approved/Paid: Emerald (`#10b981`, background `rgba(16, 185, 129, 0.1)`)
    - Cancelled/Rejected: Rose/Pink (`#ef4444` / `#ec4899`)
- **Typography:** Modern sans-serif using Google Fonts: *Outfit* (for headers and metrics) and *Inter* (for UI labels and tables).
- **Animations:** Smooth, cubic-bezier transitions on hover states, fade-in entry transitions for page switches, spinning loading states, and a pulsing status dot indicating active WebSocket connections.

---

## 📂 Backend Architecture & Context Reference

The backend is a three-module Spring Boot application (`data`, `core`, `B2B-api`) running PostgreSQL. The frontend must interact with or simulate the following structures:

### 1. Database Schema Context
- **Organization:** `empresa` (legal companies, NIT tax ID), `sucursal_empresa` (branches), `usuario` (users with roles: `ADMIN`, `BUYER`, `SUPPLIER`).
- **Catalog:** `proveedor` (suppliers), `categoria` (product category), `producto` (items with base prices), `almacen` (warehouses), `producto_almacen` (stocks mapping products to warehouses).
- **B2B Contracts:** `precio_base` (base product price), `tarifa_regla` & `tramo_tarifa` (volume or cost-based tiered discount rule), `contrato_empresa_tarifas` & `contrato_empresa_detalle` (active agreements between a buyer company and a supplier detailing specific product discounts).
- **Invoicing & Orders:** `orden_compra` & `detalle_orden` (purchase order transaction), `factura` & `detalle_factura` (billing invoice).
- **Commissions:** `reglas_comision` & `comision` (platform cuts, e.g., 3.5%).

### 2. Protected Endpoints (Headers: `Authorization: Bearer <token>`)
- **Auth:** `POST /api/v1/auth/login` (body: `{email, passwordHash}`) & `/login/2fa` (body: `{data: email, code}`) -> returns `access_token`.
- **Purchase Orders:** `POST` & `GET` `/api/v1/ordenes-compra`
- **Stereum Payments:** `POST /api/v1/stereum/charge` (body: `{orderId}`) -> returns `{qrBase64, transactionStatus}`.
- **WebSocket STOMP Broker:** `/ws` -> subscribes to `/paymenting` & `/user/paymenting`.

---

## 🖥️ Layout & View Specifications

Generate a Single Page Application divided into the following sections:

### 1. Sidebar Navigation & Top Bar
- **Sidebar:** Top brand logo + platform name. Menu items: *Overview*, *Product Catalog*, *Purchase Orders*, *B2B Contracts*, *Admin Settings*. A footer switcher containing a toggle button group: `[ LIVE API ]` and `[ SIMULATION ]`.
- **Top Bar:** Page title + subtitle, global status indicator (Pulsing connection dot and status label), and user profile widget (avatar, name, company, logout trigger).

### 2. View: Auth / Login & Register (Only shown if not logged in in Live Mode)
The authentication screen is a centralized glassmorphic card container with tabs to toggle between **Login**, **Registro (Registration)**, and **Recuperación (Password Recovery)**.

#### A. Login Form
- **Form Fields:**
  - *Email:* Institutional email input field.
  - *Password:* Masked password input field.
  - *OTP Code (Optional 2FA):* 6-digit numeric input, only visible or required if 2FA authentication is enabled.
- **Workflow:**
  - **Live Mode:** Performs a `POST /api/v1/auth/login` (or `/login/2fa` if code is provided) with the authentication payload, returning a token stored in localStorage.
  - **Simulation Mode:** Automatically logs in the user with any credentials (defaulting to simulated Administrator) or matches against the locally registered users.

#### B. Registro (Registration) Form
Allows onboarding new corporate buyers and suppliers.
- **Form Fields (mapped to `UsuarioRequest`):**
  - *Nombre Completo (`nombre`):* Text input for full user name.
  - *Email (`email`):* Institutional email input.
  - *Contraseña (`password`):* Password input with a visual strength indicator bar.
  - *Empresa (`idEmpresa`):* Dropdown selector populated dynamically:
    - *Live Mode:* Fetched from `GET /api/v1/empresas` (accessing the `content` list of the paginated response).
    - *Simulation Mode:* Loaded from the initial mock companies array.
  - *Sucursal (`idSucursal`):* Dropdown selector populated dynamically:
    - *Live Mode:* Fetched from `GET /api/v1/sucursales-empresa`. In advanced implementations, this list should filter based on the selected `idEmpresa`.
    - *Simulation Mode:* Selected from branches linked to the company.
  - *Rol de Usuario (`idRol`):* Dropdown selector populated dynamically:
    - *Live Mode:* Fetched from `GET /api/v1/roles`.
    - *Simulation Mode:* Options: `ADMIN`, `BUYER`, `SUPPLIER` mapped to mock UUIDs.
- **Workflow:**
  - **Live Mode:** Sends a `POST /api/v1/usuarios` request with body:
    ```json
    {
      "nombre": "User Name",
      "email": "user@company.com",
      "password": "securepassword",
      "activo": true,
      "idEmpresa": "uuid-here",
      "idSucursal": "uuid-here",
      "idRol": "uuid-here"
    }
    ```
    On status `201 Created`, shows a success toast, transitions to the login tab, and populates the login email field.
  - **Simulation Mode:** Appends the user object to the local mock database arrays, prints a log in the simulated console, and returns success.

#### C. Password Reset Flow
- **Step 1: Request Code**
  - *Field:* Email.
  - *Live Mode Endpoint:* `POST /api/v1/auth/password-reset/request?email=user@company.com` (sends a 6-digit verification code to the registered email).
- **Step 2: Confirm Reset**
  - *Fields:* Email, Code (6 digits), New Password.
  - *Live Mode Endpoint:* `POST /api/v1/auth/password-reset/confirm` with query parameters: `email`, `code`, and `newPassword`.
  - Shows success status and redirects user back to the login screen.

### 3. View: Overview (Dashboard Home)
- **Metrics Grid:** Four cards:
  - *Total Revenue:* Aggregated total of all approved orders.
  - *Total Orders:* Count of purchase orders.
  - *Active Contracts:* Active B2B company-supplier agreements.
  - *Commissions Earned:* Platform cuts calculated at 3.5% of total revenue.
- **Purchase Trends Chart:** A canvas element rendering a custom line chart showing monthly sales values (`$12k`, `$19k`, `$15k`, `$28k`, etc.) using gradient strokes, glow points, and month labels.
- **Recent Activities:** A timeline displaying the latest created orders and payment completions.

### 4. View: Product Catalog
- **Search bar:** Real-time filter by product name, SKU, or supplier.
- **Catalog Table:** Displays: SKU, Product Name, Category Badge, Supplier Name, Base Price, and Current stock level (with min/max alerts).
- **Stock Detail Modal:** A button "Ver stock" opens a modal displaying the product inventory split among different warehouses (`Almacén Central El Alto`, `Centro Distribución Cbba`, etc.).

### 5. View: Purchase Orders & Stereum Checkout (Core Interaction)
This view is split into two panels:
- **Panel A: Order Creation Form & Cart:**
  - Select Buyer Company, Branch (sucursal), Supplier, Product, and Quantity.
  - **Live calculations:** Display unit base price, apply contract discounts automatically (lookup if a B2B contract exists between selected buyer and supplier), subtract discount, calculate subtotal, and show final order total.
  - Button "Crear orden de compra" saves the order in the list.
- **Panel B: Checkout Board & Stereum QR Code:**
  - Displays details of the active/selected pending order.
  - Button "Proceder al Pago con Stereum" calls `/api/v1/stereum/charge` to get the payment QR.
  - Displays a custom loading spinner while fetching.
  - Renders the QR code (`qrBase64` image).
  - Status section showing a status badge (`esperando cobro`, `QR generado`, `pagada`).
  - **Live Mode WebSockets:** Listens to STOMP events. Once a webhook confirm message is broadcast, updates status to `pagada` with a success toast.
  - **Simulation Mode Timer:** If in simulation, waits 4 seconds after presenting the QR code, then automatically fires a mock timer simulating the payment webhook. Updates the order status to `aprobado` and presents a payment success screen.

### 6. View: B2B Contracts & Rules
- **Contracts Grid:** Displays active covenants between businesses, listing the applied tiered rule details (e.g. Volume discount rules table showing minimum quantities and discount percentages).

### 7. View: Admin Settings & Log Viewer
- **Company Register:** Form to insert new companies (Name, NIT, Social Name) that feed into the checkout dropdown.
- **Real-time Log Viewer:** A terminal-like box printing system events. Supports a dropdown filter by severity (`ALL`, `INFO`, `WARN`, `ERROR`).

---

## 💻 Technical Code Architecture & State Management

Ensure the script structure contains:
- **Global AppState:** Track variables like `isSimulationMode`, `isLoggedIn`, `currentUser`, `authToken`, lists of objects (`products`, `orders`, `companies`, `contracts`, `logs`), `activeOrder`, and WebSocket state.
- **Mock Databases:** Prepopulate the state with realistic initial collections of companies (e.g. *Bolivia Tech Import*, *Logística del Sur*), products (e.g. *Tubo de Acero*, *Token TOTP*), B2B contracts, discount rules, and orders.
- **Rest & STOMP Clients:** Use modern `fetch` APIs with custom header factories passing the bearer tokens. Setup the `StompJs.Client` using SockJS.
- **Toast System:** Reusable `showToast(message, type)` function appending alert nodes with slide-in slide-out CSS animations.
- **Responsive Layout logic:** Collapses the sidebar to an icon-only menu on tablets and mobiles, stacking panels vertically on narrow widths.
