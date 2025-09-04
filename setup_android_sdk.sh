#!/bin/bash

echo "🤖 Setting up Android SDK locally..."

# Set environment variables
export ANDROID_HOME="$PWD/android-sdk"
export ANDROID_SDK_ROOT="$PWD/android-sdk"
export JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"

echo "📁 Creating Android SDK directory: $ANDROID_HOME"
mkdir -p "$ANDROID_HOME"
cd "$ANDROID_HOME"

echo "📥 Downloading Android SDK commandline-tools..."
curl -L -o cmdline-tools.zip https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip

echo "📦 Extracting commandline-tools..."
mkdir -p cmdline-tools
unzip -q cmdline-tools.zip -d cmdline-tools
mv cmdline-tools/cmdline-tools cmdline-tools/latest
rm -f cmdline-tools.zip

echo "✅ Accepting SDK licenses..."
yes | cmdline-tools/latest/bin/sdkmanager --licenses || true

echo "📱 Installing required SDK packages..."
yes | "$ANDROID_SDK_ROOT/cmdline-tools/latest/bin/sdkmanager" \
  "platform-tools" \
  "platforms;android-34" \
  "build-tools;34.0.0"

echo "📝 Creating local.properties..."
cd "$PWD/../"
echo "sdk.dir=${ANDROID_SDK_ROOT}" > local.properties

echo "🎉 Android SDK setup completed!"
echo "ANDROID_HOME: $ANDROID_HOME"
echo "ANDROID_SDK_ROOT: $ANDROID_SDK_ROOT"

