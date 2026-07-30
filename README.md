
# TalentMetrics AI - Backend

Este repositorio contiene la API REST y la lógica de negocio del proyecto TalentMetrics AI. Está desarrollado con **Spring Boot** usando **OpenJDK 21**.

## 🛠️ Requisitos Previos

Para ejecutar este proyecto de forma local, necesitás:
*   **Java:** OpenJDK 21.
*   **PostgreSQL:** Una instancia corriendo (local o en Docker) en el puerto `5432`.
*   **OpenLDAP:** Un servidor de directorio activo en el puerto `389` para la autenticación de consultores.

> **Nota:** Este repositorio no incluye archivos de Docker Compose. Debés aprovisionar la base de datos y el servidor LDAP por tu cuenta antes de iniciar la aplicación.

## ⚙️ Configuración del Entorno

El backend utiliza el archivo de configuración nativo de Spring. Antes de correr el proyecto, verificá o creá el archivo `src/main/resources/application.properties` con el siguiente formato:

```properties
spring.application.name=talentmetricsAI-backend
server.port=8080
spring.output.ansi.enabled=ALWAYS
logging.level.org.springframework.security=DEBUG

# --- Base de Datos PostgreSQL ---
# Asegurate de tener creada la base de datos 'tm_database'
spring.datasource.url=jdbc:postgresql://localhost:5432/tm_database
spring.datasource.username=admin
spring.datasource.password=admin
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.open-in-view=false

# --- Autenticación (JWT) ---
jwt.secret=secretito-de-minimo-32-caracteres-para-firmar-tokens
jwt.expiration=43200000
jwt.refresh-expiration=43200000

# --- Configuración CORS ---
cors.allowed-origins=http://localhost:3000

# --- Conexión LDAP (Capital Humano) ---
# La estructura del directorio debe coincidir con estos parámetros
spring.ldap.urls=ldap://localhost:389
spring.ldap.base=dc=capitalhumano,dc=com
spring.ldap.username=cn=admin,dc=capitalhumano,dc=com
spring.ldap.password=admin

# --- Integración con IA (Gemini) ---
gemini.api.key=TU_API_KEY_AQUI

```
# Ejecución
En la raíz del proyecto ejecutá:

```bash
./mvnw spring-boot:run
```
La API estará disponible en http://localhost:8080.
