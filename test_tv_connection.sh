#!/bin/bash

# Script para probar conectividad con TV LG webOS

if [ -z "$1" ]; then
    echo "Uso: ./test_tv_connection.sh IP_DEL_TV"
    echo "Ejemplo: ./test_tv_connection.sh 192.168.1.5"
    exit 1
fi

TV_IP=$1

echo "🔍 Probando conectividad con TV LG webOS en $TV_IP"
echo "================================================"
echo ""

# Test 1: Ping
echo "1️⃣ Test de PING..."
if ping -c 3 $TV_IP > /dev/null 2>&1; then
    echo "   ✅ TV responde a ping"
else
    echo "   ❌ TV no responde a ping"
    echo "   → Verifica que el TV esté encendido y en la misma red"
    exit 1
fi
echo ""

# Test 2: Puerto 3000 (WebSocket principal)
echo "2️⃣ Test de puerto 3000 (WebSocket)..."
if timeout 5 bash -c "echo > /dev/tcp/$TV_IP/3000" 2>/dev/null; then
    echo "   ✅ Puerto 3000 está ABIERTO"
else
    echo "   ❌ Puerto 3000 está CERRADO"
    echo "   → El TV necesita habilitar 'LG Connect Apps'"
fi
echo ""

# Test 3: Puerto 3001 (WebSocket alternativo)
echo "3️⃣ Test de puerto 3001 (WebSocket alternativo)..."
if timeout 5 bash -c "echo > /dev/tcp/$TV_IP/3001" 2>/dev/null; then
    echo "   ✅ Puerto 3001 está ABIERTO"
else
    echo "   ❌ Puerto 3001 está CERRADO"
fi
echo ""

# Test 4: Puerto 9998 (REST API)
echo "4️⃣ Test de puerto 9998 (REST API)..."
if timeout 5 bash -c "echo > /dev/tcp/$TV_IP/9998" 2>/dev/null; then
    echo "   ✅ Puerto 9998 está ABIERTO"
else
    echo "   ❌ Puerto 9998 está CERRADO"
fi
echo ""

# Test 5: SSDP Discovery
echo "5️⃣ Test de SSDP Discovery..."
echo "   Buscando TVs en la red con SSDP..."
timeout 6 nmap -sU -p 1900 --script=broadcast-upnp-info $TV_IP 2>/dev/null | grep -q "webOS" && echo "   ✅ TV responde a SSDP" || echo "   ⚠️  SSDP no detectado (normal)"
echo ""

echo "================================================"
echo "📋 RESUMEN:"
echo ""

# Determinar el problema
if timeout 5 bash -c "echo > /dev/tcp/$TV_IP/3000" 2>/dev/null; then
    echo "✅ El TV está LISTO para conectar"
    echo "   → La app debería funcionar correctamente"
else
    echo "❌ El TV NO está listo"
    echo ""
    echo "🔧 SOLUCIÓN:"
    echo "   1. En el TV, ve a: Settings → General → Mobile TV On"
    echo "   2. Habilita 'LG Connect Apps'"
    echo "   3. Reinicia el TV si es necesario"
    echo ""
    echo "   O intenta con otro TV LG webOS de modelo más reciente"
fi
echo ""

