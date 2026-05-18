#!/bin/bash

echo "🚀 MANUS ANDROID BUILD FIX SCRIPT"
echo "=================================="

# Clean previous builds
echo "🧹 Cleaning previous builds..."
./gradlew clean --no-daemon --no-build-cache

# Clear Gradle cache
echo "🗑️ Clearing Gradle cache..."
rm -rf ~/.gradle/caches/
rm -rf .gradle/

# Touch Java files to force compilation
echo "👆 Touching Java files to force compilation..."
find app/src/main/java -name "*.java" -exec touch {} \;

# Force build with verbose output
echo "🔨 Force building APK with verbose output..."
./gradlew assembleDebug --no-daemon --no-build-cache --rerun-tasks --info

# List all build outputs
echo "📋 Listing all build outputs..."
find . -name "*.apk" -type f 2>/dev/null || echo "No APK files found"
find app/build -type f -name "*.apk" 2>/dev/null || echo "No APK files in app/build"
find . -path "*/outputs/*" -name "*.apk" 2>/dev/null || echo "No APK files in outputs directories"

# Check specific locations
echo "🔍 Checking specific APK locations..."
ls -la app/build/outputs/apk/debug/ 2>/dev/null || echo "Debug APK directory not found"
ls -la app/build/outputs/apk/ 2>/dev/null || echo "APK outputs directory not found"
ls -la app/build/outputs/ 2>/dev/null || echo "Build outputs directory not found"

# Try alternative build tasks
echo "🔄 Trying alternative build tasks..."
./gradlew build --no-daemon --no-build-cache --rerun-tasks

# Final APK search
echo "🎯 Final comprehensive APK search..."
find . -name "*.apk" -type f -exec ls -la {} \; 2>/dev/null || echo "No APK files found anywhere"

echo "✅ Build script completed!"

