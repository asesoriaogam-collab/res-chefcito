<!-- ================================================================
NOTA PARA EL ADMINISTRADOR (no se ve cuando la guía se lee renderizada):
1) Esta guía asume que el repositorio res-chefcito es PÚBLICO. Antes de
   repartirla, hacerlo público: GitHub → repo → Settings → Danger Zone →
   Change visibility → Public. (Si queda privado, el PASO 7 falla para
   cualquier persona que no tengas vos; existe la alternativa con token
   que se explica ahí mismo.)
2) El repositorio res-chefcito NO incluye el archivo base.apk (está
   excluido a propósito). Por eso esta guía tiene el PASO 9 con cuatro
   formas de compilarlo. Si algún día lo agregás al repo, el PASO 9
   queda como opcional.
================================================================= -->

# Chefcito T.P. — Instructivo completo paso a paso

Repositorio: **https://github.com/asesoriaogam-collab/res-chefcito**

Esta guía te lleva de la mano, desde cero y sin conocimientos de informática, hasta tener:

1. **Un servidor propio en la nube de Amazon (AWS)** — el "cerebro" del sistema, gratis.
2. **La aplicación instalada en el teléfono** — el "ojos y oídos".
3. **Un panel de control en tu navegador** — donde ves todo lo que ocurre.

- **Tiempo total:** 60 a 120 minutos (según dónde compiles el `base.apk`, ver PASO 9).
- **Necesitás:** una computadora con navegador, el teléfono Android a mano, y una tarjeta de crédito o débito para abrir la cuenta de AWS (no se te cobra si seguís la guía).
- **Costo:** $0, usando el plan gratuito de AWS.

> **Diferencia con la guía del otro repositorio:** el repo `res-chefcito` **no trae** el archivo `base.apk` ya compilado (se excluye a propósito del repositorio). Por eso esta guía tiene el **PASO 9**, donde elegís **una** de **cuatro formas** de generarlo: en la VPS, con Android Studio, o por línea de comandos en Linux o Windows. El resto de la guía es idéntica a la del proyecto hermano.

> **Cómo leer esta guía:** cuando veas un recuadro gris con texto, es un **comando**. Copialo tal cual, pegalo en la terminal del servidor (aparece en el PASO 5) y presioná Enter. Todo lo que va después de un símbolo `#` en un comando es un comentario: no hace falta copiarlo.
>
> En toda la guía vas a ver el texto **TU_IP_ELASTICA**: reemplazalo siempre por la IP que vas a anotar en el PASO 4. Ejemplo: si tu IP es `54.210.33.7`, donde dice `TU_IP_ELASTICA` escribí `54.210.33.7`.

> ⚠️ **Uso responsable:** esta herramienta es para supervisar dispositivos de menores a tu cargo, informándoles que el dispositivo está supervisado. Usala solo en ese marco.

---

## PASO 1 — Crear tu cuenta gratuita de AWS

1. Entrá a **https://aws.amazon.com/es/** y hacé clic en **"Crear cuenta de AWS"** (arriba a la derecha).
2. Completá: **correo electrónico**, **contraseña** y **nombre de la cuenta** (ej: `chefcito`).
3. Elegí el tipo de cuenta: **Personal**.
4. Completá tus datos de contacto y la **tarjeta**. AWS hace un cobro de verificación de ~1 USD que luego te devuelve.
5. Verificá tu identidad por teléfono (te llaman o te mandan un código por SMS).
6. Elegí el **plan de soporte gratuito** (Basic support / Free Plan).
7. Entrá a la consola: **https://console.aws.amazon.com/**

### ¿Cómo funciona lo "gratis" hoy? (importante)

Las cuentas creadas a partir del **15 de julio de 2025** usan un modelo de **créditos**: tenés hasta **200 USD en créditos**, válidos por un máximo de **6 meses**. Mientras uses solo los recursos "aptos para el nivel gratuito" (Free tier eligible) que indica esta guía, no se te cobra nada: los consumos cubiertos no descuentan créditos.

**Reglas de oro:**
- Elegí siempre recursos marcados como **"Free tier eligible"** (la guía te indica cuáles).
- Si dejás de usar el sistema, **dá de baja todo** (ANEXO C) para no quemar créditos.
- Podés revisar tu consumo en la consola: buscá **"Billing"** (Facturación).

---

## PASO 2 — Lanzar el servidor (instancia EC2)

1. En la barra de búsqueda de la consola escribí **EC2** y entrá.
2. Clic en **"Launch instance"** (Lanzar instancia).
3. Completá así:

| Campo | Valor |
|---|---|
| **Name** | `chefcito-server` |
| **Application and OS Images** | Pestaña **Quick Start** → **Ubuntu Server 24.04 LTS** (HVM), SSD Volume Type → **64-bit (x86)** |
| **Instance type** | **t3.micro** (debe figurar "Free tier eligible") |
| **Key pair** | Creá uno nuevo: nombre `chefcito-key`, tipo **RSA**, formato **.pem** → **Create key pair** → se descarga un archivo. **Guardalo bien: no se puede volver a descargar.** |
| **Network settings** | Dejá la VPC por defecto (con "Auto-assign public IP: Enable") → **Create security group** → nombre `chefcito-sg` |
| **Storage** | Cambiá a **15 GiB** y tipo **gp3** |

💡 **Si vas a compilar el `base.apk` en el VPS** (Opción A del PASO 9): elegí **t3.small** en vez de t3.micro — también es gratuita y tiene el doble de RAM (2 GB), con lo que la compilación va mucho más cómoda. Si vas a compilar en tu computadora (Opciones B, C o D), t3.micro alcanza de sobra.

⚠️ **Dos errores que arruinan todo (verificados en pruebas reales):**
- El Ubuntu debe ser el **x86 de 64 bits**, **NO** la versión Arm y **NO** ninguna que diga "with SQL Server". La versión Arm no puede ejecutar una herramienta del compilador y el build del APK falla siempre en el último paso.
- La instancia debe ser **t3.micro o t3.small**. La t2.micro ya no es gratuita para cuentas nuevas; la t4g es Arm (mismo problema).

4. Clic en **Launch instance** y esperá a que la instancia quede en estado **Running** con un círculo verde.

---

## PASO 3 — Abrir los puertos necesarios (Security Group)

El "Security Group" es la puerta de tu servidor: define qué conexiones se permiten.

1. En el menú izquierdo de EC2: **Security Groups** → clic en `chefcito-sg`.
2. Pestaña **Inbound rules** → **Edit inbound rules** → **Add rule** (repetí 3 veces):

| Tipo | Puerto | Origen (Source) | Para qué |
|---|---|---|---|
| SSH | 22 | **Mi IP** | Que vos te conectes al servidor |
| Custom TCP | 3000 | **Mi IP** | Panel de control (solo tu navegador) |
| Custom TCP | 7777 | **0.0.0.0/0** (Everywhere) | Conexión de los teléfonos |

3. **Save rules.**

Notas:
- En el campo **Origen**, la consola de AWS suele ofrecer la opción **"Mi IP"** en el desplegable: elegila y completa sola. Si no aparece, buscá en Google "cuál es mi IP" y escribila así: `190.45.123.7/32`.
- ⚠️ **El puerto 3000 NO lo dejes abierto a todos (0.0.0.0/0)**: el panel no tiene contraseña, así que cualquiera que conozca tu IP podría entrar. Limitado a tu IP, solo tu navegador lo ve. Si tu IP de internet cambia (pasa en varias compañías), volvé a actualizar esa regla.
- ⚠️ **El 22 mejor también a "Mi IP"** por la misma razón.
- El **7777 sí va abierto a todos**: es donde se conectan los teléfonos (la IP de un celular cambia todo el tiempo).
- No hace falta abrir ningún puerto más (ni 8000 ni otros): el propio panel entrega el APK por el puerto 3000.

---

## PASO 4 — Fijar la IP permanente (Elastic IP)

Por defecto, la IP de tu servidor cambia cada vez que la instancia se reinicia. La **Elastic IP** la deja fija:

1. Menú izquierdo de EC2 → **Elastic IPs** → **Allocate Elastic IP address** → **Allocate**.
2. Seleccioná la IP nueva → **Actions** → **Associate Elastic IP address**.
3. Elegí **Instance** → seleccioná `chefcito-server` → **Associate**.
4. **Anotá esa IP en un papel o notas. De acá en más es TU_IP_ELASTICA.**

⚠️ **Mantenela siempre asociada a la instancia.** Una Elastic IP que queda "suelta" (sin instancia asociada) se cobra y quema tus créditos. Si un día das de baja el sistema, liberás la IP también (ANEXO C).

---

## PASO 5 — Entrar al servidor desde el navegador

No hace falta instalar nada en tu computadora: AWS trae una terminal dentro del navegador.

1. Menú izquierdo de EC2 → **Instances**.
2. Tildá la casilla de `chefcito-server` → botón **Connect** (arriba).
3. Pestaña **EC2 Instance Connect** → dejá el usuario `ubuntu` → clic en **Connect**.
4. Se abre una **terminal negra dentro del navegador**. Probá escribir `whoami` y Enter: debe responder `ubuntu`.

⚠️ Esta terminal del navegador **no permite subir archivos**: por eso todo lo necesario ya viene dentro del repositorio que descargás en el PASO 7, y los APK que se generen se descargan desde el navegador con un enlace. La única excepción es si compilás el `base.apk` en tu computadora (Opciones B, C o D del PASO 9): en ese caso lo subís con `scp` usando el archivo .pem (ANEXO A).

(Si en algún momento preferís conectarte con tu computadora usando el archivo .pem, mirá el ANEXO A. No es necesario para el resto de la guía.)

---

## PASO 6 — Instalar las herramientas del servidor

Copiá y pegá estos bloques **en orden**, uno por uno, esperando que termine cada uno.

### 6.1 Actualizar el sistema

```
sudo apt update
sudo apt install -y git curl wget ca-certificates apt-transport-https unzip
```

### 6.2 Instalar Node.js y npm (el motor del servidor)

```
sudo apt install -y nodejs npm
node -v   # debe responder v18.x
npm -v    # debe responder 9.x
```

⚠️ Nunca corras `apt purge` sobre paquetes de node: arrastra npm también y quedás sin él (nos pasó). Si algún día te falta npm, se reinstala con `sudo apt install -y nodejs npm`.

### 6.3 Instalar Java 11 (lo exige el compilador del APK — y también el PASO 9)

Ubuntu 24.04 no trae Java 11 en sus repos, se instala la versión Temurin:

```
wget -qO - https://packages.adoptium.net/artifactory/api/gpg/key/public | sudo gpg --dearmor -o /etc/apt/trusted.gpg.d/adoptium.gpg
echo "deb https://packages.adoptium.net/artifactory/deb $(lsb_release -cs) main" | sudo tee /etc/apt/sources.list.d/adoptium.list
sudo apt update
sudo apt install -y temurin-11-jdk
java -version   # debe responder 11.0.x
```

### 6.4 Instalar apktool (desarma y rearma el APK)

```
cd ~
wget https://github.com/iBotPeaches/Apktool/releases/download/v2.9.3/apktool_2.9.3.jar -O apktool.jar
sudo mv apktool.jar /usr/local/bin/apktool.jar
```

Ahora el "lanzador" de apktool (⚠️ **crítico:** el `"$@"` del final debe copiarse EXACTO así, con comillas y símbolo $; si escribís `""` sin el $, apktool no funciona y solo muestra un menú de ayuda — error clásico):

```
sudo tee /usr/local/bin/apktool > /dev/null <<'EOF'
#!/bin/bash
exec java -jar /usr/local/bin/apktool.jar "$@"
EOF
sudo chmod +x /usr/local/bin/apktool
```

Verificá:

```
apktool --version   # debe responder "Apktool 2.9.3" sin menú de ayuda
```

⚠️ **No uses la versión 3.0.3 de apktool**: rompe el build (probado). La 2.9.3 que instalaste recién es la correcta.

### 6.5 Instalar zipalign (alinea el APK final)

```
cd ~
mkdir -p android-tools && cd android-tools
wget https://dl.google.com/android/repository/build-tools_r34-linux.zip
unzip -o build-tools_r34-linux.zip
sudo ln -sf ~/android-tools/*/zipalign /usr/local/bin/zipalign
zipalign 2>&1 | head -1   # debe responder "Zip alignment utility"
```

### 6.6 Confirmar el firmador del APK

Viene incluido con el Java recién instalado:

```
which jarsigner keytool
```

Deben aparecer dos rutas (`/usr/bin/jarsigner` y `/usr/bin/keytool` o similares). Si aparecen, el PASO 6 está completo.

---

## PASO 7 — Descargar el proyecto, instalar dependencias y acondicionarlo

### 7.1 Descargar el repositorio

```
cd ~
git clone https://github.com/asesoriaogam-collab/res-chefcito.git
```

> ⚠️ Si `git clone` te pide usuario y contraseña, es porque el repositorio todavía está **privado**. Solución del administrador: hacerlo público (nota inicial de esta guía). Alternativa temporaria: clonar con un token de GitHub:
> ```
> git clone https://oauth2:TU_TOKEN@github.com/asesoriaogam-collab/res-chefcito.git
> ```
> (reemplazá TU_TOKEN por un token tuyo con permiso de lectura; y no lo compartas ni lo guardes: revocalo cuando termines).

### 7.2 Instalar las dependencias del servidor

```
cd ~/res-chefcito/server
npm install
```

`npm install` tarda 1 a 3 minutos. Si termina sin mensajes rojos de error (warnings amarillos no importan), está bien.

### 7.3 Acondicionar el proyecto para TU máquina (⚠️ paso obligatorio antes de compilar)

El proyecto trae dentro una línea que apunta a una carpeta de Java de **la computadora donde se creó** (`C:\Users\Cliente\.jdks\...`). En cualquier otra máquina — incluido el VPS — esa carpeta no existe y el compilador aborta con un error. Hay que borrar esa línea:

```
nano ~/res-chefcito/client/gradle.properties
```

1. Con las flechas del teclado bajá hasta la línea que empieza con `org.gradle.java.home=`.
2. Cortala entera con **Ctrl+K** (si no se va entera de un corte, repetí Ctrl+K).
3. Guardá con **Ctrl+O**, Enter, y salí con **Ctrl+X**.

⚠️ **No borres las otras líneas** del archivo (`android.useAndroidX=true`, etc.): solo esa. Con la línea fuera, el compilador usa el Java del sistema (el 11 que instalaste en el PASO 6.3 o el que instales en tu PC en el PASO 9).

> Curiosidad útil: la primera compilación de tu propia PC (donde esa línea sí funcionaba) es la razón de que exista. Este paso deja el proyecto "portable" para cualquiera.

---

## PASO 8 — Dejar el servidor encendido para siempre

El servidor se pone en marcha como un "servicio del sistema", para que arranque solo cada vez que la instancia se encienda o reinicie.

Copiá este bloque completo tal cual (crea el servicio del panel):

```
sudo tee /etc/systemd/system/chefcito-panel.service > /dev/null <<'EOF'
[Unit]
Description=Chefcito - Panel de control (puerto 3000)
After=network.target

[Service]
WorkingDirectory=/home/ubuntu/res-chefcito/server
ExecStart=/usr/bin/node server.js
Restart=always
RestartSec=5
User=ubuntu

[Install]
WantedBy=multi-user.target
EOF
```

Y este segundo bloque (crea el servicio que abre el puerto de los teléfonos):

```
sudo tee /etc/systemd/system/chefcito-listener.service > /dev/null <<'EOF'
[Unit]
Description=Chefcito - Listener de dispositivos (puerto 7777)
After=chefcito-panel.service
Requires=chefcito-panel.service

[Service]
Type=oneshot
WorkingDirectory=/home/ubuntu/res-chefcito/server
ExecStart=/usr/bin/node start_listener.js
RemainAfterExit=yes

[Install]
WantedBy=multi-user.target
EOF
```

Ahora activá ambos servicios:

```
sudo systemctl daemon-reload
sudo systemctl enable --now chefcito-panel chefcito-listener
```

Verificá que los dos puertos estén escuchando:

```
ss -tlnp | grep -E '3000|7777'
```

Deben aparecer **dos líneas**: una con `:3000` y otra con `:7777`. Si aparecen, el servidor está listo y quedará encendido incluso si reiniciás la instancia.

---

## PASO 9 — Generar el archivo base.apk (elegí UNA opción)

El `base.apk` es la aplicación "en blanco" sobre la cual el servidor genera la versión final del teléfono. Este proyecto **no lo incluye compilado**, así que hay que generarlo. Tenés **cuatro caminos**; elegí **uno solo**:

| Opción | Dónde | Dificultad | Cuándo elegirla |
|---|---|---|---|
| **A** | En la VPS (línea de comandos) | Media | No querés instalar nada en tu PC; tenés tiempo (20-40 min de compilación) |
| **B** | En tu PC con Android Studio | Fácil (con interfaz gráfica) | La más cómoda si podés instalar Android Studio |
| **C** | En tu PC con Linux (línea de comandos) | Media | Tenés Linux y preferís la terminal |
| **D** | En tu PC con Windows (línea de comandos) | Media | Tenés Windows sin querer instalar Android Studio |

> 💡 **Atajo sin compilar:** el repositorio hermano del proyecto (`control-parental-chefcito`) incluye en `server/base.apk` el mismo APK ya compilado. Si tenés acceso, descargalo de ahí, subilo al servidor (ANEXO A) saltando directo al PASO 10. Esta sección es para cuando querés generarlo vos.

> En **todas** las opciones el resultado es el mismo archivo `app-debug.apk`, y todas terminan dejándolo en la ruta que espera el servidor: `~/res-chefcito/server/base.apk`. La primera compilación descarga cientos de MB de componentes y puede tardar bastante; las siguientes son rápidas.

---

### Opción A — Compilar en la VPS (línea de comandos Linux remota)

Todo en la terminal del navegador del PASO 5. Si tu instancia es t3.micro (1 GB de RAM), primero agregamos memoria de intercambio (swap) para que la compilación no se muera:

```
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
free -h   # debe mostrar una línea Swap de ~2.0Gi
```

Si tu instancia es t3.micro, limitá además la memoria que usa la compilación:

```
sed -i 's/-Xmx2048m/-Xmx1024m/' ~/res-chefcito/client/gradle.properties
```

Instalá las herramientas de Android (SDK) por línea de comandos:

```
cd ~
mkdir -p android-sdk/cmdline-tools
wget https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O cmdtools.zip
unzip -o cmdtools.zip -d android-sdk/cmdline-tools
mv android-sdk/cmdline-tools/cmdline-tools android-sdk/cmdline-tools/latest
```

Definí las variables de entorno:

```
echo 'export ANDROID_HOME=$HOME/android-sdk' >> ~/.bashrc
echo 'export PATH=$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH' >> ~/.bashrc
source ~/.bashrc
```

Aceptá las licencias e instalá los componentes del proyecto:

```
yes | sdkmanager --licenses
sdkmanager "platforms;android-29" "build-tools;29.0.2" "platform-tools"
```

(Instalación de unos 5-10 minutos.)

Ahora compilá (⚠️ no cortes el proceso; en t3.micro puede tardar **20 a 40 minutos** la primera vez, y es normal que parezca "colgado" a veces):

```
cd ~/res-chefcito/client
chmod +x gradlew
./gradlew assembleDebug --no-daemon
```

Al final debe decir `BUILD SUCCESSFUL`. Copiá el resultado a la ruta que espera el servidor:

```
cp ~/res-chefcito/client/app/build/outputs/apk/debug/app-debug.apk ~/res-chefcito/server/base.apk
ls -la ~/res-chefcito/server/base.apk   # debe pesar ~2 MB
```

**Ventaja:** no instalaste nada en tu PC y el archivo ya quedó en su lugar. **Desventaja:** la compilación es lenta en una instancia chica.

---

### Opción B — Compilar en tu PC con Android Studio (la más fácil con gráficos)

1. Instalá **Android Studio** en tu computadora (gratis: developer.android.com/studio).
2. Descargá el proyecto: en la página del repo en GitHub, botón verde **Code → Download ZIP**, y descomprimilo (por ejemplo en `C:\Users\TU_USUARIO\Documents\`).
3. Abrí Android Studio → **File → Open** → elegí la carpeta **`client`** dentro del proyecto (⚠️ la carpeta client, no la raíz) → **Open**. Esperá a que termine de sincronizar (la primera vez puede tardar varios minutos).
4. **Si la sincronización falla con error de Java:** File → Settings → Build, Execution, Deployment → Build Tools → Gradle → en **"Gradle JDK"** elegí un **JDK 11** (podés descargarlo desde ese mismo desplegable con "Download JDK…", versión 11, proveedor Eclipse Temurin) → OK → esperá de nuevo.
5. Menú **Build → Build App Bundle(s) / APK(s) → Build APK(s)**.
6. Cuando termine, aparece una notificación con el enlace **"locate"**: clic ahí y se abre la carpeta con el archivo **`app-debug.apk`** (ruta completa: `client\app\build\outputs\apk\debug\app-debug.apk`).
7. Subilo al servidor (necesitás el archivo .pem del PASO 2 y la conexión SSH de tu PC — mirá el ANEXO A). En CMD o PowerShell de Windows:

```
scp -i "C:\Users\TU_USUARIO\Downloads\chefcito-key.pem" "C:\ruta\del\proyecto\client\app\build\outputs\apk\debug\app-debug.apk" ubuntu@TU_IP_ELASTICA:/home/ubuntu/res-chefcito/server/base.apk
```

(En Linux o Mac, la misma línea con las rutas de tu sistema.) Verificá en la terminal del navegador con `ls -la ~/res-chefcito/server/base.apk`.

**Ventaja:** la herramienta gráfica resuelve sola casi todo (Java, SDK, licencias). **Desventaja:** instalación grande (varios GB).

---

### Opción C — Compilar en tu PC con Linux (línea de comandos)

1. Instalá Java 11 (igual que en el servidor):

```
wget -qO - https://packages.adoptium.net/artifactory/api/gpg/key/public | sudo gpg --dearmor -o /etc/apt/trusted.gpg.d/adoptium.gpg
echo "deb https://packages.adoptium.net/artifactory/deb $(lsb_release -cs) main" | sudo tee /etc/apt/sources.list.d/adoptium.list
sudo apt update && sudo apt install -y temurin-11-jdk
java -version   # debe responder 11.0.x
```

2. Instalá el SDK de Android por línea de comandos:

```
cd ~
mkdir -p android-sdk/cmdline-tools
wget https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O cmdtools.zip
unzip -o cmdtools.zip -d android-sdk/cmdline-tools
mv android-sdk/cmdline-tools/cmdline-tools android-sdk/cmdline-tools/latest
echo 'export ANDROID_HOME=$HOME/android-sdk' >> ~/.bashrc
echo 'export PATH=$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH' >> ~/.bashrc
source ~/.bashrc
yes | sdkmanager --licenses
sdkmanager "platforms;android-29" "build-tools;29.0.2" "platform-tools"
```

3. Descargá el proyecto (botón **Code → Download ZIP** en GitHub, o `git clone`) y acondicioná el archivo según el PASO 7.3 (borrar la línea `org.gradle.java.home=`).

4. Compilá:

```
cd ~/ruta/del/proyecto/client
chmod +x gradlew
./gradlew assembleDebug
```

Al final: `BUILD SUCCESSFUL`, y el APK queda en `app/build/outputs/apk/debug/app-debug.apk`.

5. Subilo al servidor:

```
scp -i ~/Downloads/chefcito-key.pem app/build/outputs/apk/debug/app-debug.apk ubuntu@TU_IP_ELASTICA:/home/ubuntu/res-chefcito/server/base.apk
```

---

### Opción D — Compilar en tu PC con Windows (línea de comandos)

1. Instalá **Java 11 (Temurin)**: descargá el instalador MSI desde https://adoptium.net/temurin/releases/ (versión 11, Windows x64, MSI). **En la instalación, activá las casillas "Add to PATH" y "Set JAVA_HOME"**. Al terminar, abrí un CMD nuevo y verificá:

```
java -version
```

Debe responder `11.0.x`. (Si dice otra versión, un Java más nuevo tomó el lugar: desinstalalo o reordená el PATH.)

2. Instalá el SDK de Android por línea de comandos:
   - Descargá: **https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip**
   - Descomprimilo de forma que quede la carpeta `bin` adentro de `C:\Android\cmdline-tools\latest` (creá esas carpetas: dentro del zip viene una carpeta `cmdline-tools`; renombrala a `latest` y ponela en esa ruta).

3. En un CMD **nuevo**:

```
setx ANDROID_HOME C:\Android
cd C:\Android\cmdline-tools\latest\bin
sdkmanager --licenses
sdkmanager "platforms;android-29" "build-tools;29.0.2" "platform-tools"
```

(Los dos comandos sdkmanager tardan varios minutos; aceptá las licencias escribiendo `y` cuando pida.)

4. Descargá el proyecto (botón **Code → Download ZIP** en GitHub) y descomprimilo. **Acondicioná** el archivo `client\gradle.properties` con el Bloc de notas: borrale la línea que empieza con `org.gradle.java.home=` y guardalo (mismo motivo que el PASO 7.3).

5. Cerrá y abrí un CMD nuevo (para que valga la variable ANDROID_HOME), navegá a la carpeta `client` del proyecto y compilá:

```
cd C:\ruta\del\proyecto\client
gradlew.bat assembleDebug
```

La primera vez descarga Gradle y librerías (varios minutos). Al final: `BUILD SUCCESSFUL`, y el APK queda en `app\build\outputs\apk\debug\app-debug.apk`.

6. Subilo al servidor (CMD; Windows 10/11 ya trae scp):

```
scp -i "C:\Users\TU_USUARIO\Downloads\chefcito-key.pem" app\build\outputs\apk\debug\app-debug.apk ubuntu@TU_IP_ELASTICA:/home/ubuntu/res-chefcito/server/base.apk
```

---

## PASO 10 — Generar el APK para el teléfono

Este comando toma el `base.apk`, lo personaliza y lo deja listo para instalar. **Reemplazá TU_IP_ELASTICA por tu IP real** (la del PASO 4):

```
cd ~/res-chefcito/server
node builder.js TU_IP_ELASTICA 7777
```

Si todo va bien, al final verás algo como:

```
> Rebuilding APK...
> Signing APK...
> Zipaligning...
--- SUCCESS: Chefcito.apk is ready ---
```

(El proceso tarda 1 a 2 minutos.)

### Descargar el APK a tu computadora

1. En tu computadora, abrí el navegador y entrá a: **http://TU_IP_ELASTICA:3000/output/Chefcito.apk**
2. Se descarga el archivo **Chefcito.apk**. Ese es el archivo que se instala en el teléfono.

⚠️ Si algún día cambiás la IP del servidor, tenés que regenerar el APK con la IP nueva (el teléfono guarda la IP con la que fue generado). Por eso conviene la Elastic IP del PASO 4, que nunca cambia.

---

## PASO 11 — Instalar la aplicación en el teléfono

1. **Pasá el archivo Chefcito.apk al teléfono** (cable USB, WhatsApp adjuntándolo a "Mensaje a mí mismo", Google Drive, Bluetooth — cualquiera sirve).
2. En el teléfono, abrí el archivo con el administrador de archivos. Android va a avisar que es una app de origen desconocido: aceptá **"Permitir instalar apps desconocidas"** para esa fuente (según la marca, está en Ajustes → Seguridad, o en Ajustes → Aplicaciones → Acceso especial → Instalar apps desconocidas). Es un paso normal para cualquier app que no venga de Play Store.
3. Instalá y abrí la aplicación. La primera vez muestra una **pantalla de configuración inicial** con 4 botones. Completá los 4 en orden:
   - **"Conceder permisos de la aplicación"** → activá todos los permisos que pida.
   - **"Activar servicio de accesibilidad"** → buscá la app en la lista y activalo (Android puede mostrar una advertencia; es normal para este tipo de servicio).
   - **"Permitir acceso a notificaciones"** → activá el acceso en la lista.
   - **"Desactivar optimización de batería"** → elegí "Permitir". Esto evita que Android cierre la app para ahorrar batería.
4. Al completar los 4, la pantalla de configuración desaparece y se ve la **pantalla informativa**. La app ya está conectada al servidor.
5. No hace falta dejar la app abierta: queda funcionando en segundo plano y se reengancha sola (incluso después de reiniciar el teléfono).

Verificación rápida: en el panel del PASO 12, el dispositivo debe aparecer conectado.

---

## PASO 12 — El panel de control

1. En tu computadora, abrí el navegador y entrá a: **http://TU_IP_ELASTICA:3000**
2. El dispositivo aparece en la lista. Tocándolo, se abren las secciones disponibles: **ubicación** (posición actual en el mapa), **batería y conexión**, **aplicaciones instaladas**, **notificaciones que recibe el teléfono**, **ver la pantalla en vivo**, **contactos**, entre otras, según las funciones que el proyecto tenga activadas.
3. Consejos de uso:
   - No compartas la IP de tu servidor: quien la conozca puede ver el panel (por eso el puerto 3000 está limitado a tu IP en el PASO 3).
   - El panel funciona con la ventana abierta; si la cerrás, los dispositivos siguen conectados al servidor igual.
   - Podés conectar más de un teléfono al mismo servidor si los instalás en varios dispositivos.

---

## ANEXO A — Conectarse por SSH con el archivo .pem

Para subir archivos (necesario en las Opciones B, C y D del PASO 9) y para usar una terminal de tu propia PC.

En Windows (CMD o PowerShell — Windows 10/11 ya trae ssh y scp):

```
ssh -i "C:\Users\TU_USUARIO\Downloads\chefcito-key.pem" ubuntu@TU_IP_ELASTICA
```

Si Windows protesta con "UNPROTECTED PRIVATE KEY", arreglá los permisos del archivo (CMD):

```
icacls "C:\Users\TU_USUARIO\Downloads\chefcito-key.pem" /inheritance:r /grant:r "%username%:R"
```

Para copiar archivos hacia el servidor:

```
scp -i "C:\Users\TU_USUARIO\Downloads\chefcito-key.pem" archivo ubuntu@TU_IP_ELASTICA:/home/ubuntu/
```

En Linux/Mac, la misma sintaxis con las rutas de tu sistema (`scp -i ~/Downloads/chefcito-key.pem ...`).

---

## ANEXO B — Problemas frecuentes y soluciones

| Síntoma | Causa | Solución |
|---|---|---|
| `apktool --version` muestra un menú de ayuda | El lanzador quedó mal escrito (faltó el `"$@"`) | Repetí el bloque del PASO 6.4 copiándolo tal cual |
| El build del APK final falla al decodificar | Se instaló apktool 3.x | Reinstalá la 2.9.3 (PASO 6.4) |
| `npm` dejó de existir después de un apt purge | Se purgaron paquetes de node | `sudo apt install -y nodejs npm` |
| `zipalign: cannot execute binary file` | La instancia es Arm (t4g) | Recreá la instancia como t3.micro/small x86 (PASO 2) y repetí del PASO 6 en más |
| Compilación aborta con `Value 'C:\Users\...' given for org.gradle.java.home ... is invalid` | No se borró (o se borró mal) la línea del `gradle.properties` | Hacé el PASO 7.3 (o el punto 4 de la Opción D) de nuevo |
| Compilación aborta con `SDK location not found` | Falta el SDK de Android o la variable ANDROID_HOME | Instalá el SDK (PASO 9, opción A/C/D) y definí ANDROID_HOME; en Android Studio no pasa (opción B) |
| Compilación aborta con `Unsupported class file major version` | Se está compilando con un Java demasiado nuevo (17 o superior) | Usá Java 11: revisá `java -version`; en Android Studio, configurá "Gradle JDK" = 11 (PASO 9 opción B, punto 4) |
| Compilación aborta con `Could not resolve com.android.tools.build:gradle` | Sin acceso a internet o red que bloquea la descarga | Probá con otra red; reintentá (la descarga continúa donde quedó) |
| Compilación se muere con `OutOfMemoryError` o `GC overhead limit exceeded` en la VPS | RAM insuficiente | Verificá el swap con `free -h` (Opción A, primer bloque); en t3.micro aplicá el `sed` que limita memoria; compilá con `--no-daemon` |
| `gradlew: Permission denied` | Falta permiso de ejecución | `chmod +x gradlew` |
| No puedo abrir el panel en el navegador | Puerto 3000 no abierto a tu IP, o servicio caído | Revisá el PASO 3; en el servidor: `systemctl status chefcito-panel --no-pager` |
| El teléfono no aparece en el panel | Listener del 7777 caído, o la app no tiene permisos completos | En el servidor: `systemctl status chefcito-listener --no-pager`; en el teléfono: reabrí la app y revisá que los 4 permisos del wizard estén activos |
| El APK no descarga desde el navegador | El servidor se reinició y falta regenerar, o IP mal escrita | Repetí el PASO 10; verificá que la URL use TU_IP_ELASTICA real |
| `git clone` pide usuario y contraseña | El repositorio está privado todavía | (Administrador: hacerlo público según la nota inicial) o clonar con token (PASO 7.1) |
| Me cobraron algo en AWS | Elastic IP suelta, instancia distinta a la de la guía, o recursos olvidados | Revisá Billing; liberá/quitá lo que no uses (ANEXO C) |
| La app se cierra sola en el teléfono | Optimización de batería activa | Wizard → botón "Desactivar optimización de batería" |

---

## ANEXO C — Dar de baja todo (si dejás de usar el sistema)

Para no consumir créditos ni recibir cobros:

1. **EC2 → Instances** → tildá `chefcito-server` → **Instance state → Terminate instance** (confirmá). Esto borra el disco del servidor.
2. **EC2 → Elastic IPs** → seleccioná la IP → **Actions → Release Elastic IP addresses**.
3. **EC2 → Security Groups** → seleccioná `chefcito-sg` → **Actions → Delete security group**.
4. Revisá **Billing** al día siguiente: no deben aparecer cobros nuevos.
5. (Opcional) **EC2 → Key pairs** → borrá `chefcito-key`.

⚠️ Al terminar la instancia se pierde el servidor: los teléfonos dejarán de conectar. Si volvés a arrancar, seguí esta guía desde el PASO 2 (y regenerá el APK si la IP nueva es distinta).