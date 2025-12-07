package com.roro.lgthinq

import android.util.Log
import kotlinx.coroutines.*
import java.net.*

class SSDPDiscovery {
    companion object {
        private const val TAG = "SSDPDiscovery"
        private const val SSDP_ADDRESS = "239.255.255.250"
        private const val SSDP_PORT = 1900
        private const val SEARCH_TARGET = "urn:lge-com:service:webos-second-screen:1"
        private const val TIMEOUT_MS = 5000
    }

    suspend fun discoverTVs(): List<String> = withContext(Dispatchers.IO) {
        val tvIPs = mutableListOf<String>()
        
        try {
            val socket = MulticastSocket()
            socket.soTimeout = TIMEOUT_MS
            
            val searchMsg = buildSearchMessage()
            val group = InetAddress.getByName(SSDP_ADDRESS)
            val packet = DatagramPacket(
                searchMsg.toByteArray(),
                searchMsg.length,
                group,
                SSDP_PORT
            )
            
            Log.d(TAG, "Enviando búsqueda SSDP...")
            socket.send(packet)
            
            // Recibir respuestas
            val buffer = ByteArray(1024)
            val startTime = System.currentTimeMillis()
            
            while (System.currentTimeMillis() - startTime < TIMEOUT_MS) {
                try {
                    val receivePacket = DatagramPacket(buffer, buffer.size)
                    socket.receive(receivePacket)
                    
                    val response = String(receivePacket.data, 0, receivePacket.length)
                    val ip = extractIP(response)
                    
                    if (ip != null && !tvIPs.contains(ip)) {
                        Log.d(TAG, "TV encontrado: $ip")
                        tvIPs.add(ip)
                    }
                } catch (e: SocketTimeoutException) {
                    break
                }
            }
            
            socket.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error en descubrimiento SSDP", e)
        }
        
        tvIPs
    }

    private fun buildSearchMessage(): String {
        return """M-SEARCH * HTTP/1.1
Host: $SSDP_ADDRESS:$SSDP_PORT
Man: "ssdp:discover"
MX: 3
ST: $SEARCH_TARGET


"""
    }

    private fun extractIP(response: String): String? {
        // Buscar LOCATION header
        val lines = response.split("\r\n", "\n")
        for (line in lines) {
            if (line.startsWith("LOCATION:", ignoreCase = true)) {
                val url = line.substringAfter(":")
                    .trim()
                    .removePrefix("http://")
                    .substringBefore(":")
                return url
            }
        }
        return null
    }
}
