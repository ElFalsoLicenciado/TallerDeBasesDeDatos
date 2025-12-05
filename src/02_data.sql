-- ==========================================================
-- 02_DATA.SQL: CARGA DE DATOS (SEED)
-- ==========================================================

SET client_encoding TO 'UTF8';

-- 1. ROLES Y PERMISOS BASE
INSERT INTO roles (nombre, descripcion) VALUES
('Super Admin', 'Acceso total'),
('Gerente Sucursal', 'Gestión de sucursal'),
('Cajero', 'Ventas'),
('Produccion', 'Cocina'),
('Supervisor', 'Autorizaciones'),
('Auditor', 'Solo lectura');

INSERT INTO permisos (codigo, descripcion, modulo) VALUES
('sys.full_access', 'Acceso total', 'Sistema'),
('ventas.crear', 'Registrar ventas', 'Ventas'),
('ventas.ver_reportes', 'Ver reportes', 'Ventas'),
('ventas.cancelar', 'Cancelar ventas', 'Ventas'),
('inventario.ajustar', 'Ajustar stock', 'Inventario'),
('inventario.merma', 'Registrar merma', 'Inventario'),
('produccion.crear', 'Crear lotes', 'Produccion'),
('usuarios.ver', 'Gestionar usuarios', 'RH'),
('reportes.auditoria', 'Auditoría', 'Sistema');

-- Asignación de Permisos
INSERT INTO roles_permisos (id_rol, id_permiso)
SELECT r.id, p.id FROM roles r, permisos p WHERE r.nombre = 'Super Admin' AND p.codigo = 'sys.full_access';

INSERT INTO roles_permisos (id_rol, id_permiso)
SELECT r.id, p.id FROM roles r, permisos p WHERE r.nombre = 'Gerente Sucursal'
                                             AND p.codigo IN ('ventas.ver_reportes', 'inventario.ajustar', 'usuarios.ver', 'inventario.merma', 'ventas.crear', 'produccion.crear');

INSERT INTO roles_permisos (id_rol, id_permiso)
SELECT r.id, p.id FROM roles r, permisos p WHERE r.nombre = 'Cajero' AND p.codigo IN ('ventas.crear');

INSERT INTO roles_permisos (id_rol, id_permiso)
SELECT r.id, p.id FROM roles r, permisos p WHERE r.nombre = 'Produccion' AND p.codigo IN ('produccion.crear');

-- Insertar el permiso de gestión de seguridad si no existe
INSERT INTO permisos (codigo, descripcion, modulo)
VALUES ('sys.security_manage', 'Gestionar Roles y Permisos', 'Sistema')
    ON CONFLICT (codigo) DO NOTHING;

-- Asignar este permiso al Super Admin
INSERT INTO roles_permisos (id_rol, id_permiso)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.nombre = 'Super Admin' AND p.codigo = 'sys.security_manage'
    ON CONFLICT DO NOTHING;

-- 2. DATOS DE NEGOCIO
INSERT INTO sucursales (nombre, ciudad, calle, colonia, numero, codigo_postal, telefono) VALUES
('Lua''s Place Centro', 'Morelia', 'Av. Madero Poniente', 'Centro Histórico', '453', '58000', '4433123456'),
('Lua''s Place Altozano', 'Morelia', 'Av. Montaña Monarca', 'Altozano', '1000', '58260', '4433987654'),
('Lua''s Place Acueducto', 'Morelia', 'Av. Acueducto', 'Chapultepec Norte', '2300', '58260', '4433112233');

INSERT INTO proveedores (rfc, nombre, correo, telefono, ciudad, calle, colonia, codigo_postal, numero) VALUES
('CAM800101AAA', 'Central de Abastos Morelia', 'ventas@abastos.com', '4433334444', 'Morelia', 'Periférico', 'Abastos', '58200', 'S/N'),
('LVA990909BBB', 'Lácteos Valladolid', 'contacto@lacteos.com', '4433335555', 'Morelia', 'Norte 4', 'Cd Industrial', '58200', '150');

INSERT INTO ingredientes (nombre, cantidad_disponible, unidad_medida, costo_unitario, cantidad_minima) VALUES
('Harina de Trigo', 50.000, 'kg', 25.50, 10.000),
('Azúcar', 30.000, 'kg', 18.75, 5.000),
('Mantequilla', 15.000, 'kg', 85.00, 3.000),
('Huevos', 200.000, 'piezas', 2.50, 50.000),
('Leche Entera', 25.000, 'litros', 22.00, 8.000),
('Café Grano', 10.000, 'kg', 180.00, 2.000),
('Chocolate Amargo', 8.000, 'kg', 95.00, 2.000),
('Vainilla', 1.000, 'litros', 250.00, 0.200),
('Vaso Desechable 12oz', 500.000, 'piezas', 1.50, 100.000),
('Cocoa en Polvo', 5.000, 'kg', 120.00, 1.000),
('Canela Molida', 2.000, 'kg', 300.00, 0.500),
('Té Manzanilla', 100.000, 'piezas', 1.50, 20.000);

INSERT INTO proveedores_ingredientes (id_proveedor, id_ingrediente, precio_unitario, cantidad_minima_compra)
SELECT 1, id, costo_unitario, 10 FROM ingredientes;

INSERT INTO recetas (nombre, instrucciones, tiempo_preparacion, cantidad_producida, unidad_produccion) VALUES
('Pan Dulce Tradicional', 'Mezclar y hornear', 120, 20.000, 'piezas'),
('Concha de Chocolate', 'Masa y cubierta', 90, 15.000, 'piezas'),
('Café Americano', 'Espresso y agua', 5, 1.000, 'taza'),
('Cappuccino', 'Espresso y leche espumada', 6, 1.000, 'taza'),
('Chocolate Caliente', 'Leche, cocoa y canela', 8, 1.000, 'taza');

-- ==========================================================
-- REPARACIÓN DE RECETAS E INGREDIENTES
-- ==========================================================

-- 1. Limpiar la tabla de relaciones para empezar de cero sin errores
TRUNCATE TABLE recetas_ingredientes CASCADE;

-- 2. Insertar ingredientes para 'Pan Dulce Tradicional'
INSERT INTO recetas_ingredientes (id_receta, id_ingrediente, cantidad_requerida, orden_uso)
VALUES
    ((SELECT id FROM recetas WHERE nombre = 'Pan Dulce Tradicional'), (SELECT id FROM ingredientes WHERE nombre = 'Harina de Trigo'), 0.100, 1), -- 100g harina
    ((SELECT id FROM recetas WHERE nombre = 'Pan Dulce Tradicional'), (SELECT id FROM ingredientes WHERE nombre = 'Azúcar'), 0.050, 2),          -- 50g azúcar
    ((SELECT id FROM recetas WHERE nombre = 'Pan Dulce Tradicional'), (SELECT id FROM ingredientes WHERE nombre = 'Huevos'), 1.000, 3),          -- 1 huevo
    ((SELECT id FROM recetas WHERE nombre = 'Pan Dulce Tradicional'), (SELECT id FROM ingredientes WHERE nombre = 'Mantequilla'), 0.020, 4);     -- 20g mantequilla

-- 3. Insertar ingredientes para 'Concha de Chocolate'
INSERT INTO recetas_ingredientes (id_receta, id_ingrediente, cantidad_requerida, orden_uso)
VALUES
    ((SELECT id FROM recetas WHERE nombre = 'Concha de Chocolate'), (SELECT id FROM ingredientes WHERE nombre = 'Harina de Trigo'), 0.100, 1),
    ((SELECT id FROM recetas WHERE nombre = 'Concha de Chocolate'), (SELECT id FROM ingredientes WHERE nombre = 'Azúcar'), 0.060, 2),
    ((SELECT id FROM recetas WHERE nombre = 'Concha de Chocolate'), (SELECT id FROM ingredientes WHERE nombre = 'Huevos'), 1.000, 3),
    ((SELECT id FROM recetas WHERE nombre = 'Concha de Chocolate'), (SELECT id FROM ingredientes WHERE nombre = 'Chocolate Amargo'), 0.030, 4); -- Cobertura

-- 4. Insertar ingredientes para 'Café Americano'
INSERT INTO recetas_ingredientes (id_receta, id_ingrediente, cantidad_requerida, orden_uso)
VALUES
    ((SELECT id FROM recetas WHERE nombre = 'Café Americano'), (SELECT id FROM ingredientes WHERE nombre = 'Café Grano'), 0.018, 1), -- 18g café
    ((SELECT id FROM recetas WHERE nombre = 'Café Americano'), (SELECT id FROM ingredientes WHERE nombre = 'Vaso Desechable 12oz'), 1.000, 2);

-- 5. Insertar ingredientes para 'Cappuccino'
INSERT INTO recetas_ingredientes (id_receta, id_ingrediente, cantidad_requerida, orden_uso)
VALUES
    ((SELECT id FROM recetas WHERE nombre = 'Cappuccino'), (SELECT id FROM ingredientes WHERE nombre = 'Café Grano'), 0.018, 1),
    ((SELECT id FROM recetas WHERE nombre = 'Cappuccino'), (SELECT id FROM ingredientes WHERE nombre = 'Leche Entera'), 0.250, 2), -- 250ml leche
    ((SELECT id FROM recetas WHERE nombre = 'Cappuccino'), (SELECT id FROM ingredientes WHERE nombre = 'Vaso Desechable 12oz'), 1.000, 3);

-- 6. Insertar ingredientes para 'Chocolate Caliente'
INSERT INTO recetas_ingredientes (id_receta, id_ingrediente, cantidad_requerida, orden_uso)
VALUES
    ((SELECT id FROM recetas WHERE nombre = 'Chocolate Caliente'), (SELECT id FROM ingredientes WHERE nombre = 'Leche Entera'), 0.300, 1),
    ((SELECT id FROM recetas WHERE nombre = 'Chocolate Caliente'), (SELECT id FROM ingredientes WHERE nombre = 'Cocoa en Polvo'), 0.040, 2),
    ((SELECT id FROM recetas WHERE nombre = 'Chocolate Caliente'), (SELECT id FROM ingredientes WHERE nombre = 'Canela Molida'), 0.005, 3),
    ((SELECT id FROM recetas WHERE nombre = 'Chocolate Caliente'), (SELECT id FROM ingredientes WHERE nombre = 'Vaso Desechable 12oz'), 1.000, 4);

-- 7. Verificación (Opcional: Ejecuta esto para ver si se guardaron)
SELECT r.nombre as receta, i.nombre as ingrediente, ri.cantidad_requerida
FROM recetas_ingredientes ri
         JOIN recetas r ON ri.id_receta = r.id
         JOIN ingredientes i ON ri.id_ingrediente = i.id
ORDER BY r.nombre;

INSERT INTO productos (nombre, sabor, precio, id_receta, tipo, descripcion) VALUES
('Pan Dulce', 'Natural', 15.00, 1, 'Pan', 'Pieza individual'),
('Concha Chocolate', 'Chocolate', 18.00, 2, 'Pan', 'Concha tradicional'),
('Café Americano', 'Café', 35.00, 3, 'Bebida', '12oz'),
('Cappuccino', 'Café', 48.00, 4, 'Bebida', '12oz con espuma'),
('Chocolate Abuelita', 'Chocolate', 45.00, 5, 'Bebida', 'Estilo mexicano');

-- Inventario Inicial (Sucursal 1 - Centro)
INSERT INTO inventario_sucursales (id_sucursal, id_producto, cantidad_disponible, cantidad_minima) VALUES
(1, 1, 30, 5), (1, 2, 30, 5),
(1, 3, 50, 10), (1, 4, 50, 10),
(1, 5, 20, 5);

-- Inventario Inicial (Sucursal 2 - Altozano)
INSERT INTO inventario_sucursales (id_sucursal, id_producto, cantidad_disponible, cantidad_minima) VALUES
(2, 1, 40, 8), (2, 2, 35, 8),
(2, 3, 100, 15), (2, 4, 80, 15),
(2, 5, 40, 10);

-- Inventario Inicial (Sucursal 3 - Acueducto)
INSERT INTO inventario_sucursales (id_sucursal, id_producto, cantidad_disponible, cantidad_minima) VALUES
(3, 1, 15, 5), (3, 2, 12, 5),
(3, 3, 40, 10), (3, 4, 30, 8),
(3, 5, 10, 5);

INSERT INTO clientes (nombre, apellido, correo) VALUES
('Cliente', 'Mostrador', 'publico@general.com');

-- 3. EMPLEADOS Y USUARIOS (Bloque dinámico con hashes SHA-256)
DO $$
    DECLARE
        v_suc_centro INT;
        v_suc_altozano INT;
        v_suc_acueducto INT;
        v_rol_gerente INT;
        v_rol_cajero INT;
        v_rol_admin INT;
        v_rol_produccion INT;
        v_emp_id INT;
        -- Hashes (Password_123 y Cande)
        v_hash_pass123 VARCHAR := 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'; --password123
        v_hash_cande VARCHAR := 'cc3cf6baf4f724be58d69993145bb5270ce4f5dea4228b7c21c28676fd180446'; --cande
    BEGIN
        SELECT id INTO v_suc_centro FROM sucursales WHERE nombre LIKE '%Centro%' LIMIT 1;
        SELECT id INTO v_suc_altozano FROM sucursales WHERE nombre LIKE '%Altozano%' LIMIT 1;
        SELECT id INTO v_suc_acueducto FROM sucursales WHERE nombre LIKE '%Acueducto%' LIMIT 1;

        SELECT id INTO v_rol_admin FROM roles WHERE nombre = 'Super Admin' LIMIT 1;
        SELECT id INTO v_rol_gerente FROM roles WHERE nombre = 'Gerente Sucursal' LIMIT 1;
        SELECT id INTO v_rol_cajero FROM roles WHERE nombre = 'Cajero' LIMIT 1;
        SELECT id INTO v_rol_produccion FROM roles WHERE nombre = 'Produccion' LIMIT 1;

        -- Admin Original (Gabriela Lua)
        INSERT INTO empleados (nombres, apellidos, fecha_nacimiento, lugar_nacimiento, direccion, telefono, correo, tipo, id_sucursal)
        VALUES ('Gabriela', 'Lua Vargas', '1985-03-15', 'Morelia', 'Centro', '4431111111', 'gabriela.lua@luasplace.com', 'Administrativo', v_suc_centro) RETURNING id INTO v_emp_id;
        INSERT INTO usuarios (id_empleado, usuario, contrasena, rol, id_rol) VALUES (v_emp_id, 'admin', v_hash_pass123, 'Admin', v_rol_admin);

        -- Gerente Centro (Maria Garcia)
        INSERT INTO empleados (nombres, apellidos, fecha_nacimiento, lugar_nacimiento, direccion, telefono, correo, tipo, id_sucursal)
        VALUES ('Maria Elena', 'Garcia Ramirez', '1990-07-22', 'Morelia', 'Camelinas', '4431111112', 'maria.garcia@luasplace.com', 'Administrativo', v_suc_centro) RETURNING id INTO v_emp_id;
        INSERT INTO usuarios (id_empleado, usuario, contrasena, rol, id_rol) VALUES (v_emp_id, 'mgarcia', v_hash_pass123, 'Administrativo', v_rol_gerente);

        -- Gerente Acueducto (Cande)
        INSERT INTO empleados (nombres, apellidos, fecha_nacimiento, lugar_nacimiento, direccion, telefono, correo, tipo, id_sucursal)
        VALUES ('Candido', 'Ortega Martinez', '2005-08-11', 'Morelia', 'Acueducto', '4439991111', 'cande.ortega@luasplace.com', 'Administrativo', v_suc_acueducto) RETURNING id INTO v_emp_id;
        INSERT INTO usuarios (id_empleado, usuario, contrasena, rol, id_rol) VALUES (v_emp_id, 'cande', v_hash_cande, 'Administrativo', v_rol_gerente);

        -- Empleados Genericos (Centro)
        INSERT INTO empleados (nombres, apellidos, fecha_nacimiento, lugar_nacimiento, direccion, telefono, correo, tipo, id_sucursal)
        VALUES ('Luis', 'Perez Lopez', '1998-02-15', 'Morelia', 'Centro', '4431002001', 'lperez@luasplace.com', 'Administrativo', v_suc_centro) RETURNING id INTO v_emp_id;
        INSERT INTO usuarios (id_empleado, usuario, contrasena, rol, id_rol) VALUES (v_emp_id, 'lperez', v_hash_pass123, 'Administrativo', v_rol_cajero);

        INSERT INTO empleados (nombres, apellidos, fecha_nacimiento, lugar_nacimiento, direccion, telefono, correo, tipo, id_sucursal)
        VALUES ('Ana', 'Torres Ruiz', '2001-08-10', 'Morelia', 'Obrera', '4431002002', 'atorres@luasplace.com', 'Administrativo', v_suc_centro) RETURNING id INTO v_emp_id;
        INSERT INTO usuarios (id_empleado, usuario, contrasena, rol, id_rol) VALUES (v_emp_id, 'atorres', v_hash_pass123, 'Administrativo', v_rol_cajero);

        -- Empleados Genericos (Altozano)
        INSERT INTO empleados (nombres, apellidos, fecha_nacimiento, lugar_nacimiento, direccion, telefono, correo, tipo, id_sucursal)
        VALUES ('Carlos', 'Gomez Diaz', '1996-11-25', 'Morelia', 'Altozano', '4431002003', 'cgomez@luasplace.com', 'Administrativo', v_suc_altozano) RETURNING id INTO v_emp_id;
        INSERT INTO usuarios (id_empleado, usuario, contrasena, rol, id_rol) VALUES (v_emp_id, 'cgomez', v_hash_pass123, 'Administrativo', v_rol_cajero);

        INSERT INTO empleados (nombres, apellidos, fecha_nacimiento, lugar_nacimiento, direccion, telefono, correo, tipo, id_sucursal)
        VALUES ('Diana', 'Silva Cruz', '2003-04-12', 'Morelia', 'Santa Maria', '4431002004', 'dsilva@luasplace.com', 'Administrativo', v_suc_altozano) RETURNING id INTO v_emp_id;
        INSERT INTO usuarios (id_empleado, usuario, contrasena, rol, id_rol) VALUES (v_emp_id, 'dsilva', v_hash_pass123, 'Administrativo', v_rol_cajero);

        -- Empleados Genericos (Acueducto)
        INSERT INTO empleados (nombres, apellidos, fecha_nacimiento, lugar_nacimiento, direccion, telefono, correo, tipo, id_sucursal)
        VALUES ('Hugo', 'Vega Mier', '1999-01-30', 'Morelia', 'Chapultepec', '4431002005', 'hvega@luasplace.com', 'Administrativo', v_suc_acueducto) RETURNING id INTO v_emp_id;
        INSERT INTO usuarios (id_empleado, usuario, contrasena, rol, id_rol) VALUES (v_emp_id, 'hvega', v_hash_pass123, 'Administrativo', v_rol_cajero);

        INSERT INTO empleados (nombres, apellidos, fecha_nacimiento, lugar_nacimiento, direccion, telefono, correo, tipo, id_sucursal)
        VALUES ('Elena', 'Paz Rico', '2004-09-05', 'Morelia', 'Ventura', '4431002006', 'epaz@luasplace.com', 'Administrativo', v_suc_acueducto) RETURNING id INTO v_emp_id;
        INSERT INTO usuarios (id_empleado, usuario, contrasena, rol, id_rol) VALUES (v_emp_id, 'epaz', v_hash_pass123, 'Administrativo', v_rol_cajero);

    END $$;