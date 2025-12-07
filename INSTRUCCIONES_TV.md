# 📺 Instrucciones para Configurar el TV LG webOS

## ⚠️ IMPORTANTE: Requisitos del TV

Para que esta app funcione, tu TV LG webOS debe tener habilitado el servicio WebSocket en el puerto 3000.

### 🔧 Pasos para habilitar en el TV:

#### Opción 1: LG Connect Apps (Recomendado)
1. En el TV, presiona **⚙️ Settings** (Configuración)
2. Ve a **General** → **Mobile TV On**
3. **Habilita "LG Connect Apps"** o "Mobile TV On"
4. El TV ahora aceptará conexiones WebSocket

#### Opción 2: Verificar Puerto 3000
Si tu TV es modelo 2014 o posterior:
- El puerto 3000 debería estar abierto por defecto
- No necesitas habilitar nada especial
- Solo asegúrate de que el TV y Android estén en la misma red WiFi

#### Opción 3: Modelos Antiguos (2011-2013)
- Algunos modelos antiguos usan **puerto 3001** en lugar de 3000
- Si no funciona, edita el código y cambia WS_PORT a 3001

### 🌐 Verificar Conectividad

#### En Termux (para verificar que el puerto esté abierto):

```bash
# Instalar netcat si no lo tienes
pkg install netcat-openbsd

# Verificar si el puerto 3000 está abierto (reemplaza IP_DEL_TV)
nc -zv IP_DEL_TV 3000

# Si responde "succeeded" o "open", el puerto está disponible
```

#### Obtener IP del TV:
1. En el TV: **Settings** → **Network** → **Wi-Fi Connection** → **Advanced**
2. Anota la **IP Address** (ej: 192.168.1.100)

### 📱 Flujo de Conexión Esperado

Cuando presionas "Conectar" en la app:

1. **App** → Conecta WebSocket a `ws://TV_IP:3000/`
2. **App** → Envía mensaje de registro con `pairingType: PIN`
3. **TV** → Muestra código PIN en pantalla (ej: "123456")
4. **App** → Muestra el mismo PIN en la pantalla del teléfono
5. **Usuario** → Verifica que ambos códigos coincidan
6. **TV** → Envía `client-key` a la app
7. **App** → Guarda `client-key` permanentemente
8. **Próximas conexiones** → Automáticas sin PIN

### ❌ Solución de Problemas

#### "No se pudo conectar al TV"
- ✅ TV está encendido
- ✅ TV y Android en la misma red WiFi
- ✅ IP del TV es correcta
- ✅ "LG Connect Apps" está habilitado
- ✅ Firewall del router no bloquea el puerto 3000

#### "TV rechazó la conexión"
- El TV no tiene habilitado "LG Connect Apps"
- Intenta reiniciar el TV
- Verifica que no haya otra app conectada al TV

#### "Tiempo de espera agotado"
- El TV no está respondiendo
- Verifica la IP nuevamente
- Prueba hacer ping al TV: `ping IP_DEL_TV`

#### Ver logs en tiempo real:
```bash
# En Termux, para ver logs de la app
adb logcat | grep WebOSClient
```

Los logs te dirán exactamente qué está pasando:
- `🔌 Intentando conectar WebSocket...` - Iniciando conexión
- `✅ WebSocket conectado exitosamente` - ¡Funciona!
- `📤 Enviando mensaje de registro` - Enviando pairing
- `📩 Mensaje recibido: {"type":"prompt"...}` - TV enviando PIN
- `❌ WebSocket error` - Algo falló

### 🔐 Sobre el Pairing con PIN

El **pairingType: "PIN"** es el método más seguro que usa LG ThinQ oficial:

- **PIN**: TV muestra código, usuario lo verifica visualmente
- **PROMPT**: Solo aparece diálogo "Aceptar/Rechazar" (menos seguro)

Esta app usa PIN para máxima seguridad.

### 📚 Documentación Técnica

**Protocolo**: WebSocket SSAP (Secure Socket API Protocol)
**Puerto**: 3000 (TCP)
**URL**: `ws://[TV_IP]:3000/`
**Formato**: JSON

**Ejemplo de mensaje de registro**:
```json
{
  "type": "register",
  "payload": {
    "forcePairing": false,
    "pairingType": "PIN",
    "manifest": {
      "manifestVersion": 1,
      "appVersion": "2.0",
      "signed": {
        "appId": "com.roro.lgthinq",
        "permissions": [...]
      }
    }
  }
}
```

**Respuesta del TV con PIN**:
```json
{
  "type": "prompt",
  "payload": {
    "pinCode": "123456"
  }
}
```

**Registro exitoso**:
```json
{
  "type": "registered",
  "payload": {
    "client-key": "abc123def456..."
  }
}
```

### ✅ Una vez conectado

Después del primer pairing:
- El `client-key` se guarda en SharedPreferences
- Próximas conexiones son automáticas
- No necesitas volver a hacer pairing
- Solo presiona "Conectar" y listo

---

**¿Problemas?** Revisa los logs con `adb logcat | grep WebOSClient` 🔍
