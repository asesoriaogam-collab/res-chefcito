<img width="736" height="1308" alt="logo" src="https://github.com/user-attachments/assets/8ee1ecce-1def-4ce0-9dc0-d836d9c3986f" />

# Chefcito

## 📋 Descripción del repositorio

Chefcito es una herramienta de administración y monitoreo remoto de dispositivos Android para entornos de prueba propios. El repositorio contiene tres componentes que funcionan en conjunto:

* **Aplicación cliente Android** (`client/`): se instala en el dispositivo a administrar. Mantiene una conexión TCP permanente con el servidor, reporta actividad y recibe órdenes.
* **Servidor central** (`server/`): aplicación Node.js que recibe las conexiones de los clientes en puertos de escucha configurables y sirve el panel web en el puerto 3000.
* **Panel de control web** (`server/public/`): la interfaz del operador, usable desde la compu o desde el navegador de un celular.

La IP y el puerto del servidor se fijan en el código del cliente antes de compilar el APK (por defecto, puerto 7777).

### Funciones

**Monitoreo pasivo** (el cliente reporta sin que el operador pida nada):

* **Keylogger por accesibilidad**: registra el texto tipeado, los toques y los cambios de foco, indicando en qué app ocurrió y a qué hora.
* **Captura de credenciales de bloqueo**: lee el PIN, el patrón o la contraseña ingresados al desbloquear y los reporta junto con las coordenadas de cada tecla.
* **Notificaciones**: cada notificación que llega al dispositivo (app, título, contenido, hora) se refleja en el panel.
* **Auto-concesión de permisos**: cuando el sistema muestra el diálogo de confirmación ("Iniciar ahora" / "Permitir" en instalador y ajustes), el cliente intenta confirmarlo automáticamente.

**Recolección bajo demanda** (el operador la pide desde el panel):

* **SMS**: los últimos 500 mensajes (recibidos y enviados).
* **Contactos**: agenda completa del dispositivo.
* **Ubicación**: lectura puntual por GPS (botón de refresco en el panel).
* **Archivos**: navegación por el sistema de archivos del dispositivo y descarga al servidor.
* **Cámaras**: captura en vivo desde la cámara frontal o trasera, con calidad configurable.
* **Micrófono**: escucha en vivo o grabación, con tasa de muestreo configurable.
* **Info del dispositivo**: datos del equipo y del sistema operativo.
* **Lector de pantalla**: volcado del árbol de interfaz (textos, descripciones y elementos clicables de lo que se ve en pantalla).

**Control activo del dispositivo**:

* **Espejo de pantalla**: transmisión en vivo de lo que se ve, vía MediaProjection.
* **Automatización por accesibilidad**: tocar elementos por texto o por ID de recurso, escribir en el campo enfocado, abrir apps o URLs y usar atrás/inicio/recientes.
* **Gestos**: toques y deslizamientos por coordenadas.
* **Modo discreto**: capa negra a pantalla completa que oculta lo que ocurre en el dispositivo mientras se opera.

### Capacidades

* Un solo servidor administra **varios dispositivos a la vez**: cada cliente se identifica con su modelo e IP, y el panel los lista todos.
* **Puertos de escucha múltiples**, que se abren y cierran desde el panel sin reiniciar el servidor.
* **Compilación del APK desde el propio panel**: el servidor ejecuta Gradle y deja el APK listo para descargar desde el navegador, sin depender de Android Studio en la compu del operador.
* **Persistencia en el dispositivo**: servicio en primer plano con reinicio automático si el usuario o el sistema lo cierran, y arranque automático al encender el teléfono.
* Panel usable desde el navegador de un celular.

### Limitaciones

* **Canal sin cifrar ni autenticar**: la conexión cliente↔servidor es TCP plano y el panel es HTTP. El instructivo recomienda limitar el acceso por IP o VPN, pero el tráfico no es privado frente a una intercepción.
* **El panel no tiene login**: quien conozca la IP y el puerto puede abrir el panel (mejora planificada).
* **La app no se oculta**: muestra una notificación permanente ("Servicio CHEFCITO") y su ícono en la bandeja; el usuario del dispositivo puede verlas. La primera activación exige habilitar a mano el servicio de accesibilidad y el acceso a notificaciones en Ajustes.
* **Orientado a Android 5–10**: minSdk 21 y target/compile SDK 29. En versiones más nuevas de Android, algunas funciones exigen pasos extra o dejan de funcionar.
* **Espejo de pantalla**: la concesión de MediaProjection requiere que la app esté abierta en el dispositivo al momento de iniciarla.
* **Ubicación puntual**: no hay seguimiento continuo; solo lecturas bajo demanda.

## AGRADECIMIENTOS

Este proyecto no habría sido posible sin la inmensa ayuda de:
* Mi Lenovo ThinkPad T430
* hackerai.co 

Yo puse la idea y ellos hicieron todo el trabajo

## 🧬 Origen y cadena de derivación

Este proyecto es una obra derivada en cadena de dos proyectos previos, y se documenta aquí la procedencia completa del código:

### 1. Base de código: [Curse_Rat v3.0.0](https://github.com/thorthehacker/Curse_Rat) — [@thorthehacker](https://github.com/thorthehacker)

La aplicación cliente Android y el servidor Node.js de Chefcito parten del código de Curse_Rat: la estructura de directorios (`client`, `server`), los módulos Java del cliente (`com.curse.rat`) y el panel de control web derivan de ese repositorio.

### 2. Modelo de permisos: [D-L3MON](https://github.com/rootuserdj/D-L3MON) — [@rootuserdj](https://github.com/rootuserdj) (basado en el trabajo original de Dhananjay Sah)

El sistema de permisos del cliente de Chefcito utiliza el mismo modelo que el cliente de D-L3MON. Para lograrlo, se modificó la aplicación cliente de Curse_Rat para que adopte dicho sistema de permisos.

### 3. Modificaciones propias de este repositorio

Sobre esa base, res-chefcito añade o cambia:

* **Rediseño del panel de control web** para que sea más fácil de usar.
* **Compatibilidad del panel con navegadores móviles**, para poder administrar el dispositivo desde un celular.
* Rebranding del proyecto (Curse_Rat → Chefcito T.P.) y eliminación de la carpeta `assets` original.
* Adaptación de la herramienta hacia un uso de control/monitoreo en entornos de prueba propios.

## ⚖️ Nota sobre licencias

* [D-L3MON](https://github.com/rootuserdj/D-L3MON) se distribuye bajo licencia **MIT**; este proyecto respeta y preserva su atribución.
* [Curse_Rat](https://github.com/thorthehacker/Curse_Rat) no incluye archivo de licencia; se le reconoce aquí como origen de la base de código.
* Se agradece a los autores originales por compartir su trabajo con la comunidad de investigación en seguridad.

## ⚠️ Descargo de Responsabilidad (Disclaimer)

Este proyecto ha sido desarrollado exclusivamente con fines educativos, académicos y de investigación en ciberseguridad (auditoría de dispositivos Android y telemetría). El autor no se hace responsable del mal uso que se le pueda dar a esta herramienta. Utilízalo únicamente en dispositivos de tu propiedad o bajo autorización expresa por escrito.
