#!/bin/bash

# ==============================================================================
# LG Remote Android - Build Script para Termux
# ==============================================================================
# Script adaptado de experimental_build.sh para compilar la app Android
# en el entorno Termux con workarounds necesarios.
# ==============================================================================

set -e

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"

echo "╔═══════════════════════════════════════════════════════════╗"
echo "║                                                           ║"
echo "║   LG Remote Android - Build Script                       ║"
echo "║   Compilando app en Termux                               ║"
echo "║                                                           ║"
echo "╚═══════════════════════════════════════════════════════════╝"
echo ""

# --- Configuration ---
SYSTEM_AAPT2_PATH="/data/data/com.termux/files/usr/bin/aapt2"
BUILD_TYPE="${1:-assembleDebug}"

# --- Verificar dependencias ---
echo "[1/5] Verificando dependencias..."

if ! command -v aapt2 &> /dev/null; then
    echo "⚠️  aapt2 no está instalado"
    echo "   Instalando aapt2..."
    pkg install -y aapt2
fi

if [ ! -f "$SYSTEM_AAPT2_PATH" ]; then
    echo "❌ ERROR: aapt2 no encontrado en $SYSTEM_AAPT2_PATH"
    echo "   Instala con: pkg install aapt2"
    exit 1
fi

if ! command -v gradle &> /dev/null; then
    echo "⚠️  Gradle no está en PATH"
    echo "   Usando gradlew incluido en el proyecto..."
fi

echo "✓ Dependencias verificadas"
echo ""

# --- Crear gradlew si no existe ---
echo "[2/5] Configurando Gradle wrapper..."

if [ ! -f "gradlew" ]; then
    echo "   Creando gradlew..."
    cat > gradlew << 'EOFGW'
#!/bin/sh

##############################################################################
# Gradle startup script for UN*X
##############################################################################

# Attempt to set APP_HOME
SAVED="`pwd`"
cd "`dirname \"$0\"`"
APP_HOME="`pwd -P`"
cd "$SAVED"

APP_NAME="Gradle"
APP_BASE_NAME=`basename "$0"`

GRADLE_USER_HOME="${GRADLE_USER_HOME:-$HOME/.gradle}"

# Use gradle from PATH or local wrapper
if command -v gradle &> /dev/null; then
    exec gradle "$@"
else
    echo "Gradle not found. Please install: pkg install gradle"
    exit 1
fi
EOFGW
    chmod +x gradlew
fi

echo "✓ Gradle wrapper listo"
echo ""

# --- Cleanup Function ---
cleanup() {
    echo ""
    echo "[*] Limpiando symlinks temporales..."
    find "$HOME/.gradle/caches" -type f -name "aapt2.original" 2>/dev/null | while read -r backup_file; do
        aapt2_dir=$(dirname "$backup_file")
        aapt2_path="$aapt2_dir/aapt2"
        if [ -L "$aapt2_path" ]; then
            rm -f "$aapt2_path"
            mv "$backup_file" "$aapt2_path"
        fi
    done
    echo "✓ Cleanup completado"
}

trap cleanup EXIT

# --- Build inicial para descargar dependencias ---
echo "[3/5] Descargando dependencias de Gradle..."
echo "   (Esto puede tomar varios minutos la primera vez)"
echo ""

./gradlew --version 2>/dev/null || {
    echo "⚠️  Instalando Gradle..."
    pkg install -y gradle
}

# Primer build para forzar descarga de aapt2 de Gradle
./gradlew clean 2>/dev/null || true
./gradlew $BUILD_TYPE --stacktrace 2>&1 | head -50 || echo "   Descarga de dependencias iniciada..."

echo ""
echo "✓ Dependencias descargadas"
echo ""

# --- Aplicar workaround de aapt2 ---
echo "[4/5] Aplicando workaround de aapt2..."

GRADLE_AAPT2_PATHS=$(find "$HOME/.gradle/caches" -type f -name "aapt2" -executable 2>/dev/null | grep "transforms" || true)

if [ -z "$GRADLE_AAPT2_PATHS" ]; then
    echo "⚠️  No se encontraron binarios de aapt2 en cache de Gradle"
    echo "   Continuando sin workaround..."
else
    echo "   Reemplazando aapt2 de Gradle con versión de Termux..."
    
    echo "$GRADLE_AAPT2_PATHS" | while read -r aapt2_path; do
        if [ -f "$aapt2_path" ]; then
            aapt2_dir=$(dirname "$aapt2_path")
            orig_path="$aapt2_dir/aapt2.original"
            
            if [ ! -f "$orig_path" ]; then
                mv "$aapt2_path" "$orig_path"
            else
                rm -f "$aapt2_path"
            fi
            
            ln -sf "$SYSTEM_AAPT2_PATH" "$aapt2_path"
            echo "   ✓ Symlink creado: $aapt2_path"
        fi
    done
    
    echo "✓ Workaround aplicado"
fi

echo ""

# --- Build final ---
echo "[5/5] Compilando aplicación..."
echo "   Tarea: $BUILD_TYPE"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

./gradlew $BUILD_TYPE --stacktrace
BUILD_EXIT_CODE=$?

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# --- Resultado ---
if [ $BUILD_EXIT_CODE -eq 0 ]; then
    echo "╔═══════════════════════════════════════════════════════════╗"
    echo "║                                                           ║"
    echo "║         ✓✓✓ COMPILACIÓN EXITOSA ✓✓✓                      ║"
    echo "║                                                           ║"
    echo "╚═══════════════════════════════════════════════════════════╝"
    echo ""
    
    # Buscar APK generado
    APK_PATH=$(find app/build/outputs/apk -name "*.apk" 2>/dev/null | head -1)
    
    if [ -n "$APK_PATH" ]; then
        APK_SIZE=$(du -h "$APK_PATH" | cut -f1)
        echo "📦 APK generado:"
        echo "   Ubicación: $APK_PATH"
        echo "   Tamaño: $APK_SIZE"
        echo ""
        echo "Para instalar en tu dispositivo:"
        echo "   1. Copia el APK a tu dispositivo Android"
        echo "   2. Habilita 'Fuentes desconocidas' en configuración"
        echo "   3. Abre el APK e instala"
    else
        echo "⚠️  APK no encontrado en ubicación esperada"
    fi
else
    echo "╔═══════════════════════════════════════════════════════════╗"
    echo "║                                                           ║"
    echo "║         ✗✗✗ COMPILACIÓN FALLIDA ✗✗✗                      ║"
    echo "║                                                           ║"
    echo "╚═══════════════════════════════════════════════════════════╝"
    echo ""
    echo "Posibles soluciones:"
    echo "  1. Ejecuta de nuevo: bash build_android.sh"
    echo "  2. Limpia cache: ./gradlew clean"
    echo "  3. Revisa logs arriba para más detalles"
fi

echo ""
exit $BUILD_EXIT_CODE
