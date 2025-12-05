# Lua's Place Management System

**Sistema de Gestión Empresarial (ERP) para Cadenas de Cafeterías**

> Proyecto universitario desarrollado en Java (JavaFX) con gestión de dependencias Maven y base de datos PostgreSQL.

---

## Descripción del Proyecto

**Lua's Place Management System** es una solución de escritorio robusta diseñada para administrar la operación completa de una cadena de cafeterías. A diferencia de un punto de venta tradicional, este sistema integra **Ventas, Inventario, Compras, Producción (Cocina), Recursos Humanos y Seguridad** en un solo flujo de información coherente.

El sistema sigue el patrón de arquitectura **MVC (Modelo-Vista-Controlador)** y delega la lógica crítica de negocio (cálculos de inventario, validaciones y auditoría) a una base de datos **PostgreSQL** altamente transaccional mediante Triggers y Vistas.

---

## Características Principales

### 1. Seguridad Avanzada y Roles (RBAC)
* **Gestión Granular de Permisos:** Módulo dedicado que permite al *Super Admin* activar o desactivar permisos específicos para cada rol en tiempo real.
* **Interfaz "Barista Style":** Panel de administración exclusivo con el componente personalizado **`CoffeeToggle`**, interruptores visuales que simulan mecanismos físicos con la paleta de colores del proyecto.
* **Seguridad en Capas:**
    * **Frontend:** El menú lateral se reconstruye dinámicamente según los permisos del usuario (`SessionManager`).
    * **Backend:** Hasheo de contraseñas con **SHA-256** y desconexión automática por inactividad.

### 2. Punto de Venta (POS)
* **Multi-Sucursal:** Filtrado automático de catálogo y stock según la sucursal del usuario.
* **Transacciones Vivas:** Cálculo inmediato de impuestos y totales.
* **Impacto Inmediato:** Trigger de base de datos (`trg_venta`) que descuenta ingredientes/productos al confirmar el pago.

### 3. Inventario y Logística
* **Semáforo de Stock:** Indicadores visuales automáticos:
    * 🔴 **AGOTADO** | 🟠 **CRÍTICO** | 🟡 **BAJO**
* **Vistas Duales:** Alternancia entre vista de *Productos Terminados* (Galería) y *Materia Prima* (Tabla detallada).
* **Compras Inteligentes:** Registro de entrada de mercancía validando proveedores autorizados.

### 4. Módulo de Producción (Cocina)
* **Transformación de Insumos:** Convierte materia prima (ej. Harina, Leche) en productos finales (ej. Pastel, Cappuccino).
* **Validación de Recetas:** El sistema impide la producción si el stock de ingredientes es insuficiente.

### 5. Recursos Humanos
* **Gestión de Personal:** CRUD completo de empleados y usuarios de sistema.
* **Jerarquía:** Los gerentes tienen acceso limitado únicamente al personal de su propia sucursal.

### 6. Inteligencia de Negocios
* **Dashboard:** Métricas en tiempo real sobre ventas diarias y alertas de stock.
* **Auditoría:** Visor de Logs del servidor (Estilo Terminal) para rastrear operaciones sensibles.

---

## Arquitectura y Estructura del Proyecto

El proyecto utiliza la estructura estándar de directorios de **Maven**.

### Stack Tecnológico
* **Lenguaje:** Java 21 (JDK 21).
* **Gestor de Construcción:** Apache Maven.
* **GUI:** JavaFX (FXML + CSS).
* **Base de Datos:** PostgreSQL 14+.
* **Librerías:** `org.postgresql:postgresql`, `org.mindrot:jbcrypt`, `com.jfoenix:jfoenix` (opcional).

### Árbol de Directorios

```text
src/
├── main/
│   ├── java/
│   │   └── org/
│   │       └── example/
│   │           ├── AppLauncher.java      # Clase de entrada (Main Wrapper para JARs)
│   │           ├── Main.java             # Clase principal JavaFX
│   │           │
│   │           ├── Controller/           # Lógica de Interacción
│   │           │   ├── Admin/            # SecurityController, LogController
│   │           │   ├── MainView/         # SidebarController
│   │           │   ├── Ventas/           # POS Logic
│   │           │   ├── Inventario/       # Stock Logic
│   │           │   └── ...
│   │           │
│   │           ├── Model/                # Lógica de Datos
│   │           │   ├── DAO/              # Data Access Objects (SQL)
│   │           │   ├── Entities/         # POJOs (Rol, Permiso, Usuario)
│   │           │   └── DatabaseConnection.java
│   │           │
│   │           ├── Util/                 # Herramientas Transversales
│   │           │   ├── SessionManager.java
│   │           │   └── HashPassword.java
│   │           │
│   │           └── View/
│   │               └── Components/       # Controles Personalizados
│   │                   └── CoffeeToggle.java  # Componente UI de Seguridad
│   │
│   └── resources/
│       └── View/                         # Recursos Estáticos (Classpath)
│           ├── Admin/                    # FXMLs de Administración
│           ├── Ventas/                   # FXMLs de Ventas
│           ├── CSS/                      # Hojas de estilo (.css)
│           ├── Images/                   # Iconos y Assets gráficos
│           └── *.fxml                    # Vistas generales
````

-----

## Base de Datos

La lógica de negocio reside en gran medida en PostgreSQL.

1.  **`01_schema.sql`**: Define tablas, tipos ENUM, Triggers (`trg_gc_emp`, `trg_venta`) y Vistas (`vista_inventario_sucursales`).
2.  **`02_data.sql`**: Seeders para roles iniciales, permisos base, sucursales y menú.

-----

## Instalación y Ejecución

### Requisitos

* JDK 21 instalado.
* Maven 3.8+ instalado (o usar el wrapper de IntelliJ).
* PostgreSQL corriendo en el puerto `5432`.

### Pasos para ejecutar

1.  **Preparar Base de Datos:**
    * Crea la DB: `CREATE DATABASE luas_place;`
    * Ejecuta los scripts SQL (`01_schema.sql` y luego `02_data.sql`).
2.  **Configurar Conexión:**
    * Edita `src/main/java/org/example/Model/DatabaseConnection.java` con tus credenciales locales.
3.  **Ejecutar con Maven:**
    Desde la terminal en la raíz del proyecto:
    ```bash
    mvn clean javafx:run
    ```
    *O para generar el ejecutable (Fat Jar):*
    ```bash
    mvn clean package
    java -jar target/LuasPlaceERP-1.0-SNAPSHOT.jar
    ```

-----

## Credenciales de Prueba

**Contraseña general:** `password123`  
**Contraseña usuario 'cande':** `cande`

| Rol | Usuario | Alcance | Funciones Clave |
| :--- | :--- | :--- | :--- |
| **Super Admin** | `admin` | Global | Acceso total, **Panel de Seguridad (Roles)**, Logs. |
| **Gerente** | `mgarcia` | Sucursal Centro | Inventario, RH, Reportes, Ventas. |
| **Gerente** | `cande` | Sucursal Acueducto | Gestión local. |
| **Cajero** | `sramirez`| Sucursal Centro | Punto de Venta exclusivo. |

-----

## Identidad Visual

La interfaz utiliza hojas de estilo CSS avanzadas para mantener la inmersión:

* **Latte Theme:** Colores base crema (`#FFF8E7`) y beige (`#DCC7AA`).
* **Barista Pro Theme:** Estilo de alto contraste (Café Espresso `#1A120B`) para paneles administrativos y controles `CoffeeToggle`.
* **Dark Roast:** Tema "Terminal Hacker" para visualización de logs del servidor.

-----

*Proyecto Académico - 2025*