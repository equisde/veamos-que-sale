package com.roro.lgthinq

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var statusText: TextView
    private lateinit var connectButton: Button
    private lateinit var tvIpInput: EditText
    private lateinit var autoDiscoverButton: Button
    private lateinit var controlsLayout: LinearLayout
    private lateinit var debugLogsButton: Button
    
    private var webOSClient: WebOSClient? = null
    private var isConnected = false
    private lateinit var tvPrefs: TVPreferences
    private val debugLogs = StringBuilder()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            setContentView(R.layout.activity_main)
            
            tvPrefs = TVPreferences(this)
            
            initViews()
            setupListeners()
            
            // Cargar última IP conectada
            val lastIp = tvPrefs.getLastConnectedIP()
            if (lastIp != null) {
                tvIpInput.setText(lastIp)
            }
            
            addDebugLog("✅ App iniciada correctamente")
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al iniciar: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun initViews() {
        statusText = findViewById(R.id.statusText)
        connectButton = findViewById(R.id.connectButton)
        tvIpInput = findViewById(R.id.tvIpInput)
        autoDiscoverButton = findViewById(R.id.autoDiscoverButton)
        controlsLayout = findViewById(R.id.controlsLayout)
        debugLogsButton = findViewById(R.id.debugLogsButton)
        
        controlsLayout.visibility = View.GONE
    }

    private fun setupListeners() {
        connectButton.setOnClickListener {
            if (isConnected) {
                disconnect()
            } else {
                connect()
            }
        }
        
        autoDiscoverButton.setOnClickListener {
            autoDiscover()
        }
        
        debugLogsButton.setOnClickListener {
            showDebugLogs()
        }
        
        // Controles - verificar que existan antes de asignar listeners
        findViewById<Button>(R.id.btnPower)?.setOnClickListener { 
            lifecycleScope.launch(Dispatchers.IO) {
                webOSClient?.powerOff() 
            }
        }
        findViewById<Button>(R.id.btnHome)?.setOnClickListener { 
            lifecycleScope.launch(Dispatchers.IO) {
                webOSClient?.home() 
            }
        }
        
        findViewById<Button>(R.id.btnVolUp)?.setOnClickListener { 
            lifecycleScope.launch(Dispatchers.IO) {
                webOSClient?.volumeUp() 
            }
        }
        findViewById<Button>(R.id.btnVolDown)?.setOnClickListener { 
            lifecycleScope.launch(Dispatchers.IO) {
                webOSClient?.volumeDown() 
            }
        }
        findViewById<Button>(R.id.btnMute)?.setOnClickListener { 
            lifecycleScope.launch(Dispatchers.IO) {
                webOSClient?.volumeMute(true) 
            }
        }
        
        findViewById<Button>(R.id.btnChUp)?.setOnClickListener { 
            lifecycleScope.launch(Dispatchers.IO) {
                webOSClient?.channelUp() 
            }
        }
        findViewById<Button>(R.id.btnChDown)?.setOnClickListener { 
            lifecycleScope.launch(Dispatchers.IO) {
                webOSClient?.channelDown() 
            }
        }
        
        findViewById<Button>(R.id.btnNetflix)?.setOnClickListener { 
            lifecycleScope.launch(Dispatchers.IO) {
                webOSClient?.openNetflix() 
            }
        }
        findViewById<Button>(R.id.btnYouTube)?.setOnClickListener { 
            lifecycleScope.launch(Dispatchers.IO) {
                webOSClient?.openYouTube() 
            }
        }
        findViewById<Button>(R.id.btnAmazon)?.setOnClickListener { 
            lifecycleScope.launch(Dispatchers.IO) {
                webOSClient?.openAmazon() 
            }
        }
    }

    private fun autoDiscover() {
        statusText.text = "Buscando TVs en la red..."
        autoDiscoverButton.isEnabled = false
        addDebugLog("🔍 Iniciando auto-discovery SSDP...")
        
        lifecycleScope.launch {
            try {
                val tvs = withContext(Dispatchers.IO) {
                    val discovery = SSDPDiscovery()
                    discovery.discoverTVs()
                }
                
                addDebugLog("📡 Descubrimiento completado: ${tvs.size} TV(s) encontrado(s)")
                
                if (tvs.isEmpty()) {
                    statusText.text = "No se encontraron TVs"
                    Toast.makeText(this@MainActivity, "No se encontraron TVs LG", Toast.LENGTH_SHORT).show()
                    addDebugLog("❌ No se encontraron TVs en la red")
                } else if (tvs.size == 1) {
                    tvIpInput.setText(tvs[0])
                    statusText.text = "TV encontrado: ${tvs[0]}"
                    addDebugLog("✅ TV encontrado: ${tvs[0]}")
                } else {
                    addDebugLog("📋 TVs encontrados: ${tvs.joinToString()}")
                    showTVSelectionDialog(tvs)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                statusText.text = "Error: ${e.message}"
                Toast.makeText(this@MainActivity, "Error de búsqueda: ${e.message}", Toast.LENGTH_SHORT).show()
                addDebugLog("❌ Error en auto-discovery: ${e.message}")
            } finally {
                autoDiscoverButton.isEnabled = true
            }
        }
    }

    private fun showTVSelectionDialog(tvs: List<String>) {
        AlertDialog.Builder(this)
            .setTitle("Selecciona tu TV")
            .setItems(tvs.toTypedArray()) { _, which ->
                tvIpInput.setText(tvs[which])
                statusText.text = "TV seleccionado: ${tvs[which]}"
            }
            .show()
    }

    private fun connect() {
        val ip = tvIpInput.text.toString().trim()
        if (ip.isEmpty()) {
            Toast.makeText(this, "Ingresa la IP del TV", Toast.LENGTH_SHORT).show()
            addDebugLog("⚠️ IP vacía, no se puede conectar")
            return
        }
        
        statusText.text = "Conectando a $ip..."
        connectButton.isEnabled = false
        addDebugLog("🔌 Iniciando conexión a $ip...")
        
        lifecycleScope.launch {
            try {
                // Obtener TV guardado con su client-key
                val savedTV = tvPrefs.getTVByIP(ip)
                val savedClientKey = savedTV?.clientKey
                
                if (savedClientKey != null) {
                    addDebugLog("🔑 Client-key encontrado para $ip")
                } else {
                    addDebugLog("📝 Primera conexión a $ip, se requiere pairing")
                }
                
                webOSClient = WebOSClient(
                    tvIp = ip,
                    onConnected = {
                        runOnUiThread {
                            isConnected = true
                            statusText.text = "Conectado a $ip"
                            connectButton.text = "Desconectar"
                            connectButton.isEnabled = true
                            controlsLayout.visibility = View.VISIBLE
                            
                            // Guardar IP como última conectada
                            tvPrefs.setLastConnectedIP(ip)
                            addDebugLog("✅ Conectado exitosamente a $ip")
                            Toast.makeText(this@MainActivity, "✅ Conectado!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onDisconnected = {
                        runOnUiThread {
                            isConnected = false
                            statusText.text = "Desconectado"
                            connectButton.text = "Conectar"
                            connectButton.isEnabled = true
                            controlsLayout.visibility = View.GONE
                            addDebugLog("❌ Desconectado de $ip")
                        }
                    },
                    onPairingRequired = { pinCode ->
                        runOnUiThread {
                            if (pinCode != null) {
                                statusText.text = "🔢 CÓDIGO PIN EN TV: $pinCode"
                                addDebugLog("🔢 PIN recibido del TV: $pinCode")
                                AlertDialog.Builder(this@MainActivity)
                                    .setTitle("📺 Pairing Requerido")
                                    .setMessage("Verifica que el código PIN en tu TV sea:\n\n$pinCode")
                                    .setPositiveButton("OK", null)
                                    .show()
                            } else {
                                statusText.text = "Acepta el pairing en el TV"
                                addDebugLog("⏳ Esperando aceptación de pairing en TV")
                                Toast.makeText(this@MainActivity, "Acepta la solicitud de pairing en tu TV", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    onRegistered = { clientKey ->
                        // Guardar client-key en el TV
                        tvPrefs.updateClientKey(ip, clientKey)
                        runOnUiThread {
                            addDebugLog("🔐 Client-key guardado: ${clientKey.take(10)}...")
                            Toast.makeText(this@MainActivity, "TV emparejado correctamente", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onError = { error ->
                        runOnUiThread {
                            statusText.text = "Error: $error"
                            connectButton.isEnabled = true
                            addDebugLog("❌ Error: $error")
                            Toast.makeText(this@MainActivity, "Error: $error", Toast.LENGTH_LONG).show()
                        }
                    },
                    onLog = { log ->
                        runOnUiThread {
                            addDebugLog(log)
                        }
                    }
                )
                
                withContext(Dispatchers.IO) {
                    webOSClient?.connect(savedClientKey)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    statusText.text = "Error: ${e.message}"
                    connectButton.isEnabled = true
                    addDebugLog("💥 Excepción: ${e.message}\n${e.stackTraceToString()}")
                    Toast.makeText(this@MainActivity, "Error de conexión: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun disconnect() {
        lifecycleScope.launch(Dispatchers.IO) {
            webOSClient?.disconnect()
            withContext(Dispatchers.Main) {
                webOSClient = null
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disconnect()
    }
    
    private fun addDebugLog(message: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        debugLogs.append("[$timestamp] $message\n")
    }
    
    private fun showDebugLogs() {
        val logs = if (debugLogs.isEmpty()) "No hay logs aún" else debugLogs.toString()
        
        AlertDialog.Builder(this)
            .setTitle("🔍 Debug Logs")
            .setMessage(logs)
            .setPositiveButton("Copiar al Clipboard") { _, _ ->
                copyToClipboard(logs)
            }
            .setNegativeButton("Cerrar", null)
            .show()
    }
    
    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        
        // Obtener contenido actual del clipboard
        val currentClip = clipboard.primaryClip
        val currentText = if (currentClip != null && currentClip.itemCount > 0) {
            currentClip.getItemAt(0).text?.toString() ?: ""
        } else {
            ""
        }
        
        // Agregar nuevo contenido al existente
        val newText = if (currentText.isNotEmpty()) {
            "$currentText\n\n=== NUEVOS LOGS ===\n$text"
        } else {
            text
        }
        
        val clip = ClipData.newPlainText("Debug Logs", newText)
        clipboard.setPrimaryClip(clip)
        
        Toast.makeText(this, "✅ Logs copiados al clipboard", Toast.LENGTH_SHORT).show()
        addDebugLog("📋 Logs copiados al clipboard")
    }
}
