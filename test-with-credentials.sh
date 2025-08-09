#!/bin/bash
# Test build with TAK.gov credentials
# Usage: ./test-with-credentials.sh <username> <password>

if [ $# -ne 2 ]; then
    echo "Usage: $0 <tak_username> <tak_password>"
    exit 1
fi

TAK_USER="$1"
TAK_PASS="$2"

echo "Testing build with TAK.gov repository..."
./gradlew clean
./gradlew -Ptakrepo.force=true \
         -Ptakrepo.url=https://artifacts.tak.gov/artifactory/maven \
         -Ptakrepo.user="$TAK_USER" \
         -Ptakrepo.password="$TAK_PASS" \
         assembleCivRelease

if [ $? -eq 0 ]; then
    echo "✓ Build successful!"
    ls -la app/build/outputs/apk/civ/release/
else
    echo "✗ Build failed"
    exit 1
fi
