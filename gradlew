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
