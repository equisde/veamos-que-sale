#!/bin/bash

# LG Remote Android V2 - Build Script
# RoRo Edition - 2024-12-07

echo "========================================="
echo "  LG ThinQ Remote v2.0 - Build Script"
echo "========================================="
echo ""

PROJECT_DIR="/data/data/com.termux/files/home/lg_webos_rooting/lg_remote_android_v2"
cd "$PROJECT_DIR" || exit 1

echo "📂 Directorio del proyecto: $PROJECT_DIR"
echo ""

# Verificar archivos clave
echo "🔍 Verificando archivos del proyecto..."
FILES_TO_CHECK=(
    "app/src/main/java/com/roro/lgthinq/MainActivity.kt"
    "app/src/main/java/com/roro/lgthinq/WebOSClient.kt"
    "app/src/main/java/com/roro/lgthinq/SSDPDiscovery.kt"
    "app/src/main/java/com/roro/lgthinq/Models.kt"
    "app/src/main/java/com/roro/lgthinq/TVPreferences.kt"
    "app/src/main/res/layout/activity_main.xml"
    "app/src/main/AndroidManifest.xml"
    "app/build.gradle"
)

ALL_OK=true
for file in "${FILES_TO_CHECK[@]}"; do
    if [ -f "$file" ]; then
        echo "  ✅ $file"
    else
        echo "  ❌ FALTA: $file"
        ALL_OK=false
    fi
done

if [ "$ALL_OK" = false ]; then
    echo ""
    echo "❌ Faltan archivos necesarios. Abortando build."
    exit 1
fi

echo ""
echo "✅ Todos los archivos necesarios están presentes"
echo ""

# Limpiar builds anteriores
echo "🧹 Limpiando builds anteriores..."
if [ -d "build" ]; then
    rm -rf build
fi
if [ -d "app/build" ]; then
    rm -rf app/build
fi
if [ -d ".gradle" ]; then
    rm -rf .gradle
fi
echo "✅ Limpieza completada"
echo ""

# Verificar Gradle
echo "🔧 Verificando Gradle..."
if command -v gradle &> /dev/null; then
    GRADLE_CMD="gradle"
    echo "✅ Gradle instalado: $(gradle --version | head -1)"
elif [ -f "gradlew" ]; then
    GRADLE_CMD="./gradlew"
    chmod +x gradlew
    echo "✅ Usando Gradle Wrapper"
else
    echo "❌ Gradle no encontrado. Por favor instala Gradle o usa Gradle Wrapper."
    exit 1
fi
echo ""

# Build Debug
echo "🏗️  Iniciando build DEBUG..."
echo "----------------------------------------"
$GRADLE_CMD assembleDebug 2>&1 | tee build_log.txt

if [ $? -eq 0 ]; then
    echo ""
    echo "========================================="
    echo "✅ BUILD EXITOSO!"
    echo "========================================="
    echo ""
    
    # Buscar APK generado
    APK_PATH=$(find app/build/outputs/apk/debug -name "*.apk" 2>/dev/null | head -1)
    
    if [ -n "$APK_PATH" ]; then
        APK_SIZE=$(du -h "$APK_PATH" | cut -f1)
        echo "📦 APK generado:"
        echo "   Ubicación: $APK_PATH"
        echo "   Tamaño: $APK_SIZE"
        echo ""
        
        # Copiar a ubicación accesible
        cp "$APK_PATH" "./LGThinQ_RoRo_v2.0_debug.apk"
        echo "✅ APK copiado a: ./LGThinQ_RoRo_v2.0_debug.apk"
        echo ""
        
        # Mostrar información del APK
        echo "📋 Información del APK:"
        if command -v aapt &> /dev/null; then
            aapt dump badging "$APK_PATH" | grep -E "package:|sdkVersion:|targetSdkVersion:" | head -3
        fi
        echo ""
        
        echo "🎉 ¡Listo para instalar!"
        echo ""
        echo "Para instalar en dispositivo conectado:"
        echo "  adb install -r ./LGThinQ_RoRo_v2.0_debug.apk"
        echo ""
        echo "O transferir a /storage/emulated/0/ para instalar manualmente"
        
    else
        echo "⚠️  APK generado pero no encontrado en ruta esperada"
    fi
    
else
    echo ""
    echo "========================================="
    echo "❌ BUILD FALLÓ"
    echo "========================================="
    echo ""
    echo "Revisa build_log.txt para más detalles"
    exit 1
fi
