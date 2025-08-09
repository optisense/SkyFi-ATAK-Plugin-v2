#!/bin/bash

# Source this file to set up Java environment for SkyFi ATAK Plugin development
# Usage: source ./export-java-env.sh

export JAVA_HOME="/opt/homebrew/Cellar/openjdk@11/11.0.28/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"

echo "✅ Java environment configured for SkyFi ATAK Plugin"
echo "JAVA_HOME: $JAVA_HOME"
echo "Java version: $(java -version 2>&1 | head -1)"