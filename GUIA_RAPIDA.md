# 🎯 GUÍA RÁPIDA - LG ThinQ Remote v2.0

## 📱 Resumen del Proyecto

Aplicación Android completa para controlar TVs LG webOS, desarrollada mediante análisis del APK oficial de LG ThinQ.

**Estado**: ✅ **COMPLETADO - Listo para usar**

---

## 🚀 INSTALACIÓN RÁPIDA

### Opción 1: Build desde código fuente

```bash
cd /data/data/com.termux/files/home/lg_webos_rooting/lg_remote_android_v2
./build_complete.sh
```

El APK se generará en: `LGThinQ_RoRo_v2.0_debug.apk`

### Opción 2: Instalar APK directamente

```bash
adb install -r LGThinQ_RoRo_v2.0_debug.apk
```

---

## 📖 CÓMO USAR LA APP

### Paso 1: Preparación
1. **TV y Android deben estar en la misma red WiFi**
2. Encender el TV
3. Abrir la app en Android

### Paso 2: Descubrir TV
**Opción A - Automático** (Recomendado):
1. Presionar "🔍 Buscar TV automáticamente"
2. Esperar 5 segundos
3. Si encuentra TVs, seleccionar uno de la lista
4. La IP se llenará automáticamente

**Opción B - Manual**:
1. Averiguar IP del TV (Configuración → Red → Estado de Red)
2. Ingresar IP en el campo (ej: 192.168.1.100)

### Paso 3: Conectar (Primera vez)
1. Presionar "Conectar"
2. **Aparecerá diálogo en el TV**: "Permitir conexión desde dispositivo?"
3. **Aceptar en el TV** con el control remoto
4. La app se conectará automáticamente
5. ✅ **El client-key se guardará para futuras conexiones**

### Paso 4: Controlar
Los controles aparecerán cuando esté conectado:
- **⏻ Power**: Apagar TV
- **🏠 Home**: Ir al menú principal
- **VOL +/-**: Controlar volumen
- **CH ↑/↓**: Cambiar canales
- **🔇 MUTE**: Silenciar
- **Netflix/YouTube/Prime**: Lanzar apps

---

## 🔑 CARACTERÍSTICAS PRINCIPALES

### ✅ Conexión Inteligente
- **Auto-discovery SSDP**: Encuentra TVs en la red automáticamente
- **Pairing persistente**: Solo necesitas aceptar una vez en el TV
- **Multi-TV**: Guarda múltiples TVs y cambia entre ellos
- **Reconexión automática**: Usa el client-key guardado

### ✅ Controles Completos
**Básicos**:
- Power On/Off
- Volumen (Up/Down/Mute)
- Canales (Up/Down)
- Home

**Apps Rápidas**:
- Netflix
- YouTube
- Amazon Prime Video

**Avanzados** (en el código, no en UI):
- 65+ comandos SSAP
- Control de teclado
- Media controls
- Cambio de inputs
- Notificaciones

---

## 🏗️ ARQUITECTURA TÉCNICA

### Componentes Principales

```
MainActivity          → UI y gestión de eventos
WebOSClient          → Cliente WebSocket SSAP
SSDPDiscovery        → Auto-discovery de TVs
TVPreferences        → Persistencia de datos
Models               → URIs SSAP y modelos
```

### Flujo de Conexión

```
1. Usuario → [Buscar TV]
2. SSDPDiscovery → Multicast SSDP → Lista de IPs
3. Usuario → [Conectar]
4. WebOSClient → WebSocket ws://IP:3000
5. WebOSClient → {"type":"register"}
6. TV → Muestra diálogo
7. Usuario → Acepta en TV
8. TV → {"type":"registered", "client-key":"ABC123"}
9. TVPreferences → Guarda client-key
10. MainActivity → Muestra controles
```

### Protocolo SSAP

```json
// Registro inicial
{
  "type": "register",
  "payload": {
    "forcePairing": false,
    "pairingType": "PROMPT",
    "manifest": { ... }
  }
}

// Comando (ejemplo: subir volumen)
{
  "type": "request",
  "id": "req_1",
  "uri": "ssap://audio/volumeUp"
}
```

---

## 📊 COMANDOS SSAP DISPONIBLES

### Sistema
```kotlin
powerOff()           // ssap://system/turnOff
screenOff()          // ssap://com.webos.service.tvpower/power/turnOffScreen
screenOn()           // ssap://com.webos.service.tvpower/power/turnOnScreen
```

### Audio
```kotlin
volumeUp()           // ssap://audio/volumeUp
volumeDown()         // ssap://audio/volumeDown
volumeMute(true)     // ssap://audio/setMute
setVolume(50)        // ssap://audio/setVolume
```

### TV
```kotlin
channelUp()          // ssap://tv/channelUp
channelDown()        // ssap://tv/channelDown
openChannel("7")     // ssap://tv/openChannel
```

### Apps
```kotlin
openNetflix()        // Launch netflix
openYouTube()        // Launch youtube.leanback.v4
openAmazon()         // Launch amazon
openDisneyPlus()     // Launch com.disney.disneyplus-prod
openSpotify()        // Launch spotify-beehive
```

### Teclado (IME)
```kotlin
inputText("Hola")    // ssap://com.webos.service.ime/insertText
sendEnterKey()       // ssap://com.webos.service.ime/sendEnterKey
deleteCharacters(3)  // ssap://com.webos.service.ime/deleteCharacters
```

### Media
```kotlin
mediaPlay()          // ssap://media.controls/play
mediaPause()         // ssap://media.controls/pause
mediaStop()          // ssap://media.controls/stop
mediaRewind()        // ssap://media.controls/rewind
mediaFastForward()   // ssap://media.controls/fastForward
```

---

## 🎨 PERSONALIZACIÓN

### Agregar más apps

1. Abrir `Models.kt`
2. Agregar ID en `AppIds`:
```kotlin
const val MI_APP = "com.ejemplo.miapp"
```

3. Abrir `WebOSClient.kt`
4. Agregar función:
```kotlin
fun openMiApp() = launchApp(AppIds.MI_APP)
```

5. Agregar botón en layout y MainActivity

### Cambiar colores

Editar `res/values/colors.xml`:
```xml
<color name="lg_red">#TU_COLOR</color>
```

---

## 🐛 TROUBLESHOOTING

### ❌ "No se encontraron TVs"
**Solución**:
- Verifica que TV y Android estén en la misma red WiFi
- Asegúrate que el TV esté encendido
- Verifica que el firewall no bloquee SSDP (puerto 1900 UDP)
- Intenta ingresar la IP manualmente

### ❌ "Error al conectar"
**Solución**:
- Verifica la IP del TV (Configuración → Red)
- Asegúrate que el TV esté encendido
- Verifica conexión a WiFi
- Prueba reiniciar el TV

### ❌ "Pairing no funciona"
**Solución**:
- Asegúrate de aceptar en el TV cuando aparezca el diálogo
- Si no aparece el diálogo, elimina el client-key guardado y vuelve a intentar
- Verifica que el TV permita conexiones remotas (generalmente habilitado por defecto)

### ❌ "Controles no responden"
**Solución**:
- Verifica que estés conectado (status = "● Conectado")
- Revisa los logs: `adb logcat | grep WebOSClient`
- Desconecta y vuelve a conectar
- Reinicia la app

---

## 📚 RECURSOS

### Archivos del Proyecto
- `README.md` - Documentación técnica completa
- `PROYECTO_V2_STATUS.md` - Estado del desarrollo
- `ANALISIS_LG_THINQ.md` - Análisis del APK original
- `build_complete.sh` - Script de build

### Código Fuente
```
app/src/main/java/com/roro/lgthinq/
├── MainActivity.kt      - 192 líneas
├── WebOSClient.kt       - 237 líneas
├── SSDPDiscovery.kt     - 89 líneas
├── Models.kt            - 81 líneas
└── TVPreferences.kt     - 76 líneas

Total: ~675 líneas de código Kotlin
```

### Dependencias Clave
- WebSocket: `org.java-websocket:Java-WebSocket:1.5.3`
- JSON: `com.google.code.gson:gson:2.10.1`
- Coroutines: `kotlinx-coroutines-android:1.7.3`

---

## 📈 ROADMAP FUTURO

### Próximas versiones podrían incluir:
- [ ] Widget de Android para control rápido
- [ ] Control de mouse/pointer con gestos
- [ ] Teclado virtual completo en pantalla
- [ ] Lista dinámica de apps del TV
- [ ] Control de inputs (HDMI)
- [ ] Modo landscape con D-Pad
- [ ] Macros programables
- [ ] Voice control

---

## 💡 TIPS & TRICKS

### Tip 1: Múltiples TVs
La app guarda automáticamente todos los TVs que emparejes. Solo cambia la IP para conectarte a otro TV.

### Tip 2: Cliente-Key persistente
Una vez emparejado, nunca más necesitarás aceptar el pairing en el TV. La app se conecta automáticamente.

### Tip 3: Auto-discovery
El auto-discovery funciona mejor cuando el TV está en la pantalla de inicio y no en una app.

### Tip 4: Comandos personalizados
Puedes enviar cualquier comando SSAP usando:
```kotlin
webOSClient?.sendCommand("ssap://tu/comando/aqui")
```

---

## 🎓 CRÉDITOS

**Desarrollado por**: RoRo  
**Basado en**: Análisis del APK oficial LG ThinQ  
**Código decompilado**: `/storage/emulated/0/Apktool_M/lgthinq_decompiled_src`  
**Versión**: 2.0  
**Fecha**: 2024-12-07

---

## 📄 LICENCIA

Proyecto educativo y de investigación.  
No afiliado con LG Electronics.

---

**¿Preguntas? ¿Problemas? ¿Mejoras?**  
Revisa los logs, lee el código fuente, y experimenta! 🚀
