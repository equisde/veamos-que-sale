#!/bin/bash

# ==============================================================================
# GITHUB ACTIONS AGENT - Gestor Interactivo de Workflows
# ==============================================================================
# Este script permite interactuar con GitHub Actions de forma fácil
# ==============================================================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_NAME="veamos-que-sale"

# Colores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Funciones de utilidad
print_header() {
    echo -e "${CYAN}╔════════════════════════════════════════════════════════╗${NC}"
    echo -e "${CYAN}║${NC}                                                        ${CYAN}║${NC}"
    echo -e "${CYAN}║${NC}  ${PURPLE}🤖 GITHUB ACTIONS AGENT - $REPO_NAME${NC}       ${CYAN}║${NC}"
    echo -e "${CYAN}║${NC}                                                        ${CYAN}║${NC}"
    echo -e "${CYAN}╚════════════════════════════════════════════════════════╝${NC}"
    echo ""
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

# Verificar credenciales
check_credentials() {
    if [ -z "$GITHUB_USER" ] || [ -z "$GITHUB_TOKEN" ]; then
        echo ""
        print_warning "Necesito tus credenciales de GitHub"
        echo ""
        
        if [ -z "$GITHUB_USER" ]; then
            read -p "Usuario de GitHub: " GITHUB_USER
        fi
        
        if [ -z "$GITHUB_TOKEN" ]; then
            echo ""
            echo "Personal Access Token (https://github.com/settings/tokens/new)"
            echo "Permisos necesarios: repo + workflow"
            read -sp "Token: " GITHUB_TOKEN
            echo ""
        fi
        
        export GITHUB_USER
        export GITHUB_TOKEN
    fi
}

# Verificar si el repo existe
check_repo() {
    check_credentials
    
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" \
        -H "Authorization: token ${GITHUB_TOKEN}" \
        "https://api.github.com/repos/${GITHUB_USER}/${REPO_NAME}")
    
    if [ "$HTTP_CODE" = "200" ]; then
        return 0
    else
        return 1
    fi
}

# Listar workflows
list_workflows() {
    check_credentials
    
    print_info "Listando workflows..."
    echo ""
    
    RESPONSE=$(curl -s \
        -H "Authorization: token ${GITHUB_TOKEN}" \
        -H "Accept: application/vnd.github.v3+json" \
        "https://api.github.com/repos/${GITHUB_USER}/${REPO_NAME}/actions/workflows")
    
    if echo "$RESPONSE" | grep -q '"total_count"'; then
        echo "$RESPONSE" | grep -o '"name":"[^"]*"' | sed 's/"name":"//g' | sed 's/"//g' | nl
        echo ""
        print_success "Workflows encontrados"
    else
        print_error "No se pudieron obtener los workflows"
        echo "$RESPONSE" | grep -o '"message":"[^"]*"'
    fi
}

# Listar últimas ejecuciones
list_runs() {
    check_credentials
    
    print_info "Obteniendo últimas ejecuciones..."
    echo ""
    
    RESPONSE=$(curl -s \
        -H "Authorization: token ${GITHUB_TOKEN}" \
        -H "Accept: application/vnd.github.v3+json" \
        "https://api.github.com/repos/${GITHUB_USER}/${REPO_NAME}/actions/runs?per_page=10")
    
    if echo "$RESPONSE" | grep -q '"total_count"'; then
        echo -e "${CYAN}ID${NC}\t\t${CYAN}Estado${NC}\t\t${CYAN}Workflow${NC}\t\t${CYAN}Commit${NC}"
        echo "─────────────────────────────────────────────────────────────────────"
        
        echo "$RESPONSE" | python3 -c "
import sys, json
try:
    data = json.load(sys.stdin)
    for run in data.get('workflow_runs', [])[:10]:
        status = run['status']
        conclusion = run.get('conclusion', 'running')
        
        # Emoji para el estado
        if status == 'completed':
            if conclusion == 'success':
                emoji = '✅'
            elif conclusion == 'failure':
                emoji = '❌'
            else:
                emoji = '⚠️'
        else:
            emoji = '🔄'
        
        print(f\"{run['id']}\t{emoji} {status}\t{run['name'][:20]}\t{run['head_commit']['message'][:30]}\")
except:
    pass
"
        echo ""
        print_success "Últimas 10 ejecuciones"
    else
        print_error "No se pudieron obtener las ejecuciones"
    fi
}

# Ver detalles de una ejecución
show_run_details() {
    check_credentials
    
    if [ -z "$1" ]; then
        print_error "Debes especificar un RUN_ID"
        echo "Uso: $0 details <RUN_ID>"
        return 1
    fi
    
    RUN_ID="$1"
    
    print_info "Obteniendo detalles de ejecución #${RUN_ID}..."
    echo ""
    
    RESPONSE=$(curl -s \
        -H "Authorization: token ${GITHUB_TOKEN}" \
        -H "Accept: application/vnd.github.v3+json" \
        "https://api.github.com/repos/${GITHUB_USER}/${REPO_NAME}/actions/runs/${RUN_ID}")
    
    if echo "$RESPONSE" | grep -q '"id"'; then
        echo "$RESPONSE" | python3 -c "
import sys, json
try:
    run = json.load(sys.stdin)
    print(f\"🔹 ID: {run['id']}\")
    print(f\"🔹 Workflow: {run['name']}\")
    print(f\"🔹 Estado: {run['status']}\")
    print(f\"🔹 Conclusión: {run.get('conclusion', 'N/A')}\")
    print(f\"🔹 Branch: {run['head_branch']}\")
    print(f\"🔹 Commit: {run['head_sha'][:7]} - {run['head_commit']['message']}\")
    print(f\"🔹 Creado: {run['created_at']}\")
    print(f\"🔹 URL: {run['html_url']}\")
except Exception as e:
    print(f'Error: {e}')
"
        echo ""
        print_success "Detalles obtenidos"
    else
        print_error "No se pudo obtener la ejecución"
    fi
}

# Trigger manual de workflow
trigger_workflow() {
    check_credentials
    
    print_info "Listando workflows disponibles..."
    echo ""
    
    # Obtener workflows
    WORKFLOWS=$(curl -s \
        -H "Authorization: token ${GITHUB_TOKEN}" \
        "https://api.github.com/repos/${GITHUB_USER}/${REPO_NAME}/actions/workflows")
    
    echo "$WORKFLOWS" | python3 -c "
import sys, json
try:
    data = json.load(sys.stdin)
    for i, wf in enumerate(data.get('workflows', []), 1):
        print(f\"{i}. {wf['name']} (ID: {wf['id']})\")
except:
    pass
"
    
    echo ""
    read -p "Selecciona el workflow (número): " WORKFLOW_NUM
    
    WORKFLOW_ID=$(echo "$WORKFLOWS" | python3 -c "
import sys, json
try:
    data = json.load(sys.stdin)
    workflows = data.get('workflows', [])
    if $WORKFLOW_NUM > 0 and $WORKFLOW_NUM <= len(workflows):
        print(workflows[$WORKFLOW_NUM - 1]['id'])
except:
    pass
")
    
    if [ -z "$WORKFLOW_ID" ]; then
        print_error "Workflow inválido"
        return 1
    fi
    
    print_info "Ejecutando workflow #${WORKFLOW_ID}..."
    
    RESPONSE=$(curl -s -X POST \
        -H "Authorization: token ${GITHUB_TOKEN}" \
        -H "Accept: application/vnd.github.v3+json" \
        "https://api.github.com/repos/${GITHUB_USER}/${REPO_NAME}/actions/workflows/${WORKFLOW_ID}/dispatches" \
        -d '{"ref":"main"}')
    
    if [ $? -eq 0 ]; then
        print_success "Workflow ejecutado!"
        echo ""
        print_info "Ver progreso en:"
        echo "https://github.com/${GITHUB_USER}/${REPO_NAME}/actions"
    else
        print_error "Error al ejecutar workflow"
    fi
}

# Descargar artefactos
download_artifacts() {
    check_credentials
    
    print_info "Obteniendo artefactos disponibles..."
    echo ""
    
    RESPONSE=$(curl -s \
        -H "Authorization: token ${GITHUB_TOKEN}" \
        "https://api.github.com/repos/${GITHUB_USER}/${REPO_NAME}/actions/artifacts?per_page=10")
    
    if echo "$RESPONSE" | grep -q '"total_count"'; then
        echo "$RESPONSE" | python3 -c "
import sys, json
try:
    data = json.load(sys.stdin)
    for i, art in enumerate(data.get('artifacts', [])[:10], 1):
        size_mb = art['size_in_bytes'] / 1024 / 1024
        print(f\"{i}. {art['name']} ({size_mb:.1f} MB) - Creado: {art['created_at'][:10]}\")
        print(f\"   ID: {art['id']}\")
except:
    pass
"
        echo ""
        read -p "Descargar artefacto (número, 0 para cancelar): " ART_NUM
        
        if [ "$ART_NUM" != "0" ]; then
            ARTIFACT_ID=$(echo "$RESPONSE" | python3 -c "
import sys, json
try:
    data = json.load(sys.stdin)
    artifacts = data.get('artifacts', [])
    if $ART_NUM > 0 and $ART_NUM <= len(artifacts):
        print(artifacts[$ART_NUM - 1]['id'])
except:
    pass
")
            
            if [ ! -z "$ARTIFACT_ID" ]; then
                print_info "Descargando artefacto #${ARTIFACT_ID}..."
                
                curl -L \
                    -H "Authorization: token ${GITHUB_TOKEN}" \
                    -H "Accept: application/vnd.github.v3+json" \
                    "https://api.github.com/repos/${GITHUB_USER}/${REPO_NAME}/actions/artifacts/${ARTIFACT_ID}/zip" \
                    -o "artifact_${ARTIFACT_ID}.zip"
                
                if [ -f "artifact_${ARTIFACT_ID}.zip" ]; then
                    print_success "Descargado: artifact_${ARTIFACT_ID}.zip"
                else
                    print_error "Error al descargar"
                fi
            fi
        fi
    else
        print_error "No se pudieron obtener artefactos"
    fi
}

# Ver estado del build actual
watch_build() {
    check_credentials
    
    print_info "Monitoreando último build..."
    echo ""
    
    while true; do
        clear
        print_header
        
        RESPONSE=$(curl -s \
            -H "Authorization: token ${GITHUB_TOKEN}" \
            "https://api.github.com/repos/${GITHUB_USER}/${REPO_NAME}/actions/runs?per_page=1")
        
        echo "$RESPONSE" | python3 -c "
import sys, json
try:
    data = json.load(sys.stdin)
    if data.get('workflow_runs'):
        run = data['workflow_runs'][0]
        print(f\"🔹 Workflow: {run['name']}\")
        print(f\"🔹 Estado: {run['status']}\")
        print(f\"🔹 Conclusión: {run.get('conclusion', 'En progreso')}\")
        print(f\"🔹 Commit: {run['head_commit']['message'][:50]}\")
        print(f\"🔹 Iniciado: {run['created_at']}\")
        print(f\"\n🔗 {run['html_url']}\")
        
        if run['status'] == 'completed':
            print(f\"\n{'✅ COMPLETADO!' if run.get('conclusion') == 'success' else '❌ FALLÓ'}\")
            sys.exit(0)
except:
    pass
"
        
        if [ $? -eq 0 ]; then
            break
        fi
        
        echo ""
        print_info "Actualizando en 10 segundos... (Ctrl+C para salir)"
        sleep 10
    done
}

# Menú principal
show_menu() {
    clear
    print_header
    
    if check_repo; then
        print_success "Repositorio: https://github.com/${GITHUB_USER}/${REPO_NAME}"
    else
        print_warning "Repositorio aún no existe o no tienes acceso"
    fi
    
    echo ""
    echo -e "${CYAN}OPCIONES:${NC}"
    echo ""
    echo "  1. 📋 Listar workflows"
    echo "  2. 🔍 Ver últimas ejecuciones"
    echo "  3. 📊 Ver detalles de ejecución"
    echo "  4. 🚀 Ejecutar workflow manualmente"
    echo "  5. 📥 Descargar artefactos/APKs"
    echo "  6. 👀 Monitorear build actual"
    echo "  7. 🌐 Abrir en navegador"
    echo "  8. 🔄 Actualizar credenciales"
    echo "  0. ❌ Salir"
    echo ""
    echo -e "${CYAN}═══════════════════════════════════════════════════════${NC}"
    echo ""
}

# Abrir en navegador
open_browser() {
    check_credentials
    
    URL="https://github.com/${GITHUB_USER}/${REPO_NAME}"
    
    echo ""
    echo "Selecciona qué abrir:"
    echo "  1. Repositorio principal"
    echo "  2. Actions"
    echo "  3. Releases"
    echo "  4. Issues"
    echo ""
    read -p "Opción: " BROWSER_OPT
    
    case $BROWSER_OPT in
        1) URL="$URL" ;;
        2) URL="$URL/actions" ;;
        3) URL="$URL/releases" ;;
        4) URL="$URL/issues" ;;
    esac
    
    print_info "Abriendo: $URL"
    
    if command -v termux-open-url &> /dev/null; then
        termux-open-url "$URL"
    elif command -v xdg-open &> /dev/null; then
        xdg-open "$URL"
    else
        echo "$URL"
        print_info "Copia el enlace de arriba"
    fi
}

# Main loop
main() {
    while true; do
        show_menu
        read -p "Selecciona una opción: " option
        echo ""
        
        case $option in
            1)
                list_workflows
                ;;
            2)
                list_runs
                ;;
            3)
                read -p "ID de ejecución: " run_id
                show_run_details "$run_id"
                ;;
            4)
                trigger_workflow
                ;;
            5)
                download_artifacts
                ;;
            6)
                watch_build
                ;;
            7)
                open_browser
                ;;
            8)
                unset GITHUB_USER
                unset GITHUB_TOKEN
                print_success "Credenciales borradas. Se pedirán de nuevo."
                ;;
            0)
                print_success "¡Hasta luego!"
                exit 0
                ;;
            *)
                print_error "Opción inválida"
                ;;
        esac
        
        echo ""
        read -p "Presiona Enter para continuar..."
    done
}

# Modo comando único
if [ ! -z "$1" ]; then
    case "$1" in
        list|workflows)
            list_workflows
            ;;
        runs)
            list_runs
            ;;
        details)
            show_run_details "$2"
            ;;
        trigger)
            trigger_workflow
            ;;
        download)
            download_artifacts
            ;;
        watch)
            watch_build
            ;;
        *)
            echo "Comandos disponibles:"
            echo "  list/workflows  - Listar workflows"
            echo "  runs           - Ver últimas ejecuciones"
            echo "  details <id>   - Ver detalles de ejecución"
            echo "  trigger        - Ejecutar workflow"
            echo "  download       - Descargar artefactos"
            echo "  watch          - Monitorear build"
            echo ""
            echo "Sin argumentos: Modo interactivo"
            ;;
    esac
else
    # Modo interactivo
    main
fi
