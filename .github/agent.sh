#!/bin/bash

# GitHub Copilot Agent Helper for veamos-que-sale
# Usage: Source this file to get agent-like commands

export REPO_NAME="veamos-que-sale"

# Agent commands
agent() {
    local cmd="$1"
    shift
    
    case "$cmd" in
        status)
            echo "🔍 Checking build status..."
            gh run list --repo "$GITHUB_USER/$REPO_NAME" --limit 5
            ;;
        watch)
            echo "👀 Watching latest build..."
            gh run watch --repo "$GITHUB_USER/$REPO_NAME"
            ;;
        logs)
            local run_id="${1:-$(gh run list --repo $GITHUB_USER/$REPO_NAME --limit 1 --json databaseId --jq '.[0].databaseId')}"
            echo "📄 Getting logs for run $run_id..."
            gh run view "$run_id" --repo "$GITHUB_USER/$REPO_NAME" --log-failed
            ;;
        download)
            local run_id="${1:-$(gh run list --repo $GITHUB_USER/$REPO_NAME --limit 1 --json databaseId --jq '.[0].databaseId')}"
            echo "📥 Downloading artifacts from run $run_id..."
            gh run download "$run_id" --repo "$GITHUB_USER/$REPO_NAME"
            ;;
        trigger)
            echo "🚀 Triggering new build..."
            gh workflow run build.yml --repo "$GITHUB_USER/$REPO_NAME"
            ;;
        fix)
            echo "🔧 Analyzing last failed build..."
            local run_id="$(gh run list --repo $GITHUB_USER/$REPO_NAME --status failure --limit 1 --json databaseId --jq '.[0].databaseId')"
            if [ -n "$run_id" ]; then
                echo "❌ Failed run: $run_id"
                gh run view "$run_id" --repo "$GITHUB_USER/$REPO_NAME" --log-failed | tail -100
                echo ""
                echo "💡 Common fixes:"
                echo "  1. Missing gradle-wrapper.jar → gradle wrapper"
                echo "  2. Permission denied on gradlew → chmod +x gradlew"
                echo "  3. Build tools mismatch → Update build.gradle"
            else
                echo "✅ No failed builds found"
            fi
            ;;
        help|*)
            echo "GitHub Copilot Agent - veamos-que-sale"
            echo ""
            echo "Commands:"
            echo "  agent status    - Check build status"
            echo "  agent watch     - Watch latest build"
            echo "  agent logs [id] - Get build logs"
            echo "  agent download  - Download latest APK"
            echo "  agent trigger   - Trigger new build"
            echo "  agent fix       - Analyze and fix errors"
            echo ""
            echo "Setup:"
            echo "  export GITHUB_USER='your-username'"
            echo "  gh auth login"
            ;;
    esac
}

# Auto-complete
_agent_complete() {
    local cur="${COMP_WORDS[COMP_CWORD]}"
    COMPREPLY=($(compgen -W "status watch logs download trigger fix help" -- "$cur"))
}

complete -F _agent_complete agent

echo "✅ GitHub Copilot Agent loaded"
echo "   Usage: agent <command>"
echo "   Type 'agent help' for commands"
