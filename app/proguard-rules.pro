# Kotlin
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *

# DataStore
-keep class androidx.datastore.** { *; }

# WorkManager
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.CoroutineWorker
-keep class androidx.work.** { *; }

# Coil
-keep class coil.** { *; }

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Models
-keep class com.shaikh.storentry.domain.model.** { *; }
-keep class com.shaikh.storentry.data.local.entity.** { *; }

# Firebase Analytics
-keep class com.google.android.gms.measurement.** { *; }
-keep class com.google.firebase.analytics.** { *; }

# Firebase Crashlytics
-keepattributes SourceFile,LineNumberTable
-keep public class com.google.firebase.crashlytics.** { *; }

# Google Play Services
-keep class com.google.android.gms.common.** { *; }
