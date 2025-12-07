# GitHub Copilot Agent Instructions

## Context
This is an Android project for controlling LG webOS TVs. Built with Kotlin and Gradle.

## Common Build Issues & Fixes

### Issue 1: Gradle Wrapper Not Found
**Symptoms**: "gradlew: Permission denied" or "gradlew not found"
**Fix**: Ensure gradlew exists and is executable
```yaml
- name: Make gradlew executable
  run: chmod +x gradlew
```

### Issue 2: Missing gradle-wrapper.jar
**Symptoms**: "Could not find or load gradle-wrapper.jar"
**Fix**: Add gradle-wrapper.jar to repository or use gradle wrapper task

### Issue 3: Build Tools Version Mismatch
**Symptoms**: "Failed to find Build Tools revision X.X.X"
**Fix**: Update build.gradle with correct versions

### Issue 4: Kotlin Version Conflicts
**Symptoms**: "Incompatible Kotlin version"
**Fix**: Sync Kotlin plugin version with Gradle version

## Quick Commands

Check build status:
```bash
gh run list --limit 5
```

Get logs from failed build:
```bash
gh run view <run-id> --log-failed
```

Download artifacts:
```bash
gh run download <run-id>
```

Trigger new build:
```bash
gh workflow run build.yml
```

## Project Structure
- Kotlin source: `app/src/main/java/com/roro/lgthinq/`
- Layouts: `app/src/main/res/layout/`
- Build config: `app/build.gradle`, `build.gradle`
- Workflow: `.github/workflows/build.yml`
