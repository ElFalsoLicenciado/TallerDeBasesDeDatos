### 1\. Documento de Especificación de Requisitos de Software (SRS)

**Proyecto:** Lua's Place Management System
**Versión:** 1.0

#### 1.1. Descripción General

El sistema es una aplicación de escritorio desarrollada en Java (JavaFX) para la gestión integral de una cadena de cafeterías/panaderías. Permite el control de ventas, inventario, producción, recursos humanos y seguridad mediante roles y permisos granulares.

#### 1.2. Requisitos Funcionales

**Módulo 1: Seguridad y Acceso (Auth)**

* **RF-01 Login:** El sistema debe permitir el acceso mediante usuario y contraseña encriptada (BCrypt).
* **RF-02 Control de Sesión:** El sistema debe registrar el inicio de sesión, IP y mantener una sesión activa en memoria.
* **RF-03 RBAC (Roles y Permisos):** La interfaz debe restringir el acceso a botones y pantallas según los permisos del usuario (`sys.full_access`, `ventas.crear`, etc.) definidos en la base de datos.

**Módulo 2: Ventas (Punto de Venta)**

* **RF-04 Registro de Ventas:** Permitir agregar productos a un carrito, calcular subtotales, impuestos y descuentos.
* **RF-05 Métodos de Pago:** Soportar pagos en Efectivo, Tarjeta y Transferencia.
* **RF-06 Descuento de Stock:** Al finalizar una venta, se debe descontar automáticamente la cantidad vendida del inventario de la sucursal actual.
* **RF-07 Gestión de Clientes:** Permitir registrar y buscar clientes por RFC o nombre para asociarlos a la venta.

**Módulo 3: Inventario y Proveedores**

* **RF-08 Visualización de Stock:** Mostrar el inventario actual por sucursal con indicadores de estado (Adecuado, Bajo, Crítico, Agotado).
* **RF-09 Gestión de Ingredientes:** CRUD (Crear, Leer, Actualizar, Borrar) de ingredientes y sus costos.
* **RF-10 Compras:** Registrar órdenes de compra a proveedores y actualizar el stock de ingredientes al recibirlas.

**Módulo 4: Producción (Cocina)**

* **RF-11 Recetario:** Gestión de recetas que vincula productos con ingredientes y cantidades requeridas.
* **RF-12 Lotes de Producción:** Registrar la elaboración de productos (ej. "Lote de 20 Pasteles").
* **RF-13 Transformación de Inventario:** Al completar un lote, el sistema debe restar los ingredientes (materia prima) y sumar el producto terminado al inventario de la sucursal.
* **RF-14 Cálculo de Costos:** Calcular el costo unitario de producción basado en el costo actual de los ingredientes.

**Módulo 5: Recursos Humanos (RH)**

* **RF-15 Gestión de Empleados:** CRUD de empleados, asignación a sucursales y puestos.
* **RF-16 Gestión de Usuarios:** Asignar credenciales de acceso y roles a los empleados existentes.

#### 1.3. Requisitos de Interfaz (GUI)

* **RI-01 Navegación:** Uso de una barra lateral (Sidebar) colapsable con animación suave para maximizar el espacio de trabajo.
* **RI-02 Estilo:** Diseño moderno "Flat" con paleta de colores oscuros para menús y claros para áreas de trabajo.
* **RI-03 Feedback:** Uso de alertas y diálogos para confirmar acciones críticas o errores.