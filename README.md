# Walert – Aplicación de Recordatorio de Hidratación Inteligente

**Asignatura:** DSY1105 – Desarrollo de Aplicaciones Móviles  
**Integrantes:** Bruno Araya – Matías Cerda  

---

## 1. Descripción General del Proyecto
**Walert** es una aplicación móvil desarrollada en **Android Studio (Kotlin)** orientada a fomentar hábitos saludables de hidratación.  
El sistema permite **registrar el consumo diario de agua**, **visualizar el progreso**, y **recibir recordatorios automáticos** durante el día para mantener una ingesta adecuada.  

El diseño prioriza una **interfaz intuitiva, organizada y funcional**, con animaciones que refuerzan la retroalimentación visual y mejoran la experiencia del usuario.  
La aplicación busca promover el bienestar personal mediante una herramienta accesible y fácil de usar, aprovechando los recursos nativos del sistema Android.  

---

## 2. Funcionalidades Principales
- **Registro de consumo de agua:** el usuario puede sumar o restar unidades de consumo diario.  
- **Recordatorios automáticos:** notificaciones locales que avisan en intervalos configurados.  
- **Persistencia de datos:** almacenamiento con `SharedPreferences` para conservar el progreso del usuario.  
- **Pantallas de inicio de sesión y registro:** permiten la personalización básica de la experiencia.  
- **Interfaz visual coherente:** jerarquía de elementos, íconos, colores y mensajes claros.  
- **Animaciones funcionales:** transiciones y efectos visuales que aportan fluidez.  
- **Widget de escritorio:** muestra el avance de la meta diaria sin abrir la app.  
- **Receptor de arranque (BootReceiver):** mantiene los recordatorios activos tras reiniciar el dispositivo.  

---

## 3. Arquitectura del Proyecto
El proyecto sigue una **estructura modular simple** con enfoque **MVVM parcial**, separando responsabilidades lógicas, visuales y funcionales.  
La persistencia se maneja localmente mediante `SharedPreferences`, garantizando un funcionamiento estable y sin conexión.  
Además, se emplean componentes nativos de Android para optimizar la interacción del usuario con la aplicación.  

---

## 4. Tecnologías Utilizadas
- **Lenguaje:** Kotlin  
- **Entorno de desarrollo:** Android Studio  
- **Gestor de dependencias:** Gradle (KTS)  
- **Interfaz:** ViewBinding + XML Layouts  
- **Persistencia:** SharedPreferences  
- **Animaciones:** ObjectAnimator, AnimationUtils  
- **Recursos nativos:** Notificaciones, BootReceiver, Widget  

---

## 5. Planificación y Colaboración
El desarrollo se realizó utilizando herramientas colaborativas para mantener una organización efectiva del equipo.  

- **Repositorio GitHub:** [https://github.com/Brunowo69/Walert](https://github.com/Brunowo69/Walert)  
- **Tablero Trello:** [https://trello.com/invite/b/690225779ed4057e6ec3a787/ATTI18386aa3d72a41a7660b0d88d8845270C960DB36/walert](https://trello.com/invite/b/690225779ed4057e6ec3a787/ATTI18386aa3d72a41a7660b0d88d8845270C960DB36/walert)  

Cada integrante participó en el desarrollo del código, diseño visual, pruebas de funcionamiento y documentación del proyecto.  
El control de versiones se realizó con GitHub, asegurando trazabilidad, respaldo y colaboración constante.  

---

## 6. Mejoras Planificadas
- Integrar **Room Database** para almacenar el historial de consumo.  
- Implementar **gráficos estadísticos** del progreso semanal o mensual.  
- Añadir un **sistema de logros** para motivar al usuario.  
- Optimizar la arquitectura hacia **MVVM completo**.  
- Mejorar las **validaciones visuales** en formularios.  

---

## 7. Conclusión
**Walert** es una aplicación móvil funcional, intuitiva y accesible, diseñada para ayudar a los usuarios a mantener hábitos saludables de hidratación.  
Combina un **diseño visual atractivo**, **persistencia local**, **animaciones dinámicas** y **recursos nativos integrados**, brindando una experiencia motivadora y fluida.  

Gracias a su estructura modular y a la planificación colaborativa del equipo, el proyecto establece una base sólida para futuras versiones que incorporen nuevas funcionalidades, análisis de datos y mejoras visuales.  
