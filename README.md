# Lua's Place Management System

**Sistema de Gestión Empresarial (ERP) para Cadenas de Cafeterías**

> Proyecto universitario desarrollado en Java con JavaFX y PostgreSQL.

---

## Descripción del Proyecto

**Lua's Place Management System** es una solución de escritorio robusta diseñada para administrar la operación completa de una cadena de cafeterías ficticias. A diferencia de un punto de venta tradicional, este sistema integra **Ventas, Inventario, Compras, Producción (Cocina) y Recursos Humanos** en un solo flujo de información coherente.

El sistema está diseñado bajo una arquitectura **MVC (Modelo-Vista-Controlador)** y utiliza una base de datos **PostgreSQL "inteligente"** que delega gran parte de la lógica de negocio (cálculo de inventarios, validaciones y auditoría) a Triggers y Vistas SQL.

---

## Características Principales

### 1. Seguridad y Control de Acceso (RBAC)
* **Autenticación Segura:** Login con hasheo de contraseñas mediante algoritmo **SHA-256**.
* **Gestión de Sesiones:** Control de usuarios activos y **Timeout automático** por inactividad (90 segundos) para proteger la terminal.
* **Roles y Permisos:** La interfaz (Sidebar) se adapta dinámicamente:
    * *Cajeros:* Solo ven Punto de Venta.
    * *Gerentes:* Ven Inventario y Reportes de su sucursal.
    * *Admin:* Acceso total global.
* **Auditoría:** Visor de Logs del sistema (Estilo Terminal Hacker) exclusivo para Super Admins.

### 2. Punto de Venta (POS)
* **Multi-Sucursal:** El catálogo de productos se filtra automáticamente según la sucursal del empleado logueado.
* **Carrito de Compras:** Cálculo en tiempo real de subtotales e impuestos.
* **Clientes:** Opción para vender a "Público General" o seleccionar clientes registrados.
* **Actualización en Tiempo Real:** Al confirmar una venta, un Trigger de base de datos descuenta el stock inmediatamente.

### 3. Inventario y Logística
* **Visualización Inteligente:** Tablas con indicadores de color automáticos (`RowFactory`) según el estado del stock:
    * 🔴 **AGOTADO**
    * 🟠 **CRÍTICO**
    * 🟡 **BAJO**
* **Vistas Alternables:** Switch para cambiar entre vista de "Productos Terminados" (Cuadrícula con imágenes) y "Materia Prima" (Tabla detallada).
* **Módulo de Compras:** Registro de entrada de insumos. Incluye validación para comprar solo ingredientes que el proveedor seleccionado ofrece.

### 4. Módulo de Producción (Cocina)
* **Cierre del Ciclo:** Permite transformar Materia Prima en Producto Terminado.
* **Recetario:** Visualización de ingredientes requeridos por producto.
* **Validación de Stock:** El sistema impide producir si no hay suficientes insumos en la sucursal.

### 5. Recursos Humanos
* **CRUD Completo:** Altas, bajas y modificaciones de empleados con formulario a dos columnas.
* **Gestión de Credenciales:** Ventana modal para crear/editar usuarios de sistema vinculados a un empleado.
* **Seguridad:** Los gerentes solo pueden gestionar personal de su propia sucursal.

### 6. Inteligencia de Negocios (BI)
* **Dashboard Inicial:** Tarjetas de métricas vivas (Ventas del día, Alertas de Stock) personalizadas por sucursal.
* **Reportes Financieros:** Gráficos y tablas basados en vistas SQL para analizar márgenes de ganancia por producto.

---

##  Arquitectura y Tecnologías

### Stack Tecnológico
* **Lenguaje:** Java 21 (JDK BellSoft Liberica Full recomendado).
* **GUI:** JavaFX (FXML + CSS).
* **Base de Datos:** PostgreSQL 14+.
* **Drivers/Libs:** JDBC PostgreSQL Driver.

### Estructura del Proyecto (MVC + DAO)

```text
src/
├── Controller/           # Lógica de interacción (JavaFX)
│   ├── LogIn/            # Controladores de acceso
│   ├── MainView/         # Layout principal y Sidebar
│   ├── Ventas/           # POS
│   ├── Inventario/       # Stock y Productos
│   ├── Compras/          # Abastecimiento
│   ├── Produccion/       # Transformación de insumos
│   ├── RH/               # Empleados y Usuarios
│   └── Admin/            # Logs y Auditoría
│
├── Model/                # Lógica de Datos
│   ├── DAO/              # Data Access Objects (SQL Queries)
│   ├── Entities/         # POJOs (Usuario, Producto, etc.)
│   └── DatabaseConnection.java (Singleton JDBC)
│
├── View/                 # Interfaz de Usuario
│   ├── CSS/              # Estilos (Temas Latte, Frappe, Hacker)
│   ├── Images/           # Iconos y recursos
│   └── *.fxml            # Vistas definidos en XML
│
└── Util/                 # Herramientas Transversales
    ├── SessionManager.java  # Singleton de sesión
    ├── Navigation.java      # Gestor de vistas
    ├── AlertUtils.java      # Fábrica de alertas estilizadas
    └── HashPassword.java    # Utilidad SHA-256
````

-----

##  Base de Datos

El sistema se apoya fuertemente en la base de datos. Los scripts se encuentran divididos para facilitar el mantenimiento:

1.  **`01_schema.sql`**: Crea la estructura (Tablas, Tipos ENUM, Funciones, Triggers y Vistas).
2.  **`02_data.sql`**: Carga los datos semilla (Roles, Permisos, Sucursales, Productos y Usuarios iniciales).

**Elementos Clave de BD:**

* **Triggers:** `trg_venta` y `trg_produccion` manejan el descuento de inventario automáticamente.
* **Vistas:** `vista_inventario_sucursales` y `vista_productos_margen` simplifican consultas complejas para los reportes.

-----

## Instalación y Ejecución

### Requisitos Previos

* Java JDK 21+ instalado.
* PostgreSQL instalado y corriendo en el puerto `5432`.

### Pasos

1.  **Base de Datos:**
    * Crea una base de datos llamada `luas_place`.
    * Ejecuta `01_schema.sql`.
    * Ejecuta `02_data.sql`.
2.  **Configuración:**
    * Abre `src/Model/DatabaseConnection.java`.
    * Ajusta la `URL`, `USER` y `PASSWORD` según tu entorno local o nube.
3.  **Ejecución:**
    * Ejecuta la clase `Main.java` desde tu IDE.

-----

## Credenciales de Prueba

El sistema viene precargado con usuarios que representan la jerarquía de la empresa.

**Contraseña para todos:** `password123`    
**Contraseña para cande:** `cande`

| Rol | Usuario | Alcance | Funciones Principales |
| :--- | :--- | :--- | :--- |
| **Super Admin** | `admin` | Global (Todas las sucursales) | Acceso total, Logs, Configuración. |
| **Gerente** | `mgarcia` | Sucursal Centro | Inventario, RH, Reportes, Ventas. |
| **Gerente** | `cande` | Sucursal Acueducto | Gestión local de Acueducto. |
| **Cajero** | `sramirez`| Sucursal Centro | Solo Punto de Venta. |

-----

## Estilos Visuales (Temas)

La aplicación maneja una identidad visual consistente definida en `Style.css`:

* **Tema Principal (Latte & Frappe):** Colores crema (`#FFF8E7`), café (`#6F4E37`) y caramelo para la operación diaria.
* **Tema Seguridad (Dark Roast):** Fondo negro y texto terminal color espumita exclusivo para la vista de Logs de Servidor.

-----

*Proyecto académico - Taller de Bases de Datos.*