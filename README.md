# Walert2 — Aplicación de Control de Hidratación  
Desarrollo de Aplicaciones Móviles — DSY1105  
Evaluaciones Parcial 4 y Parcial 5

---

## 1. Descripción del Proyecto  
Walert2 es una aplicación móvil desarrollada en Kotlin con Jetpack Compose que permite a los usuarios registrar, monitorear y mejorar su hidratación diaria.

El proyecto incluye:
- Registro de consumo de agua
- Historial y calendario
- Sistema de logros
- Recordatorios locales
- Consumo de API externa
- Conexión a microservicio Spring Boot
- Pruebas unitarias
- APK firmada

Cumple con los requerimientos establecidos en EP4 (entrega funcional) y EP5 (defensa técnica individual) para la asignatura DSY1105.

---

## 2. Integrantes  
- Bruno Araya  
- Matías Cerda  

---

## 3. Funcionalidades Principales  

### Aplicación móvil (Kotlin + Jetpack Compose)
- Registro diario de hidratación  
- Visualización del progreso  
- Historial completo  
- Ajuste de metas  
- Sistema de logros  
- Notificaciones locales  
- Pantalla dedicada para API externa  
- Arquitectura MVVM  
- Navegación mediante Navigation Compose  

### Microservicio Spring Boot
- CRUD completo para la entidad WaterRecord  
- Persistencia remota en base de datos  
- Endpoints REST simples  
- Comunicación en tiempo real con Retrofit  

### API Externa
API utilizada: Dog API (imagen aleatoria)  
https://dog.ceo/api/breeds/image/random

Se muestra en tiempo real dentro de la aplicación.

### Pruebas Unitarias
- Implementadas en ViewModel  
- Mock de llamadas REST  
- JUnit 5 + MockK  
- Pruebas de cálculos de hidratación  
- Cobertura lógica superior al 80%  

### APK Firmada
- Archivo Walert2-release.apk  
- Llave walert2-key.jks  
- Configuración Release en Gradle  

---

## 4. Arquitectura del Proyecto  

### App móvil (Compose + MVVM)
app/
├── screens/
├── viewmodel/
├── components/
├── navigation/
├── network/
└── data/

shell
Copiar código

### Microservicio Spring Boot
backend/
├── controller/
├── service/
├── repository/
├── model/
└── config/

yaml
Copiar código

---

## 5. Endpoints del Microservicio

Entidad: WaterRecord
```json
{
  "id": 1,
  "amount": 250,
  "date": "2025-01-01",
  "userId": 1
}
Endpoints:

bash
Copiar código
GET     /water/list
POST    /water/add
PUT     /water/update/{id}
DELETE  /water/delete/{id}
Swagger no se utiliza en esta asignatura.

6. API Externa Integrada
Dog API – Imagen aleatoria
https://dog.ceo/api/breeds/image/random

Ejemplo de respuesta:

json
Copiar código
{
  "message": "https://images.dog.ceo/breeds/husky/example.jpg",
  "status": "success"
}
7. Pruebas Unitarias
Tecnologías utilizadas:

JUnit 5

MockK

Coroutines Test

Aspectos evaluados:

Lógica del ViewModel

Estados UI

Validación de datos

Simulación de llamadas REST

8. Instrucciones de Ejecución
Backend (Spring Boot)
Abrir carpeta /backend en IntelliJ

Ejecutar:

arduino
Copiar código
./mvnw spring-boot:run
Backend disponible en:

arduino
Copiar código
http://localhost:8080
Swagger y Postman no se utilizan en DSY1105.

App Android (Android Studio)
Abrir carpeta /Walert2

Sincronizar Gradle

Ejecutar en emulador o dispositivo físico

Verificar conexión con backend

Probar CRUD desde la interfaz

9. APK Firmada
Archivos incluidos:

Walert2-release.apk

walert2-key.jks

Configuración Release en Gradle aplicada correctamente.

10. Planificación y Control de Versiones
GitHub
Repositorio oficial:
https://github.com/Brunowo69/Walert

Incluye:

Evidencia real de commits

Ramas independientes

Control de versiones del móvil y backend

Trello
Tablero oficial del proyecto:
https://trello.com/invite/b/69277d8c094c51f2ba33b23b/ATTI260b1eccfb3566bba46838636b25e0a74F522AEE/walert-desarrollo-de-aplicaciones-moviles

Columnas:

Backlog

En progreso

Backend

App móvil

Completado

11. Estado Actual del Proyecto
Aplicación móvil funcional

Microservicio operativo

CRUD completo

API externa integrada

Pruebas unitarias implementadas

APK firmada

Documentación técnica finalizada

Evidencias en GitHub y Trello

12. Autores
Bruno Araya

Matías Cerda
Duoc UC — 2025
