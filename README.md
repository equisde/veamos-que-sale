# 📱 LG ThinQ Remote v2.0 - RoRo Edition

Control remoto completo para LG webOS TV basado en análisis del APK oficial de LG ThinQ.

## 🎯 Características

### ✅ Implementado

- **Auto-Discovery SSDP**: Búsqueda automática de TVs en la red local
- **Pairing Seguro**: Sistema de emparejamiento con client-key persistente
- **Controles Básicos**:
  - Power On/Off
  - Volumen Up/Down/Mute
  - Canales Up/Down
  - Navegación Home
  
- **Apps Rápidas**:
  - Netflix
  - YouTube
  - Amazon Prime Video
  
- **Gestión Inteligente**:
  - Guardado automático de TVs emparejados
  - Reconexión automática con client-key guardado
  - Multi-TV support (cambia entre diferentes TVs)

### 🚀 Basado en Análisis LG ThinQ Original

Este proyecto fue desarrollado mediante ingeniería inversa del APK oficial de LG ThinQ:

**Hallazgos clave del análisis:**
- Puerto WebSocket: `3000` (NO 3001)
- Protocolo: SSAP (Simple Service Access Protocol)
- Discovery: SSDP con `urn:lge-com:service:webos-second-screen:1`
- Pairing: Sistema de `client-key` con confirmación visual en TV
- Formato: JSON sobre WebSocket

## 🏗️ Arquitectura

```
lg_remote_android_v2/
├── app/src/main/java/com/roro/lgthinq/
│   ├── MainActivity.kt         # UI principal
│   ├── WebOSClient.kt          # Cliente WebSocket SSAP
│   ├── SSDPDiscovery.kt        # Discovery de TVs en red
│   ├── Models.kt               # Modelos de datos y URIs SSAP
│   └── TVPreferences.kt        # Gestión de TVs guardados
└── app/src/main/res/
    ├── layout/
    │   └── activity_main.xml   # UI inspirada en LG ThinQ oficial
    └── drawable/
        ├── ic_add.xml          # Iconos copiados del APK original
        ├── ic_remove.xml
        ├── ic_chevron_up.xml
        ├── ic_chevron_down.xml
        ├── ic_home.xml
        └── ic_power.xml
```

## 🔧 Tecnologías

- **Kotlin** - Lenguaje principal
- **Coroutines** - Manejo asíncrono
- **WebSocket** - Comunicación con TV (Java-WebSocket)
- **SSDP** - Descubrimiento en red local
- **Gson** - Parseo JSON
- **Material Design 3** - UI moderna

## 📋 Comandos SSAP Soportados

### Sistema
- `ssap://system/turnOff` - Apagar TV
- `ssap://com.webos.service.tvpower/power/turnOnScreen` - Encender pantalla
- `ssap://com.webos.service.tvpower/power/turnOffScreen` - Apagar pantalla

### Audio
- `ssap://audio/volumeUp` - Subir volumen
- `ssap://audio/volumeDown` - Bajar volumen
- `ssap://audio/setMute` - Silenciar
- `ssap://audio/setVolume` - Establecer volumen

### TV
- `ssap://tv/channelUp` - Canal siguiente
- `ssap://tv/channelDown` - Canal anterior
- `ssap://tv/openChannel` - Abrir canal específico

### Launcher
- `ssap://system.launcher/launch` - Lanzar app
- `ssap://system.launcher/close` - Cerrar app

### IME (Teclado)
- `ssap://com.webos.service.ime/insertText` - Insertar texto
- `ssap://com.webos.service.ime/sendEnterKey` - Enviar Enter
- `ssap://com.webos.service.ime/deleteCharacters` - Borrar caracteres

### Media Controls
- `ssap://media.controls/play` - Play
- `ssap://media.controls/pause` - Pause
- `ssap://media.controls/stop` - Stop
- `ssap://media.controls/rewind` - Rebobinar
- `ssap://media.controls/fastForward` - Avanzar

## 🚀 Instalación y Uso

### Prerequisitos
- Android SDK 24+ (Android 7.0+)
- Kotlin 1.9+
- Gradle 8.0+

### Build

```bash
# Construir APK Debug
./gradlew assembleDebug

# Construir APK Release
./gradlew assembleRelease

# Instalar en dispositivo conectado
./gradlew installDebug
```

### Uso

1. **Descubrir TV**:
   - Presiona "🔍 Buscar TV automáticamente"
   - O ingresa manualmente la IP del TV
   
2. **Conectar**:
   - Presiona "Conectar"
   - Si es la primera vez, acepta el pairing en el TV
   - El client-key se guarda automáticamente
   
3. **Controlar**:
   - Usa los controles de volumen, canales, y apps
   - La app recordará el TV para futuras conexiones

## 🔐 Pairing Process

```
1. App → TV: {"type":"register"}
2. TV muestra diálogo "Permitir conexión desde dispositivo?"
3. Usuario acepta en TV
4. TV → App: {"type":"registered", "payload":{"client-key":"XXXXX"}}
5. App guarda client-key en SharedPreferences
6. Próximas conexiones: {"type":"register", "client-key":"XXXXX"}
```

## 📊 Estructura de Datos

### TVDevice
```kotlin
data class TVDevice(
    val ip: String,
    val name: String?,
    val model: String?,
    val clientKey: String?
)
```

### SSAPCommand
```kotlin
data class SSAPCommand(
    val type: String = "request",
    val id: String,
    val uri: String,
    val payload: Map<String, Any>?
)
```

## 🎨 Diseño UI

El diseño está inspirado en el UI oficial de LG ThinQ con:
- Colores corporativos LG (Rojo #A50034, Azul #00A3E0)
- Material Design 3
- Cards con elevación
- Iconos vectoriales del APK original
- Layout responsivo

## 🐛 Debug

Para ver logs de WebSocket:
```bash
adb logcat | grep WebOSClient
```

## 📝 Notas Técnicas

- **Puerto correcto**: 3000 (NO 3001 como algunos documentos sugieren)
- **SSDP Target**: `urn:lge-com:service:webos-second-screen:1`
- **Manifest Version**: 1
- **Pairing Type**: PROMPT

## 🔮 Roadmap

### Próximas funcionalidades:
- [ ] Mouse/Pointer control
- [ ] Keyboard virtual completo
- [ ] Lista de apps instaladas
- [ ] Control de inputs (HDMI)
- [ ] Widget de Android para control rápido
- [ ] Soporte para múltiples idiomas
- [ ] Modo oscuro/claro
- [ ] Notificaciones al TV

## 📚 Referencias

- Análisis APK: `/ANALISIS_LG_THINQ.md`
- Código decompilado: `/storage/emulated/0/Apktool_M/lgthinq_decompiled_src`
- LG webOS Developer: https://webostv.developer.lge.com/

## 👨‍💻 Autor

**RoRo** - Basado en análisis del APK oficial de LG ThinQ

## 📄 Licencia

Este proyecto es para fines educativos y de investigación.

---

**Version**: 2.0  
**Build**: 2024-12-07  
**Target SDK**: 34 (Android 14)  
**Min SDK**: 24 (Android 7.0)
