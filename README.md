# 🏋️ FitSystem

**FitSystem** es una aplicación web para la gestión integral de gimnasios, desarrollada con Jakarta EE 10. Permite administrar usuarios, clientes, productos y facturación con IVA de forma sencilla y segura.

---

## 🚀 Tecnologías utilizadas

- Jakarta EE 10 (JSF 4.0, JPA 3.1, CDI, JSON-B)  
- Open Liberty 22.0.0.10 como servidor de aplicaciones  
- MySQL 8 (contenedor Docker)  
- JPA (Hibernate) para persistencia  
- Facelets (XHTML) para interfaz de usuario  
- Docker y Docker Compose para contenedores  
- Maven para gestión y compilación  

---

## ⚙️ Requisitos previos

- Java 17 o superior (OpenJDK u Oracle JDK)  
- Maven  
- Docker y Docker Compose  

---

## 📦 Instalación y ejecución

1. Clonar el repositorio:

```bash
git clone https://github.com/BryanVilla2000/proyect-FitSystem.git
cd proyect-FitSystem
```
2. Construir el proyecto con Maven Wrapper:
```bash
./mvnw clean package
```
3. Levantar los servicios con Docker Compose (abre MySQL, Adminer y la app):
```bash
docker compose -f docker-compose-dev-mysql.yml up --build -d
```
4. Acceder a la aplicación en el navegador:
```bash
http://localhost:9080/proyect-FitSystem/login.xhtml
```
5. Acceder a Adminer para gestionar la base de datos:
```bash
http://localhost:8080
```
## Datos para conexión a la base:
```bash
Sistema: MySQL
Servidor: mysql
Usuario: root
Contraseña: root
Base de datos: fitsystemdb
```
## 🔐 Credenciales de acceso de prueba
Usuario	Contraseña
admin	admin12

## 📋 Funcionalidades principales
- Autenticación y autorización con roles
- Gestión CRUD de usuarios, clientes y productos
- Registro y edición de facturas con detalle de productos
- Cálculo automático de subtotal, IVA (12%) y total
- Interfaz moderna y responsiva con JSF y Facelets
- Protección de vistas mediante filtros de seguridad

## 🗂️ Estructura del proyecto

`/src/main/java/com/fitsystem/bean`        # Managed Beans (controladores)
`/src/main/java/com/fitsystem/model  `     # Entidades JPA (modelo de datos)
`/src/main/resources/META-INF/persistence.xml ` # Configuración JPA
`/src/main/liberty/config/server.xml  `    # Configuración Open Liberty
`/src/main/webapp/views       `             # Vistas JSF (.xhtml) organizadas por módulos
`docker-compose-dev-mysql.yml  `            # Orquestación de servicios para desarrollo
