#!/bin/bash

# Fix R class imports in all Java files in com.optisense package
for file in $(find app/src/main/java/com/optisense -type f -name "*.java"); do
    # Check if file uses R.something
    if grep -q "R\." "$file"; then
        # Check if it already has the correct import
        if ! grep -q "import com.skyfi.atak.plugin.R;" "$file"; then
            # Add the import after the package declaration
            sed -i '' '/^package /a\
import com.skyfi.atak.plugin.R;
' "$file"
            echo "Fixed: $file"
        fi
    fi
done

# Fix BuildConfig imports
for file in $(find app/src/main/java/com/optisense -type f -name "*.java"); do
    if grep -q "BuildConfig" "$file"; then
        if ! grep -q "import com.skyfi.atak.plugin.BuildConfig;" "$file"; then
            sed -i '' '/^package /a\
import com.skyfi.atak.plugin.BuildConfig;
' "$file"
            echo "Fixed BuildConfig in: $file"
        fi
    fi
done

echo "Import fixes complete"