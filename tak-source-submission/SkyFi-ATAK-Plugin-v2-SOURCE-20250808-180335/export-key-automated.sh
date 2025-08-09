#!/bin/bash

# Export Play Store key with automated password entry
echo "Exporting Play Store upload key for Google Play Console..."

# Create a temporary expect script
cat > /tmp/pepk_export.exp << 'EOF'
#!/usr/bin/expect -f

set timeout 30

spawn java -jar /Users/jfuginay/Downloads/pepk.jar \
    --keystore=keystores/playstore-skyfi.keystore \
    --alias=skyfi-playstore \
    --output=skyfi-playstore-upload-key.zip \
    --include-cert \
    --rsa-aes-encryption \
    --encryption-key-path=/Users/jfuginay/Downloads/encryption_public_key.pem

expect "Enter password for store 'keystores/playstore-skyfi.keystore':"
send "skyfi2024\r"

expect "Enter password for key 'skyfi-playstore':"
send "skyfi2024\r"

expect eof
EOF

# Make it executable and run
chmod +x /tmp/pepk_export.exp

if command -v expect >/dev/null 2>&1; then
    /tmp/pepk_export.exp
    RESULT=$?
else
    echo "Expect not found. Running with manual password entry..."
    echo "Enter password: skyfi2024 (twice when prompted)"
    java -jar /Users/jfuginay/Downloads/pepk.jar \
        --keystore=keystores/playstore-skyfi.keystore \
        --alias=skyfi-playstore \
        --output=skyfi-playstore-upload-key.zip \
        --include-cert \
        --rsa-aes-encryption \
        --encryption-key-path=/Users/jfuginay/Downloads/encryption_public_key.pem
    RESULT=$?
fi

# Clean up
rm -f /tmp/pepk_export.exp

if [ $RESULT -eq 0 ]; then
    echo ""
    echo "Success! Upload key exported to: skyfi-playstore-upload-key.zip"
    ls -la skyfi-playstore-upload-key.zip
else
    echo "Export failed. Please run manually with:"
    echo "./export-playstore-key.sh"
fi