# 🚀 GUÍA DE SUBIDA A GITHUB

## Pasos para subir el proyecto y obtener APK compilado

### 1. Instalar Git (si no lo tienes)

```bash
pkg install git
git config --global user.name "Tu Nombre"
git config --global user.email "tu@email.com"
```

### 2. Crear repositorio en GitHub

1. Ve a https://github.com
2. Click en "New repository"
3. Nombre: `veamos-que-sale`
4. Descripción: "no se xd"
5. **Public** (para usar GitHub Actions gratis)
6. **NO** marcar "Initialize with README"
7. Click "Create repository"

### 3. Preparar proyecto local

```bash
cd /data/data/com.termux/files/home/lg_webos_rooting/lg_remote_android_v2

# Inicializar git
git init

# Agregar todos los archivos
git add .

# Primer commit
git commit -m "Initial commit - LG ThinQ Remote v2.0"
```

### 4. Conectar con GitHub

```bash
# Reemplaza TU_USUARIO con tu usuario de GitHub
git remote add origin https://github.com/TU_USUARIO/veamos-que-sale.git

# Cambiar a branch main (GitHub usa main por defecto)
git branch -M main

# Subir código
git push -u origin main
```

**Nota**: GitHub te pedirá autenticación. Necesitarás un **Personal Access Token**:

1. Ve a GitHub → Settings → Developer settings → Personal access tokens → Tokens (classic)
2. Click "Generate new token (classic)"
3. Nombre: "Termux Git"
4. Permisos: Marca `repo` (completo) y `workflow`
5. Click "Generate token"
6. **Copia el token** (solo se muestra una vez)
7. Cuando git pida password, usa el token (no tu password de GitHub)

### 5. GitHub Actions compilará automáticamente

Una vez subido, GitHub Actions:
- ✅ Detectará el workflow en `.github/workflows/build.yml`
- ✅ Compilará el APK automáticamente
- ✅ Creará una Release con los APKs
- ✅ Subirá los archivos a la sección Releases

### 6. Descargar APK

1. Ve a tu repositorio en GitHub
2. Click en "Actions" → Verás el build en progreso
3. Cuando termine (≈5-10 minutos):
   - Click en "Releases" (sidebar derecho)
   - Descarga `app-debug.apk`
4. Instala en tu Android

## 📱 Actualizar el README en GitHub

Después de subir, edita el README desde GitHub web:

1. Ve a tu repositorio
2. Click en `README_GITHUB.md`
3. Click en el lápiz (Edit)
4. Reemplaza `TU_USUARIO` con tu nombre de usuario de GitHub
5. Renombra el archivo a `README.md`
6. Commit changes

## 🔄 Futuras actualizaciones

Cuando hagas cambios al código:

```bash
cd /data/data/com.termux/files/home/lg_webos_rooting/lg_remote_android_v2

# Agregar cambios
git add .

# Commit
git commit -m "Descripción de cambios"

# Subir
git push
```

GitHub Actions compilará automáticamente y creará una nueva Release.

## ⚡ Trigger manual del build

Si quieres compilar sin hacer cambios:

1. Ve a tu repositorio en GitHub
2. Click en "Actions"
3. Click en "Android CI - Build APK"
4. Click en "Run workflow"
5. Click en "Run workflow" (botón verde)

## 📊 Ver progreso del build

1. Ve a "Actions" en tu repositorio
2. Click en el workflow más reciente
3. Verás cada paso del build en tiempo real
4. Cuando termine, los APKs estarán en "Releases"

## ⚠️ Solución de problemas

### "Permission denied (publickey)"
- Usa HTTPS en lugar de SSH: `https://github.com/USUARIO/REPO.git`
- O configura SSH keys en GitHub

### "Authentication failed"
- Usa un Personal Access Token en lugar de tu password
- Asegúrate de que el token tiene permisos de `repo` y `workflow`

### "Build failed"
- Ve a Actions y revisa los logs
- Generalmente es un problema de dependencias (se resuelve solo)
- Intenta re-run el workflow

---

**¡Listo! Tu app se compilará en la nube y obtendrás el APK en menos de 10 minutos.**
