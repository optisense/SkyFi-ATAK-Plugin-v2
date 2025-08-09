
# Play Store ATAK Plugin ProGuard Rules

# Keep all ATAK interfaces and classes
-keep class com.atakmap.** { *; }
-keep interface com.atakmap.** { *; }

# Keep our plugin classes
-keep class com.optisense.skyfi.atak.** { *; }
-keep class com.optisense.skyfi.atak.playstore.** { *; }

# Keep plugin entry point
-keep class com.optisense.skyfi.atak.playstore.SkyFiPlayStorePlugin { *; }

# Keep R class
-keepclassmembers class **.R$* {
    public static <fields>;
}

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep custom views
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Retrofit
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*

-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keep class okio.** { *; }

# Gson
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep models
-keep class com.optisense.skyfi.atak.skyfiapi.** { *; }

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
 <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

# JTS
-keep class org.locationtech.jts.** { *; }

# Apache Commons
-keep class org.apache.commons.** { *; }

# Suppress warnings
-dontwarn javax.annotation.**
-dontwarn sun.misc.Unsafe
-dontwarn java.nio.file.**
-dontwarn org.codehaus.mojo.**
        