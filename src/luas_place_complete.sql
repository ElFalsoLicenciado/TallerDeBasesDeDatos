-- ==========================================================
-- BASE DE DATOS: LUA'S PLACE
-- ==========================================================
-- Configuración Inicial
SET client_encoding TO 'UTF8';

-- Crear base de datos si no existe
SELECT 'CREATE DATABASE luas_place'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'luas_place')\gexec
\c luas_place;

-- =======================
-- 1. LIMPIEZA INICIAL
-- =======================

-- Tipos Base
DROP TYPE IF EXISTS tipo_producto CASCADE;
DROP TYPE IF EXISTS tipo_empleado CASCADE;
DROP TYPE IF EXISTS rol_usuario CASCADE;
DROP TYPE IF EXISTS metodo_pago CASCADE;
DROP TYPE IF EXISTS estado_lote CASCADE;

-- Tablas Base (El CASCADE borrará las dependientes)
DROP TABLE IF EXISTS detalle_produccion_ingredientes CASCADE;
DROP TABLE IF EXISTS lotes_produccion CASCADE;
DROP TABLE IF EXISTS detalles_ventas CASCADE;
DROP TABLE IF EXISTS ventas CASCADE;
DROP TABLE IF EXISTS detalles_compras CASCADE;
DROP TABLE IF EXISTS compras CASCADE;
DROP TABLE IF EXISTS inventario_sucursales CASCADE;
DROP TABLE IF EXISTS recetas_ingredientes CASCADE;
DROP TABLE IF EXISTS proveedores_ingredientes CASCADE;
DROP TABLE IF EXISTS movimientos_inventario CASCADE;
DROP TABLE IF EXISTS log_accesos CASCADE;
DROP TABLE IF EXISTS productos CASCADE;
DROP TABLE IF EXISTS recetas CASCADE;
DROP TABLE IF EXISTS ingredientes CASCADE;
DROP TABLE IF EXISTS proveedores CASCADE;
DROP TABLE IF EXISTS clientes CASCADE;
DROP TABLE IF EXISTS usuarios CASCADE;
DROP TABLE IF EXISTS empleados CASCADE;
DROP TABLE IF EXISTS sucursales CASCADE;

-- Tablas de Seguridad
DROP TABLE IF EXISTS usuarios_permisos CASCADE;
DROP TABLE IF EXISTS roles_permisos CASCADE;
DROP TABLE IF EXISTS permisos CASCADE;
DROP TABLE IF EXISTS roles CASCADE;
DROP TABLE IF EXISTS sesiones CASCADE;
DROP TABLE IF EXISTS logs CASCADE;

-- =======================
-- 2. TIPOS ENUMERADOS
-- =======================

CREATE TYPE tipo_producto AS ENUM ('Bebida', 'Pan', 'Postre', 'Otros');
CREATE TYPE tipo_empleado AS ENUM ('Administrativo', 'Produccion');
CREATE TYPE rol_usuario AS ENUM ('Admin', 'Administrativo', 'Produccion'); -- Se mantiene por compatibilidad de vistas
CREATE TYPE metodo_pago AS ENUM ('Efectivo', 'Tarjeta', 'Transferencia', 'Otro');
CREATE TYPE estado_lote AS ENUM ('En Proceso', 'Completado', 'Cancelado');

-- =======================
-- 3. ESTRUCTURA DE SEGURIDAD (RBAC)
-- =======================

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

-- =======================
-- 4. ESTRUCTURA PRINCIPAL
-- =======================

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
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_telefono CHECK (telefono ~ '^[0-9\-\+\(\) ]+$')
);

CREATE TABLE usuarios (
    id SERIAL NOT NULL PRIMARY KEY,
    id_empleado INT NOT NULL UNIQUE REFERENCES empleados(id) ON DELETE CASCADE,
    usuario VARCHAR(30) NOT NULL UNIQUE,
    contrasena VARCHAR(255) NOT NULL,
    rol rol_usuario NOT NULL DEFAULT 'Produccion', -- Rol Legacy (Enum)
    id_rol INT REFERENCES roles(id),               -- Rol Nuevo (Tabla de Seguridad)
    ultimo_acceso TIMESTAMP,
    intentos_fallidos INT DEFAULT 0,
    bloqueado BOOLEAN DEFAULT FALSE,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_usuario_length CHECK (LENGTH(usuario) >= 4),
    CONSTRAINT chk_contrasena_length CHECK (LENGTH(contrasena) >= 8)
);

-- Tablas de Seguridad dependientes de usuarios
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

-- Tablas de Negocio
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
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_cantidad_disponible CHECK (cantidad_disponible >= 0)
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

CREATE TABLE log_accesos (
    id SERIAL NOT NULL PRIMARY KEY,
    id_usuario INT REFERENCES usuarios(id),
    usuario VARCHAR(30),
    accion VARCHAR(50) NOT NULL,
    ip_address INET,
    user_agent TEXT,
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    exitoso BOOLEAN NOT NULL
);

-- =======================
-- 5. VISTAS Y REPORTES
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
-- 6. FUNCIONES Y TRIGGERS
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

CREATE OR REPLACE FUNCTION actualizar_ultimo_acceso()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.exitoso = TRUE THEN
        UPDATE usuarios SET ultimo_acceso = NEW.fecha, intentos_fallidos = 0 WHERE id = NEW.id_usuario;
    ELSE
        UPDATE usuarios SET intentos_fallidos = intentos_fallidos + 1,
            bloqueado = CASE WHEN intentos_fallidos >= 5 THEN TRUE ELSE bloqueado END
        WHERE id = NEW.id_usuario;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_actualizar_ultimo_acceso AFTER INSERT ON log_accesos
FOR EACH ROW EXECUTE FUNCTION actualizar_ultimo_acceso();

-- Generación de Códigos Automáticos
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

-- Triggers de Negocio (Inventario y Costos)
CREATE OR REPLACE FUNCTION descontar_ingredientes_produccion()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.estado = 'Completado' AND (OLD.estado IS NULL OR OLD.estado != 'Completado') THEN
        INSERT INTO detalle_produccion_ingredientes (id_lote_produccion, id_ingrediente, cantidad_usada, costo_unitario, costo_total)
        SELECT NEW.id, ri.id_ingrediente, ri.cantidad_requerida, COALESCE(i.costo_unitario, 0), ri.cantidad_requerida * COALESCE(i.costo_unitario, 0)
        FROM recetas_ingredientes ri JOIN ingredientes i ON ri.id_ingrediente = i.id WHERE ri.id_receta = NEW.id_receta;
        
        UPDATE ingredientes i SET cantidad_disponible = cantidad_disponible - ri.cantidad_requerida
        FROM recetas_ingredientes ri WHERE i.id = ri.id_ingrediente AND ri.id_receta = NEW.id_receta;
        
        -- Actualizar Inventario Sucursal (Producto terminado)
        UPDATE inventario_sucursales SET cantidad_disponible = cantidad_disponible + NEW.cantidad_producida::INT, fecha_actualizacion = CURRENT_TIMESTAMP
        WHERE id_sucursal = NEW.id_sucursal AND id_producto = NEW.id_producto;
        
        IF NOT FOUND THEN
            INSERT INTO inventario_sucursales (id_sucursal, id_producto, cantidad_disponible) VALUES (NEW.id_sucursal, NEW.id_producto, NEW.cantidad_producida::INT);
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_produccion AFTER INSERT OR UPDATE ON lotes_produccion FOR EACH ROW EXECUTE FUNCTION descontar_ingredientes_produccion();

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

-- =======================
-- 7. CARGA DE DATOS (SEED)
-- =======================

-- 7.1 DATOS DE SEGURIDAD (ROLES Y PERMISOS)
INSERT INTO roles (nombre, descripcion) VALUES 
('Super Admin', 'Acceso total al sistema y configuración global'),
('Gerente Sucursal', 'Gestión completa de inventarios, personal y ventas de su sucursal'),
('Cajero', 'Registro de ventas, manejo de caja y atención al cliente'),
('Produccion', 'Gestión de recetas, lotes de producción y consumo de ingredientes'),
('Supervisor', 'Autorización de cancelaciones, cortes de caja y mermas'),
('Auditor', 'Acceso de solo lectura a reportes financieros y logs');

INSERT INTO permisos (codigo, descripcion, modulo) VALUES 
('sys.full_access', 'Acceso total', 'Sistema'),
('ventas.crear', 'Registrar nuevas ventas', 'Ventas'),
('ventas.ver_reportes', 'Ver reportes financieros', 'Ventas'),
('ventas.cancelar', 'Autorizar cancelación de tickets', 'Ventas'),
('inventario.ajustar', 'Realizar ajustes manuales de stock', 'Inventario'),
('inventario.merma', 'Registrar desperdicios o accidentes', 'Inventario'),
('produccion.crear', 'Crear lotes de producción', 'Produccion'),
('usuarios.ver', 'Ver lista de empleados y usuarios', 'RH'),
('reportes.auditoria', 'Acceso profundo a logs y finanzas', 'Sistema');

-- === ASIGNACIÓN DE PERMISOS ===

-- Super Admin
INSERT INTO roles_permisos (id_rol, id_permiso)
SELECT r.id, p.id FROM roles r, permisos p WHERE r.nombre = 'Super Admin' AND p.codigo = 'sys.full_access';

-- Gerente
INSERT INTO roles_permisos (id_rol, id_permiso)
SELECT r.id, p.id FROM roles r, permisos p 
WHERE r.nombre = 'Gerente Sucursal' 
AND p.codigo IN ('ventas.ver_reportes', 'inventario.ajustar', 'usuarios.ver', 'inventario.merma');

-- Cajero
INSERT INTO roles_permisos (id_rol, id_permiso)
SELECT r.id, p.id FROM roles r, permisos p 
WHERE r.nombre = 'Cajero' 
AND p.codigo IN ('ventas.crear');

-- Producción
INSERT INTO roles_permisos (id_rol, id_permiso)
SELECT r.id, p.id FROM roles r, permisos p 
WHERE r.nombre = 'Produccion' 
AND p.codigo IN ('produccion.crear');

-- Supervisor
INSERT INTO roles_permisos (id_rol, id_permiso)
SELECT r.id, p.id FROM roles r, permisos p 
WHERE r.nombre = 'Supervisor' 
AND p.codigo IN ('ventas.cancelar', 'inventario.merma', 'ventas.crear', 'ventas.ver_reportes');

-- Auditor
INSERT INTO roles_permisos (id_rol, id_permiso)
SELECT r.id, p.id FROM roles r, permisos p 
WHERE r.nombre = 'Auditor' 
AND p.codigo IN ('ventas.ver_reportes', 'usuarios.ver', 'reportes.auditoria');


-- 7.2 DATOS DE NEGOCIO (MORELIA)

-- Sucursales
INSERT INTO sucursales (nombre, ciudad, calle, colonia, numero, codigo_postal, telefono) VALUES
('Lua''s Place Centro', 'Morelia', 'Av. Madero Poniente', 'Centro Histórico', '453', '58000', '4433123456'),
('Lua''s Place Altozano', 'Morelia', 'Av. Montaña Monarca', 'Altozano', '1000', '58260', '4433987654'),
('Lua''s Place Acueducto', 'Morelia', 'Av. Acueducto', 'Chapultepec Norte', '2300', '58260', '4433112233');

-- Empleados (Gabriela Lua es la Admin)
INSERT INTO empleados (nombres, apellidos, fecha_nacimiento, lugar_nacimiento, direccion, telefono, correo, tipo, id_sucursal) VALUES
('Gabriela', 'Lua Vargas', '1985-03-15', 'Morelia, Michoacán', 'Calle Virrey de Mendoza #120, Centro', '4431111111', 'gabriela.lua@luasplace.com', 'Administrativo', NULL),
('María Elena', 'García Ramírez', '1990-07-22', 'Morelia, Michoacán', 'Av. Camelinas #230', '4431111112', 'maria.garcia@luasplace.com', 'Administrativo', NULL),
('Pedro', 'Martínez Sánchez', '1992-11-08', 'Pátzcuaro, Michoacán', 'Villas del Pedregal Etapa 3', '4431111113', 'pedro.martinez@luasplace.com', 'Produccion', 1),
('Ana Laura', 'Hernández Torres', '1988-05-30', 'Uruapan, Michoacán', 'Col. Ventura Puente', '4431111114', 'ana.hernandez@luasplace.com', 'Produccion', 2),
('Roberto', 'González Flores', '1995-09-12', 'Morelia, Michoacán', 'Col. Félix Ireta', '4431111115', 'roberto.gonzalez@luasplace.com', 'Produccion', 3),
('Sofía', 'Ramírez Cruz', '1998-02-14', 'Morelia, Michoacán', 'Calle La Paz, Centro', '4431112222', 'sofia.ramirez@luasplace.com', 'Administrativo', 1),
('Miguel', 'Loza Pantoja', '1997-11-30', 'Tarímbaro, Michoacán', 'Fracc. Metrópolis', '4431113333', 'miguel.loza@luasplace.com', 'Administrativo', 2);

-- Usuarios
-- Password hash genérico: password123
INSERT INTO usuarios (id_empleado, usuario, contrasena, rol, id_rol) VALUES
((SELECT id FROM empleados WHERE correo='gabriela.lua@luasplace.com'), 'admin', '$2a$10$xQEZqvLhSJjqeY3R6P3o2.rMVZ3j3Y8lKqHhCQpZnMcxEzD1t5Kz2', 'Admin', (SELECT id FROM roles WHERE nombre = 'Super Admin')),
((SELECT id FROM empleados WHERE correo='maria.garcia@luasplace.com'), 'mgarcia', '$2a$10$xQEZqvLhSJjqeY3R6P3o2.rMVZ3j3Y8lKqHhCQpZnMcxEzD1t5Kz2', 'Administrativo', (SELECT id FROM roles WHERE nombre = 'Gerente Sucursal')),
((SELECT id FROM empleados WHERE correo='pedro.martinez@luasplace.com'), 'pmartinez', '$2a$10$xQEZqvLhSJjqeY3R6P3o2.rMVZ3j3Y8lKqHhCQpZnMcxEzD1t5Kz2', 'Produccion', (SELECT id FROM roles WHERE nombre = 'Produccion')),
((SELECT id FROM empleados WHERE correo='ana.hernandez@luasplace.com'), 'ahernandez', '$2a$10$xQEZqvLhSJjqeY3R6P3o2.rMVZ3j3Y8lKqHhCQpZnMcxEzD1t5Kz2', 'Produccion', (SELECT id FROM roles WHERE nombre = 'Produccion')),
((SELECT id FROM empleados WHERE correo='roberto.gonzalez@luasplace.com'), 'rgonzalez', '$2a$10$xQEZqvLhSJjqeY3R6P3o2.rMVZ3j3Y8lKqHhCQpZnMcxEzD1t5Kz2', 'Produccion', (SELECT id FROM roles WHERE nombre = 'Produccion')),
((SELECT id FROM empleados WHERE correo='sofia.ramirez@luasplace.com'), 'sramirez', '$2a$10$xQEZqvLhSJjqeY3R6P3o2.rMVZ3j3Y8lKqHhCQpZnMcxEzD1t5Kz2', 'Administrativo', (SELECT id FROM roles WHERE nombre = 'Cajero')),
((SELECT id FROM empleados WHERE correo='miguel.loza@luasplace.com'), 'mloza', '$2a$10$xQEZqvLhSJjqeY3R6P3o2.rMVZ3j3Y8lKqHhCQpZnMcxEzD1t5Kz2', 'Administrativo', (SELECT id FROM roles WHERE nombre = 'Cajero'));

-- Proveedores (Locales)
INSERT INTO proveedores (rfc, nombre, correo, telefono, ciudad, calle, colonia, codigo_postal, numero, contacto_nombre) VALUES
('CAM800101AAA', 'Central de Abastos Morelia', 'ventas@abastosmorelia.com', '4433334444', 'Morelia', 'Periférico Paseo de la República', 'Mercado de Abastos', '58200', 'S/N', 'Sr. Rogelio'),
('LVA990909BBB', 'Lácteos Valladolid', 'contacto@lacteosvalladolid.com', '4433335555', 'Morelia', 'Calle Norte 4', 'Ciudad Industrial', '58200', '150', 'Carmen Ruiz'),
('CLU500505CCC', 'Café La Lucha Uruapan', 'ventas@cafelalucha.com', '4525559988', 'Uruapan', 'Paseo Lázaro Cárdenas', 'Centro', '60000', '500', 'Roberto Silva'),
('EMI880808DDD', 'Empaques de Michoacán', 'ventas@empaquesmichoacan.com', '4433123412', 'Morelia', 'Av. Periodismo', 'Agustín Arriaga', '58190', '44', 'Susana Loza'),
('FRU770707EEE', 'Frutas y Verduras El Tarasco', 'ventas@eltarasco.com', '4433887766', 'Morelia', 'Vicente Santa María', 'Ventura Puente', '58020', '800', 'Don José');

-- Ingredientes (Lista Ampliada)
INSERT INTO ingredientes (nombre, cantidad_disponible, unidad_medida, costo_unitario, cantidad_minima, descripcion) VALUES
('Harina de Trigo', 50.000, 'kg', 25.50, 10.000, 'Harina de trigo refinada'),
('Azúcar', 30.000, 'kg', 18.75, 5.000, 'Azúcar estándar'),
('Mantequilla', 15.000, 'kg', 85.00, 3.000, 'Mantequilla sin sal'),
('Huevos', 200.000, 'piezas', 2.50, 50.000, 'Huevos frescos'),
('Leche Entera', 25.000, 'litros', 22.00, 8.000, 'Leche pasteurizada'),
('Café Grano', 10.000, 'kg', 180.00, 2.000, 'Café de altura'),
('Chocolate Amargo', 8.000, 'kg', 95.00, 2.000, 'Chocolate 70% cacao'),
('Vainilla', 1.000, 'litros', 250.00, 0.200, 'Extracto natural'),
('Levadura', 3.000, 'kg', 45.00, 0.500, 'Levadura fresca'),
('Sal', 10.000, 'kg', 8.50, 2.000, 'Sal de mar'),
('Vaso Desechable 12oz', 500.000, 'piezas', 1.50, 100.000, 'Vaso cartón bebida caliente'),
('Cocoa en Polvo', 5.000, 'kg', 120.00, 1.000, 'Cocoa alcalina sin azúcar'),
('Canela Molida', 2.000, 'kg', 300.00, 0.500, 'Canela pura molida'),
('Fresas Naturales', 5.000, 'kg', 60.00, 1.000, 'Fresa limpia desinfectada'),
('Té Manzanilla', 100.000, 'piezas', 1.50, 20.000, 'Sobre de té individual');

-- Proveedores-Ingredientes
INSERT INTO proveedores_ingredientes (id_proveedor, id_ingrediente, precio_unitario, cantidad_minima_compra, tiempo_entrega_dias, es_proveedor_principal) VALUES
(1, 1, 25.50, 10.000, 1, TRUE), (1, 2, 18.75, 5.000, 1, TRUE), (1, 4, 2.50, 50.000, 1, TRUE),
(1, 12, 120.00, 1.000, 1, TRUE), (1, 13, 300.00, 0.500, 1, TRUE), (1, 15, 1.50, 20.000, 1, TRUE),
(2, 3, 85.00, 5.000, 1, TRUE), (2, 5, 22.00, 10.000, 1, TRUE),
(3, 6, 180.00, 2.000, 2, TRUE),
(4, 11, 1.50, 1000.00, 1, TRUE),
(5, 14, 60.00, 2.000, 1, TRUE); -- Fresas del Tarasco

-- Recetas (Lista Ampliada)
INSERT INTO recetas (nombre, instrucciones, tiempo_preparacion, cantidad_producida, unidad_produccion) VALUES
('Pan Dulce Tradicional', 'Mezclar y hornear', 120, 20.000, 'piezas'),
('Concha de Chocolate', 'Masa y cubierta', 90, 15.000, 'piezas'),
('Café Americano', 'Espresso y agua', 5, 1.000, 'taza'),
('Cappuccino', 'Espresso y leche espumada', 6, 1.000, 'taza'),
('Chocolate Caliente', 'Leche, cocoa y canela', 8, 1.000, 'taza'),
('Muffin de Vainilla', 'Batido cremoso y hornear', 45, 12.000, 'piezas'),
('Dona Glaseada', 'Masa fermentada frita', 60, 24.000, 'piezas'),
('Cheesecake de Fresa', 'Base galleta, relleno queso, top fresa', 120, 8.000, 'rebanadas'),
('Té de Manzanilla', 'Infusión agua caliente', 3, 1.000, 'taza');

-- Ingredientes por Receta
INSERT INTO recetas_ingredientes (id_receta, id_ingrediente, cantidad_requerida, orden_uso) VALUES
-- Pan Dulce (1)
(1, 1, 2.000, 1), (1, 2, 0.500, 2), (1, 4, 10.000, 3),
-- Concha (2)
(2, 1, 1.500, 1), (2, 2, 0.400, 2), (2, 7, 0.300, 3),
-- Americano (3)
(3, 6, 0.020, 1), (3, 11, 1.000, 2),
-- Cappuccino (4)
(4, 6, 0.020, 1), (4, 5, 0.200, 2), (4, 11, 1.000, 3),
-- Chocolate Caliente (5)
(5, 5, 0.250, 1), (5, 12, 0.020, 2), (5, 13, 0.005, 3), (5, 11, 1.000, 4),
-- Muffin (6)
(6, 1, 0.500, 1), (6, 2, 0.300, 2), (6, 3, 0.200, 3), (6, 8, 0.010, 4),
-- Dona (7)
(7, 1, 1.000, 1), (7, 2, 0.500, 2), (7, 9, 0.050, 3),
-- Cheesecake (8) - Requiere fresa
(8, 2, 0.500, 1), (8, 4, 5.000, 2), (8, 14, 0.500, 3), -- Usa 500g de fresa por pastel
-- Té (9)
(9, 15, 1.000, 1), (9, 11, 1.000, 2);

-- Productos (Menú Completo)
INSERT INTO productos (nombre, sabor, precio, id_receta, tipo, descripcion) VALUES
('Pan Dulce', 'Natural', 15.00, 1, 'Pan', 'Pieza individual'),
('Concha Chocolate', 'Chocolate', 18.00, 2, 'Pan', 'Concha tradicional'),
('Café Americano', 'Café', 35.00, 3, 'Bebida', '12oz'),
('Cappuccino', 'Café', 48.00, 4, 'Bebida', '12oz con espuma'),
('Chocolate Abuelita', 'Chocolate', 45.00, 5, 'Bebida', 'Estilo mexicano'),
('Muffin Vainilla', 'Vainilla', 22.00, 6, 'Pan', 'Esponjoso'),
('Dona Glaseada', 'Original', 12.00, 7, 'Pan', 'Estilo Krispy'),
('Cheesecake Fresa', 'Queso/Fresa', 65.00, 8, 'Postre', 'Rebanada'),
('Té Manzanilla', 'Herbal', 25.00, 9, 'Bebida', 'Relajante');

-- Inventario Inicial Sucursales (Carga para todas las tiendas)
INSERT INTO inventario_sucursales (id_sucursal, id_producto, cantidad_disponible, cantidad_minima) VALUES
-- Centro (Tiene todo)
(1, 1, 30, 5), (1, 2, 30, 5), (1, 3, 50, 10), (1, 4, 50, 10), 
(1, 5, 20, 5), (1, 6, 15, 5), (1, 7, 40, 10), (1, 8, 8, 2), (1, 9, 20, 5),
-- Altozano (Más bebidas y postres)
(2, 1, 10, 5), (2, 2, 10, 5), (2, 3, 60, 10), (2, 4, 60, 10),
(2, 5, 30, 5), (2, 6, 10, 5), (2, 7, 20, 5), (2, 8, 12, 2), (2, 9, 25, 5),
-- Acueducto (Básico)
(3, 1, 15, 5), (3, 2, 15, 5), (3, 3, 30, 10), (3, 4, 30, 10),
(3, 5, 10, 5), (3, 7, 20, 5);

-- Clientes (Morelia Realista)
INSERT INTO clientes (rfc, nombre, apellido, correo, telefono, direccion) VALUES
('GARA901201XY8', 'Roberto', 'García Aguirre', 'roberto.garcia@email.com', '4433334444', 'Av. Camelinas 500, Las Américas'),
('LOPM880315AB9', 'Patricia', 'López Morales', 'patricia.lopez@email.com', '4433334445', 'Calle Aquiles Serdán, Centro'),
('HEVA950505ZZ1', 'Verónica', 'Hernández', 'vero.hdez@email.com', '4431239876', 'Paseo Altozano'),
('MORA800808H45', 'Carlos', 'Morales', 'charly@hotmail.com', '4439998877', 'Ventura Puente');

UPDATE usuarios SET contrasena = 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f';

INSERT INTO roles_permisos (id_rol, id_permiso)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.nombre = 'Gerente Sucursal' AND p.codigo = 'ventas.crear';

UPDATE empleados
SET id_sucursal = 1
WHERE correo = 'maria.garcia@luasplace.com';