# General Android rules
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontwarn kotlinx.serialization.**
-keep,includedescriptorclasses class com.astranavi.app.data.model.**$$serializer { *; }
-keepclassmembers class com.astranavi.app.data.model.** { *** Companion; }
-keepclasseswithmembers class com.astranavi.app.data.model.** { kotlinx.serialization.KSerializer serializer(...); }

# Retrofit
-keepattributes Signature
-keepattributes Exceptions
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
