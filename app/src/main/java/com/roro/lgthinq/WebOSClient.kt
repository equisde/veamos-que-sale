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
                onLog("✅ WebSocket conectado - HTTP ${handshake?.httpStatus}")
                register()
            }

            override fun onMessage(message: String?) {
                Log.d(TAG, "📩 Mensaje recibido: $message")
                onLog("📩 Mensaje del TV: ${message?.take(100)}...")
                handleMessage(message)
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                Log.d(TAG, "❌ WebSocket cerrado - Code: $code, Reason: $reason, Remote: $remote")
                onLog("❌ WebSocket cerrado - Code: $code, Reason: ${reason ?: "sin razón"}, Remote: $remote")
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
            webSocket?.addHeader("Origin", "file://")
            webSocket?.addHeader("Sec-WebSocket-Protocol", "lge-tv")
            
            webSocket?.setConnectionLostTimeout(CONNECTION_TIMEOUT / 1000)
            
            onLog("🚀 Iniciando conexión...")
            webSocket?.connect()
            
        } catch (e: Exception) {
            Log.e(TAG, "💥 Excepción al intentar conectar", e)
            onLog("💥 Excepción al conectar: ${e.javaClass.simpleName} - ${e.message}")
            onError("Error iniciando conexión: ${e.message}")
        }
    }

    private fun register() {
        Log.d(TAG, "📤 Enviando mensaje de registro (pairingType: PIN)")
        onLog("📤 Enviando registro con pairingType: PIN")
        if (!clientKey.isNullOrEmpty()) {
            Log.d(TAG, "🔑 Usando client-key guardado: ${clientKey?.take(10)}...")
            onLog("🔑 Usando client-key guardado")
        }
        
        val registerMsg = JsonObject().apply {
            addProperty("type", "register")
            if (!clientKey.isNullOrEmpty()) {
                addProperty("client-key", clientKey)
            }
            add("payload", JsonObject().apply {
                addProperty("forcePairing", false)
                addProperty("pairingType", "PIN")
                add("manifest", JsonObject().apply {
                    addProperty("manifestVersion", 1)
                    addProperty("appVersion", "2.0")
                    add("signed", JsonObject().apply {
                        addProperty("created", "20241207")
                        addProperty("appId", "com.roro.lgthinq")
                        addProperty("vendorId", "com.roro")
                        add("localizedAppNames", JsonObject().apply {
                            addProperty("", "LG ThinQ RoRo")
                            addProperty("es-ES", "LG ThinQ RoRo")
                        })
                        add("localizedVendorNames", JsonObject().apply {
                            addProperty("", "RoRo")
                        })
                        add("permissions", gson.toJsonTree(listOf(
                            "TEST_SECURE",
                            "CONTROL_INPUT_TEXT",
                            "CONTROL_MOUSE_AND_KEYBOARD",
                            "READ_INSTALLED_APPS",
                            "READ_LGE_SDX",
                            "READ_NOTIFICATIONS",
                            "SEARCH",
                            "WRITE_SETTINGS",
                            "WRITE_NOTIFICATION_ALERT",
                            "CONTROL_POWER",
                            "READ_CURRENT_CHANNEL",
                            "READ_RUNNING_APPS",
                            "READ_UPDATE_INFO",
                            "UPDATE_FROM_REMOTE_APP",
                            "READ_LGE_TV_INPUT_EVENTS",
                            "READ_TV_CURRENT_TIME"
                        )))
                        addProperty("serial", "2f930e2d2cfe083771f68e4fe7bb07")
                    })
                })
            })
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
