package com.roro.lgthinq

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.util.concurrent.atomic.AtomicInteger

class WebOSClient(
    private val tvIp: String,
    private val onConnected: () -> Unit,
    private val onDisconnected: () -> Unit,
    private val onPairingRequired: (String?) -> Unit,
    private val onRegistered: (String) -> Unit,
    private val onError: (String) -> Unit,
    private val onLog: (String) -> Unit
) {
    private var webSocket: WebSocketClient? = null
    private val gson = Gson()
    private val messageId = AtomicInteger(0)
    private var clientKey: String? = null
    
    companion object {
        private const val TAG = "WebOSClient"
        private const val WS_PORT = 3000
        private const val CONNECTION_TIMEOUT = 10000 // 10 segundos
    }

    fun connect(savedClientKey: String? = null) {
        Log.d(TAG, "Iniciando conexión a $tvIp:$WS_PORT")
        onLog("🔌 Conectando a ws://$tvIp:$WS_PORT/")
        clientKey = savedClientKey
        val uri = URI("ws://$tvIp:$WS_PORT/")
        
        webSocket = object : WebSocketClient(uri) {
            override fun onOpen(handshake: ServerHandshake?) {
                Log.d(TAG, "✅ WebSocket conectado exitosamente")
                Log.d(TAG, "Handshake HTTP status: ${handshake?.httpStatus}")
                Log.d(TAG, "Handshake status message: ${handshake?.httpStatusMessage}")
                onLog("✅ WebSocket ABIERTO - HTTP ${handshake?.httpStatus} ${handshake?.httpStatusMessage}")
                register()
            }

            override fun onMessage(message: String?) {
                Log.d(TAG, "📩 Mensaje recibido: $message")
                onLog("📩 Mensaje del TV: ${message?.take(100)}...")
                handleMessage(message)
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                Log.d(TAG, "❌ WebSocket cerrado - Code: $code, Reason: $reason, Remote: $remote")
                val reasonText = when (code) {
                    -1 -> "Handshake falló - TV rechazó conexión antes de completar HTTP Upgrade"
                    1000 -> "Cierre normal"
                    1001 -> "Endpoint desaparecido"
                    1002 -> "Error de protocolo"
                    1003 -> "Datos no aceptables"
                    1006 -> "Conexión cerrada anormalmente (sin handshake)"
                    1007 -> "Datos inconsistentes"
                    1008 -> "Política violada"
                    1009 -> "Mensaje muy grande"
                    1010 -> "Extensión faltante"
                    1011 -> "Error interno del servidor"
                    else -> reason ?: "Sin razón específica"
                }
                onLog("❌ WebSocket cerrado - Code: $code ($reasonText), Remote: $remote")
                
                if (code == -1) {
                    onLog("💡 Sugerencia: El TV puede necesitar 'LG Connect Apps' habilitado")
                    onError("WebSocket handshake falló. El TV rechazó la conexión.\n\nVerifica:\n1. Settings → General → Mobile TV On\n2. Habilita 'LG Connect Apps'\n3. Reinicia el TV")
                }
                
                onDisconnected()
            }

            override fun onError(ex: Exception?) {
                Log.e(TAG, "⚠️ WebSocket error conectando a $tvIp:$WS_PORT", ex)
                val errorMsg = when {
                    ex?.message?.contains("failed to connect") == true -> 
                        "No se pudo conectar al TV. Verifica:\n1. TV encendido\n2. Misma red WiFi\n3. IP correcta: $tvIp"
                    ex?.message?.contains("Connection refused") == true -> 
                        "TV rechazó la conexión. Asegúrate de que el TV esté encendido y en la red"
                    ex?.message?.contains("timeout") == true -> 
                        "Tiempo de espera agotado. El TV no responde"
                    else -> ex?.message ?: "Error desconocido conectando al TV"
                }
                onLog("⚠️ Error WebSocket: ${ex?.javaClass?.simpleName} - ${ex?.message}")
                onError(errorMsg)
            }
        }
        
        try {
            Log.d(TAG, "🔌 Configurando WebSocket...")
            onLog("⏳ Configurando WebSocket (timeout: ${CONNECTION_TIMEOUT/1000}s)...")
            
            // Configurar headers compatibles con LG webOS
            webSocket?.addHeader("Origin", "null")
            webSocket?.addHeader("Sec-WebSocket-Version", "13")
            
            webSocket?.setConnectionLostTimeout(CONNECTION_TIMEOUT / 1000)
            
            onLog("🚀 Intentando conectar (método: connectBlocking)...")
            
            // Usar connectBlocking para mejor detección de errores
            Thread {
                try {
                    val connected = webSocket?.connectBlocking(CONNECTION_TIMEOUT.toLong(), java.util.concurrent.TimeUnit.MILLISECONDS)
                    if (connected == true) {
                        onLog("✅ connectBlocking exitoso")
                    } else {
                        onLog("❌ connectBlocking falló - timeout o rechazo")
                        onError("No se pudo establecer conexión con el TV en ${CONNECTION_TIMEOUT/1000}s")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error en connectBlocking", e)
                    onLog("💥 Error en connectBlocking: ${e.javaClass.simpleName} - ${e.message}")
                    onError("Error conectando: ${e.message}")
                }
            }.start()
            
        } catch (e: Exception) {
            Log.e(TAG, "💥 Excepción al intentar conectar", e)
            onLog("💥 Excepción al configurar: ${e.javaClass.simpleName} - ${e.message}")
            onError("Error iniciando conexión: ${e.message}")
        }
    }

    private fun register() {
        Log.d(TAG, "📤 Enviando mensaje de registro (ConnectSDK compatible)")
        onLog("📤 Enviando registro (formato ConnectSDK)")
        
        if (!clientKey.isNullOrEmpty()) {
            Log.d(TAG, "🔑 Usando client-key guardado: ${clientKey?.take(10)}...")
            onLog("🔑 Usando client-key guardado")
        } else {
            onLog("📝 Primera conexión, el TV mostrará diálogo de pairing")
        }
        
        // Crear manifest simplificado como Connect SDK
        val manifest = JsonObject().apply {
            addProperty("manifestVersion", 1)
            add("permissions", gson.toJsonTree(listOf(
                "LAUNCH",
                "LAUNCH_WEBAPP",
                "APP_TO_APP",
                "CLOSE",
                "TEST_OPEN",
                "TEST_PROTECTED",
                "CONTROL_AUDIO",
                "CONTROL_DISPLAY",
                "CONTROL_INPUT_JOYSTICK",
                "CONTROL_INPUT_MEDIA_RECORDING",
                "CONTROL_INPUT_MEDIA_PLAYBACK",
                "CONTROL_INPUT_TV",
                "CONTROL_INPUT_TEXT",
                "CONTROL_MOUSE_AND_KEYBOARD",
                "CONTROL_POWER",
                "READ_INSTALLED_APPS",
                "READ_LGE_SDX",
                "READ_NOTIFICATIONS",
                "SEARCH",
                "WRITE_SETTINGS",
                "WRITE_NOTIFICATION_ALERT",
                "CONTROL_INPUT_PHONE",
                "READ_INPUT_DEVICE_LIST",
                "READ_NETWORK_STATE",
                "READ_RUNNING_APPS",
                "READ_TV_CHANNEL_LIST",
                "WRITE_NOTIFICATION_TOAST",
                "READ_CURRENT_CHANNEL",
                "READ_UPDATE_INFO",
                "UPDATE_FROM_REMOTE_APP",
                "READ_LGE_TV_INPUT_EVENTS",
                "READ_TV_CURRENT_TIME"
            )))
        }
        
        // Mensaje de registro simple como ConnectSDK
        val registerMsg = JsonObject().apply {
            addProperty("type", "register")
            addProperty("id", "register_0")
            
            // Si tenemos client-key, agregarlo al nivel raíz
            if (!clientKey.isNullOrEmpty()) {
                addProperty("client-key", clientKey)
            }
            
            // Payload solo contiene el manifest
            add("payload", manifest)
        }
        
        send(registerMsg.toString())
    }

    private fun handleMessage(message: String?) {
        message ?: return
        
        try {
            val json = gson.fromJson(message, JsonObject::class.java)
            val type = json.get("type")?.asString
            
            onLog("🔍 Tipo de mensaje: $type")
            
            when (type) {
                "registered" -> {
                    val key = json.getAsJsonObject("payload")?.get("client-key")?.asString
                    if (key != null) {
                        clientKey = key
                        onLog("🔐 Client-key recibido del TV")
                        onRegistered(key)
                    }
                    onConnected()
                }
                "response" -> {
                    // Respuesta a comandos
                    Log.d(TAG, "Respuesta: $message")
                    onLog("✅ Comando ejecutado")
                }
                "prompt" -> {
                    // El TV está mostrando el PIN
                    val pinCode = json.getAsJsonObject("payload")?.get("pinCode")?.asString
                    Log.d(TAG, "PIN Code recibido: $pinCode")
                    onLog("🔢 PIN del TV: $pinCode")
                    onPairingRequired(pinCode)
                }
                "error" -> {
                    val error = json.get("error")?.asString ?: "Error desconocido"
                    onLog("❌ Error del TV: $error")
                    if (error.contains("pairing", ignoreCase = true) || error.contains("401", ignoreCase = true)) {
                        onPairingRequired(null)
                    } else {
                        onError(error)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parseando mensaje", e)
            onLog("⚠️ Error parseando mensaje: ${e.message}")
        }
    }

    fun sendCommand(uri: String, payload: JsonObject? = null) {
        val id = "req_${messageId.incrementAndGet()}"
        val command = JsonObject().apply {
            addProperty("type", "request")
            addProperty("id", id)
            addProperty("uri", uri)
            if (payload != null) {
                add("payload", payload)
            }
        }
        
        send(command.toString())
    }

    private fun send(message: String) {
        Log.d(TAG, "📡 Enviando: ${message.take(200)}${if(message.length > 200) "..." else ""}")
        try {
            webSocket?.send(message)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error enviando mensaje", e)
        }
    }

    fun disconnect() {
        webSocket?.close()
    }

    // Comandos específicos - Audio
    fun volumeUp() = sendCommand(SSAPUris.VOLUME_UP)
    fun volumeDown() = sendCommand(SSAPUris.VOLUME_DOWN)
    fun volumeMute(mute: Boolean = true) = sendCommand(SSAPUris.SET_MUTE, JsonObject().apply { 
        addProperty("mute", mute) 
    })
    fun setVolume(level: Int) = sendCommand(SSAPUris.SET_VOLUME, JsonObject().apply {
        addProperty("volume", level)
    })
    
    // TV & Canales
    fun channelUp() = sendCommand(SSAPUris.CHANNEL_UP)
    fun channelDown() = sendCommand(SSAPUris.CHANNEL_DOWN)
    fun openChannel(channelId: String) = sendCommand(SSAPUris.OPEN_CHANNEL, JsonObject().apply {
        addProperty("channelId", channelId)
    })
    
    // Sistema
    fun powerOff() = sendCommand(SSAPUris.TURN_OFF)
    fun screenOff() = sendCommand(SSAPUris.TURN_OFF_SCREEN)
    fun screenOn() = sendCommand(SSAPUris.TURN_ON_SCREEN)
    
    // Launcher
    fun home() = sendCommand(SSAPUris.LAUNCH_APP, JsonObject().apply {
        addProperty("id", AppIds.HOME)
    })
    
    fun launchApp(appId: String) = sendCommand(SSAPUris.LAUNCH_APP, JsonObject().apply {
        addProperty("id", appId)
    })
    
    fun closeApp(appId: String) = sendCommand(SSAPUris.CLOSE_APP, JsonObject().apply {
        addProperty("id", appId)
    })
    
    // IME (Teclado)
    fun sendEnterKey() = sendCommand(SSAPUris.SEND_ENTER)
    
    fun inputText(text: String) = sendCommand(SSAPUris.INSERT_TEXT, JsonObject().apply {
        addProperty("text", text)
        addProperty("replace", 0)
    })
    
    fun deleteCharacters(count: Int) = sendCommand(SSAPUris.DELETE_CHARACTERS, JsonObject().apply {
        addProperty("count", count)
    })
    
    // Media Controls
    fun mediaPlay() = sendCommand(SSAPUris.PLAY)
    fun mediaPause() = sendCommand(SSAPUris.PAUSE)
    fun mediaStop() = sendCommand(SSAPUris.STOP)
    fun mediaRewind() = sendCommand(SSAPUris.REWIND)
    fun mediaFastForward() = sendCommand(SSAPUris.FAST_FORWARD)
    
    // Notificaciones
    fun showToast(message: String) = sendCommand(SSAPUris.CREATE_TOAST, JsonObject().apply {
        addProperty("message", message)
    })
    
    // Input Source
    fun switchInput(inputId: String) = sendCommand(SSAPUris.SWITCH_INPUT, JsonObject().apply {
        addProperty("inputId", inputId)
    })
    
    // Apps comunes
    fun openNetflix() = launchApp(AppIds.NETFLIX)
    fun openYouTube() = launchApp(AppIds.YOUTUBE)
    fun openAmazon() = launchApp(AppIds.AMAZON)
    fun openDisneyPlus() = launchApp(AppIds.DISNEY_PLUS)
    fun openSpotify() = launchApp(AppIds.SPOTIFY)
    fun openBrowser() = launchApp(AppIds.BROWSER)
    fun openLiveTV() = launchApp(AppIds.LIVE_TV)
}
