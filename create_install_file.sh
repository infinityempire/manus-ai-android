#!/bin/bash

# Script to build Manus AI Android APK
# Usage: ./create_install_file.sh [debug|release]

BUILD_TYPE=${1:-debug}
echo "🚀 Starting build process: $BUILD_TYPE"

# Ensure gradlew is executable
chmod +x gradlew

# Run gradle build
if [ "$BUILD_TYPE" == "debug" ]; then
    echo "🔨 Building Debug APK..."
    ./gradlew assembleDebug
    APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
else
    echo "🔨 Building Release APK..."
    ./gradlew assembleRelease
    APK_PATH="app/build/outputs/apk/release/app-release-unsigned.apk"
fi

# Check if build was successful
if [ $? -ne 0 ]; then
    echo "❌ Gradle build failed!"
    exit 1
fi

# Create dist directory
mkdir -p dist

# Move and rename APK to the expected location
if [ -f "$APK_PATH" ]; then
    cp "$APK_PATH" dist/manus-free-installer.apk
    echo "✅ APK created successfully at dist/manus-free-installer.apk"
else
    echo "❌ APK file not found at $APK_PATH"
    exit 1
fi
