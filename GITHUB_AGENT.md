# 🤖 GITHUB ACTIONS AGENT

Agente interactivo para gestionar GitHub Actions desde la terminal.

## 🚀 Uso

### Modo Interactivo (Menú)
```bash
./github_agent.sh
```

### Modo Comando Único
```bash
./github_agent.sh list              # Listar workflows
./github_agent.sh runs              # Ver últimas ejecuciones
./github_agent.sh details <run_id>  # Ver detalles de ejecución
./github_agent.sh trigger           # Ejecutar workflow manualmente
./github_agent.sh download          # Descargar artefactos/APKs
./github_agent.sh watch             # Monitorear build en tiempo real
```

## 📋 Funcionalidades

### 1. 📋 Listar Workflows
Muestra todos los workflows configurados en el repositorio.

### 2. 🔍 Ver Últimas Ejecuciones
Lista las últimas 10 ejecuciones con:
- ID de ejecución
- Estado (✅ success, ❌ failed, 🔄 en progreso)
- Nombre del workflow
- Mensaje del commit

### 3. 📊 Ver Detalles de Ejecución
Muestra información detallada de una ejecución específica:
- ID y nombre del workflow
- Estado y conclusión
- Branch y commit
- Fecha de creación
- URL directa

### 4. 🚀 Ejecutar Workflow Manualmente
Permite ejecutar cualquier workflow con `workflow_dispatch` configurado.

### 5. 📥 Descargar Artefactos
Lista y descarga artefactos generados (APKs, etc.):
- Muestra tamaño y fecha
- Descarga como ZIP
- Guarda en el directorio actual

### 6. 👀 Monitorear Build Actual
Monitorea en tiempo real el último build:
- Actualización automática cada 10 segundos
- Muestra progreso y estado
- Notifica cuando completa

### 7. 🌐 Abrir en Navegador
Abre directamente en el navegador:
- Repositorio principal
- Actions
- Releases
- Issues

### 8. 🔄 Actualizar Credenciales
Permite cambiar usuario y token sin reiniciar.

## 🔑 Credenciales

El agente necesita:
1. **Usuario de GitHub**: Tu nombre de usuario
2. **Personal Access Token**: Con permisos `repo` + `workflow`

Se pedirán la primera vez y se guardan en la sesión.

## 💡 Ejemplos de Uso

### Ver si hay builds en progreso
```bash
./github_agent.sh runs
```

### Monitorear el build actual hasta que termine
```bash
./github_agent.sh watch
```

### Descargar el último APK generado
```bash
./github_agent.sh download
```

### Ejecutar build manualmente
```bash
./github_agent.sh trigger
```

## 🎯 Casos de Uso

### Después de hacer push
```bash
./github_agent.sh watch
# Monitorea el build hasta que termine
```

### Descargar APK cuando esté listo
```bash
./github_agent.sh download
# Selecciona el APK más reciente
```

### Ver por qué falló un build
```bash
./github_agent.sh runs
# Copia el ID del build fallido
./github_agent.sh details <ID>
# Ve los detalles y el enlace a los logs
```

## 🔧 Requisitos

- `curl`: Para llamadas a la API
- `python3`: Para parsear JSON
- Personal Access Token de GitHub

## 📝 Notas

- Las credenciales se piden solo una vez por sesión
- Los artefactos se descargan como ZIP
- El modo watch se actualiza cada 10 segundos
- Puedes usar Ctrl+C para salir del modo watch

## 🎨 Interfaz

El agente usa colores para facilitar la lectura:
- 🔵 Azul: Información
- 🟢 Verde: Éxito
- 🔴 Rojo: Error
- 🟡 Amarillo: Advertencia
- 🟣 Morado: Títulos

## 🚀 Flujo Típico

1. Subir código: `./subir_automatico.sh`
2. Monitorear: `./github_agent.sh watch`
3. Cuando termine: `./github_agent.sh download`
4. ¡APK listo!

---

**Creado para**: veamos-que-sale  
**Compatible con**: GitHub API v3  
**Plataforma**: Termux/Linux
