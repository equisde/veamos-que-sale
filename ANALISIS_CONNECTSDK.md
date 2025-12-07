# 📋 Análisis Completo de LG ThinQ - Conexión WebOS

## 🔍 Hallazgos del Código Descompilado

### 1. **LG ThinQ usa ConnectSDK**
- **Librería**: `com.connectsdk.service.WebOSTVService`
- **Cliente WebSocket**: `com.connectsdk.service.webos.WebOSTVServiceSocketClient`
- **Repositorio oficial**: https://github.com/ConnectSDK/Connect-SDK-Android-Core

### 2. **Estructura de Clases Clave**

```
com/connectsdk/service/
├── WebOSTVService.java          ← Servicio principal
├── webos/
│   ├── WebOSTVServiceSocketClient.java  ← Cliente WebSocket
│   ├── WebOSTVMouseSocketConnection.java
│   ├── WebOSTVKeyboardInput.java
│   └── WebOSTVDeviceService.java
```

### 3. **Tipo de Pairing**

Del código descompilado (`WebOSTVService.smali`):
```smali
.field pairingType:Lcom/connectsdk/service/DeviceService$PairingType;

# En el constructor:
sget-object p1, Lcom/connectsdk/service/DeviceService$PairingType;->FIRST_SCREEN:Lcom/connectsdk/service/DeviceService$PairingType;
iput-object p1, p0, Lcom/connectsdk/service/DeviceService;->pairingType:Lcom/connectsdk/service/DeviceService$PairingType;
```

**Conclusión**: LG ThinQ usa `PairingType.FIRST_SCREEN` NO `PIN`!

### 4. **Protocolo de Conexión**

Del código fuente de ConnectSDK:

#### Paso 1: Conectar WebSocket
```java
URI uri = URI("ws://TV_IP:3000/")
WebSocketClient.connect()
```

#### Paso 2: Handshake HTTP
```
GET / HTTP/1.1
Host: TV_IP:3000
Upgrade: websocket
Connection: Upgrade
Sec-WebSocket-Version: 13
Sec-WebSocket-Key: [generated]
```

#### Paso 3: onOpen - Enviar "hello"
```java
protected void handleConnected() {
    helloTV();  // Envía mensaje "hello"
}
```

#### Paso 4: Registro
```java
JSONObject manifest = new JSONObject();
manifest.put("manifestVersion", 1);
manifest.put("permissions", convertStringListToJSONArray(permissions));

JSONObject registerMsg = new JSONObject();
registerMsg.put("type", "register");
registerMsg.put("payload", manifest);

// Si hay client-key guardado:
if (clientKey != null) {
    registerMsg.put("client-key", clientKey);
}
```

#### Paso 5: Respuesta del TV

**Con client-key válido**:
```json
{
  "type": "registered",
  "payload": {
    "client-key": "abc123..."
  }
}
```

**Sin client-key o pairing requerido**:
```json
{
  "type": "response",
  "payload": {
    "pairingType": "PROMPT"  // o "PIN"
  }
}
```

### 5. **URIs SSAP Importantes**

Del `WebOSTVService.smali`:
```java
static APP_STATE = "ssap://system.launcher/getAppState"
static VOLUME = "ssap://audio/getVolume"
static MUTE = "ssap://audio/getMute"
static CHANNEL = "ssap://tv/getCurrentChannel"
static FOREGROUND_APP = "ssap://com.webos.applicationManager/getForegroundAppInfo"
```

### 6. **Permisos Requeridos**

```java
permissions.add("LAUNCH");
permissions.add("LAUNCH_WEBAPP");
permissions.add("APP_TO_APP");
permissions.add("CONTROL_AUDIO");
permissions.add("CONTROL_INPUT_MEDIA_PLAYBACK");
permissions.add("CONTROL_POWER");
permissions.add("READ_INSTALLED_APPS");
permissions.add("CONTROL_DISPLAY");
permissions.add("CONTROL_INPUT_JOYSTICK");
permissions.add("CONTROL_INPUT_MEDIA_RECORDING");
permissions.add("CONTROL_INPUT_TV");
permissions.add("READ_INPUT_DEVICE_LIST");
permissions.add("READ_NETWORK_STATE");
permissions.add("READ_TV_CHANNEL_LIST");
permissions.add("WRITE_NOTIFICATION_TOAST");
permissions.add("CONTROL_INPUT_TEXT");
permissions.add("CONTROL_MOUSE_AND_KEYBOARD");
permissions.add("READ_CURRENT_CHANNEL");
permissions.add("READ_RUNNING_APPS");
permissions.add("READ_UPDATE_INFO");
permissions.add("UPDATE_FROM_REMOTE_APP");
permissions.add("READ_LGE_SDX");
permissions.add("READ_NOTIFICATIONS");
permissions.add("SEARCH");
permissions.add("WRITE_SETTINGS");
permissions.add("WRITE_NOTIFICATION_ALERT");
permissions.add("CONTROL_INPUT_PHONE");
permissions.add("READ_LGE_TV_INPUT_EVENTS");
permissions.add("READ_TV_CURRENT_TIME");
```

### 7. **Manifest Completo**

```json
{
  "manifestVersion": 1,
  "permissions": [/* lista arriba */],
  "signatures": []  // Opcional
}
```

### 8. **Client-Key Persistente**

```java
// Guardar después de "registered":
String clientKey = payload.getString("client-key");
preferences.edit().putString("webos_client_key_" + tvIp, clientKey).apply();

// Usar en próximas conexiones:
{
  "type": "register",
  "client-key": "saved_key_here",
  "payload": { manifest }
}
```

### 9. **Manejo de Errores**

Del código fuente:
```java
@Override
public void onError(Exception ex) {
    if (!mConnectSucceeded) {
        handleConnectError(ex);  // Error antes de abrir
    } else {
        handleConnectionLost(false, ex);  // Error después de conectar
    }
}

@Override
public void onClose(int code, String reason, boolean remote) {
    handleConnectionLost(true, null);
}
```

**Códigos de cierre importantes**:
- **-1**: Handshake falló
- **1000**: Cierre normal
- **1006**: Cierre anormal sin handshake

### 10. **Estado de Conexión**

```java
enum State {
    INITIAL,
    CONNECTING,
    REGISTERING,
    REGISTERED
}
```

## ⚠️ Problema Actual

Nuestra app envía:
```json
{
  "type": "register",
  "client-key": "...",  // Si existe
  "payload": {
    "forcePairing": false,
    "pairingType": "PIN",  ❌ INCORRECTO
    "manifest": { ... }
  }
}
```

**LG ThinQ envía**:
```json
{
  "type": "register",
  "client-key": "...",  // Si existe
  "payload": {
    "manifestVersion": 1,
    "permissions": [...] // ✅ CORRECTO
  }
}
```

## ✅ Solución

1. **NO usar `pairingType` en el mensaje de registro**
2. **NO usar `forcePairing`**
3. **Simplificar el manifest** a solo `manifestVersion` y `permissions`
4. **El TV decide el tipo de pairing** (no nosotros)
5. **Esperar respuesta "registered" o mensaje con "pairingType"**

## 🔧 Implementación Correcta

Ver archivo: `WebOSClient_FIXED.kt` (próximo commit)

