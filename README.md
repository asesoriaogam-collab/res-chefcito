# res-chefcito (Chefcito T.P.)

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
