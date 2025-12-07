
#!/bin/bash

# ==============================================================================
# WARNING: EXPERIMENTAL SCRIPT - NOT GUARANTEED TO WORK (v2)
# ==============================================================================
# This script attempts to work around an environment incompatibility by replacing
# ALL of Gradle's cached aapt2 binaries with a symlink to the one from the
# Termux environment. This is an unsupported hack.
# ==============================================================================

set -e

# --- Configuration ---
SYSTEM_AAPT2_PATH="/data/data/com.termux/files/usr/bin/aapt2"

# --- Verification ---
echo "Verifying that the Termux version of aapt2 exists..."
if [ ! -f "$SYSTEM_AAPT2_PATH" ]; then
    echo "ERROR: System aapt2 not found at $SYSTEM_AAPT2_PATH"
    echo "Please ensure aapt2 is installed via 'pkg install aapt'."
    exit 1
fi
echo "Termux aapt2 found."

# --- Find ALL Gradle aapt2 instances ---
echo "Finding all Gradle's cached aapt2 binaries..."
# We need to run a build first to make sure the files are downloaded.
# We will ignore the output and expected failure.
./build_debug.sh || echo "Ignoring expected failure to ensure dependencies are downloaded."

GRADLE_AAPT2_PATHS=$(find "$HOME/.gradle/caches" -type f -name "aapt2" -executable | grep "transforms")

if [ -z "$GRADLE_AAPT2_PATHS" ]; then
    echo "ERROR: Could not find any Gradle cached aapt2 binaries."
    echo "This may happen if the initial build didn't fail in the expected way."
    exit 1
fi

# --- Cleanup Function ---
cleanup() {
    echo ""
    echo "--- Running cleanup ---"
    find "$HOME/.gradle/caches" -type f -name "aapt2.original" | while read -r backup_file; do
        aapt2_dir=$(dirname "$backup_file")
        aapt2_path="$aapt2_dir/aapt2"
        if [ -L "$aapt2_path" ]; then # Check if it's a symlink
            echo "Restoring original Gradle aapt2 for $aapt2_path..."
            rm -f "$aapt2_path"
            mv "$backup_file" "$aapt2_path"
            echo "Restore complete."
        fi
    done
}

# Register the cleanup function to run on script exit (EXIT)
trap cleanup EXIT

# --- The Hack (Looping version) ---
echo "Found the following aapt2 instances to replace:"
echo "$GRADLE_AAPT2_PATHS"
echo ""

echo "$GRADLE_AAPT2_PATHS" | while read -r aapt2_path; do
    echo "Processing: $aapt2_path"
    aapt2_dir=$(dirname "$aapt2_path")
    orig_path="$aapt2_dir/aapt2.original"

    if [ -f "$orig_path" ]; then
        echo "Backup already exists. Re-creating symlink to be safe."
        rm -f "$aapt2_path"
    else
        echo "Backing up original..."
        mv "$aapt2_path" "$orig_path"
    fi
    ln -sf "$SYSTEM_AAPT2_PATH" "$aapt2_path"
    echo "Symlink created for $aapt2_path"
    echo ""
done
echo "Symlinking complete for all found aapt2 instances."


# --- Run the Build ---
echo ""
echo "--- Attempting build with Termux aapt2... ---"
./build_debug.sh
BUILD_EXIT_CODE=$?

# --- Final Result ---
if [ $BUILD_EXIT_CODE -eq 0 ]; then
    echo "--- BUILD SUCCEEDED! ---"
    echo "The experimental workaround was successful."
else
    echo "--- BUILD FAILED ---"
    echo "The build failed again. The Termux aapt2 may be incompatible with this version of the Android Gradle Plugin."
fi

exit $BUILD_EXIT_CODE
