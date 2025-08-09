#\!/bin/bash

# Add missing Android imports to fix Fortify warnings
for file in app/src/main/java/com/optisense/skyfi/atak/*.java; do
    if grep -q "Log\." "$file" && \! grep -q "import android.util.Log;" "$file"; then
        sed -i '' '/^package /a\
import android.util.Log;
' "$file"
        echo "Added Log import to: $file"
    fi
    
    if grep -q "PreferenceManager" "$file" && \! grep -q "import android.preference.PreferenceManager;" "$file"; then
        sed -i '' '/^package /a\
import android.preference.PreferenceManager;
' "$file"  
        echo "Added PreferenceManager import to: $file"
    fi
    
    if grep -q "Color\." "$file" && \! grep -q "import android.graphics.Color;" "$file"; then
        sed -i '' '/^package /a\
import android.graphics.Color;
' "$file"
        echo "Added Color import to: $file"
    fi
done

echo "Import fixes complete"
