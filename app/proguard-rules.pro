# General Android rules
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontwarn kotlinx.serialization.*
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.astranavi.app.data.model.**$$serializer { *; }
-keepclassmembers class com.astranavi.app.data.model.** { *** Companion; }
-keepclasseswithmembers class com.astranavi.app.data.model.** { kotlinx.serialization.KSerializer serializer(...); }

# Gson / Retrofit — keep all serialized model fields
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.astranavi.app.data.model.** { *; }
-keep class com.astranavi.app.data.api.** { *; }
-keep class com.astranavi.app.data.repository.** { *; }
-keep class com.astranavi.app.data.cache.** { *; }
-keep class com.astranavi.app.util.** { *; }

# Gson Specifics
-keep class com.google.gson.** { *; }
-keepattributes Exceptions
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keepclassmembers class * {
    @retrofit2.http.* <methods>;
}

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep class okio.** { *; }
-keep class com.astranavi.app.data.api.RetrofitClient { *; }
-keep class com.astranavi.app.data.api.FlexibleJsonDeserializer { *; }

# Coil
-dontwarn coil.**
-keep class coil.** { *; }

# DataStore Preferences
-keepclassmembers class * extends androidx.datastore.preferences.core.Preferences {
    *;
}

# BuildConfig fields
-keepclassmembers class com.astranavi.app.BuildConfig {
    *;
}

# Android components
-keep class com.astranavi.app.MainActivity { *; }
-keep class com.astranavi.app.*ViewModel { *; }

# Keep all Composable functions
-if @kotlin.Metadata class * implements androidx.compose.runtime.Composer
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}