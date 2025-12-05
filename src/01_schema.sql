-- ==========================================================
-- 01_SCHEMA.SQL: ESTRUCTURA DE LA BASE DE DATOS
-- ==========================================================

-- Configuración Inicial
SET client_encoding TO 'UTF8';

-- Limpieza Total (Reinicia la base de datos)
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;

-- =======================
-- 1. TIPOS ENUMERADOS
-- =======================
CREATE TYPE tipo_producto AS ENUM ('Bebida', 'Pan', 'Postre', 'Otros');
CREATE TYPE tipo_empleado AS ENUM ('Administrativo', 'Produccion');
CREATE TYPE rol_usuario AS ENUM ('Admin', 'Administrativo', 'Produccion');
CREATE TYPE metodo_pago AS ENUM ('Efectivo', 'Tarjeta', 'Transferencia', 'Otro');
CREATE TYPE estado_lote AS ENUM ('En Proceso', 'Completado', 'Cancelado');

-- =======================
-- 2. TABLAS
-- =======================

-- Seguridad (RBAC)
CREATE TABLE roles (
                       id SERIAL PRIMARY KEY,
                       nombre VARCHAR(50) UNIQUE NOT NULL,
                       descripcion TEXT,
                       activo BOOLEAN DEFAULT TRUE
);

CREATE TABLE permisos (
                          id SERIAL PRIMARY KEY,
                          codigo VARCHAR(100) UNIQUE NOT NULL,
                          descripcion TEXT,
                          modulo VARCHAR(50)
);

CREATE TABLE roles_permisos (
                                id_rol INT REFERENCES roles(id) ON DELETE CASCADE,
                                id_permiso INT REFERENCES permisos(id) ON DELETE CASCADE,
                                PRIMARY KEY (id_rol, id_permiso)
);

-- Negocio Principal
CREATE TABLE sucursales (
                            id SERIAL NOT NULL PRIMARY KEY,
                            codigo VARCHAR(20) NOT NULL UNIQUE,
                            nombre VARCHAR(100) NOT NULL,
                            ciudad VARCHAR(100) NOT NULL,
                            calle VARCHAR(100) NOT NULL,
                            colonia VARCHAR(100) NOT NULL,
                            numero VARCHAR(10) NOT NULL,
                            codigo_postal VARCHAR(10) NOT NULL,
                            telefono VARCHAR(15),
                            activo BOOLEAN NOT NULL DEFAULT TRUE,
                            fecha_apertura DATE NOT NULL DEFAULT CURRENT_DATE
);

CREATE TABLE empleados (
                           id SERIAL NOT NULL PRIMARY KEY,
                           codigo VARCHAR(15) NOT NULL UNIQUE,
                           nombres VARCHAR(50) NOT NULL,
                           apellidos VARCHAR(60) NOT NULL,
                           fecha_nacimiento DATE NOT NULL,
                           lugar_nacimiento VARCHAR(100) NOT NULL,
                           direccion VARCHAR(200) NOT NULL,
                           telefono VARCHAR(20) NOT NULL,
                           correo VARCHAR(100) UNIQUE,
                           fecha_ingreso DATE NOT NULL DEFAULT CURRENT_DATE,
                           tipo tipo_empleado NOT NULL,
                           id_sucursal INT REFERENCES sucursales(id),
                           activo BOOLEAN NOT NULL DEFAULT TRUE,
                           fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE usuarios (
                          id SERIAL NOT NULL PRIMARY KEY,
                          id_empleado INT NOT NULL UNIQUE REFERENCES empleados(id) ON DELETE CASCADE,
                          usuario VARCHAR(30) NOT NULL UNIQUE,
                          contrasena VARCHAR(255) NOT NULL,
                          rol rol_usuario NOT NULL DEFAULT 'Produccion',
                          id_rol INT REFERENCES roles(id),
                          ultimo_acceso TIMESTAMP,
                          intentos_fallidos INT DEFAULT 0,
                          bloqueado BOOLEAN DEFAULT FALSE,
                          activo BOOLEAN NOT NULL DEFAULT TRUE,
                          fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE usuarios_permisos (
                                   id_usuario INT REFERENCES usuarios(id) ON DELETE CASCADE,
                                   id_permiso INT REFERENCES permisos(id) ON DELETE CASCADE,
                                   tipo VARCHAR(10) CHECK (tipo IN ('allow', 'deny')),
                                   PRIMARY KEY (id_usuario, id_permiso)
);

CREATE TABLE sesiones (
                          id SERIAL PRIMARY KEY,
                          id_usuario INT REFERENCES usuarios(id) ON DELETE CASCADE,
                          token_hash VARCHAR(255) NOT NULL,
                          ip VARCHAR(45),
                          user_agent TEXT,
                          inicio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          ultimo_movimiento TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          activa BOOLEAN DEFAULT TRUE,
                          fecha_expiracion TIMESTAMP NOT NULL
);

CREATE TABLE logs (
                      id SERIAL PRIMARY KEY,
                      id_usuario INT REFERENCES usuarios(id),
                      accion VARCHAR(100) NOT NULL,
                      descripcion JSONB,
                      ip VARCHAR(45),
                      user_agent TEXT,
                      fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE proveedores (
                             id SERIAL NOT NULL PRIMARY KEY,
                             codigo VARCHAR(20) NOT NULL UNIQUE,
                             rfc VARCHAR(13) NOT NULL UNIQUE,
                             nombre VARCHAR(100) NOT NULL,
                             correo VARCHAR(100) NOT NULL UNIQUE,
                             telefono VARCHAR(15) NOT NULL,
                             ciudad VARCHAR(100) NOT NULL,
                             calle VARCHAR(100) NOT NULL,
                             colonia VARCHAR(100) NOT NULL,
                             codigo_postal VARCHAR(10) NOT NULL,
                             numero VARCHAR(10) NOT NULL,
                             contacto_nombre VARCHAR(100),
                             activo BOOLEAN NOT NULL DEFAULT TRUE,
                             fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE ingredientes (
                              id SERIAL NOT NULL PRIMARY KEY,
                              codigo VARCHAR(20) NOT NULL UNIQUE,
                              nombre VARCHAR(100) NOT NULL,
                              cantidad_disponible DECIMAL(10, 3) NOT NULL DEFAULT 0,
                              unidad_medida VARCHAR(20) NOT NULL,
                              costo_unitario DECIMAL(10, 2),
                              descripcion TEXT,
                              cantidad_minima DECIMAL(10, 3),
                              activo BOOLEAN NOT NULL DEFAULT TRUE,
                              fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE recetas (
                         id SERIAL NOT NULL PRIMARY KEY,
                         codigo VARCHAR(20) NOT NULL UNIQUE,
                         nombre VARCHAR(100) NOT NULL,
                         instrucciones TEXT NOT NULL,
                         tiempo_preparacion INT,
                         cantidad_producida DECIMAL(10, 3) NOT NULL DEFAULT 1,
                         unidad_produccion VARCHAR(20),
                         activo BOOLEAN NOT NULL DEFAULT TRUE,
                         fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         id_usuario_creador INT REFERENCES usuarios(id)
);

CREATE TABLE productos (
                           id SERIAL NOT NULL PRIMARY KEY,
                           codigo VARCHAR(20) NOT NULL UNIQUE,
                           nombre VARCHAR(100) NOT NULL,
                           sabor VARCHAR(50),
                           precio DECIMAL(10, 2) NOT NULL CHECK (precio > 0),
                           id_receta INT NOT NULL REFERENCES recetas(id),
                           tipo tipo_producto NOT NULL,
                           descripcion TEXT,
                           activo BOOLEAN NOT NULL DEFAULT TRUE,
                           fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE clientes (
                          id SERIAL NOT NULL PRIMARY KEY,
                          codigo VARCHAR(20) UNIQUE,
                          rfc VARCHAR(13) UNIQUE,
                          nombre VARCHAR(100) NOT NULL,
                          apellido VARCHAR(100) NOT NULL,
                          correo VARCHAR(100) UNIQUE,
                          telefono VARCHAR(15),
                          direccion VARCHAR(255),
                          activo BOOLEAN NOT NULL DEFAULT TRUE,
                          fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE lotes_produccion (
                                  id SERIAL NOT NULL PRIMARY KEY,
                                  codigo VARCHAR(30) NOT NULL UNIQUE,
                                  id_producto INT NOT NULL REFERENCES productos(id),
                                  id_receta INT NOT NULL REFERENCES recetas(id),
                                  cantidad_producida DECIMAL(10, 3) NOT NULL CHECK (cantidad_producida > 0),
                                  fecha_elaboracion DATE NOT NULL DEFAULT CURRENT_DATE,
                                  hora_inicio TIME,
                                  hora_fin TIME,
                                  id_empleado_productor INT NOT NULL REFERENCES empleados(id),
                                  id_usuario_registro INT NOT NULL REFERENCES usuarios(id),
                                  id_sucursal INT NOT NULL REFERENCES sucursales(id),
                                  estado estado_lote NOT NULL DEFAULT 'En Proceso',
                                  observaciones TEXT,
                                  costo_total DECIMAL(10, 2),
                                  fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE detalle_produccion_ingredientes (
                                                 id SERIAL NOT NULL PRIMARY KEY,
                                                 id_lote_produccion INT NOT NULL REFERENCES lotes_produccion(id) ON DELETE CASCADE,
                                                 id_ingrediente INT NOT NULL REFERENCES ingredientes(id),
                                                 cantidad_usada DECIMAL(10, 3) NOT NULL CHECK (cantidad_usada > 0),
                                                 costo_unitario DECIMAL(10, 2) NOT NULL,
                                                 costo_total DECIMAL(10, 2) NOT NULL,
                                                 UNIQUE(id_lote_produccion, id_ingrediente)
);

CREATE TABLE compras (
                         id SERIAL NOT NULL PRIMARY KEY,
                         codigo VARCHAR(30) NOT NULL UNIQUE,
                         id_proveedor INT NOT NULL REFERENCES proveedores(id),
                         fecha_compra DATE NOT NULL DEFAULT CURRENT_DATE,
                         fecha_entrega_esperada DATE,
                         fecha_entrega_real DATE,
                         subtotal DECIMAL(10, 2) NOT NULL CHECK (subtotal >= 0),
                         impuestos DECIMAL(10, 2) NOT NULL DEFAULT 0 CHECK (impuestos >= 0),
                         total DECIMAL(10, 2) NOT NULL CHECK (total > 0),
                         id_usuario_registro INT NOT NULL REFERENCES usuarios(id),
                         estado VARCHAR(30) DEFAULT 'Pendiente',
                         notas TEXT,
                         fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE detalles_compras (
                                  id SERIAL NOT NULL PRIMARY KEY,
                                  id_compra INT NOT NULL REFERENCES compras(id) ON DELETE CASCADE,
                                  id_ingrediente INT NOT NULL REFERENCES ingredientes(id),
                                  cantidad DECIMAL(10, 3) NOT NULL CHECK (cantidad > 0),
                                  precio_unitario DECIMAL(10, 2) NOT NULL CHECK (precio_unitario > 0),
                                  subtotal DECIMAL(10, 2) NOT NULL CHECK (subtotal >= 0),
                                  lote_proveedor VARCHAR(50),
                                  fecha_caducidad DATE
);

CREATE TABLE ventas (
                        id SERIAL NOT NULL PRIMARY KEY,
                        codigo VARCHAR(30) NOT NULL UNIQUE,
                        id_cliente INT REFERENCES clientes(id),
                        id_sucursal INT NOT NULL REFERENCES sucursales(id),
                        id_empleado_vendedor INT REFERENCES empleados(id),
                        id_usuario_registro INT REFERENCES usuarios(id),
                        fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        subtotal DECIMAL(10, 2) NOT NULL CHECK (subtotal >= 0),
                        descuento DECIMAL(10, 2) NOT NULL DEFAULT 0 CHECK (descuento >= 0),
                        impuestos DECIMAL(10, 2) NOT NULL DEFAULT 0 CHECK (impuestos >= 0),
                        total DECIMAL(10, 2) NOT NULL CHECK (total >= 0),
                        metodo_pago metodo_pago,
                        notas TEXT
);

CREATE TABLE detalles_ventas (
                                 id SERIAL NOT NULL PRIMARY KEY,
                                 id_venta INT NOT NULL REFERENCES ventas(id) ON DELETE CASCADE,
                                 id_producto INT NOT NULL REFERENCES productos(id),
                                 id_lote_produccion INT REFERENCES lotes_produccion(id),
                                 cantidad INT NOT NULL CHECK (cantidad > 0),
                                 precio_unitario DECIMAL(10, 2) NOT NULL CHECK (precio_unitario > 0),
                                 descuento DECIMAL(10, 2) DEFAULT 0 CHECK (descuento >= 0),
                                 subtotal DECIMAL(10, 2) NOT NULL CHECK (subtotal >= 0)
);

CREATE TABLE proveedores_ingredientes (
                                          id_proveedor INT NOT NULL REFERENCES proveedores(id) ON DELETE CASCADE,
                                          id_ingrediente INT NOT NULL REFERENCES ingredientes(id) ON DELETE CASCADE,
                                          precio_unitario DECIMAL(10, 2) NOT NULL CHECK (precio_unitario > 0),
                                          cantidad_minima_compra DECIMAL(10, 3) NOT NULL CHECK (cantidad_minima_compra > 0),
                                          tiempo_entrega_dias INT,
                                          es_proveedor_principal BOOLEAN DEFAULT FALSE,
                                          fecha_ultima_compra DATE,
                                          PRIMARY KEY (id_proveedor, id_ingrediente)
);

CREATE TABLE recetas_ingredientes (
                                      id_receta INT NOT NULL REFERENCES recetas(id) ON DELETE CASCADE,
                                      id_ingrediente INT NOT NULL REFERENCES ingredientes(id) ON DELETE CASCADE,
                                      cantidad_requerida DECIMAL(10, 3) NOT NULL CHECK (cantidad_requerida > 0),
                                      orden_uso INT,
                                      PRIMARY KEY (id_receta, id_ingrediente)
);

CREATE TABLE inventario_sucursales (
                                       id_sucursal INT NOT NULL REFERENCES sucursales(id) ON DELETE CASCADE,
                                       id_producto INT NOT NULL REFERENCES productos(id) ON DELETE CASCADE,
                                       cantidad_disponible INT NOT NULL DEFAULT 0 CHECK (cantidad_disponible >= 0),
                                       cantidad_minima INT DEFAULT 5,
                                       fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       PRIMARY KEY (id_sucursal, id_producto)
);

CREATE TABLE movimientos_inventario (
                                        id SERIAL NOT NULL PRIMARY KEY,
                                        tipo_movimiento VARCHAR(30) NOT NULL,
                                        referencia_tipo VARCHAR(30),
                                        referencia_id INT,
                                        id_ingrediente INT REFERENCES ingredientes(id),
                                        id_producto INT REFERENCES productos(id),
                                        id_sucursal INT REFERENCES sucursales(id),
                                        cantidad_anterior DECIMAL(10, 3),
                                        cantidad_movimiento DECIMAL(10, 3) NOT NULL,
                                        cantidad_nueva DECIMAL(10, 3),
                                        id_usuario INT NOT NULL REFERENCES usuarios(id),
                                        fecha_movimiento TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                        observaciones TEXT,
                                        CONSTRAINT chk_tipo_item CHECK (
                                            (id_ingrediente IS NOT NULL AND id_producto IS NULL) OR
                                            (id_ingrediente IS NULL AND id_producto IS NOT NULL)
                                            )
);

-- =======================
-- 3. VISTAS
-- =======================

CREATE OR REPLACE VIEW vista_empleados_completa AS
SELECT
    e.id, e.codigo, e.nombres, e.apellidos,
    CONCAT(e.nombres, ' ', e.apellidos) AS nombre_completo,
    e.fecha_nacimiento, e.lugar_nacimiento, e.direccion, e.telefono, e.correo,
    e.fecha_ingreso, e.tipo AS tipo_empleado,
    s.nombre AS sucursal, s.ciudad AS ciudad_sucursal,
    e.activo AS empleado_activo,
    u.id AS id_usuario, u.usuario,
    u.rol AS rol_legacy, r.nombre AS rol_seguridad,
    u.ultimo_acceso, u.activo AS usuario_activo,
    EXTRACT(YEAR FROM AGE(CURRENT_DATE, e.fecha_ingreso)) AS anos_antiguedad
FROM empleados e
         LEFT JOIN sucursales s ON e.id_sucursal = s.id
         LEFT JOIN usuarios u ON e.id = u.id_empleado
         LEFT JOIN roles r ON u.id_rol = r.id
ORDER BY e.apellidos, e.nombres;

CREATE OR REPLACE VIEW vista_costo_recetas AS
SELECT
    r.id AS id_receta, r.codigo, r.nombre AS receta, r.tiempo_preparacion,
    r.cantidad_producida, r.unidad_produccion,
    COALESCE(ROUND(SUM(ri.cantidad_requerida * i.costo_unitario), 2), 0) AS costo_total,
    COALESCE(ROUND(SUM(ri.cantidad_requerida * i.costo_unitario) / NULLIF(r.cantidad_producida, 0), 2), 0) AS costo_unitario,
    COUNT(DISTINCT ri.id_ingrediente) AS numero_ingredientes, r.activo
FROM recetas r
         LEFT JOIN recetas_ingredientes ri ON r.id = ri.id_receta
         LEFT JOIN ingredientes i ON ri.id_ingrediente = i.id
WHERE r.activo = TRUE
GROUP BY r.id, r.codigo, r.nombre, r.tiempo_preparacion, r.cantidad_producida, r.unidad_produccion, r.activo;

CREATE OR REPLACE VIEW vista_productos_margen AS
SELECT
    p.id, p.codigo, p.nombre AS producto, p.sabor, p.tipo, p.precio AS precio_venta,
    vcr.costo_unitario AS costo_produccion,
    ROUND(p.precio - vcr.costo_unitario, 2) AS ganancia_unitaria,
    ROUND(((p.precio - vcr.costo_unitario) / NULLIF(p.precio, 0)) * 100, 2) AS margen_porcentaje,
    p.activo
FROM productos p
         LEFT JOIN vista_costo_recetas vcr ON p.id_receta = vcr.id_receta
WHERE p.activo = TRUE
ORDER BY margen_porcentaje DESC;

CREATE OR REPLACE VIEW vista_inventario_sucursales AS
SELECT
    s.id AS id_sucursal, s.codigo AS codigo_sucursal, s.nombre AS sucursal, s.ciudad,
    p.id AS id_producto, p.codigo AS codigo_producto, p.nombre AS producto, p.tipo,
    p.precio AS precio_venta, i.cantidad_disponible, i.cantidad_minima,
    CASE
        WHEN i.cantidad_disponible <= 0 THEN 'AGOTADO'
        WHEN i.cantidad_disponible <= i.cantidad_minima THEN 'CRÍTICO'
        WHEN i.cantidad_disponible <= i.cantidad_minima * 2 THEN 'BAJO'
        ELSE 'ADECUADO'
        END AS estado_stock,
    i.fecha_actualizacion
FROM sucursales s
         JOIN inventario_sucursales i ON s.id = i.id_sucursal
         JOIN productos p ON i.id_producto = p.id
WHERE s.activo = TRUE AND p.activo = TRUE;

-- =======================
-- 4. FUNCIONES Y TRIGGERS
-- =======================

CREATE OR REPLACE FUNCTION generar_codigo_secuencial(
    p_prefijo VARCHAR, p_tabla VARCHAR, p_columna VARCHAR DEFAULT 'codigo'
)
    RETURNS VARCHAR AS $$
DECLARE
    v_ultimo_numero INT;
    v_nuevo_codigo VARCHAR;
BEGIN
    EXECUTE format(
            'SELECT COALESCE(MAX(CAST(SUBSTRING(%I FROM LENGTH($1) + 1) AS INT)), 0) FROM %I WHERE %I LIKE $1 || ''%%''',
            p_columna, p_tabla, p_columna
            ) INTO v_ultimo_numero USING p_prefijo;
    v_nuevo_codigo := p_prefijo || LPAD((v_ultimo_numero + 1)::TEXT, 6, '0');
    RETURN v_nuevo_codigo;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION generar_codigo_automatico()
    RETURNS TRIGGER AS $$
BEGIN
    IF NEW.codigo IS NULL OR NEW.codigo = '' THEN
        CASE TG_TABLE_NAME
            WHEN 'empleados' THEN NEW.codigo := generar_codigo_secuencial('EMP', 'empleados');
            WHEN 'proveedores' THEN NEW.codigo := generar_codigo_secuencial('PROV', 'proveedores');
            WHEN 'ingredientes' THEN NEW.codigo := generar_codigo_secuencial('ING', 'ingredientes');
            WHEN 'recetas' THEN NEW.codigo := generar_codigo_secuencial('REC', 'recetas');
            WHEN 'productos' THEN NEW.codigo := generar_codigo_secuencial('PROD', 'productos');
            WHEN 'sucursales' THEN NEW.codigo := generar_codigo_secuencial('SUC', 'sucursales');
            WHEN 'clientes' THEN NEW.codigo := generar_codigo_secuencial('CLI', 'clientes');
            WHEN 'ventas' THEN NEW.codigo := generar_codigo_secuencial('VTA', 'ventas');
            WHEN 'compras' THEN NEW.codigo := generar_codigo_secuencial('COMP', 'compras');
            WHEN 'lotes_produccion' THEN NEW.codigo := generar_codigo_secuencial('LOTE', 'lotes_produccion');
            END CASE;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_gc_emp BEFORE INSERT ON empleados FOR EACH ROW EXECUTE FUNCTION generar_codigo_automatico();
CREATE TRIGGER trg_gc_prov BEFORE INSERT ON proveedores FOR EACH ROW EXECUTE FUNCTION generar_codigo_automatico();
CREATE TRIGGER trg_gc_ing BEFORE INSERT ON ingredientes FOR EACH ROW EXECUTE FUNCTION generar_codigo_automatico();
CREATE TRIGGER trg_gc_rec BEFORE INSERT ON recetas FOR EACH ROW EXECUTE FUNCTION generar_codigo_automatico();
CREATE TRIGGER trg_gc_prod BEFORE INSERT ON productos FOR EACH ROW EXECUTE FUNCTION generar_codigo_automatico();
CREATE TRIGGER trg_gc_suc BEFORE INSERT ON sucursales FOR EACH ROW EXECUTE FUNCTION generar_codigo_automatico();
CREATE TRIGGER trg_gc_cli BEFORE INSERT ON clientes FOR EACH ROW EXECUTE FUNCTION generar_codigo_automatico();
CREATE TRIGGER trg_gc_ven BEFORE INSERT ON ventas FOR EACH ROW EXECUTE FUNCTION generar_codigo_automatico();
CREATE TRIGGER trg_gc_com BEFORE INSERT ON compras FOR EACH ROW EXECUTE FUNCTION generar_codigo_automatico();
CREATE TRIGGER trg_gc_lot BEFORE INSERT ON lotes_produccion FOR EACH ROW EXECUTE FUNCTION generar_codigo_automatico();

CREATE OR REPLACE FUNCTION actualizar_inventario_venta()
    RETURNS TRIGGER AS $$
DECLARE v_venta RECORD;
BEGIN
    SELECT * INTO v_venta FROM ventas WHERE id = NEW.id_venta;
    UPDATE inventario_sucursales SET cantidad_disponible = cantidad_disponible - NEW.cantidad, fecha_actualizacion = CURRENT_TIMESTAMP
    WHERE id_sucursal = v_venta.id_sucursal AND id_producto = NEW.id_producto;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_venta AFTER INSERT ON detalles_ventas FOR EACH ROW EXECUTE FUNCTION actualizar_inventario_venta();

-- 1. Función para sumar al inventario cuando se crea un lote
CREATE OR REPLACE FUNCTION actualizar_stock_produccion()
    RETURNS TRIGGER AS $$
BEGIN
    -- Verificamos si ya existe el registro en inventario para esa sucursal/producto
    IF EXISTS (SELECT 1 FROM inventario_sucursales WHERE id_sucursal = NEW.id_sucursal AND id_producto = NEW.id_producto) THEN
        -- Si existe, sumamos
        UPDATE inventario_sucursales
        SET cantidad_disponible = cantidad_disponible + NEW.cantidad_producida,
            fecha_actualizacion = CURRENT_TIMESTAMP
        WHERE id_sucursal = NEW.id_sucursal AND id_producto = NEW.id_producto;
    ELSE
        -- Si no existe (producto nuevo en esa sucursal), lo insertamos
        INSERT INTO inventario_sucursales (id_sucursal, id_producto, cantidad_disponible, cantidad_minima)
        VALUES (NEW.id_sucursal, NEW.id_producto, NEW.cantidad_producida, 5);
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 2. El Trigger que dispara la función
-- Se activa cada vez que insertas un registro en 'lotes_produccion'
DROP TRIGGER IF EXISTS trg_produccion ON lotes_produccion;

CREATE TRIGGER trg_produccion
    AFTER INSERT ON lotes_produccion
    FOR EACH ROW
EXECUTE FUNCTION actualizar_stock_produccion();