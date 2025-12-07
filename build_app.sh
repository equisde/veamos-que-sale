#!/data/data/com.termux/files/usr/bin/bash

set -e

PROJECT_DIR="$PWD"
APP_NAME="LGThinQRoRo"

echo "╔════════════════════════════════════════╗"
echo "║   Building $APP_NAME v2.0              ║"
echo "╚════════════════════════════════════════╝"

# Verificar gradle wrapper
if [ ! -f "gradlew" ]; then
    echo "[+] Creando Gradle Wrapper..."
    gradle wrapper --gradle-version 8.4
fi

# Asegurar permisos
chmod +x gradlew

# Limpiar build anterior
echo "[+] Limpiando builds anteriores..."
./gradlew clean --no-daemon 2>/dev/null || true

# Build APK
echo "[+] Compilando APK..."
./gradlew assembleDebug --no-daemon --stacktrace

APK_PATH="app/build/outputs/apk/debug/app-debug.apk"

if [ -f "$APK_PATH" ]; then
    echo ""
    echo "╔════════════════════════════════════════╗"
    echo "║         ✓ BUILD EXITOSO                ║"
    echo "╚════════════════════════════════════════╝"
    echo ""
    echo "[+] APK ubicado en: $APK_PATH"
    
    # Copiar a /sdcard
    echo "[+] Copiando a /sdcard..."
    cp "$APK_PATH" /sdcard/lgthinqroro_v2.apk
    echo "[+] APK copiado a: /sdcard/lgthinqroro_v2.apk"
    
    # Intentar instalar
    echo ""
    read -p "¿Deseas instalar la app ahora? (s/n): " install
    if [ "$install" = "s" ] || [ "$install" = "S" ]; then
        echo "[+] Instalando..."
        pm install -r /sdcard/lgthinqroro_v2.apk && echo "✓ App instalada!" || echo "✗ Error instalando"
    fi
else
    echo ""
    echo "╔════════════════════════════════════════╗"
    echo "║           ✗ BUILD FALLIDO              ║"
    echo "╚════════════════════════════════════════╝"
    exit 1
fi
