# 📱 LG ThinQ Remote v2.0 - RoRo Edition

[![Android CI](https://github.com/TU_USUARIO/veamos-que-sale/actions/workflows/build.yml/badge.svg)](https://github.com/TU_USUARIO/veamos-que-sale/actions/workflows/build.yml)
[![License](https://img.shields.io/badge/License-Educational-blue.svg)](LICENSE)
[![Version](https://img.shields.io/badge/Version-2.0-brightgreen.svg)](https://github.com/TU_USUARIO/veamos-que-sale/releases)

Control remoto completo para LG webOS TV basado en análisis del APK oficial de LG ThinQ.

## 🚀 Descargar APK

**[📥 Última versión - Releases](https://github.com/TU_USUARIO/veamos-que-sale/releases/latest)**

El APK se compila automáticamente con GitHub Actions cada vez que se hace un push.

## ✨ Características

- ✅ **Auto-discovery SSDP** - Encuentra TVs automáticamente en la red
- ✅ **Pairing persistente** - Solo emparejas una vez con client-key
- ✅ **65+ comandos SSAP** - Control completo del TV
- ✅ **Multi-TV** - Guarda y cambia entre múltiples TVs
- ✅ **UI moderna** - Diseño inspirado en LG ThinQ oficial

### Controles Disponibles
- Power On/Off
- Volumen Up/Down/Mute
- Canales Up/Down
- Home, Apps (Netflix, YouTube, Amazon)
- Control de teclado (IME)
- Media controls (play, pause, stop)
- Y más...

## 📖 Cómo Usar

1. **Descargar APK** desde [Releases](https://github.com/TU_USUARIO/veamos-que-sale/releases/latest)
2. **Instalar** en tu dispositivo Android
3. **Conectar a la misma WiFi** que tu TV LG
4. **Buscar TV** automáticamente o ingresar IP manualmente
5. **Aceptar pairing** en el TV cuando aparezca el diálogo
6. **¡Listo!** - El client-key se guarda automáticamente

Ver [GUIA_RAPIDA.md](GUIA_RAPIDA.md) para más detalles.

## 🏗️ Arquitectura

Basado en análisis del APK oficial de LG ThinQ:

- **Puerto WebSocket**: 3000 (NO 3001)
- **Protocolo**: SSAP (Simple Service Access Protocol)
- **Discovery**: SSDP multicast
- **Pairing**: client-key con confirmación visual

Ver [README.md](README.md) para documentación técnica completa.

## 🔧 Build Local

```bash
# Clonar repositorio
git clone https://github.com/TU_USUARIO/veamos-que-sale.git
cd veamos-que-sale

# Build con Gradle
./gradlew assembleDebug

# APK generado en:
# app/build/outputs/apk/debug/app-debug.apk
```

## 🤝 Contribuir

Las contribuciones son bienvenidas! Por favor:

1. Fork el proyecto
2. Crea tu feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push al branch (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## 📄 Licencia

Este proyecto es para fines educativos y de investigación.  
No está afiliado con LG Electronics.

## 👨‍💻 Autor

**RoRo** - Basado en análisis del APK oficial LG ThinQ

## 🙏 Agradecimientos

- LG Electronics por la app oficial ThinQ
- Comunidad de webOS por la documentación del protocolo SSAP

---

**⭐ Si te gusta este proyecto, dale una estrella!**
