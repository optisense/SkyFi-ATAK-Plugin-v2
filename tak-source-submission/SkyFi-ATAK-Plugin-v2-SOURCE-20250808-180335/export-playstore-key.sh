#!/bin/bash

echo "Exporting Play Store upload key for Google Play Console..."
echo ""
echo "This will prompt for passwords. Use: skyfi2024"
echo ""

java -jar /Users/jfuginay/Downloads/pepk.jar \
    --keystore=keystores/playstore-skyfi.keystore \
    --alias=skyfi-playstore \
    --output=skyfi-playstore-upload-key.zip \
    --include-cert \
    --rsa-aes-encryption \
    --encryption-key-path=/Users/jfuginay/Downloads/encryption_public_key.pem

if [ $? -eq 0 ]; then
    echo ""
    echo "Success! Upload key exported to: skyfi-playstore-upload-key.zip"
    echo ""
    echo "Next steps:"
    echo "1. Upload skyfi-playstore-upload-key.zip to Google Play Console"
    echo "2. Google Play will use this key to verify your uploads"
    echo "3. Google Play will re-sign your app with their distribution key"
    echo ""
    echo "Keep the original keystore file safe:"
    echo "  keystores/playstore-skyfi.keystore"
    echo "  Password: skyfi2024"
    echo "  Alias: skyfi-playstore"
else
    echo "Failed to export key. Please check the error messages above."
fi