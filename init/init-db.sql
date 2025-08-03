-- =======================================
-- Crear usuario y base de datos
-- =======================================
CREATE DATABASE IF NOT EXISTS fitsystemdb;
CREATE USER IF NOT EXISTS 'fitsystemuser'@'%' IDENTIFIED BY 'fitsystempass';
GRANT ALL PRIVILEGES ON fitsystemdb.* TO 'fitsystemuser'@'%';
FLUSH PRIVILEGES;

-- =======================================
-- Usar base de datos
-- =======================================
USE fitsystemdb;

-- Tabla: usuarios
CREATE TABLE usuarios (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    telefono VARCHAR(20),
    email VARCHAR(100),
    direccion VARCHAR(200),
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,  -- Aquí se guardará la contraseña (encriptada por la app)
    rol VARCHAR(20) NOT NULL DEFAULT 'USER'
);

-- Tabla: clientes
CREATE TABLE clientes (
    id_cliente INT AUTO_INCREMENT PRIMARY KEY,
    cedula VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    telefono VARCHAR(20),
    email VARCHAR(100),
    direccion VARCHAR(200)
);

-- Tabla: productos
CREATE TABLE productos (
    id_producto INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    categoria VARCHAR(50),
    stock INT NOT NULL DEFAULT 0,
    precioUnitario DECIMAL(10,2) NOT NULL
);

-- Tabla: facturas
CREATE TABLE facturas (
    id_factura INT AUTO_INCREMENT PRIMARY KEY,
    fecha DATETIME DEFAULT CURRENT_TIMESTAMP,
    id_usuario INT NOT NULL,
    id_cliente INT NOT NULL,
    total DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
    FOREIGN KEY (id_cliente) REFERENCES clientes(id_cliente) ON DELETE CASCADE
);

-- Tabla: detalles_factura
CREATE TABLE detalles_factura (
    id_detalle INT AUTO_INCREMENT PRIMARY KEY,
    id_factura INT NOT NULL,
    id_producto INT NOT NULL,
    cantidad INT NOT NULL,
    precioUnitario DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (id_factura) REFERENCES facturas(id_factura) ON DELETE CASCADE,
    FOREIGN KEY (id_producto) REFERENCES productos(id_producto) ON DELETE CASCADE
);

-- Insertar datos de ejemplo en usuarios la clave ya se incresa encriptada
-- (la contraseña "admin" se ha encriptado previamente) y es admin12 la clave para poder ingresar
-- al sistema.
INSERT INTO usuarios (nombre, apellido, telefono, email, direccion, username, password, rol)
VALUES ('Administrador', 'Principal', '0999999999', 'admin@fitsystem.com', 'Av. Central 123', 'admin', '114663ab194edcb3f61d409883ce4ae6c3c2f9854194095a5385011d15becbef', 'ADMIN');

-- Insertar datos de ejemplo en clientes
INSERT INTO clientes (cedula,nombre, apellido, telefono, email, direccion)
VALUES 
('00000000000','Juan', 'Pérez', '0998887777', 'juan.perez@mail.com', 'Calle Falsa 123'),
('1111111111','María', 'González', '0987654321', 'maria.gonzalez@mail.com', 'Av. Siempre Viva 742');

-- Insertar datos de ejemplo en productos
INSERT INTO productos (codigo, nombre, descripcion, categoria, stock, precioUnitario)
VALUES
('PRD001', 'Cinta de correr', 'Máquina para cardio', 'Cardio', 10, 499.99),
('PRD002', 'Bicicleta estática', 'Bicicleta para ejercicio indoor', 'Cardio', 5, 399.50),
('PRD003', 'Pesas', 'Set de pesas de varios tamaños', 'Fuerza', 20, 59.99);